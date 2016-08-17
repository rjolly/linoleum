package linoleum;

import java.awt.Component;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.prefs.Preferences;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import linoleum.application.FileChooser;
import linoleum.application.Frame;
import linoleum.application.OptionPanel;

public class FileManager extends Frame {
	private final Icon fileIcon = new ImageIcon(getClass().getResource("/javax/swing/plaf/metal/icons/ocean/file.gif"));
	private final Icon directoryIcon = new ImageIcon(getClass().getResource("/javax/swing/plaf/metal/icons/ocean/directory.gif"));
	private final Preferences prefs = Preferences.userNodeForPackage(getClass());
	private final FileChooser chooser = new FileChooser();
	private final DefaultListModel<Path> model = new DefaultListModel<>();
	private final ListCellRenderer renderer = new Renderer();
	private final Map<String, ?> env = new HashMap<>();
	private final Thread thread = new Thread() {
		@Override
		public void run() {
			try (final WatchService service = fs.newWatchService()) {
				WatchKey key = register(service);
				for (;;) {
					try {
						key = service.take();
						for (final WatchEvent<?> event : key.pollEvents()) {
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									rescan();
								}
							});
						}
						key.reset();
					} catch (final InterruptedException e) {
						if (closing) {
							service.close();
							break;
						} else {
							key.cancel();
							key = register(service);
						}
					}
				}
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}

		private WatchKey register(final WatchService service) throws IOException {
			final Path name = path.getFileName();
			if (name != null) {
				setTitle(name.toString());
			}
			return path.register(service, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.OVERFLOW);
		}
	};
	private final FileSystem defaultfs = FileSystems.getDefault();
	private final Map<FileSystem, Collection<Integer>> openFrames = new HashMap<>();
	protected FileManager parent;
	private boolean closing;
	private FileSystem fs;
	private Path path;

	private class Renderer extends JLabel implements ListCellRenderer {
		public Renderer() {
			setOpaque(true);
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
			final Path path = (Path)value;
			setIcon(Files.isDirectory(path)?directoryIcon:fileIcon);
			setText(path.getFileName().toString());
			setFont(list.getFont());
			return this;
		}
	};

	public FileManager() {
		this(null);
	}

	public FileManager(final Frame parent) {
		super(parent);
		initComponents();
		setIcon(new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Open24.gif")));
		setMimeType("application/octet-stream:application/java-archive:application/zip");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		this.parent = (FileManager) parent;
	}

	@Override
	public Frame getFrame(final Frame parent) {
		return new FileManager(parent);
	}

	@Override
	public OptionPanel getOptionPanel() {
		return optionPanel1;
	}

	@Override
	public void load() {
		jTextField1.setText(prefs.get(getName() + ".home", ""));
	}

	@Override
	public void save() {
		prefs.put(getName() + ".home", jTextField1.getText());
	}

	public void setURI(final URI uri) {
		try {
			path = Paths.get(uri).toRealPath();
		} catch (final IOException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public URI getURI() {
		return path == null?null:path.toUri();
	}

	@Override
	public void open() {
		if (path == null) {
			setURI(getHome());
		}
		fs = path.getFileSystem();
		if(Files.isRegularFile(path) && isJar()) {
			final URI uri = path.toUri();
			try {
				fs = FileSystems.newFileSystem(new URI("jar", uri.toString(), null), env);
			} catch (final URISyntaxException | IOException ex) {
				ex.printStackTrace();
			}
		}
		rescan();
		if (fs != defaultfs && parent != null) {
			Collection<Integer> coll = parent.openFrames.get(fs);
			if (coll == null) {
				parent.openFrames.put(fs, coll = new HashSet<Integer>());
			}
			coll.add(index);
		}
		if (fs == defaultfs && Files.isDirectory(path)) {
			thread.start();
		} else {
			setTitle(getFileName().toString());
		}
	}

	@Override
	public void close() {
		closing = true;
		if (fs == defaultfs && Files.isDirectory(path)) {
			thread.interrupt();
		}
		if (fs != defaultfs && parent != null) {
			Collection<Integer> coll = parent.openFrames.get(fs);
			coll.remove(index);
			if (coll.isEmpty()) {
				parent.openFrames.remove(fs);
				try {
					fs.close();
				} catch (final IOException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	private void rescan() {
		model.clear();
		Path files[] = new Path[0];
		if (Files.isDirectory(path) || isJar()) {
			files = listFiles(Files.isDirectory(path)?path:getRootDirectory()).toArray(files);
		}
		Arrays.sort(files, new Comparator<Path>() {
			public int compare(final Path a, final Path b) {
				boolean ac = Files.isDirectory(a);
				boolean bc = Files.isDirectory(b);
				return ac == bc?a.compareTo(b):ac?-1:1;
			}
		});
		for (final Path entry : files) {
			model.addElement(entry);
		}
	}
 
	private boolean isJar() {
		if (path.getFileSystem() == defaultfs) try {
			final MimeType t = new MimeType(Files.probeContentType(path));
			return t.match("application/java-archive") || t.match("application/zip");
		} catch (final IOException | MimeTypeParseException ex) {
			ex.printStackTrace();
		}
		return false;
	}

	private Path getRootDirectory() {
		for (final Path entry : fs.getRootDirectories()) {
			return entry;
		}
		return null;
	}

	private final Path getFileName() {
		final int n = path.getNameCount();
		return n > 0?path.getName(n - 1):path;
	}

	@Override
	public boolean canOpen(final Path entry) {
		try {
			if (!Files.isHidden(entry)) {
				return true;
			}
		} catch (final IOException ex) {
			ex.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean reuseFor(final URI that) {
		return getURI().equals(that == null?getHome():that);
	}

	private URI getHome() {
		return Paths.get(prefs.get(getName() + ".home", System.getProperty("user.home"))).toUri();
	}

	private void open(final int index) {
		if (index < 0) {
		} else {
			final Path entry = model.getElementAt(index);
			try {
				getApplicationManager().open(entry.toRealPath().toUri());
			} catch (final IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                optionPanel1 = new linoleum.application.OptionPanel();
                jLabel2 = new javax.swing.JLabel();
                jTextField1 = new javax.swing.JTextField();
                jButton1 = new javax.swing.JButton();
                jScrollPane1 = new javax.swing.JScrollPane();
                jList1 = new javax.swing.JList();

                optionPanel1.setFrame(this);

                jLabel2.setText("Home :");

                jButton1.setText("Choose...");
                jButton1.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jButton1ActionPerformed(evt);
                        }
                });

                javax.swing.GroupLayout optionPanel1Layout = new javax.swing.GroupLayout(optionPanel1);
                optionPanel1.setLayout(optionPanel1Layout);
                optionPanel1Layout.setHorizontalGroup(
                        optionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(optionPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(optionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(optionPanel1Layout.createSequentialGroup()
                                                .addComponent(jLabel2)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jTextField1))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, optionPanel1Layout.createSequentialGroup()
                                                .addGap(0, 303, Short.MAX_VALUE)
                                                .addComponent(jButton1)))
                                .addContainerGap())
                );
                optionPanel1Layout.setVerticalGroup(
                        optionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(optionPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(optionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel2)
                                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton1)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );

                setClosable(true);
                setIconifiable(true);
                setMaximizable(true);
                setResizable(true);
                setName("Files"); // NOI18N

                jList1.setModel(model);
                jList1.setCellRenderer(renderer);
                jList1.setLayoutOrientation(javax.swing.JList.HORIZONTAL_WRAP);
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

        private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
		final int returnVal = chooser.showInternalOpenDialog(optionPanel1);
		switch (returnVal) {
		case JFileChooser.APPROVE_OPTION:
			jTextField1.setText(chooser.getSelectedFile().getPath());
			break;
		default:
		}
        }//GEN-LAST:event_jButton1ActionPerformed

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JButton jButton1;
        private javax.swing.JLabel jLabel2;
        private javax.swing.JList jList1;
        private javax.swing.JScrollPane jScrollPane1;
        private javax.swing.JTextField jTextField1;
        private linoleum.application.OptionPanel optionPanel1;
        // End of variables declaration//GEN-END:variables
}
