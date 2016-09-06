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
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.DirectoryStream;
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

public class Frame extends JInternalFrame {
	private final Preferences prefs = Preferences.userNodeForPackage(getClass());
	private ApplicationManager manager;
	private JMenuBar savedMenuBar;
	private JMenuBar menuBar;
	private String type;
	private Icon icon;
	private URI uri;
	private boolean opened;
	protected final int index;
	protected final Frame parent;
	private final Collection<Integer> openFrames = new HashSet<Integer>();
	private static final int offset = 30;

	public Frame() {
		this((Frame) null);
	}

	public Frame(final String title) {
		this(null, title);
	}

	public Frame(final Frame parent, final String title) {
		super(title, true, true, true, true);
		initComponents();
		this.parent = parent == null?this:parent;
		index = this.parent.nextIndex();
	}

	public Frame(final Frame parent) {
		initComponents();
		this.parent = parent == null?this:parent;
		index = this.parent.nextIndex();
	}

	private int nextIndex() {
		int index = 0;
		while (openFrames.contains(index)) {
			index++;
		}
		return index;
	}

	private void openFrame() {
		parent.openFrames.add(index);
		open();
	}

	private void closeFrame() {
		close();
		parent.openFrames.remove(index);
	}

	public void setApplicationManager(final ApplicationManager manager) {
		this.manager = manager;
		init();
	}

	public ApplicationManager getApplicationManager() {
		return manager;
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

	public void setURI(final URI uri) {
		this.uri = uri;
	}

	public URI getURI() {
		return uri;
	}

	protected JInternalFrame getFrame() {
		return getFrame(this);
	}

	protected JInternalFrame getFrame(final Frame parent) {
		return this;
	}

	public void open(final URI uri) {
		final JDesktopPane desktop = manager.getDesktopPane();
		final JInternalFrame internal = getFrame(desktop, uri);
		if (internal instanceof Frame) {
			final Frame frame = (Frame) internal;
			final boolean changed = uri != null && !uri.equals(frame.getURI());
			if (changed) {
				frame.setURI(uri);
			}
			if (frame.getDesktopPane() == null) {
				if (!frame.opened) {
					frame.manager = manager;
					desktop.add(frame);
				} else {
					desktop.add(frame);
					frame.openFrame();
				}
			} else if (changed) {
				frame.openFrame();
			}
			frame.select();
		} else {
			if (internal.getDesktopPane() == null) {
				desktop.add(internal);
			}
			manager.select(internal);
		}
	}

	public void select() {
		manager.select(this);
	}

	private JInternalFrame getFrame(final JDesktopPane desktop, final URI uri) {
		JInternalFrame frame = null;
		for (final JInternalFrame c : desktop.getAllFrames()) {
			final String name = c.getName();
			if (name != null && name.equals(getName()) && c instanceof Frame) {
				final Frame f = (Frame)c;
				if (f.reuseFor(uri)) {
					frame = f;
					break;
				}
			}
		}
		if (frame == null) {
			frame = getFrame();
		}
		return frame;
	}

	protected boolean reuseFor(final URI that) {
		final URI uri = getURI();
		return that == null?uri == null:uri == null?false:uri.equals(that);
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
			final MimeType t = new MimeType(Files.probeContentType(entry));
			for (final String s : type.split(":")) {
				if (t.match(s)) {
					return true;
				}
			}
		} catch (final IOException | MimeTypeParseException ex) {
			ex.printStackTrace();
		}
		return false;
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

	@Override
	public String toString() {
		return getName();
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
		final int x = prefs.getInt(getName() + ".x", getX());
		final int y = prefs.getInt(getName() + ".y", getY());
		final int width = prefs.getInt(getName() + ".width", getWidth());
		final int height = prefs.getInt(getName() + ".height", getHeight());
		setBounds(x + offset * index, y + offset * index, width, height);
		openFrame();
		opened = true;
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
		if (isShowing() && !isMaximum()) {
			final Component c = evt.getComponent();
			prefs.putInt(getName() + ".x", c.getX() - offset * index);
			prefs.putInt(getName() + ".y", c.getY() - offset * index);
		}
        }//GEN-LAST:event_formComponentMoved

        private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
		if (isShowing() && !isMaximum()) {
			final Component c = evt.getComponent();
			prefs.putInt(getName() + ".width", c.getWidth());
			prefs.putInt(getName() + ".height", c.getHeight());
		}
        }//GEN-LAST:event_formComponentResized

        // Variables declaration - do not modify//GEN-BEGIN:variables
        // End of variables declaration//GEN-END:variables
}
