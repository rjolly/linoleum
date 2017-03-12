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
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
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
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import linoleum.application.event.ClassPathListener;
import linoleum.application.event.ClassPathChangeEvent;

public class ApplicationManager extends Frame {
	private final Icon defaultIcon = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/development/Application24.gif"));
	private final List<ClassPathListener> listeners = new ArrayList<>();
	private final Map<String, Frame> map = new HashMap<>();
	private final Comparator<MimeType> comparator = new Comparator<MimeType>() {
		@Override
		public int compare(final MimeType a, final MimeType b) {
			final int p = a.getPrimaryType().compareTo(b.getPrimaryType());
			return p != 0?p:compare(a.getSubType(), b.getSubType());
		}

		private int compare(final String a, final String b) {
			return a.equals("*") && !b.equals("*")?1:!a.equals("*") && b.equals("*")?-1:a.compareTo(b);
		}
	};
	private final SortedMap<MimeType, String> pref = new TreeMap<>(comparator);
	private final SortedMap<MimeType, List<String>> apps = new TreeMap<>(comparator);
	private final SortedMap<String, String> spref = new TreeMap<>();
	private final SortedMap<String, List<String>> sapps = new TreeMap<>();
	private final DefaultListModel<Frame> model = new DefaultListModel<>();
	private final List<String> names = new ArrayList<>(); 
	private final List<OptionPanel> options = new ArrayList<>(); 
	private final Logger logger = Logger.getLogger(getClass().getName());
	private final Preferences prefs = Preferences.userNodeForPackage(getClass());
	private final ListCellRenderer renderer = new Renderer();
	private final DefaultComboBoxModel<String> comboModel = new DefaultComboBoxModel<>();
	private final DefaultTableModel tableModel;
	private final DefaultTableModel schemeTableModel;

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
			final Frame app = (Frame) value;
			final Icon icon = app.getIcon();
			setIcon(icon == null?defaultIcon:icon);
			setText(app.getName());
			setFont(list.getFont());
			return this;
		}
	}

	public ApplicationManager() {
		initComponents();
		tableModel = (DefaultTableModel) jTable1.getModel();
		schemeTableModel = (DefaultTableModel) jTable2.getModel();
		prefs.addPreferenceChangeListener(new PreferenceChangeListener() {
			@Override
			public void preferenceChange(final PreferenceChangeEvent evt) {
				if (evt.getKey().equals(getKey("preferred"))) {
					pref.clear();
					pref.putAll(getPreferred());
				} else if (evt.getKey().equals(getKey("scheme-preferred"))) {
					spref.clear();
					spref.putAll(getPreferredByScheme());
				}
			}
		});
		setApplicationManager(this);
		pref.putAll(getPreferred());
		spref.putAll(getPreferredByScheme());
	}

	private Map<MimeType, String> getPreferred() {
		final Map<MimeType, String> pref = new TreeMap<>(comparator);
		final String str = prefs.get(getKey("preferred"), "");
		for (final String entry : str.split(", ")) {
			final String s[] = entry.split("=");
			if (s.length > 1) try {
				pref.put(new MimeType(s[0]), s[1]);
			} catch (final MimeTypeParseException ex) {
				ex.printStackTrace();
			}
		}
		return pref;
	}

	private void setPreferred(final Map<MimeType, String> pref) {
		final String str = pref.toString();
		prefs.put(getKey("preferred"), str.substring(1, str.length() - 1));
	}

	private Map<String, String> getPreferredByScheme() {
		final Map<String, String> pref = new TreeMap<>();
		final String str = prefs.get(getKey("scheme-preferred"), "");
		for (final String entry : str.split(", ")) {
			final String s[] = entry.split("=");
			if (s.length > 1) {
				pref.put(s[0], s[1]);
			}
		}
		return pref;
	}

	private void setPreferredByScheme(final Map<String, String> pref) {
		final String str = pref.toString();
		prefs.put(getKey("scheme-preferred"), str.substring(1, str.length() - 1));
	}

	@Override
	public void init() {
		getApplicationManager().addClassPathListener(new ClassPathListener() {
			@Override
			public void classPathChanged(final ClassPathChangeEvent e) {
				open();
			}
		});
	}

	void addOptionPanel(final OptionPanel panel) {
		options.add(panel);
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
				final MimeType type = (MimeType) tableModel.getValueAt(row, 0);
				for (final String name : apps.get(type)) {
					comboModel.addElement(name);
				}
				return super.getTableCellEditorComponent(table, value, isSelected, row, column);
			}
		};
	}

	@Override
	public void load() {
		tableModel.setRowCount(0);
		final Map<MimeType, String> pref = getPreferred();
		for (final MimeType type : apps.keySet()) {
			tableModel.addRow(new Object[] {type, pref.get(type)});
		}
		schemeTableModel.setRowCount(0);
		final Map<String, String> spref = getPreferredByScheme();
		for (final String scheme : sapps.keySet()) {
			schemeTableModel.addRow(new Object[] {scheme, spref.get(scheme)});
		}
	}

	@Override
	public void save() {
		final Map<MimeType, String> pref = new TreeMap<>(comparator);
		for (int row = 0 ; row < tableModel.getRowCount() ; row++) {
			final MimeType type = (MimeType) tableModel.getValueAt(row, 0);
			final String name = (String) tableModel.getValueAt(row, 1);
			if (name != null && !name.isEmpty()) {
				pref.put(type, name);
			}
		}
		setPreferred(pref);
		final Map<String, String> spref = new TreeMap<>();
		for (int row = 0 ; row < schemeTableModel.getRowCount() ; row++) {
			final String scheme = (String) schemeTableModel.getValueAt(row, 0);
			final String name = (String) schemeTableModel.getValueAt(row, 1);
			if (name != null && !name.isEmpty()) {
				spref.put(scheme, name);
			}
		}
		setPreferredByScheme(spref);
	}

	@Override
	public void open(final URI uri) {
		String name = getApplication(uri);
		if (name != null) {
			open(name, uri);
		} else {
			final List<String> names = getApplications(uri);
			if (names.size() > 0) {
				open(names.get(0), uri);
			}
		}
	}

	public String getApplication(final URI uri) {
		try {
			return getApplication(Paths.get(uri));
		} catch (final FileSystemNotFoundException ex) {
		}
		return getApplication(uri.getScheme());
	}

	private String getApplication(final Path path) {
		try {
			return getApplication(new MimeType(Files.probeContentType(path)));
		} catch (final IOException | MimeTypeParseException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	private String getApplication(final MimeType type) {
		if (pref.containsKey(type)) {
			return pref.get(type);
		}
		for (final Map.Entry<MimeType, String> entry : pref.entrySet()) {
			if (type.match(entry.getKey())) {
				return entry.getValue();
			}
		}
		return null;
	}

	public String getApplication(final String scheme) {
		if (spref.containsKey(scheme)) {
			return spref.get(scheme);
		}
		return null;
	}

	public List<String> getApplications(final URI uri) {
		try {
			return getApplications(Paths.get(uri));
		} catch (final FileSystemNotFoundException ex) {
		}
		return getApplications(uri.getScheme());
	}

	private List<String> getApplications(final Path path) {
		final List<String> names = new ArrayList<>();
		try {
			names.addAll(getApplications(new MimeType(Files.probeContentType(path))));
		} catch (final IOException | MimeTypeParseException ex) {
			ex.printStackTrace();
		}
		return Collections.unmodifiableList(names);
	}

	private List<String> getApplications(final MimeType type) {
		final List<String> names = new ArrayList<>();
		if (apps.containsKey(type)) {
			names.addAll(apps.get(type));
		}
		for (final Map.Entry<MimeType, List<String>> entry : apps.entrySet()) {
			if (type.match(entry.getKey())) {
				final List<String> s = new ArrayList<>(entry.getValue());
				s.removeAll(names);
				names.addAll(s);
			}
		}
		return Collections.unmodifiableList(names);
	}

	public List<String> getApplications(final String scheme) {
		final List<String> names = new ArrayList<>();
		if (sapps.containsKey(scheme)) {
			names.addAll(sapps.get(scheme));
		}
		return Collections.unmodifiableList(names);
	}

	public List<String> getApplications() {
		return Collections.unmodifiableList(names);
	}

	public void open(final String name, final URI uri) {
		if (map.containsKey(name)) {
			map.get(name).open(uri);
		}
	}

	public void open(final String name) {
		open(name, null);
	}

	private void open(final int index) {
		if (index < 0) {
		} else {
			model.getElementAt(index).open((URI) null);
		}
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
	public void open() {
		(new SwingWorker<Object, Object>() {
			@Override
			public Object doInBackground() {
				doOpen();
				return null;
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

	@SuppressWarnings("deprecation")
	private void doOpen() {
		for (final JInternalFrame frame : ServiceLoader.load(JInternalFrame.class)) {
			if (frame instanceof Frame) {
				process((Frame) frame);
			} else {
				process(new Frame() {
					@Override
					public JInternalFrame getFrame() {
						return frame;
					}

					@Override
					public String getName() {
						final String name = frame.getName();
						return name == null?frame.getClass().getSimpleName():name;
					}
				});
			}
		}
		for (final Application app : ServiceLoader.load(Application.class)) {
			process(new Frame() {
				@Override
				public JInternalFrame getFrame() {
					return app.open(getURI());
				}

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
				public void open(final URI uri) {
					setURI(uri);
					super.open(uri);
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
		} catch (final PropertyVetoException ex) {
			ex.printStackTrace();
		}
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

	private void process(final Frame app) {
		final String name = app.getName();
		if (!map.containsKey(name)) {
			logger.config("Processing " + name);
			map.put(name, app);
			final String str = app.getMimeType();
			if (str != null) {
				names.add(name);
				for (final String s : str.split(":")) try {
					final MimeType type = new MimeType(s);
					List<String> list = apps.get(type);
					if (list == null) {
						apps.put(type, list = new ArrayList<>());
					}
					list.add(name);
				} catch (final MimeTypeParseException ex) {
					ex.printStackTrace();
				}
			}
			final String ss = app.getScheme();
			if (ss != null) {
				for (final String scheme : ss.split(":")) {
					List<String> list = sapps.get(scheme);
					if (list == null) {
						sapps.put(scheme, list = new ArrayList<>());
					}
					list.add(name);
				}
			}
			model.addElement(app);
			app.setApplicationManager(this);
		}
	}

	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                optionPanel1 = new linoleum.application.OptionPanel();
                jTabbedPane1 = new javax.swing.JTabbedPane();
                jScrollPane2 = new javax.swing.JScrollPane();
                jTable1 = new javax.swing.JTable();
                jScrollPane3 = new javax.swing.JScrollPane();
                jTable2 = new javax.swing.JTable();
                jComboBox1 = new javax.swing.JComboBox();
                jScrollPane1 = new javax.swing.JScrollPane();
                jList1 = new javax.swing.JList();

                jTable1.setModel(new javax.swing.table.DefaultTableModel(
                        new Object [][] {

                        },
                        new String [] {
                                "Type", "Name"
                        }
                ) {
                        Class[] types = new Class [] {
                                javax.activation.MimeType.class, java.lang.String.class
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

                jTabbedPane1.addTab("Types", jScrollPane2);

                jTable2.setModel(new javax.swing.table.DefaultTableModel(
                        new Object [][] {

                        },
                        new String [] {
                                "Scheme", "Name"
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
                jScrollPane3.setViewportView(jTable2);
                if (jTable2.getColumnModel().getColumnCount() > 0) {
                        jTable2.getColumnModel().getColumn(1).setCellEditor(getEditor());
                }

                jTabbedPane1.addTab("Schemes", jScrollPane3);

                javax.swing.GroupLayout optionPanel1Layout = new javax.swing.GroupLayout(optionPanel1);
                optionPanel1.setLayout(optionPanel1Layout);
                optionPanel1Layout.setHorizontalGroup(
                        optionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                );
                optionPanel1Layout.setVerticalGroup(
                        optionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
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
                setOptionPanel(optionPanel1);

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
        private javax.swing.JScrollPane jScrollPane3;
        private javax.swing.JTabbedPane jTabbedPane1;
        private javax.swing.JTable jTable1;
        private javax.swing.JTable jTable2;
        private linoleum.application.OptionPanel optionPanel1;
        // End of variables declaration//GEN-END:variables
}
