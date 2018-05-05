package linoleum.pkg;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.util.Arrays;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.swing.ImageIcon;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import linoleum.application.ApplicationManager;
import linoleum.application.Frame;
import linoleum.application.Package;
import linoleum.application.event.ClassPathListener;
import linoleum.application.event.ClassPathChangeEvent;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.publish.PublishOptions;
import org.apache.ivy.core.retrieve.RetrieveOptions;
import org.apache.ivy.core.retrieve.RetrieveReport;
import org.apache.ivy.plugins.parser.m2.PomModuleDescriptorWriter;
import org.apache.ivy.plugins.parser.m2.PomWriterOptions;

public class PackageManager extends Frame {
	private final Ivy ivy = Ivy.newInstance();
	private final DefaultTableModel model;

	public PackageManager() {
		super("Packages");
		initComponents();
		setScheme("mvn");
		setIcon(new ImageIcon(getClass().getResource("/toolbarButtonGraphics/development/Jar24.gif")));
		model = (DefaultTableModel) jTable1.getModel();
		final File settings = new File("ivysettings.xml");
		try {
			if (settings.exists()) {
				ivy.configure(settings);
			} else {
				ivy.configureDefault();
			}
		} catch (final ParseException | IOException ex) {
			ex.printStackTrace();
		}
	}

	private Package[] installed() {
		final SortedMap<String, Package> map = new TreeMap<>();
		for (final String str : System.getProperty("java.class.path").split(File.pathSeparator)) {
			final Package pkg = new Package(new File(str));
			map.put(pkg.getName(), pkg);
		}
		return map.values().toArray(new Package[0]);
	}

	@Override
	public void open() {
		model.setRowCount(0);
		for (final Package pkg : installed()) {
			model.addRow(new Object[] {pkg.getName(), pkg.getVersion(), pkg.isSnapshot()});
		}
		final URI uri = getURI();
		if (uri != null) {
			final String str = uri.getSchemeSpecificPart();
			if (str != null) {
				final String s[] = str.split("/");
				if (s.length > 0) {
					jTextField1.setText(s[0]);
				}
				if (s.length > 1) {
					jTextField2.setText(s[1]);
				}
				if (s.length > 2) {
					jTextField3.setText(s[2]);
				}
			}
		}
	}

	@Override
	public void close() {
		clear();
	}

	private void clear() {
		setURI(null);
		jTextField1.setText("");
		jTextField2.setText("");
		jTextField3.setText("");
	}

	public void makepom(final File source, final File pom) throws Exception {
		final ResolveReport resolveReport = ivy.resolve(source);
		final ModuleDescriptor md = resolveReport.getModuleDescriptor();
		final PomWriterOptions options = new PomWriterOptions();
		options.setPrintIvyInfo(false);
		PomModuleDescriptorWriter.write(md, pom, options);
	}

	public void publish(final File source, final File dir, final String resolver) throws Exception {
		final ResolveReport resolveReport = ivy.resolve(source);
		final ModuleDescriptor md = resolveReport.getModuleDescriptor();
		final ModuleRevisionId mRID = md.getModuleRevisionId();
		final String pattern[] = new String[] { new File(dir, "[artifact]-[type].[ext]").getPath(), new File(dir, "[artifact].[ext]").getPath() };
		final PublishOptions options = new PublishOptions();
		options.setOverwrite(true);
		if (resolver.startsWith("local")) {
			options.setSrcIvyPattern(new File(source.getParentFile(), "[artifact].[ext]").getPath());
		}
		ivy.publish(mRID, Arrays.asList(pattern), resolver, options);
	}

	public void install(final String name, final String conf) {
		install(name, conf, new File("lib"));
	}

	public void install(final String name, final String conf, final File dir) {
		final ApplicationManager apps = getApplicationManager();
		final ModuleRevisionId mRID = ModuleRevisionId.parse(name);
		final ResolveOptions resolveOptions = new ResolveOptions();
		resolveOptions.setConfs(new String[] { conf });
		try {
			final ResolveReport resolveReport = ivy.resolve(mRID, resolveOptions, true);
			final ModuleDescriptor md = resolveReport.getModuleDescriptor();
			final RetrieveOptions retrieveOptions = new RetrieveOptions();
			retrieveOptions.setDestArtifactPattern(dir.getPath() + "/[artifact]-[revision](-[classifier]).[ext]");
			final RetrieveReport retrieveReport = ivy.retrieve(md.getModuleRevisionId(), retrieveOptions);
			boolean changed = false;
			for (final Object obj : retrieveReport.getCopiedFiles()) {
				final File file = (File) obj;
				if (file.getName().endsWith(".jar")) {
					changed |= apps.addToClassPath(file.toPath());
				}
			}
			if(changed) {
				apps.fireClassPathChange(new ClassPathChangeEvent(this));
			}
		} catch (final ParseException | IOException ex) {
			ex.printStackTrace();
		}
	}

	private void install(final String organization, final String module, final String revision) {
		install(organization + "#" + module + ";" + revision, "default");
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

	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                jLabel1 = new javax.swing.JLabel();
                jTextField1 = new javax.swing.JTextField();
                jLabel2 = new javax.swing.JLabel();
                jTextField2 = new javax.swing.JTextField();
                jLabel3 = new javax.swing.JLabel();
                jTextField3 = new javax.swing.JTextField();
                jButton1 = new javax.swing.JButton();
                jSeparator1 = new javax.swing.JSeparator();
                jScrollPane1 = new javax.swing.JScrollPane();
                jTable1 = new javax.swing.JTable();
                jButton2 = new javax.swing.JButton();

                setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/development/Jar16.gif"))); // NOI18N
                setName("Packages"); // NOI18N

                jLabel1.setText("Organization :");

                jLabel2.setText("Module :");

                jLabel3.setText("Revision :");

                jButton1.setText("Install");
                jButton1.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jButton1ActionPerformed(evt);
                        }
                });

                jTable1.setModel(new javax.swing.table.DefaultTableModel(
                        new Object [][] {

                        },
                        new String [] {
                                "Name", "Version", "Snapshot"
                        }
                ) {
                        Class[] types = new Class [] {
                                java.lang.String.class, java.lang.String.class, java.lang.Boolean.class
                        };
                        boolean[] canEdit = new boolean [] {
                                false, false, false
                        };

                        public Class getColumnClass(int columnIndex) {
                                return types [columnIndex];
                        }

                        public boolean isCellEditable(int rowIndex, int columnIndex) {
                                return canEdit [columnIndex];
                        }
                });
                jTable1.getTableHeader().setReorderingAllowed(false);
                jScrollPane1.setViewportView(jTable1);

                jButton2.setText("Clear");
                jButton2.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jButton2ActionPerformed(evt);
                        }
                });

                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
                getContentPane().setLayout(layout);
                layout.setHorizontalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jSeparator1)
                                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jTextField1)
                                        .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jTextField2)
                                        .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jTextField3)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addGap(0, 0, Short.MAX_VALUE)
                                                .addComponent(jButton2)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jButton1))
                                        .addComponent(jScrollPane1))
                                .addContainerGap())
                );
                layout.setVerticalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jButton1)
                                        .addComponent(jButton2))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE)
                                .addContainerGap())
                );

                pack();
        }// </editor-fold>//GEN-END:initComponents

        private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
		jButton1.setEnabled(false);
		(new SwingWorker<Object, Object>() {
			@Override
			public Object doInBackground() {
				install(jTextField1.getText(), jTextField2.getText(), jTextField3.getText());
				return null;
			}

			@Override
			protected void done() {
				jButton1.setEnabled(true);
			}
		}).execute();
        }//GEN-LAST:event_jButton1ActionPerformed

        private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
		clear();
        }//GEN-LAST:event_jButton2ActionPerformed

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JButton jButton1;
        private javax.swing.JButton jButton2;
        private javax.swing.JLabel jLabel1;
        private javax.swing.JLabel jLabel2;
        private javax.swing.JLabel jLabel3;
        private javax.swing.JScrollPane jScrollPane1;
        private javax.swing.JSeparator jSeparator1;
        private javax.swing.JTable jTable1;
        private javax.swing.JTextField jTextField1;
        private javax.swing.JTextField jTextField2;
        private javax.swing.JTextField jTextField3;
        // End of variables declaration//GEN-END:variables
}
