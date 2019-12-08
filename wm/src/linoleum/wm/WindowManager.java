package linoleum.wm;

import gnu.x11.Display;
import gnu.x11.Error;
import gnu.x11.Rectangle;
import gnu.x11.Window;
import gnu.x11.event.ConfigureNotify;
import gnu.x11.event.ConfigureRequest;
import gnu.x11.event.CreateNotify;
import gnu.x11.event.DestroyNotify;
import gnu.x11.event.Event;
import gnu.x11.event.MapNotify;
import gnu.x11.event.MapRequest;
import gnu.x11.event.MappingNotify;
import gnu.x11.event.UnmapNotify;
import java.awt.Container;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import linoleum.application.Frame;
import linoleum.application.PreferenceSupport;

public class WindowManager extends PreferenceSupport {
	private final String os = System.getProperty("os.name");
	private final String name = System.getenv("DISPLAY");
	private Display display;
	private Window root;
	private Client client;
	private JRootPane panel = getRootPane();
	private final Map<Integer, WindowManager> frames = new HashMap<>();
	private final Logger logger = Logger.getLogger(getClass().getName());
	private int above_sibling_id;
	private boolean closed;

	// internal state
	public static final int UNMANAGED = 0;
	public static final int NORMAL = 1;
	public static final int HIDDEN = 2;
	public static final int NO_FOCUS = 3;

	public WindowManager() {
		initComponents();
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
		if (os != null && os.startsWith("Windows")) {
			return;
		}
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
				} catch (final ExecutionException ex) {
					ex.getCause().printStackTrace();
				} catch (final InterruptedException ex) {
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

	private void read_and_dispatch_event() {
		final Event first_event = display.next_event();
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					dispatch(first_event);
				}
			});
		} catch (final InvocationTargetException e) {
			e.getTargetException().printStackTrace();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private void dispatch(final Event first_event) {
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
		case ConfigureNotify.CODE:	// Event.SUBSTRUCTURE_NOTIFY
			when_configure_notify((ConfigureNotify) event);
			break;
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
			client.set_input_focus();
		}
	}

	private void when_destroy_notify(final DestroyNotify event) {
		final WindowManager frame = getFrame(event.window_id);
		if (frame != null) {
			frame.closed = true;
			frame.doDefaultCloseAction();
		}
	}

	private void when_map_request(final MapRequest event) {
		final WindowManager frame = getFrame(event.window_id);
		if (frame == null) {
			open(URI.create(String.valueOf(event.window_id)), getApplicationManager().getDesktopPane());
		}
	}

	private void when_map_notify(final MapNotify event) {
		final WindowManager frame = getFrame(event.window_id);
		if (frame != null) {
			frame.map();
		}
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
			client.set_input_focus();
		}
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

	private void when_configure_notify(final ConfigureNotify event) {
		final WindowManager frame = getFrame(event.window_id);
		if (frame != null) {
			frame.configure(event.rectangle());
			frame.configure(event.above_sibling_id());
		}
	}

	private void configure(final Rectangle bounds) {
		final int x = bounds.x - panel.getX();
		final int y = bounds.y - panel.getY() - getContent().getY();
		final int width = bounds.width - panel.getWidth() + getWidth();
		final int height = bounds.height - panel.getHeight() + getHeight();
		if (x != getX() || y != getY() || width != getWidth() || height != getHeight()) {
			setBounds(x, y, width, height);
		}
	}

	private void configure(final int above_sibling_id) {
		if (this.above_sibling_id != above_sibling_id) {
			this.above_sibling_id = above_sibling_id;
			if (isSelected()) {
				client.set_input_focus();
			}
		}
	}

	private Container getContent() {
		return getDesktopPane().getRootPane().getContentPane();
	}

 	@Override
	public void load() {
		jTextField1.setText(getPref("program"));
	}

	@Override
	public void save() {
		putPref("program", jTextField1.getText());
	}

	@Override
	public void open() {
		final URI uri = getURI();
		if (uri != null) try {
			open(Integer.parseInt(uri.getSchemeSpecificPart()));
		} catch (final NumberFormatException ex) {
			ex.printStackTrace();
		} else try {
			Runtime.getRuntime().exec(getPref("program"));
		} catch (final IOException ex) {
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
		if (bounds.x != 0 || bounds.y != 0 || bounds.width != 1 || bounds.height != 1) {
			configure(bounds);
		}
	}

	@Override
	public void close() {
		if (client != null) {
			client.unintern();
			getOwner().frames.remove(client.id);
		}
	}

	private WindowManager getFrame(final int id) {
		return getOwner().frames.get(id);
	}

	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                optionPanel1 = new linoleum.application.OptionPanel();
                jLabel1 = new javax.swing.JLabel();
                jTextField1 = new javax.swing.JTextField();

                jLabel1.setText("Program :");

                javax.swing.GroupLayout optionPanel1Layout = new javax.swing.GroupLayout(optionPanel1);
                optionPanel1.setLayout(optionPanel1Layout);
                optionPanel1Layout.setHorizontalGroup(
                        optionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(optionPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 59, Short.MAX_VALUE)
                                .addContainerGap())
                );
                optionPanel1Layout.setVerticalGroup(
                        optionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(optionPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(optionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel1)
                                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );

                setClosable(true);
                setIconifiable(true);
                setMaximizable(true);
                setResizable(true);
                setName("Windows"); // NOI18N
                setOptionPanel(optionPanel1);
                addInternalFrameListener(new javax.swing.event.InternalFrameListener() {
                        public void internalFrameActivated(javax.swing.event.InternalFrameEvent evt) {
                                formInternalFrameActivated(evt);
                        }
                        public void internalFrameClosed(javax.swing.event.InternalFrameEvent evt) {
                                formInternalFrameClosed(evt);
                        }
                        public void internalFrameClosing(javax.swing.event.InternalFrameEvent evt) {
                        }
                        public void internalFrameDeactivated(javax.swing.event.InternalFrameEvent evt) {
                                formInternalFrameDeactivated(evt);
                        }
                        public void internalFrameDeiconified(javax.swing.event.InternalFrameEvent evt) {
                        }
                        public void internalFrameIconified(javax.swing.event.InternalFrameEvent evt) {
                        }
                        public void internalFrameOpened(javax.swing.event.InternalFrameEvent evt) {
                        }
                });
                addComponentListener(new java.awt.event.ComponentAdapter() {
                        public void componentMoved(java.awt.event.ComponentEvent evt) {
                                formComponentMoved(evt);
                        }
                        public void componentResized(java.awt.event.ComponentEvent evt) {
                                formComponentResized(evt);
                        }
                });

                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
                getContentPane().setLayout(layout);
                layout.setHorizontalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 394, Short.MAX_VALUE)
                );
                layout.setVerticalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 274, Short.MAX_VALUE)
                );

                pack();
        }// </editor-fold>//GEN-END:initComponents

        private void formInternalFrameActivated(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameActivated
		if (client != null) {
			client.raise();
			getOwner().display.flush();
		}
        }//GEN-LAST:event_formInternalFrameActivated

        private void formInternalFrameClosed(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameClosed
		if (client != null && !closed) {
			if (client.early_unmapped || client.early_destroyed) {
				return;
			}
			client.kill();
			getOwner().display.flush();
		}
        }//GEN-LAST:event_formInternalFrameClosed

        private void formInternalFrameDeactivated(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameDeactivated
		if (client != null && !closed) {
			client.lower();
			getOwner().display.flush();
		}
        }//GEN-LAST:event_formInternalFrameDeactivated

        private void formComponentMoved(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentMoved
		if (client != null) {
			client.move(getX() + panel.getX(), getY() + panel.getY() + getContent().getY());
			getOwner().display.flush();
		}
        }//GEN-LAST:event_formComponentMoved

        private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
		if (client != null) {
			client.resize(panel.getWidth(), panel.getHeight());
			getOwner().display.flush();
		}
        }//GEN-LAST:event_formComponentResized

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JLabel jLabel1;
        private javax.swing.JTextField jTextField1;
        private linoleum.application.OptionPanel optionPanel1;
        // End of variables declaration//GEN-END:variables
}
