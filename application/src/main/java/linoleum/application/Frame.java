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
import java.beans.PropertyVetoException;
import java.net.URI;
import java.util.HashSet;
import java.util.Collection;
import java.util.Collections;
import java.util.prefs.Preferences;
import javax.swing.Icon;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JMenuBar;
import javax.swing.JRootPane;
import linoleum.application.event.ClassPathListener;
import linoleum.application.event.ClassPathChangeEvent;

public class Frame extends JInternalFrame implements App, ClassPathListener {
	private final Preferences prefs = Preferences.userNodeForPackage(getClass());
	private ApplicationManager manager;
	private JMenuBar savedMenuBar;
	private JMenuBar menuBar;
	private String type;
	private Icon icon;
	private URI uri;
	private boolean ready;
	protected final int index;
	private final Collection<Integer> openFrames;
	private static final int offset = 30;

	public Frame() {
		this(new HashSet<Integer>());
                setName(getClass().getSimpleName());
	}

	public Frame(final String title) {
		this(new HashSet<Integer>(), title);
                setName(getClass().getSimpleName());
	}

	public Frame(final Collection<Integer> openFrames, final String title) {
		super(title, true, true, true, true);
		initComponents();
		index = openFrames.isEmpty()?0:Collections.max(openFrames) + 1;
		this.openFrames = openFrames;
	}

	public Frame(final Collection<Integer> openFrames) {
		initComponents();
		index = openFrames.isEmpty()?0:Collections.max(openFrames) + 1;
		this.openFrames = openFrames;
	}

	private void openFrame() {
		if (manager != null) {
			manager.addClassPathListener(this);
		}
		openFrames.add(index);
		open();
	}

	private void closeFrame() {
		close();
		openFrames.remove(index);
		if (manager != null) {
			manager.removeClassPathListener(this);
		}
	}

	public void setApplicationManager(final ApplicationManager manager) {
		this.manager = manager;
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

	@Override
	public Icon getIcon() {
		return icon;
	}

	public void setMimeType(final String type) {
		this.type = type;
	}

	@Override
	public String getMimeType() {
		return type;
	}

	public void setURI(final URI uri) {
		this.uri = uri;
	}

	public URI getURI() {
		return uri;
	}

	protected Frame getFrame() {
		return getFrame(openFrames);
	}

	protected Frame getFrame(final Collection<Integer> openFrames) {
		return this;
	}

	public final void open(final ApplicationManager manager) {
		open(manager, null);
	}

	public final void open(final ApplicationManager manager, final URI uri) {
		final JDesktopPane desktop = manager.getDesktopPane();
		final Frame frame = getFrame(desktop, uri);
		final boolean changed = uri != null && !uri.equals(frame.getURI());
		if (changed) {
			frame.setURI(uri);
		}
		if (frame.getDesktopPane() == null) {
			if (frame.manager == null) {
				frame.manager = manager;
				desktop.add(frame);
			} else {
				desktop.add(frame);
				frame.openFrame();
			}
		} else if (changed) {
			frame.openFrame();
		}
		manager.select(frame);
	}

	private Frame getFrame(final JDesktopPane desktop, final URI uri) {
		for (final JInternalFrame c : desktop.getAllFrames()) {
			if (getName().equals(c.getName()) && c instanceof Frame) {
				final Frame frame = (Frame)c;
				if (uri == null?null == frame.getURI():uri.equals(frame.getURI())) {
					return frame;
				}
			}
		}
		final Frame frame = getFrame();
		if (frame != this) {
			frame.setName(getName());
			frame.icon = icon;
			frame.type = type;
		}
		return frame;
	}

	public OptionPanel getOptionPanel() {
		return null;
	}

	protected void open() {
	}

	protected void close() {
	}

	public void classPathChanged(final ClassPathChangeEvent e) {
	}

	@Deprecated
	public void open(final JDesktopPane desktop) {
		desktop.add(this);
	}

	/**
	 * This method is called from within the constructor to initialize the
	 * form. WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

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
                        public void componentShown(java.awt.event.ComponentEvent evt) {
                                formComponentShown(evt);
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
		if (ready) {
			final Component c = evt.getComponent();
			prefs.putInt(getName() + ".x", c.getX() - offset * index);
			prefs.putInt(getName() + ".y", c.getY() - offset * index);
		}
        }//GEN-LAST:event_formComponentMoved

        private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
		if (ready) {
			final Component c = evt.getComponent();
			prefs.putInt(getName() + ".width", c.getWidth());
			prefs.putInt(getName() + ".height", c.getHeight());
		}
        }//GEN-LAST:event_formComponentResized

        private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
		ready = true;
        }//GEN-LAST:event_formComponentShown

        // Variables declaration - do not modify//GEN-BEGIN:variables
        // End of variables declaration//GEN-END:variables
}
