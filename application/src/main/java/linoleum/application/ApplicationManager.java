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
import java.awt.Dimension;
import java.awt.Point;
import java.beans.PropertyVetoException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.ServiceLoader;
import javax.activation.MimeType;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import linoleum.application.event.ClassPathListener;
import linoleum.application.event.ClassPathChangeEvent;

public class ApplicationManager extends Frame {
	private final Icon defaultIcon = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/development/Application24.gif"));
	private final List<ClassPathListener> listeners = new ArrayList<>();
	private final Map<String, App> map = new HashMap<>();
	private final Map<String, String> pref = new HashMap<>();
	private final Map<String, List<String>> apps = new HashMap<>();
	private final DefaultListModel<App> model = new DefaultListModel<>();
	private final List<OptionPanel> options = new ArrayList<>(); 
	private final ListCellRenderer renderer = new Renderer();
	private final DefaultComboBoxModel<String> comboModel = new DefaultComboBoxModel<>();
	private final DefaultTableModel tableModel;

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
		tableModel = (DefaultTableModel)jTable1.getModel();
		setApplicationManager(this);
		addOptionPanel(this);
	}

	@Override
	public OptionPanel getOptionPanel() {
		return optionPanel1;
	}

	public List<OptionPanel> getOptionPanels() {
		return Collections.unmodifiableList(options);
	}

	private TableCellEditor getEditor() {
		return new DefaultCellEditor(jComboBox1) {
			@Override
			public Component getTableCellEditorComponent(final JTable table, final Object value, final boolean isSelected, final int row, final int column) {
				comboModel.removeAllElements();
				comboModel.addElement(null);
				final String str = (String) tableModel.getValueAt(row, 0);
				for (final String name : apps.get(str)) {
					comboModel.addElement(name);
				}
				return super.getTableCellEditorComponent(table, value, isSelected, row, column);
			}
		};
	};

	@Override
	public void load() {
		tableModel.setRowCount(0);
		for (final String str : apps.keySet()) {
			tableModel.addRow(new Object[] {str, pref.get(str)});
		}
	}

	@Override
	public void save() {
		for (int row = 0 ; row < tableModel.getRowCount() ; row++) {
			final String str = (String) tableModel.getValueAt(row, 0);
			final String name = (String) tableModel.getValueAt(row, 1);
			if (name != null && !name.isEmpty()) {
				pref.put(str, name);
			} else {
				pref.remove(str);
			}
		}
	}

	public void open(final URI uri) {
		open(getApplication(uri), uri);
	}

	private String getApplication(final URI uri) {
		final Collection<String> coll = new LinkedHashSet<>();
		final Path path = Paths.get(uri);
		try {
			final String str = Files.probeContentType(path);
			if (apps.containsKey(str)) {
				if (pref.containsKey(str)) {
					coll.add(pref.get(str));
				}
				coll.addAll(apps.get(str));
			}
			final MimeType type = new MimeType(str);
			for (final Map.Entry<String, List<String>> entry : apps.entrySet()) {
				final String key = entry.getKey();
				final List<String> value = entry.getValue();
				if (type.match(key)) {
					if (pref.containsKey(key)) {
						coll.add(pref.get(key));
					}
					coll.addAll(value);
				}
			}
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
		final Iterator<String> it = coll.iterator();
		return it.hasNext()?it.next():null;
	}

	public void open(final String name, final URI uri) {
		if (map.containsKey(name)) {
			map.get(name).open(this, uri);
		}
	}

	public void open(final String name) {
		if (map.containsKey(name)) {
			map.get(name).open(this);
		}
	}

	private void open(final int index) {
		model.getElementAt(index).open(this);
	}

	public void addClassPathListener(final ClassPathListener listener) {
		listeners.add(listener);
	}

	public void removeClassPathListener(final ClassPathListener listener) {
		listeners.remove(listener);
	}

	public void fireClassPathChange(final ClassPathChangeEvent evt) {
		for (final ClassPathListener listener : listeners) {
			listener.classPathChanged(evt);
		}
	}

	@Override
	public void classPathChanged(final ClassPathChangeEvent e) {
		open();
	}

	@Override
	public void open() {
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
						manager.select(frame);
					}

					@Override
					public OptionPanel getOptionPanel() {
						return null;
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
					if (frame.getDesktopPane() == null) {
						if (frame instanceof Frame) {
							((Frame)frame).setApplicationManager(manager);
						}
						manager.getDesktopPane().add(frame);
					}
					manager.select(frame);
				}

				@Override
				public void open(final ApplicationManager manager) {
					open(manager, null);
				}

				@Override
				public OptionPanel getOptionPanel() {
					return null;
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
		final Dimension size = getDesktopPane().getSize();
		final Dimension s = frame.getSize();
		final Point p = frame.getLocation();
		final int x = Math.max(Math.min(p.x - (p.x + s.width - size.width), p.x), 0);
		final int y = Math.max(Math.min(p.y - (p.y + s.height - size.height), p.y), 0);
		if (x != p.x || y != p.y) {
			frame.setLocation(x, y);
		}
		final int width = Math.min(s.width, size.width);
		final int height = Math.min(s.height, size.height);
		if (width < s.width || height < s.height) {
			frame.setSize(width, height);
		}
	}

	public void select() {
		select(this);
	}

	private void process(final App app) {
		final String name = app.getName();
		if (!map.containsKey(name)) {
			map.put(name, app);
			final String type = app.getMimeType();
			if (type != null) {
				for (final String s : type.split(":")) {
					List<String> list = apps.get(s);
					if (list == null) {
						apps.put(s, list = new ArrayList<>());
					}
					list.add(name);
				}
			}
			model.addElement(app);
			addOptionPanel(app);
		}
	}

	private void addOptionPanel(final App app) {
		final OptionPanel panel = app.getOptionPanel();
		if (panel != null) {
			options.add(panel);
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

                optionPanel1 = new linoleum.application.OptionPanel();
                jScrollPane2 = new javax.swing.JScrollPane();
                jTable1 = new javax.swing.JTable();
                jComboBox1 = new javax.swing.JComboBox();
                jScrollPane1 = new javax.swing.JScrollPane();
                jList1 = new javax.swing.JList();

                optionPanel1.setFrame(this);

                jTable1.setModel(new javax.swing.table.DefaultTableModel(
                        new Object [][] {

                        },
                        new String [] {
                                "Type", "Name"
                        }
                ) {
                        Class[] types = new Class [] {
                                java.lang.String.class, java.lang.String.class
                        };
                        boolean[] canEdit = new boolean [] {
                                false, true
                        };

                        public Class getColumnClass(int columnIndex) {
                                return types [columnIndex];
                        }

                        public boolean isCellEditable(int rowIndex, int columnIndex) {
                                return canEdit [columnIndex];
                        }
                });
                jScrollPane2.setViewportView(jTable1);
                if (jTable1.getColumnModel().getColumnCount() > 0) {
                        jTable1.getColumnModel().getColumn(1).setCellEditor(getEditor());
                }

                javax.swing.GroupLayout optionPanel1Layout = new javax.swing.GroupLayout(optionPanel1);
                optionPanel1.setLayout(optionPanel1Layout);
                optionPanel1Layout.setHorizontalGroup(
                        optionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                );
                optionPanel1Layout.setVerticalGroup(
                        optionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                );

                jComboBox1.setModel(comboModel);
                jComboBox1.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jComboBox1ActionPerformed(evt);
                        }
                });

                setClosable(true);
                setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
                setIconifiable(true);
                setMaximizable(true);
                setResizable(true);
                setTitle("Applications");
                setName("Applications"); // NOI18N

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
		if (evt.getClickCount() == 1) {
			open(jList1.locationToIndex(evt.getPoint()));
		}
        }//GEN-LAST:event_jList1MouseClicked

        private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
		optionPanel1.setDirty(true);
        }//GEN-LAST:event_jComboBox1ActionPerformed

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JComboBox jComboBox1;
        private javax.swing.JList jList1;
        private javax.swing.JScrollPane jScrollPane1;
        private javax.swing.JScrollPane jScrollPane2;
        private javax.swing.JTable jTable1;
        private linoleum.application.OptionPanel optionPanel1;
        // End of variables declaration//GEN-END:variables
}
