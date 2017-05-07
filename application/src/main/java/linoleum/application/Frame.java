/* Frame.java
 *
 * Copyright (C) 2015 Raphael Jolly
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package linoleum.application;

import java.awt.Component;
import java.beans.Transient;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.DirectoryStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.prefs.Preferences;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.swing.Icon;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JMenuBar;
import javax.swing.JRootPane;

public class Frame extends JInternalFrame implements App {
	private final Preferences prefs = Preferences.userNodeForPackage(getClass());
	private ApplicationManager manager;
	private OptionPanel optionPanel;
	private JMenuBar savedMenuBar;
	private JMenuBar menuBar;
	private String type;
	private String scheme;
	private Icon icon;
	private URI uri;
	private boolean open;
	@Deprecated
	protected int index;
	@Deprecated
	protected Frame parent;
	private final Collection<Integer> openFrames = new HashSet<Integer>();

	public Frame() {
		this((Frame) null);
	}

	public Frame(final String title) {
		this(null, title);
	}

	@Deprecated
	public Frame(final Frame owner, final String title) {
		super(title, true, true, true, true);
		initComponents();
		setOwner(owner == null?this:owner);
	}

	@Deprecated
	public Frame(final Frame owner) {
		initComponents();
		setOwner(owner == null?this:owner);
	}

	public void setOwner(final Frame owner) {
		parent = owner;
		index = parent.nextIndex();
	}

	public Frame getOwner() {
		return parent == this?null:parent;
	}

	public int getIndex() {
		return index;
	}

	private int nextIndex() {
		int index = 0;
		while (openFrames.contains(index)) {
			index++;
		}
		return index;
	}

	@Override
	@Transient
	public int getLayer() {
		return super.getLayer();
	}

	private void openFrame() {
		parent.openFrames.add(index);
		open();
	}

	private void closeFrame() {
		close();
		parent.openFrames.remove(index);
	}

	void setApplicationManager(final ApplicationManager manager) {
		this.manager = manager;
	}

	public ApplicationManager getApplicationManager() {
		return parent.manager;
	}

	public void setOptionPanel(final OptionPanel optionPanel) {
		this.optionPanel = optionPanel;
	}

	OptionPanel getOptionPanel() {
		return optionPanel;
	}

	@Override
	public void setJMenuBar(final JMenuBar menuBar) {
		this.menuBar = menuBar;
	}

	@Override
	public JMenuBar getJMenuBar() {
		return menuBar;
	}

	public void setIcon(final Icon icon) {
		this.icon = icon;
	}

	public Icon getIcon() {
		return icon;
	}

	public void setMimeType(final String type) {
		this.type = type;
	}

	public String getMimeType() {
		return type;
	}

	public void setScheme(final String scheme) {
		this.scheme = scheme;
	}

	public String getScheme() {
		return scheme;
	}

	public void setURI(final URI uri) {
		this.uri = uri;
	}

	public URI getURI() {
		return uri;
	}

	protected JInternalFrame getFrame() {
		return getFrame(this);
	}

	@Deprecated
	protected JInternalFrame getFrame(final Frame owner) {
		return this;
	}

	public void open(final JDesktopPane desktop) {
		open(null, desktop);
	}

	public void open(final URI uri, final JDesktopPane desktop) {
		final JInternalFrame c = getFrame(desktop, uri);
		if (c instanceof Frame) {
			final Frame frame = (Frame) c;
			final boolean changed = uri != null && !frame.reuseFor(uri);
			if (changed) {
				frame.setURI(uri);
			}
			if (frame.getDesktopPane() == null) {
				desktop.add(frame);
				if (frame.open) {
					frame.openFrame();
				}
			} else if (changed) {
				frame.openFrame();
			}
			frame.select();
		} else {
			if (c.getDesktopPane() == null) {
				desktop.add(c);
			}
			manager.select(c);
		}
	}

	public void select() {
		getApplicationManager().select(this);
	}

	private JInternalFrame getFrame(final JDesktopPane desktop, final URI uri) {
		for (final JInternalFrame c : desktop.getAllFrames()) {
			final String name = c.getName();
			if (name != null && name.equals(getName()) && c instanceof Frame) {
				if (((Frame) c).reuseFor(uri)) {
					return c;
				}
			}
		}
		final JInternalFrame c = getFrame();
		if (c instanceof Frame && c != this) {
			((Frame) c).setOwner(this);
		}
		return c;
	}

	protected boolean reuseFor(final URI that) {
		final URI uri = getURI();
		return that == null?that == uri:that.equals(uri);
	}

	public final List<Path> listFiles(final Path path) {
		final List<Path> list = new ArrayList<>();
		try (final DirectoryStream<Path> stream = Files.newDirectoryStream(path, new DirectoryStream.Filter<Path>() {
			public boolean accept(final Path entry) {
				return canOpen(entry);
			}
		})) {
			for (final Path entry : stream) {
				list.add(entry);
			}
		} catch (final IOException ex) {
			ex.printStackTrace();
		}
		return Collections.unmodifiableList(list);
	}

	protected boolean canOpen(final Path entry) {
		try {
			return canOpen(new MimeType(Files.probeContentType(entry)));
		} catch (final IOException | MimeTypeParseException ex) {
			ex.printStackTrace();
		}
		return false;
	}

	public final boolean canOpen(final MimeType t) throws MimeTypeParseException {
		for (final String s : type.split(":")) {
			if (t.match(s)) {
				return true;
			}
		}
		return false;
	}

	public final boolean canOpen(final String p) {
		return Arrays.asList(scheme.split(":")).contains(p);
	}

	protected void init() {
	}

	protected void open() {
	}

	protected void close() {
	}

	protected void load() {
	}

	protected void save() {
	}

	public String getKey(final String str) {
		return getName() + "." + str;
	}

	@Override
	public final String toString() {
		return getName();
	}

	private boolean isRecording() {
		final Object obj = getDesktopPane().getClientProperty("DesktopPane.recording");
		return obj instanceof Boolean?(Boolean) obj:false;
	}

	private boolean isReopen() {
		final Object obj = getDesktopPane().getClientProperty("DesktopPane.reopen");
		return obj instanceof Boolean?(Boolean) obj:false;
	}

	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                setName(getClass().getSimpleName());
                addInternalFrameListener(new javax.swing.event.InternalFrameListener() {
                        public void internalFrameOpened(javax.swing.event.InternalFrameEvent evt) {
                                formInternalFrameOpened(evt);
                        }
                        public void internalFrameClosing(javax.swing.event.InternalFrameEvent evt) {
                                formInternalFrameClosing(evt);
                        }
                        public void internalFrameClosed(javax.swing.event.InternalFrameEvent evt) {
                        }
                        public void internalFrameIconified(javax.swing.event.InternalFrameEvent evt) {
                        }
                        public void internalFrameDeiconified(javax.swing.event.InternalFrameEvent evt) {
                        }
                        public void internalFrameActivated(javax.swing.event.InternalFrameEvent evt) {
                                formInternalFrameActivated(evt);
                        }
                        public void internalFrameDeactivated(javax.swing.event.InternalFrameEvent evt) {
                                formInternalFrameDeactivated(evt);
                        }
                });
                addComponentListener(new java.awt.event.ComponentAdapter() {
                        public void componentResized(java.awt.event.ComponentEvent evt) {
                                formComponentResized(evt);
                        }
                        public void componentMoved(java.awt.event.ComponentEvent evt) {
                                formComponentMoved(evt);
                        }
                });

                pack();
        }// </editor-fold>//GEN-END:initComponents

        private void formInternalFrameOpened(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameOpened
		if (!isReopen()) {
			final int x = prefs.getInt(getKey("x"), getX());
			final int y = prefs.getInt(getKey("y"), getY());
			final int width = prefs.getInt(getKey("width"), getWidth());
			final int height = prefs.getInt(getKey("height"), getHeight());
			setBounds(x, y, width, height);
		}
		openFrame();
		open = true;
        }//GEN-LAST:event_formInternalFrameOpened

        private void formInternalFrameClosing(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameClosing
		closeFrame();
        }//GEN-LAST:event_formInternalFrameClosing

        private void formInternalFrameActivated(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameActivated
		final JRootPane panel = getDesktopPane().getRootPane();
		if (savedMenuBar == null) savedMenuBar = panel.getJMenuBar();
		if (menuBar != null) panel.setJMenuBar(menuBar);
        }//GEN-LAST:event_formInternalFrameActivated

        private void formInternalFrameDeactivated(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameDeactivated
		final JRootPane panel = getDesktopPane().getRootPane();
		if (menuBar != null) panel.setJMenuBar(savedMenuBar);
        }//GEN-LAST:event_formInternalFrameDeactivated

        private void formComponentMoved(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentMoved
		if (isShowing() && !isMaximum() && isRecording()) {
			final Component c = evt.getComponent();
			prefs.putInt(getKey("x"), c.getX());
			prefs.putInt(getKey("y"), c.getY());
		}
        }//GEN-LAST:event_formComponentMoved

        private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
		if (isShowing() && !isMaximum() && isRecording()) {
			final Component c = evt.getComponent();
			prefs.putInt(getKey("width"), c.getWidth());
			prefs.putInt(getKey("height"), c.getHeight());
		}
        }//GEN-LAST:event_formComponentResized

        // Variables declaration - do not modify//GEN-BEGIN:variables
        // End of variables declaration//GEN-END:variables
}
