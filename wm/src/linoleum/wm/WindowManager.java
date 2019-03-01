package linoleum.wm;

import java.awt.Container;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.URI;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.JDesktopPane;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import gnu.x11.Display;
import gnu.x11.Error;
import gnu.x11.Rectangle;
import gnu.x11.Window;
import gnu.x11.event.Event;
import gnu.x11.event.ConfigureRequest;
import gnu.x11.event.DestroyNotify;
import gnu.x11.event.UnmapNotify;
import gnu.x11.event.MapNotify;
import gnu.x11.event.MapRequest;
import gnu.x11.event.ConfigureNotify;
import gnu.x11.event.CreateNotify;
import gnu.x11.event.MappingNotify;
import gnu.x11.event.DestroyNotify;
import linoleum.application.Frame;

public class WindowManager extends Frame {
	private final String name = System.getenv("DISPLAY");
	private Display display;
	private Window root;
	private Client client;
	private JRootPane panel;
	private Container content;
	private boolean iconified;
	private Map<Integer, WindowManager> frames = new HashMap<>();
	private final Logger logger = Logger.getLogger(getClass().getName());

	// internal state
	public static final int UNMANAGED = 0;
	public static final int NORMAL = 1;
	public static final int HIDDEN = 2;
	public static final int NO_FOCUS = 3;

	public WindowManager() {
		setName("Windows");
		setClosable(true);
		setSize(150, 150);
		setIconifiable(true);
		setMaximizable(true);
		setResizable(true);
		addInternalFrameListener(new InternalFrameAdapter() {
			public void internalFrameClosed(final InternalFrameEvent evt) {
				formInternalFrameClosed(evt);
			}
			public void internalFrameIconified(final InternalFrameEvent evt) {
				formInternalFrameIconified(evt);
			}
			public void internalFrameDeiconified(final InternalFrameEvent evt) {
				formInternalFrameDeiconified(evt);
			}
			public void internalFrameActivated(final InternalFrameEvent evt) {
				formInternalFrameActivated(evt);
			}
			public void internalFrameDeactivated(final InternalFrameEvent evt) {
				formInternalFrameDeactivated(evt);
			}
		});
		addComponentListener(new ComponentAdapter() {
			public void componentResized(final ComponentEvent evt) {
				formComponentResized(evt);
			}
			public void componentMoved(final ComponentEvent evt) {
				formComponentMoved(evt);
			}
		});
		panel = getRootPane();
	}

	@Override
	public WindowManager getOwner() {
		return (WindowManager) super.getOwner();
	}

	@Override
	public Frame getFrame() {
		return new WindowManager();
	}

	@Override
	public void init() {
		display = new Display(new Display.Name(name == null?":0.0":name));
		if (display.connected) {
			root = display.default_root;
			control_root_window();
			loop();
		}
	}

	private void loop() {
		(new SwingWorker<Boolean, Object>() {
			@Override
			public Boolean doInBackground() {
				while (true) {
					read_and_dispatch_event();
				}
			}

			@Override
			protected void done() {
				try {
					get();
				} catch (final InterruptedException ex) {
					ex.printStackTrace();
				} catch (final ExecutionException ex) {
					ex.printStackTrace();
				}
			}
		}).execute();
	}

	private void control_root_window() {
		try {
			root.select_input(Event.SUBSTRUCTURE_NOTIFY_MASK | Event.SUBSTRUCTURE_REDIRECT_MASK | Event.PROPERTY_CHANGE_MASK);
			display.check_error();
		} catch (final Error e) {
			if (e.code == Error.BAD_ACCESS && e.bad == root.id) {
				logger.info("Failed to access root window. Another WM is running ?");
			} else {
				throw e;
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void read_and_dispatch_event() {
		final Event first_event = display.next_event();
//		display.grab_server();
		display.check_error();
		final List<Event> other_events = (List<Event>) display.in.pull_all_events();
		for (final Iterator<Event> it = other_events.iterator(); it.hasNext();) {
			final Event event = it.next();
			if (event.code() == DestroyNotify.CODE) {
				final WindowManager frame = getFrame(((DestroyNotify) event).window_id);
				if (frame != null) {
					frame.client.early_destroyed = true;
				}
			} else if (event.code() == UnmapNotify.CODE) {
				final WindowManager frame = getFrame(((UnmapNotify) event).window_id);
				if (frame != null) {
					frame.client.early_unmapped = true;
				}
			}
		}
		when(first_event);
		for (final Iterator<Event> it = other_events.iterator(); it.hasNext();) {
			when(it.next());
		}
		display.flush();
//		display.ungrab_server();
	}

	private void when(final Event event) {
		logger.config("Event: " + event);
		switch (event.code()) {
		case ConfigureRequest.CODE: // Event.SUBSTRUCTURE_NOTIFY
			when_configure_request((ConfigureRequest) event);
			break;
		case DestroyNotify.CODE:	// Event.SUBSTRUCTURE_NOTIFY
			when_destroy_notify((DestroyNotify) event);
			break;
		case MapRequest.CODE:		// Event.SUBSTRUCTURE_REDIRECT
			when_map_request((MapRequest) event);
			break;
		case MapNotify.CODE:		// Event.SUBSTRUCTURE_NOTIFY
			when_map_notify((MapNotify) event);
			break;
		case UnmapNotify.CODE:		// Event.SUBSTRUCTURE_NOTIFY
			when_unmap_notify((UnmapNotify) event);
			break;
		case ConfigureNotify.CODE:	// Event.SUBSTRUCTURE_NOTIFY, ignored
		case CreateNotify.CODE:		// Event.SUBSTRUCTURE_NOTIFY, ignored
		case MappingNotify.CODE:	// un-avoidable, ignored TODO
			break;
		default:
			logger.config("Unhandled event: " + event);
		}
	}

	private void when_configure_request(final ConfigureRequest event) {
		final WindowManager frame = getFrame(event.window_id);
		if (frame != null) {
			frame.configure(event);
		}
	}

	private void configure(final ConfigureRequest event) {
		if (client.early_unmapped || client.early_destroyed) {
			return;
		}
		client.configure(event.changes());
		client.set_geometry_cache(event.rectangle());
		if (client.state == NORMAL && event.stack_mode () == Window.Changes.ABOVE) {
			set_focus();
		}
	}

	private void when_destroy_notify(final DestroyNotify event) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				final WindowManager frame = getFrame(event.window_id);
				if (frame != null) {
					frame.doDefaultCloseAction();
				}
			}
		});
	}

	private void when_map_request(final MapRequest event) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				open(URI.create(String.valueOf(event.window_id)), getApplicationManager().getDesktopPane());
			}
		});
	}

	private void when_map_notify(final MapNotify event) {
		final WindowManager frame = getFrame(event.window_id);
		if (frame != null) {
			frame.map();
		}
	}

	private WindowManager getFrame(final int id) {
		return getOwner().frames.get(id);
	}

	private void map() {
		if (client.early_unmapped || client.early_destroyed) {
			return;
		}
		if (client.attributes == null) {
			client.attributes = client.attributes();
		}
		if (client.attributes.override_redirect()) {
			return;
		}
		if (client.state != NO_FOCUS) {
			client.state = NORMAL;
		}
		client.set_wm_state(Window.WMState.NORMAL);
		if (client.state != NO_FOCUS) {
			set_focus();
		}
	}

	private void set_focus() {
		client.raise();
		client.set_input_focus();
	}

	private void unset_focus() {
		client.lower();
		getOwner().root.set_input_focus();
	}


	private void when_unmap_notify(final UnmapNotify event) {
		final WindowManager frame = getFrame(event.window_id);
		if (frame != null) {
			frame.unmap();
		}
	}

	private void unmap() {
		if (client.early_destroyed) {
			return;
		}
		client.early_unmapped = false;
		if (client.state != HIDDEN) {
			client.state = UNMANAGED;
			client.set_wm_state(Window.WMState.WITHDRAWN);
			client.change_save_set(true);
		}
	}

	@Override
	public void open() {
		final URI uri = getURI();
		if (uri != null) try {
			open(Integer.parseInt(uri.getSchemeSpecificPart()));
		} catch (final NumberFormatException ex) {
			ex.printStackTrace();
		}
	}

	private void open(final int id) {
		getOwner().frames.put(id, this);
		client = Client.intern(getOwner().display, id);
		if (client.early_unmapped || client.early_destroyed) {
			return;
		}
		client.attributes = client.attributes();
		if (client.attributes.override_redirect()) {
			return;
		}
		client.get_geometry();
		client.class_hint = client.wm_class_hint();
		client.size_hints = client.wm_normal_hints();
		client.name = client.wm_name();
		client.change_save_set(false);
		final Window.WMHints wm_hints = client.wm_hints();
		if (wm_hints == null || (wm_hints.flags() & Window.WMHints.STATE_HINT_MASK) == 0 || wm_hints.initial_state() == Window.WMHints.NORMAL) {
			client.map();
		} else {
			client.state = HIDDEN;
			client.set_wm_state (Window.WMState.ICONIC);
		}
		setTitle(client.name);
		final Rectangle bounds = client.rectangle();
		content = getDesktopPane().getRootPane().getContentPane();
		if (bounds.x != 0 || bounds.y != 0 || bounds.width != 1 || bounds.height != 1) {
			setBounds(bounds.x - panel.getX(), bounds.y - panel.getY() - content.getY(), bounds.width - panel.getWidth() + getWidth(), bounds.height - panel.getHeight() + getHeight());
		} else {
			client.move_resize(getX() + panel.getX(), getY() + panel.getY() + content.getY(), panel.getWidth(), panel.getHeight());
		}
	}

	@Override
	public void close() {
		if (client != null) {
			client.unintern();
			getOwner().frames.remove(client.id);
		}
	}

	private void formInternalFrameClosed(final InternalFrameEvent evt) {
		if (client != null) {
			if (client.early_unmapped || client.early_destroyed) {
				return;
			}
			client.kill();
			getOwner().display.flush();
		}
	}

	private void formInternalFrameIconified(final InternalFrameEvent evt) {
		if (client != null) {
			client.unmap();
			getOwner().display.flush();
			iconified = true;
		}
	}

	private void formInternalFrameDeiconified(final InternalFrameEvent evt) {
		if (client != null) {
			client.map();
			iconified = false;
		}
	}

	private void formInternalFrameActivated(final InternalFrameEvent evt) {
		if (client != null) {
			if (client.early_unmapped || client.early_destroyed) {
				return;
			}
			if (!iconified) {
				set_focus();
				getOwner().display.flush();
			}
		}
	}

	private void formInternalFrameDeactivated(final InternalFrameEvent evt) {
		if (client != null) {
			if (client.early_unmapped || client.early_destroyed) {
				return;
			}
			unset_focus();
			getOwner().display.flush();
		}
	}

	private void formComponentMoved(final ComponentEvent evt) {
		if (client != null && isShowing()) {
			client.move(getX() + panel.getX(), getY() + panel.getY() + content.getY());
			getOwner().display.flush();
		}
	}

	private void formComponentResized(final ComponentEvent evt) {
		if (client != null && isShowing()) {
			client.resize(panel.getWidth(), panel.getHeight());
			getOwner().display.flush();
		}
	}
}
