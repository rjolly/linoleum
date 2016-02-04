/* ApplicationManager.java
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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import javax.activation.MimeType;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.SwingUtilities;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import linoleum.application.event.ClassPathListener;
import linoleum.application.event.ClassPathChangeEvent;

public class ApplicationManager extends Frame implements ClassPathListener {
	private final Icon defaultIcon = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/development/Application24.gif"));
	private final Map<String, App> map = new HashMap<>();
	private final Map<String, String> apps = new HashMap<>();
	private final DefaultListModel<App> model = new DefaultListModel<>();
	private final ListCellRenderer renderer = new Renderer();

	private class Renderer extends JLabel implements ListCellRenderer {

		public Renderer() {
			setOpaque(true);
			setHorizontalAlignment(CENTER);
			setVerticalAlignment(CENTER);
			setVerticalTextPosition(BOTTOM);
			setHorizontalTextPosition(CENTER);
		}

		@Override
		public Component getListCellRendererComponent(final JList list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}

			final App app = (App)value;
			final Icon icon = app.getIcon();
			setIcon(icon == null?defaultIcon:icon);
			setText(app.getName());
			setFont(list.getFont());

			return this;
		}
	};

	public ApplicationManager() {
		initComponents();
		refresh();
	}

	public void open(final URI uri) {
		open(getApplication(uri), uri);
	}

	private String getApplication(final URI uri) {
		final Path path = Paths.get(uri);
		try {
			final String str = Files.probeContentType(path);
			if (apps.containsKey(str)) {
				return apps.get(str);
			}
			final MimeType type = new MimeType(str);
			for (final Map.Entry<String, String> entry : apps.entrySet()) {
				final String key = entry.getKey();
				final String value = entry.getValue();
				if (type.match(key)) {
					return value;
				}
			}
		} catch (final Exception ex) {}
		return path.toFile().isDirectory()?"FileManager":null;
	}

	public void open(final String name, final URI uri) {
		if (map.containsKey(name)) {
			map.get(name).open(this, uri);
		}
	}

	private void open(final int index) {
		model.getElementAt(index).open(this);
	}

	public void classPathChanged(final ClassPathChangeEvent e) {
		refresh();
		for (final App app : map.values()) {
			if (app instanceof ClassPathListener) {
				((ClassPathListener)app).classPathChanged(e);
			}
		}
	}

	private void refresh() {
		for (final JInternalFrame frame : ServiceLoader.load(JInternalFrame.class)) {
			if (frame instanceof App) {
				process((App)frame);
			} else {
				process(new App() {

					@Override
					public String getName() {
						final String name = frame.getName();
						return name == null?frame.getClass().getSimpleName():name;
					}

					@Override
					public Icon getIcon() {
						return null;
					}

					@Override
					public String getMimeType() {
						return null;
					}

					@Override
					public void open(final ApplicationManager manager, final URI uri) {
						open(manager);
					}

					@Override
					public void open(final ApplicationManager manager) {
						if (frame.getDesktopPane() == null) {
							manager.getDesktopPane().add(frame);
						}
						select(frame);
					}
				});
			}
		}
		for (final Application app : ServiceLoader.load(Application.class)) {
			process(new App() {

				@Override
				public String getName() {
					return app.getName();
				}

				@Override
				public Icon getIcon() {
					return app.getIcon();
				}

				@Override
				public String getMimeType() {
					return app.getMimeType();
				}

				@Override
				public void open(final ApplicationManager manager, final URI uri) {
					final JInternalFrame frame = app.open(uri);
					if (frame instanceof Frame) {
						((Frame)frame).setApplicationManager(manager);
					} else {
						if (frame.getDesktopPane() == null) {
							manager.getDesktopPane().add(frame);
						}
						select(frame);
					}
				}

				@Override
				public void open(final ApplicationManager manager) {
					open(manager, null);
				}
			});
		}
	}

	void select(final JInternalFrame frame) {
		frame.setVisible(true);
		try {
			if (frame.isIcon()) {
				frame.setIcon(false);
			} else {
				frame.setSelected(true);
			}
		} catch (final PropertyVetoException ex) {}
	}

	public void select() {
		select(this);
	}

	private final void process(final App app) {
		final String name = app.getName();
		if (!map.containsKey(name)) {
			map.put(name, app);
			final String type = app.getMimeType();
			if (type != null) for (final String s : type.split(":")) apps.put(s, name);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					model.addElement(app);
				}
			});
		}
	}

	/**
	 * This method is called from within the constructor to initialize the
	 * form. WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                jScrollPane1 = new javax.swing.JScrollPane();
                jList1 = new javax.swing.JList();

                setClosable(true);
                setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
                setIconifiable(true);
                setMaximizable(true);
                setResizable(true);
                setTitle("Applications");

                jList1.setModel(model);
                jList1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
                jList1.setCellRenderer(renderer);
                jList1.setLayoutOrientation(javax.swing.JList.VERTICAL_WRAP);
                jList1.setVisibleRowCount(-1);
                jList1.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                                jList1MouseClicked(evt);
                        }
                });
                jScrollPane1.setViewportView(jList1);

                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
                getContentPane().setLayout(layout);
                layout.setHorizontalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 394, Short.MAX_VALUE)
                );
                layout.setVerticalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE)
                );

                pack();
        }// </editor-fold>//GEN-END:initComponents

        private void jList1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jList1MouseClicked
		if (evt.getClickCount() == 2) {
			open(jList1.locationToIndex(evt.getPoint()));
		}
        }//GEN-LAST:event_jList1MouseClicked


        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JList jList1;
        private javax.swing.JScrollPane jScrollPane1;
        // End of variables declaration//GEN-END:variables
}
