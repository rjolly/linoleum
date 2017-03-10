package linoleum.pkg;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import javax.swing.ImageIcon;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import linoleum.application.Frame;
import linoleum.application.event.ClassPathListener;
import linoleum.application.event.ClassPathChangeEvent;
import linoleum.Desktop;
import linoleum.Package;
import linoleum.Packages;
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
	private final Packages pkgs = Desktop.instance.getPackages();
	private final Ivy ivy = Ivy.newInstance();
	private final File lib = new File("lib");
	private final DefaultTableModel model;
	public static PackageManager instance;

	public PackageManager() {
		super("Packages");
		initComponents();
		setIcon(new ImageIcon(getClass().getResource("/toolbarButtonGraphics/development/Jar24.gif")));
		model = (DefaultTableModel) jTable1.getModel();
		if (instance == null) {
			instance = this;
		}
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

	@Override
	public void open() {
		model.setRowCount(0);
		for (final File file : pkgs.installed()) {
			final Package pkg = new Package(file);
			model.addRow(new Object[] {pkg.getName(), pkg.getVersion(), pkg.isSnapshot()});
		}
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
		final String pattern[] = new String[] { dir.getPath() + "/[artifact]-[type].[ext]", dir.getPath() + "/[artifact].[ext]" };
		final PublishOptions options = new PublishOptions();
		options.setOverwrite(true);
		if (resolver.startsWith("local")) {
			options.setSrcIvyPattern(source.getParent() + "/[artifact].[ext]");
		}
		ivy.publish(mRID, Arrays.asList(pattern), resolver, options);
	}

	public void install(final String name, final String conf) {
		install(name, conf, lib);
	}

	public void install(final String name, final String conf, final File dir) {
		final ModuleRevisionId mRID = ModuleRevisionId.parse(name);
		final ResolveOptions resolveOptions = new ResolveOptions();
		resolveOptions.setConfs(new String[] { conf });
		try {
			final ResolveReport resolveReport = ivy.resolve(mRID, resolveOptions, true);
			final ModuleDescriptor md = resolveReport.getModuleDescriptor();
			final RetrieveOptions retrieveOptions = new RetrieveOptions();
			retrieveOptions.setDestArtifactPattern(dir.getPath() + "/[artifact]-[revision](-[classifier]).[ext]");
			final RetrieveReport retrieveReport = ivy.retrieve(md.getModuleRevisionId(), retrieveOptions);
			for (final Object obj : retrieveReport.getCopiedFiles()) {
				final File file = (File)obj;
				if (file.getName().endsWith(".jar")) {
					pkgs.add(file);
				}
			}
			pkgs.commit(this);
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

                setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/development/Jar16.gif"))); // NOI18N
                setName("Packages");

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
                                .addComponent(jButton1)
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

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JButton jButton1;
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
