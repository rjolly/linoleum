package linoleum;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Rectangle;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import linoleum.application.ApplicationManager;
import linoleum.application.FileChooser;
import linoleum.application.Frame;

public class FileManager extends Frame {
	private final Preferences prefs = Preferences.userNodeForPackage(getClass());
	private final Icon openIcon = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Open16.gif"));
	private final Icon cutIcon = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Cut16.gif"));
	private final Icon copyIcon = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Copy16.gif"));
	private final Icon pasteIcon = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Paste16.gif"));
	private final Icon deleteIcon = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Delete16.gif"));
	private final Icon newFolderIcon = new ImageIcon(getClass().getResource("/javax/swing/plaf/metal/icons/ocean/newFolder.gif"));
	private final Action openAction = new OpenAction();
	private final Action openLocationAction = new OpenLocationAction();
	private final Action closeAction = new CloseAction();
	private final Action cutAction = new CutAction();
	private final Action copyAction = new CopyAction();
	private final Action pasteAction = new PasteAction();
	private final Action pasteAsLinkAction = new PasteAsLinkAction();
	private final Action newFolderAction = new NewFolderAction();
	private final Action renameAction = new RenameAction();
	private final Action deleteAction = new DeleteAction();
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
									process(event);
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

		private void process(final WatchEvent<?> event) {
			final Path entry = getPath().resolve((Path) event.context());
			final WatchEvent.Kind kind = event.kind();
			if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
				model.addElement(entry);
			} else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
				model.removeElement(entry);
			}
		}

		private WatchKey register(final WatchService service) throws IOException {
			setTitle(getFileName());
			return path.register(service, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.OVERFLOW);
		}
	};
	private final FileSystem defaultfs = FileSystems.getDefault();
	private final Map<FileSystem, Collection<Integer>> openFrames = new HashMap<>();
	protected FileManager parent;
	private FileManager source;
	private boolean closing;
	private FileSystem fs;
	private boolean show;
	private int action;
	private Path path;
	private int idx;

	private class OpenAction extends AbstractAction {
		public OpenAction() {
			super("Open");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			open(jList1.getSelectedValue());
			
		}
	}

	private class OpenLocationAction extends AbstractAction {
		public OpenLocationAction() {
			super("Open location...", openIcon);
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final int returnVal = parent.chooser.showInternalOpenDialog(FileManager.this);
			switch (returnVal) {
			case JFileChooser.APPROVE_OPTION:
				open(parent.chooser.getSelectedFile().toPath());
				break;
			default:
			}
		}
	}

	private class CloseAction extends AbstractAction {
		public CloseAction() {
			super("Close");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_MASK));
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			doDefaultCloseAction();
			
		}
	}

	private ActionEvent createActionEvent() {
		return new ActionEvent(jList1, ActionEvent.ACTION_PERFORMED, null);
	}

	private class CutAction extends AbstractAction {
		public CutAction() {
			super("Cut", cutIcon);
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK));
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			TransferHandler.getCutAction().actionPerformed(createActionEvent());
		}
	}

	private class CopyAction extends AbstractAction {
		public CopyAction() {
			super("Copy", copyIcon);
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			TransferHandler.getCopyAction().actionPerformed(createActionEvent());
		}
	}

	private class PasteAction extends AbstractAction {
		public PasteAction() {
			super("Paste", pasteIcon);
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK));
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			TransferHandler.getPasteAction().actionPerformed(createActionEvent());
		}
	}

	private class PasteAsLinkAction extends AbstractAction {
		public PasteAsLinkAction() {
			super("Paste as link");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK));
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			if (parent.source != null) {
				parent.source.action = TransferHandler.LINK;
			}
			TransferHandler.getPasteAction().actionPerformed(createActionEvent());
		}
	}

	private class NewFolderAction extends AbstractAction {
		public NewFolderAction() {
			super("New folder", newFolderIcon);
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			newFolder(getPath().resolve("New folder"));
		}
	}

	private class RenameAction extends AbstractAction {
		public RenameAction() {
			super("Rename");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK));
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			editFileName();
		}
	}

	private class DeleteAction extends AbstractAction {
		public DeleteAction() {
			super("Delete", deleteIcon);
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final int option = JOptionPane.showInternalConfirmDialog(FileManager.this, "Delete ?", "Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
			switch (option) {
			case JOptionPane.OK_OPTION:
				for (final Path entry : jList1.getSelectedValuesList()) {
					try {
						if (Files.isDirectory(entry)) {
							delete(entry);
						} else {
							Files.delete(entry);
						}
					} catch (final IOException ex) {
						ex.printStackTrace();
					}
				}
				refresh();
				break;
			default:
			}
		}
	}

	private Path newFolderFile;
	private Path editFile;

	private final JTextField editCell = new JTextField();

	private void applyEdit() {
		if (editFile != null && Files.exists(editFile)) {
			final String oldFileName = jList1.getFileName(editFile).toString();
			final String newFileName = editCell.getText().trim();

			if (!newFileName.equals(oldFileName)) try {
				Files.move(editFile, editFile.resolveSibling(newFileName));
				refresh();
			} catch (final IOException ex) {
				ex.printStackTrace();
			}
		}
		cancelEdit();
	}

	private void cancelEdit() {
		if (editFile != null) {
			editFile = null;
			jList1.remove(editCell);
			jList1.repaint();
		}
	}

	private void editFileName() {
		final int index = jList1.getSelectedIndex();
		jList1.ensureIndexIsVisible(index);
		editFile = jList1.getModel().getElementAt(index);
		final Rectangle r = jList1.getCellBounds(index, index);
		jList1.add(editCell);
		editCell.setText(jList1.getFileName(editFile).toString());
		final ComponentOrientation orientation = jList1.getComponentOrientation();
		editCell.setComponentOrientation(orientation);

		final Icon icon = jList1.getFileIcon(editFile);

		// PENDING - grab padding (4) below from defaults table.
		final int editX = icon == null ? 20 : icon.getIconWidth() + 4;

		if (orientation.isLeftToRight()) {
			editCell.setBounds(editX + r.x, r.y, r.width - editX, r.height);
		} else {
			editCell.setBounds(r.x, r.y, r.width - editX, r.height);
		}
		editCell.requestFocus();
		editCell.selectAll();
	}

	private void newFolder(final Path path) {
		try {
			newFolderFile = Files.createDirectory(path);
			refresh();
		} catch (final IOException ex) {
			ex.printStackTrace();
		}
	}

	private void editNewFolder(final Path path) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				jList1.setSelectedValue(path, false);
				editFileName();
			}
		});
		newFolderFile = null;
	}

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
			setIcon(((FileList) list).getFileIcon(path));
			setText(((FileList) list).getFileName(path).toString());
			setFont(list.getFont());
			return this;
		}
	};

	private class Handler extends TransferHandler {
		@Override
		public boolean canImport(final TransferHandler.TransferSupport support) {
			return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
		}

		@Override
		@SuppressWarnings("unchecked")
		public boolean importData(final TransferHandler.TransferSupport support) {
			if (!canImport(support)) {
				return false;
			}
			final List<Path> files;
			final Transferable transferable = support.getTransferable();
			try {
				files = (List<Path>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
			} catch (final UnsupportedFlavorException e) {
				return false;
			} catch (final IOException e) {
				return false;
			}
			final int action;
			final Path recipient;
			final FileList list = (FileList) support.getComponent();
			if (support.isDrop()) {
				action = support.getDropAction();
				final JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();
				recipient = dl.isInsert()?getPath():getDirectory(list.getModel().getElementAt(dl.getIndex()));
			} else {
				if (parent.source != null) {
					action = parent.source.action;
				} else {
					action = NONE;
				}
				recipient = list.isSelectionEmpty()?getPath():getDirectory(list.getSelectedValue());
			}
			for (final Path entry : files) {
				final Path target = recipient.resolve(list.getFileName(entry).toString());
				try {
					switch (action) {
					case COPY:
						if (Files.isDirectory(entry)) {
							copy(entry, target);
						} else {
							Files.copy(entry, target);
						}
						break;
					case MOVE:
						if (Files.isDirectory(entry)) {
							copy(entry, target);
							delete(entry);
						} else {
							Files.move(entry, target);
						}
						break;
					case LINK:
						Files.createSymbolicLink(target, entry.isAbsolute()?entry:recipient.isAbsolute()?entry.toAbsolutePath():recipient.relativize(entry));
						break;
					}
				} catch (final IOException ex) {
					return false;
				}
			}
			refresh();
			if (!support.isDrop()) {
				switch (action) {
				case MOVE:
					parent.source.refresh();
					break;
				}
			}
			return true;
		}

		@Override
		public Transferable createTransferable(final JComponent c) {
			return new FileTransferable(((FileList) c).getSelectedValuesList());
		}

		@Override
		public int getSourceActions(final JComponent c) {
			return MOVE | COPY | LINK;
		}

		@Override
		public void exportToClipboard(final JComponent comp, final Clipboard clip, final int action) throws IllegalStateException {
			if ((action == COPY || action == MOVE) && (getSourceActions(comp) & action) != 0) {
				final Transferable t = createTransferable(comp);
				if (t != null)  try {
					clip.setContents(t, null);
					done(action);
					return;
				} catch (final IllegalStateException ise) {
					done(NONE);
					throw ise;
				}
			}
			done(NONE);
		}

		@Override
		public void exportDone(final JComponent c, final Transferable data, final int action) {
			refresh();
		}
	}

	private void refresh() {
		if (fs != defaultfs) {
			rescan();
		}
	}

	private void done(final int action) {
		this.action = action;
		parent.source = this;
	}

	private Path getDirectory(final Path path) {
		if (Files.isDirectory(path)) try {
			return relativize(path.toRealPath());
		} catch (final IOException ex) {
			ex.printStackTrace();
		}
		return path.getParent();
	}

	private void copy(final Path source, final Path target) throws IOException {
		Files.walkFileTree(source, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
				final Path targetdir = target.resolve(source.relativize(dir).toString());
				try {
					Files.copy(dir, targetdir);
				} catch (final FileAlreadyExistsException e) {
					 if (!Files.isDirectory(targetdir)) {
						 throw e;
					 }
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
				Files.copy(file, target.resolve(source.relativize(file).toString()));
				return FileVisitResult.CONTINUE;
			}
		});
	}

	private void delete(final Path start) throws IOException {
		Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(final Path dir, final IOException e) throws IOException {
				if (e == null) {
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				} else {
					// directory iteration failed
					throw e;
				}
			}
		});
	}

	public FileManager() {
		this(null);
	}

	@SuppressWarnings("deprecation")
	public FileManager(final Frame parent) {
		super(parent);
		initComponents();
		jList1.setTransferHandler(new Handler());
		setIcon(new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Open24.gif")));
		setMimeType("application/x-directory:application/java-archive:application/zip");
		Preferences.userNodeForPackage(ApplicationManager.class).addPreferenceChangeListener(new PreferenceChangeListener() {
			@Override
			public void preferenceChange(final PreferenceChangeEvent evt) {
				if (evt.getKey().equals(getApplicationManager().getKey("preferred"))) {
					prepare();
				}
			}
		});
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		editCell.setName("FileList.cellEditor");
		editCell.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				applyEdit();
			}
		});
		editCell.addFocusListener(new FocusAdapter() {
			public void focusLost(final FocusEvent e) {
				if (!e.isTemporary()) {
					applyEdit();
				}
			}
		});
		editCell.setNextFocusableComponent(jList1);
		model.addListDataListener(new ListDataListener() {
			public void contentsChanged(final ListDataEvent e) {
			}
			public void intervalAdded(final ListDataEvent e) {
				final int i0 = e.getIndex0();
				final int i1 = e.getIndex1();
				if (i0 == i1) {
					final Path path = model.getElementAt(i0);
					if (path.equals(newFolderFile)) {
						editNewFolder(path);
					}
				}
			}
			public void intervalRemoved(final ListDataEvent e) {
			}
		});
		this.parent = (FileManager) super.parent;
		setURI(getHome());
	}

	@Override
	public Frame getFrame(final Frame parent) {
		return new FileManager(parent);
	}

	@Override
	public void init() {
		getApplicationManager().addOptionPanel(optionPanel1);
	}

	@Override
	public void load() {
		jTextField1.setText(prefs.get(getKey("home"), ""));
		jCheckBox1.setSelected(isShowHidden());
	}

	@Override
	public void save() {
		prefs.put(getKey("home"), jTextField1.getText());
		prefs.putBoolean(getKey("showHidden"), jCheckBox1.isSelected());
	}

	@Override
	public void setURI(final URI uri) {
		try {
			path = relativize(Paths.get(uri).toRealPath());
			fs = path.getFileSystem();
		} catch (final IOException ex) {
			ex.printStackTrace();
		}
	}

	private Path relativize(final Path path) throws IOException {
		final FileSystem fs = path.getFileSystem();
		final Path user = Paths.get("").toRealPath();
		return fs == defaultfs && path.startsWith(user)?user.relativize(path):path;
	}

	@Override
	public URI getURI() {
		return path.toUri();
	}

	@Override
	public void open() {
		if(Files.isRegularFile(path) && isJar()) {
			final URI uri = path.toUri();
			try {
				fs = FileSystems.newFileSystem(new URI("jar", uri.toString(), null), env);
			} catch (final URISyntaxException | IOException ex) {
				ex.printStackTrace();
			}
		}
		rescan();
		if (fs != defaultfs) {
			Collection<Integer> coll = parent.openFrames.get(fs);
			if (coll == null) {
				parent.openFrames.put(fs, coll = new HashSet<Integer>());
			}
			coll.add(index);
		}
		if (fs == defaultfs && Files.isDirectory(path)) {
			thread.start();
		} else {
			setTitle(getFileName());
		}
	}

	private String getFileName() {
		return jList1.getFileName(path).toString();
	}

	@Override
	public void close() {
		closing = true;
		if (fs == defaultfs && Files.isDirectory(path)) {
			thread.interrupt();
		}
		if (fs != defaultfs) {
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

	private boolean isShowHidden() {
		return prefs.getBoolean(getKey("showHidden"), false);
	}

	private void rescan() {
		show = jCheckBoxMenuItem1.isSelected();
		model.clear();
		Path files[] = new Path[0];
		if (Files.isDirectory(path) || isJar()) {
			files = listFiles(getPath()).toArray(files);
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
		prepare();
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

	private Path getPath() {
		return Files.isDirectory(path)?path:getRootDirectory();
	}

	private Path getRootDirectory() {
		for (final Path entry : fs.getRootDirectories()) {
			return entry;
		}
		return null;
	}

	@Override
	public boolean canOpen(final Path entry) {
		try {
			if (show || !Files.isHidden(entry)) {
				return true;
			}
		} catch (final IOException ex) {
			ex.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean reuseFor(final URI that) {
		try {
			return Files.isSameFile(path, Paths.get(that == null?getHome():that));
		} catch (final IOException ex) {
			ex.printStackTrace();
		}
		return false;
	}

	private URI getHome() {
		return Paths.get(prefs.get(getKey("home"), "")).toUri();
	}

	private void open(final Path entry) {
		try {
			getApplicationManager().open(entry.toRealPath().toUri());
		} catch (final IOException ex) {
			ex.printStackTrace();
		}
	}

	private void prepare() {
		jMenu3.removeAll();
		jPopupMenu1.removeAll();
		if (jList1.isSelectionEmpty()) {
			openAction.setEnabled(false);
			jMenu3.setEnabled(false);
			renameAction.setEnabled(false);
			deleteAction.setEnabled(false);
		} else {
			openAction.setEnabled(true);
			jMenu3.setEnabled(true);
			renameAction.setEnabled(true);
			deleteAction.setEnabled(true);
			try {
				final URI uri = jList1.getSelectedValue().toRealPath().toUri();
				final ApplicationManager mgr = getApplicationManager();
				final String s = mgr.getApplication(uri);
				boolean sep = false;
				if (s != null) {
					final Action action = new AbstractAction(s) {
						@Override
						public void actionPerformed(final ActionEvent evt) {
							mgr.open(s, uri);
						}
					};
					jMenu3.add(action);
					jPopupMenu1.add(action);
					sep = true;
				}
				for (final String str : mgr.getApplications(uri)) {
					if (!str.equals(s)) {
						if (sep) {
							jMenu3.addSeparator();
							jPopupMenu1.addSeparator();
							sep = false;
						}
						final Action action = new AbstractAction(str) {
							@Override
							public void actionPerformed(final ActionEvent evt) {
								mgr.open(str, uri);
							}
						};
						jMenu3.add(action);
						jPopupMenu1.add(action);
					}
				}
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
                jCheckBox1 = new javax.swing.JCheckBox();
                jPopupMenu1 = new javax.swing.JPopupMenu();
                jScrollPane1 = new javax.swing.JScrollPane();
                jList1 = new linoleum.FileList();
                jMenuBar1 = new javax.swing.JMenuBar();
                jMenu1 = new javax.swing.JMenu();
                jMenuItem1 = new javax.swing.JMenuItem();
                jMenu3 = new javax.swing.JMenu();
                jMenuItem2 = new javax.swing.JMenuItem();
                jSeparator3 = new javax.swing.JPopupMenu.Separator();
                jMenuItem10 = new javax.swing.JMenuItem();
                jMenu2 = new javax.swing.JMenu();
                jMenuItem4 = new javax.swing.JMenuItem();
                jMenuItem5 = new javax.swing.JMenuItem();
                jMenuItem6 = new javax.swing.JMenuItem();
                jMenuItem8 = new javax.swing.JMenuItem();
                jSeparator1 = new javax.swing.JPopupMenu.Separator();
                jMenuItem9 = new javax.swing.JMenuItem();
                jMenuItem3 = new javax.swing.JMenuItem();
                jSeparator2 = new javax.swing.JPopupMenu.Separator();
                jMenuItem7 = new javax.swing.JMenuItem();
                jMenu4 = new javax.swing.JMenu();
                jCheckBoxMenuItem1 = new javax.swing.JCheckBoxMenuItem();

                optionPanel1.setFrame(this);

                jLabel2.setText("Home :");

                jButton1.setText("Choose...");
                jButton1.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jButton1ActionPerformed(evt);
                        }
                });

                jCheckBox1.setText("Show hidden");
                jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jCheckBox1ActionPerformed(evt);
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
                                                .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(optionPanel1Layout.createSequentialGroup()
                                                .addComponent(jCheckBox1)
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap())
                );
                optionPanel1Layout.setVerticalGroup(
                        optionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(optionPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(optionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel2)
                                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jButton1))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jCheckBox1)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );

                jPopupMenu1.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
                        public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                                jPopupMenu1PopupMenuWillBecomeVisible(evt);
                        }
                        public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                        }
                        public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
                        }
                });

                setClosable(true);
                setIconifiable(true);
                setMaximizable(true);
                setResizable(true);
                setName("Files"); // NOI18N

                jList1.setModel(model);
                jList1.setCellRenderer(renderer);
                jList1.setComponentPopupMenu(jPopupMenu1);
                jList1.setDragEnabled(true);
                jList1.setDropMode(javax.swing.DropMode.ON_OR_INSERT);
                jList1.setLayoutOrientation(javax.swing.JList.HORIZONTAL_WRAP);
                jList1.setVisibleRowCount(-1);
                jList1.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                                jList1MouseClicked(evt);
                        }
                });
                jList1.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
                        public void mouseMoved(java.awt.event.MouseEvent evt) {
                                jList1MouseMoved(evt);
                        }
                });
                jList1.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
                        public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                                jList1ValueChanged(evt);
                        }
                });
                jScrollPane1.setViewportView(jList1);

                jMenu1.setText("File");

                jMenuItem1.setAction(openAction);
                jMenu1.add(jMenuItem1);

                jMenu3.setText("Open with");
                jMenu1.add(jMenu3);

                jMenuItem2.setAction(openLocationAction);
                jMenu1.add(jMenuItem2);
                jMenu1.add(jSeparator3);

                jMenuItem10.setAction(closeAction);
                jMenu1.add(jMenuItem10);

                jMenuBar1.add(jMenu1);

                jMenu2.setText("Edit");

                jMenuItem4.setAction(cutAction);
                jMenu2.add(jMenuItem4);

                jMenuItem5.setAction(copyAction);
                jMenu2.add(jMenuItem5);

                jMenuItem6.setAction(pasteAction);
                jMenu2.add(jMenuItem6);

                jMenuItem8.setAction(pasteAsLinkAction);
                jMenu2.add(jMenuItem8);
                jMenu2.add(jSeparator1);

                jMenuItem9.setAction(newFolderAction);
                jMenu2.add(jMenuItem9);

                jMenuItem3.setAction(renameAction);
                jMenu2.add(jMenuItem3);
                jMenu2.add(jSeparator2);

                jMenuItem7.setAction(deleteAction);
                jMenu2.add(jMenuItem7);

                jMenuBar1.add(jMenu2);

                jMenu4.setText("View");

                jCheckBoxMenuItem1.setSelected(isShowHidden());
                jCheckBoxMenuItem1.setText("Show hidden");
                jCheckBoxMenuItem1.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jCheckBoxMenuItem1ActionPerformed(evt);
                        }
                });
                jMenu4.add(jCheckBoxMenuItem1);

                jMenuBar1.add(jMenu4);

                setJMenuBar(jMenuBar1);

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
		if (evt.getClickCount() == 2 && !jList1.isSelectionEmpty()) {
			open(jList1.getSelectedValue());
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

        private void jList1ValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jList1ValueChanged
		prepare();
        }//GEN-LAST:event_jList1ValueChanged

        private void jPopupMenu1PopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_jPopupMenu1PopupMenuWillBecomeVisible
		if (idx > -1 && !jList1.isSelectedIndex(idx)) {
			jList1.setSelectedIndex(idx);
		}
		prepare();
        }//GEN-LAST:event_jPopupMenu1PopupMenuWillBecomeVisible

        private void jList1MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jList1MouseMoved
		idx = jList1.locationToIndex(evt.getPoint());
        }//GEN-LAST:event_jList1MouseMoved

        private void jCheckBoxMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMenuItem1ActionPerformed
		rescan();
        }//GEN-LAST:event_jCheckBoxMenuItem1ActionPerformed

        private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
		optionPanel1.setDirty(true);
        }//GEN-LAST:event_jCheckBox1ActionPerformed

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JButton jButton1;
        private javax.swing.JCheckBox jCheckBox1;
        private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem1;
        private javax.swing.JLabel jLabel2;
        private linoleum.FileList jList1;
        private javax.swing.JMenu jMenu1;
        private javax.swing.JMenu jMenu2;
        private javax.swing.JMenu jMenu3;
        private javax.swing.JMenu jMenu4;
        private javax.swing.JMenuBar jMenuBar1;
        private javax.swing.JMenuItem jMenuItem1;
        private javax.swing.JMenuItem jMenuItem10;
        private javax.swing.JMenuItem jMenuItem2;
        private javax.swing.JMenuItem jMenuItem3;
        private javax.swing.JMenuItem jMenuItem4;
        private javax.swing.JMenuItem jMenuItem5;
        private javax.swing.JMenuItem jMenuItem6;
        private javax.swing.JMenuItem jMenuItem7;
        private javax.swing.JMenuItem jMenuItem8;
        private javax.swing.JMenuItem jMenuItem9;
        private javax.swing.JPopupMenu jPopupMenu1;
        private javax.swing.JScrollPane jScrollPane1;
        private javax.swing.JPopupMenu.Separator jSeparator1;
        private javax.swing.JPopupMenu.Separator jSeparator2;
        private javax.swing.JPopupMenu.Separator jSeparator3;
        private javax.swing.JTextField jTextField1;
        private linoleum.application.OptionPanel optionPanel1;
        // End of variables declaration//GEN-END:variables
}
