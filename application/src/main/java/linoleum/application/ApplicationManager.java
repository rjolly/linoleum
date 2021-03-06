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
import java.awt.event.ActionEvent;
import java.beans.ConstructorProperties;
import java.beans.PropertyVetoException;
import java.net.URI;
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
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import linoleum.application.event.ClassPathListener;
import linoleum.application.event.ClassPathChangeEvent;

public class ApplicationManager extends FileSupport {
	private final Icon defaultIcon = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/development/Application24.gif"));
	private final List<ClassPathListener> listeners = new ArrayList<>();
	private final Map<String, App> appsByName = new HashMap<>();
	private final Map<Class<? extends App>, App> appsByClass = new HashMap<>();
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
	private final SortedMap<MimeType, App> pref = new TreeMap<>(comparator);
	private final SortedMap<MimeType, List<App>> appsByType = new TreeMap<>(comparator);
	private final SortedMap<String, App> prefByScheme = new TreeMap<>();
	private final SortedMap<String, List<App>> appsByScheme = new TreeMap<>();
	private final DefaultListModel<App> model = new DefaultListModel<>();
	private final List<OptionPanel> options = new ArrayList<>();
	private final Logger logger = Logger.getLogger(getClass().getName());
	private final ListCellRenderer renderer = new Renderer();
	private final DefaultComboBoxModel<App> comboModel = new DefaultComboBoxModel<>();
	private final DefaultTableModel tableModel;
	private final DefaultTableModel schemeTableModel;
	private final Packages instance = new Packages();
	private final List<App> apps;

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
			final App app = (App) value;
			final Icon icon = app.getIcon();
			setIcon(icon == null?defaultIcon:icon);
			setText(app.getName());
			setToolTipText(app.getDescription());
			setFont(list.getFont());
			return this;
		}
	}

	public ApplicationManager() {
		this(new ArrayList<App>());
	}

	@ConstructorProperties({"applications"})
	public ApplicationManager(final List<App> apps) {
		initComponents();
		tableModel = (DefaultTableModel) jTable1.getModel();
		schemeTableModel = (DefaultTableModel) jTable2.getModel();
		final List<App> s = new ArrayList<>(apps);
		apps.clear();
		this.apps = apps;
		for (final App app : s) {
			process(app);
		}
	}

	public List<App> getApplications() {
		return apps;
	}

	private MimeType getMimeType(final String str) {
		try {
			return new MimeType(str);
		} catch (final MimeTypeParseException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	private Map<String, App> getMapPref(final String key) {
		final Map<String, App> pref = new TreeMap<>();
		for (final Map.Entry<String, String> entry : getMapProperty(key).entrySet()) {
			pref.put(entry.getKey(), get(entry.getValue()));
		}
		final String str = getPref(key);
		for (final String entry : str.split(", ")) {
			final String s[] = entry.split("=");
			if (s.length > 1) {
				pref.put(s[0], get(s[1]));
			}
		}
		return pref;
	}

	private void putMapPref(final String key, final Map<String, App> pref) {
		for (final Map.Entry<String, String> entry : getMapProperty(key).entrySet()) {
			if (pref.containsKey(entry.getKey()) && entry.getValue().equals(pref.get(entry.getKey()).toString())) {
				pref.remove(entry.getKey());
			}
		}
		final String str = pref.toString();
		putPref(key, str.substring(1, str.length() - 1));
	}

	@Override
	public void init() {
		getApplicationManager().addClassPathListener(new ClassPathListener() {
			@Override
			public void classPathChanged(final ClassPathChangeEvent e) {
				open();
			}
		});
		Preferences.userNodeForPackage(getClass()).addPreferenceChangeListener(this);
	}

	@Override
	public void preferenceChange(final PreferenceChangeEvent evt) {
		if (evt.getKey().equals(getKey("preferred"))) {
			pref.clear();
			for (final Map.Entry<String, App> entry : getMapPref("preferred").entrySet()) {
				pref.put(getMimeType(entry.getKey()), entry.getValue());
			}
		} else if (evt.getKey().equals(getKey("scheme-preferred"))) {
			prefByScheme.clear();
			prefByScheme.putAll(getMapPref("scheme-preferred"));
		}
	}

	private void manage(final PreferenceSupport frame) {
		final OptionPanel panel = frame.getOptionPanel();
		if (panel != null) {
			panel.setFrame(frame);
			panel.setName(frame.getName());
			options.add(panel);
		}
	}

	private void manage(final Frame frame) {
		frame.setApplicationManager(this);
		frame.init();
	}

	public void manage(final App app) {
		if (app instanceof PreferenceSupport) {
			manage((PreferenceSupport) app);
		}
		if (app instanceof Frame) {
			manage((Frame) app);
		}
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
				final List<App> apps = new ArrayList<>();
				if (table == jTable1) {
					final MimeType type = (MimeType) tableModel.getValueAt(row, 0);
					apps.addAll(wildcard(type)?appsByType.get(type):getApplications(type));
				} else if (table == jTable2) {
					final String scheme = (String) schemeTableModel.getValueAt(row, 0);
					apps.addAll(appsByScheme.get(scheme));
				}
				for (final App app : apps) {
					comboModel.addElement(app);
				}
				return super.getTableCellEditorComponent(table, value, isSelected, row, column);
			}
		};
	}

	private boolean wildcard(final MimeType type) {
		return type.getSubType().equals("*");
	}

	@Override
	public void load() {
		tableModel.setRowCount(0);
		final Map<String, App> pref = getMapPref("preferred");
		for (final MimeType type : appsByType.keySet()) {
			tableModel.addRow(new Object[] {type, pref.get(type.toString())});
		}
		schemeTableModel.setRowCount(0);
		final Map<String, App> prefByScheme = getMapPref("scheme-preferred");
		for (final String scheme : appsByScheme.keySet()) {
			schemeTableModel.addRow(new Object[] {scheme, prefByScheme.get(scheme)});
		}
	}

	@Override
	public void save() {
		final Map<String, App> pref = new TreeMap<>();
		for (int row = 0 ; row < tableModel.getRowCount() ; row++) {
			final MimeType type = (MimeType) tableModel.getValueAt(row, 0);
			final App app = (App) tableModel.getValueAt(row, 1);
			if (app != null) {
				pref.put(type.toString(), app);
			}
		}
		putMapPref("preferred", pref);
		final Map<String, App> prefByScheme = new TreeMap<>();
		for (int row = 0 ; row < schemeTableModel.getRowCount() ; row++) {
			final String scheme = (String) schemeTableModel.getValueAt(row, 0);
			final App app = (App) schemeTableModel.getValueAt(row, 1);
			if (app != null) {
				prefByScheme.put(scheme, app);
			}
		}
		putMapPref("scheme-preferred", prefByScheme);
	}

	public void open(final URI uri) {
		open(uri, getDesktopPane());
	}

	@Override
	public void open(final URI uri, final JDesktopPane desktop) {
		if (uri == null) {
			super.open(uri, desktop);
		} else {
			final MimeType type = getMimeType(uri);
			final App app = type == null?getApplication(uri.getScheme()):getApplication(type);
			if (app != null) {
				app.open(uri, desktop);
			}
		}
	}

	private App getApplication(final MimeType type) {
		final App app = getPreferredApplication(type);
		if (app == null) {
			final List<App> apps = getApplications(type);
			if (apps.size() > 0) {
				return apps.get(0);
			}
		}
		return app;
	}

	private App getApplication(final String scheme) {
		final App app = getPreferredApplication(scheme);
		if (app == null) {
			final List<App> apps = getApplications(scheme);
			if (apps.size() > 0) {
				return apps.get(0);
			}
		}
		return app;
	}

	public boolean populate(final URI uri, JPopupMenu... menus) {
		final String scheme = uri.getScheme();
		final MimeType type = getMimeType(uri);
		final App a = type == null?getPreferredApplication(scheme):getPreferredApplication(type);
		boolean sep = false;
		if (a != null) {
			final Action action = new AbstractAction(a.getName(), a.getFrameIcon()) {
				@Override
				public void actionPerformed(final ActionEvent evt) {
					a.open(uri, getDesktopPane());
				}
			};
			for (final JPopupMenu menu : menus) {
				menu.add(action);
			}
			sep = true;
		}
		boolean sep0 = sep;
		for (final App app : type == null?getApplications(scheme):getApplications(type)) {
			if (!app.equals(a)) {
				if (sep) {
					for (final JPopupMenu menu : menus) {
						menu.addSeparator();
					}
					sep = false;
				}
				final Action action = new AbstractAction(app.getName(), app.getFrameIcon()) {
					@Override
					public void actionPerformed(final ActionEvent evt) {
						app.open(uri, getDesktopPane());
					}
				};
				for (final JPopupMenu menu : menus) {
					menu.add(action);
				}
				sep0 = true;
			}
		}
		return sep0;
	}

	private App getPreferredApplication(final MimeType type) {
		if (pref.containsKey(type)) {
			return pref.get(type);
		}
		for (final Map.Entry<MimeType, App> entry : pref.entrySet()) {
			if (type.match(entry.getKey())) {
				return entry.getValue();
			}
		}
		return null;
	}

	private App getPreferredApplication(final String scheme) {
		if (prefByScheme.containsKey(scheme)) {
			return prefByScheme.get(scheme);
		}
		return null;
	}

	private List<App> getApplications(final MimeType type) {
		final List<App> apps = new ArrayList<>();
		if (appsByType.containsKey(type)) {
			apps.addAll(appsByType.get(type));
		}
		for (final Map.Entry<MimeType, List<App>> entry : appsByType.entrySet()) {
			if (type.match(entry.getKey())) {
				final List<App> s = new ArrayList<>(entry.getValue());
				s.removeAll(apps);
				apps.addAll(s);
			}
		}
		return Collections.unmodifiableList(apps);
	}

	private List<App> getApplications(final String scheme) {
		final List<App> apps = new ArrayList<>();
		if (appsByScheme.containsKey(scheme)) {
			apps.addAll(appsByScheme.get(scheme));
		}
		return Collections.unmodifiableList(apps);
	}

	public App get(final String name) {
		return appsByName.get(name);
	}

	@SuppressWarnings("unchecked")
	public <A extends App> A get(final Class<A> cls) {
		return (A) appsByClass.get(cls);
	}

	private void open(final int index) {
		if (index < 0) {
		} else {
			model.getElementAt(index).open(getDesktopPane());
		}
	}

	public boolean addToClassPath(final Path path) {
		return instance.add(path.toFile());
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
		(new SwingWorker<Boolean, Object>() {
			@Override
			public Boolean doInBackground() {
				doOpen();
				return true;
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
				initPref();
				for (final Map.Entry<String, App> entry : getMapPref("preferred").entrySet()) {
					pref.put(getMimeType(entry.getKey()), entry.getValue());
				}
				prefByScheme.putAll(getMapPref("scheme-preferred"));
			}
		}).execute();
	}

	@SuppressWarnings("deprecation")
	public void doOpen() {
		for (final JInternalFrame frame : ServiceLoader.load(JInternalFrame.class)) {
			process(frame instanceof Frame?(Frame) frame:new InternalFrameWrapper(frame));
		}
		for (final Application app : ServiceLoader.load(Application.class)) {
			process(new ApplicationWrapper(app));
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

	private void process(final App app) {
		final String name = app.getName();
		if (!appsByName.containsKey(name)) {
			logger.config("Processing " + name);
			apps.add(app);
			appsByName.put(name, app);
			appsByClass.put(app.getClass(), app);
			final String str = app.getMimeType();
			if (str != null) {
				for (final String s : str.split(":")) {
					final MimeType type = getMimeType(s);
					List<App> apps = appsByType.get(type);
					if (apps == null) {
						appsByType.put(type, apps = new ArrayList<>());
					}
					apps.add(app);
				}
			}
			final String ss = app.getScheme();
			if (ss != null) {
				for (final String scheme : ss.split(":")) {
					List<App> apps = appsByScheme.get(scheme);
					if (apps == null) {
						appsByScheme.put(scheme, apps = new ArrayList<>());
					}
					apps.add(app);
				}
			}
			model.addElement(app);
			manage(app);
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
                                "Type", "Application"
                        }
                ) {
                        Class[] types = new Class [] {
                                javax.activation.MimeType.class, linoleum.application.App.class
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
                                "Scheme", "Application"
                        }
                ) {
                        Class[] types = new Class [] {
                                java.lang.String.class, linoleum.application.App.class
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
