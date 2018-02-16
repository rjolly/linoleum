package linoleum.notepad;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.ExecutionException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Segment;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import linoleum.application.FileChooser;

@SuppressWarnings("serial")
public class Notepad extends JPanel {
	private final Properties properties = new Properties();
	static final ResourceBundle resources = ResourceBundle.getBundle(Notepad.class.getName());

	private static final String[] MENUBAR_KEYS = {"file", "edit", "debug"};
	private static final String[] TOOLBAR_KEYS = {"new", "open", "save", "-", "cut", "copy", "paste"};
	private static final String[] FILE_KEYS = {"new", "open", "save", "saveAs", "-", "exit"};
	private static final String[] EDIT_KEYS = {"cut", "copy", "paste", "-", "undo", "redo", "-", "find", "replace"};
	private static final String[] DEBUG_KEYS = {"dump", "showElementTree"};

	final Editor editor = new Editor();
	private final JToolBar toolbar = new JToolBar();
	private final JPanel status = new JPanel();
	private final JLabel label = new JLabel();
	private final JProgressBar progress = new JProgressBar();
	private final JInternalFrame elementTreeFrame = new JInternalFrame(resources.getString("ElementTreeFrameTitle"));
	private final ElementTreePanel elementTreePanel = new ElementTreePanel(editor);
	private final UndoManager undo = new UndoManager();
	private final Frame frame;
	private Path file;
	private Path prev;
	int modified;

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public Notepad(final Frame frame) {
		super(true);
		try {
			properties.load(getClass().getResourceAsStream("NotepadSystem.properties"));
		} catch (final IOException  e) {
			e.printStackTrace();
		}

		// Create a frame containing an instance of ElementTreePanel.
		frame.addInternalFrameListener(new InternalFrameAdapter() {
			@Override
			public void internalFrameClosing(final InternalFrameEvent weeee) {
				elementTreeFrame.doDefaultCloseAction();
			}
		});
		final Container fContentPane = elementTreeFrame.getContentPane();
		fContentPane.setLayout(new BorderLayout());
		fContentPane.add(elementTreePanel);
		elementTreeFrame.setClosable(true);
		elementTreeFrame.pack();

		setBorder(BorderFactory.createEtchedBorder());
		setLayout(new BorderLayout());

		// create the embedded JTextComponent
		editor.addCaretListener(caretListener);

		// install the command table
		final ActionMap commands = editor.getActionMap();
		for (final Action a : defaultActions) {
			commands.put(a.getValue(Action.NAME), a);
		}

		final JScrollPane scroller = new JScrollPane();
		final JViewport port = scroller.getViewport();
		port.add(editor);

		final String vpFlag = getProperty("ViewportBackingStore");
		if (vpFlag != null) {
			Boolean bs = Boolean.valueOf(vpFlag);
			port.setScrollMode(bs
					? JViewport.BACKINGSTORE_SCROLL_MODE
					: JViewport.BLIT_SCROLL_MODE);
		}

		final JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add("North", createToolbar());
		panel.add("Center", scroller);
		add("Center", panel);
		add("South", createStatusbar());
		this.frame = frame;
	}

	@Override
	public void requestFocus() {
		editor.requestFocus();
	}

	private Action getAction(final String cmd) {
		String astr = getProperty(cmd + actionSuffix);
		if (astr == null) {
			astr = cmd;
		}
		final Action a = editor.getActionMap().get(astr);
		if (a != null) {
			final String name = getResourceString(cmd + labelSuffix);
			if (name != null) {
				a.putValue(Action.NAME, name);
			}
			final URL url = getSmallResource(cmd + imageSuffix);
			if (url != null) {
				a.putValue(Action.SMALL_ICON, new ImageIcon(url));
			}
			final URL lurl = getResource(cmd + imageSuffix);
			if (lurl != null) {
				a.putValue(Action.LARGE_ICON_KEY, new ImageIcon(lurl));
			}
			final String tip = getResourceString(cmd + tipSuffix);
			if (tip != null) {
				a.putValue(Action.SHORT_DESCRIPTION, tip);
			}
			final String acc = getResourceString(cmd + acceleratorSuffix);
			if (acc != null) {
				a.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(acc));
			}
		}
		return a;
	}

	private String getProperty(final String key) {
		return properties.getProperty(key);
	}

	private String getResourceString(final String nm) {
		try {
			return resources.getString(nm);
		} catch (final MissingResourceException e) {}
		return null;
	}

	private URL getResource(final String key) {
		final String name = getResourceString(key);
		if (name != null) {
			return getClass().getResource(name);
		}
		return null;
	}

	private URL getSmallResource(final String key) {
		final String name = getResourceString(key);
		if (name != null) {
			return getClass().getResource(name.replace("24", "16"));
		}
		return null;
	}

	private JPanel createStatusbar() {
		status.setLayout(new CardLayout());
		status.add(label, "label");
		status.add(progress, "progress");
		return status;
	}

	private JToolBar createToolbar() {
		for (final String toolKey: getToolBarKeys()) {
			if (toolKey.equals("-")) {
				toolbar.add(Box.createHorizontalStrut(5));
			} else {
				toolbar.add(getAction(toolKey));
			}
		}
		toolbar.add(Box.createHorizontalGlue());
		return toolbar;
	}

	JMenuBar createMenubar() {
		final JMenuBar mb = new JMenuBar();
		for(final String menuKey : getMenuBarKeys()){
			final JMenu m = createMenu(menuKey);
			if (m != null) {
				mb.add(m);
			}
		}
		return mb;
	}

	private JMenu createMenu(final String key) {
		final JMenu menu = new JMenu(getResourceString(key + labelSuffix));
		for (final String itemKey : getItemKeys(key)) {
			if (itemKey.equals("-")) {
				menu.addSeparator();
			} else {
				menu.add(getAction(itemKey));
			}
		}
		return menu;
	}

	private String[] getItemKeys(final String key) {
		switch (key) {
			case "file":
				return FILE_KEYS;
			case "edit":
				return EDIT_KEYS;
			case "debug":
				return DEBUG_KEYS;
			default:
				return null;
		}
	}

	private String[] getMenuBarKeys() {
		return MENUBAR_KEYS;
	}

	private String[] getToolBarKeys() {
		return TOOLBAR_KEYS;
	}

	public static final String imageSuffix = "Image";
	public static final String labelSuffix = "Label";
	public static final String actionSuffix = "Action";
	public static final String tipSuffix = "Tooltip";
	public static final String acceleratorSuffix = "Accelerator";

	private UndoableEditListener undoHandler = new UndoableEditListener() {
		public void undoableEditHappened(final UndoableEditEvent e) {
			undo.addEdit(e.getEdit());
			if (modified >= 0) modified += 1;
			undoAction.update();
			redoAction.update();
		}
	};

	private CaretListener caretListener = new CaretListener() {
		public void caretUpdate(final CaretEvent e) {
			try {
				final int pos = editor.getCaretPosition();
				final int line = editor.getLineOfOffset(pos);
				final int column = pos - editor.getLineStartOffset(line);
				label.setText((line + 1) + "," + (column + 1));
			} catch (final BadLocationException ex) {
				ex.printStackTrace();
			}
		}
	};

	// --- action implementations -----------------------------------
	private final UndoAction undoAction = new UndoAction();
	private final RedoAction redoAction = new RedoAction();

	private final Action[] defaultActions = {
		new NewAction(),
		new OpenAction(),
		new SaveAction(),
		new SaveAsAction(),
		new ExitAction(),
		new DefaultEditorKit.CutAction(),
		new DefaultEditorKit.CopyAction(),
		new DefaultEditorKit.PasteAction(),
		new ShowElementTreeAction(),
		undoAction,
		redoAction,
		new FindAction(),
		new ReplaceAction()
	};

	class UndoAction extends AbstractAction {
		public UndoAction() {
			super("Undo");
			setEnabled(false);
		}

		public void actionPerformed(final ActionEvent e) {
			try {
				undo.undo();
				modified -= 1;
			} catch (final CannotUndoException ex) {
				Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Unable to undo", ex);
			}
			update();
			redoAction.update();
		}

		void update() {
			if (undo.canUndo()) {
				setEnabled(true);
				putValue(Action.NAME, undo.getUndoPresentationName());
			} else {
				setEnabled(false);
				putValue(Action.NAME, resources.getString("undoLabel"));
			}
			Notepad.this.update();
		}
	}

	class RedoAction extends AbstractAction {
		public RedoAction() {
			super("Redo");
			setEnabled(false);
		}

		public void actionPerformed(final ActionEvent e) {
			try {
				undo.redo();
				modified += 1;
			} catch (final CannotRedoException ex) {
				Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Unable to redo", ex);
			}
			update();
			undoAction.update();
		}

		void update() {
			if (undo.canRedo()) {
				setEnabled(true);
				putValue(Action.NAME, undo.getRedoPresentationName());
			} else {
				setEnabled(false);
				putValue(Action.NAME, resources.getString("redoLabel"));
			}
		}
	}

	private class FindAction extends AbstractAction {
		private FindAction() {
			super("find");
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			frame.find();
		}
	}

	private class ReplaceAction extends AbstractAction {
		private ReplaceAction() {
			super("replace");
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			frame.replace();
		}
	}

	private class NewAction extends AbstractAction {
		private NewAction() {
			super("new");
		}

		public void actionPerformed(final ActionEvent e) {
			frame.closeDialog();
			setFile(null);
			open();
		}
	}

	private class OpenAction extends AbstractAction {
		private OpenAction() {
			super("open");
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			frame.closeDialog();
			frame.getApplicationManager().get("Files").open(file == null?prev == null?null:prev.toUri():file.toUri(), frame.getDesktopPane());
		}
	}

	private class SaveAction extends AbstractAction {
		private SaveAction() {
			super("save");
		}

		public void actionPerformed(final ActionEvent e) {
			save();
		}
	}

	private class SaveAsAction extends AbstractAction {
		private SaveAsAction() {
			super("saveAs");
		}

		public void actionPerformed(final ActionEvent e) {
			frame.closeDialog();
			final FileChooser chooser = frame.getFileChooser();
			switch (chooser.showInternalSaveDialog(Notepad.this)) {
			case JFileChooser.APPROVE_OPTION:
				save(chooser.getSelectedFile().toPath());
				break;
			default:
			}
		}
	}

	private class ExitAction extends AbstractAction {
		private ExitAction() {
			super("exit");
		}

		public void actionPerformed(final ActionEvent e) {
			frame.doDefaultCloseAction();
		}
	}

	void setFile(final Path file) {
		prev = this.file;
		this.file = file;
	}

	Path getFile() {
		return file;
	}

	void open() {
		if (proceed()) {
			editor.getDocument().removeUndoableEditListener(undoHandler);
			editor.setDocument(new Document());
			if (file != null && Files.exists(file)) {
				try {
					frame.getFileChooser().setSelectedFile(file.toFile());
				} catch (final UnsupportedOperationException e) {}
				new FileLoader().execute();
			} else {
				reset();
			}
		} else {
			file = prev;
		}
	}

	private void reset() {
		editor.getDocument().addUndoableEditListener(undoHandler);
		resetUndoManager();
	}

	private void resetUndoManager() {
		undo.discardAllEdits();
		modified = 0;
		undoAction.update();
		redoAction.update();
	}

	private void save(final Path file) {
		if (!Files.exists(file) || file.equals(this.file) || proceed("Overwrite")) {
			this.file = file;
			save();
		}
	}

	private void save() {
		new FileSaver().execute();
	}

	private void update() {
		String title = file == null?resources.getString("Title"):file.getFileName().toString();
		if (modified != 0) {
			title += " (modified)";
		}
		frame.setTitle(title);
		getAction("save").setEnabled(file != null && modified != 0);
	}

	private boolean proceed() {
		return modified == 0 || proceed("Warning");
	}

	private boolean proceed(final String key) {
		switch (JOptionPane.showInternalConfirmDialog(frame, resources.getString(key), resources.getString("WarningTitle"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE)) {
		case JOptionPane.OK_OPTION:
			return true;
		default:
		}
		return false;
	}

	class ShowElementTreeAction extends AbstractAction {
		ShowElementTreeAction() {
			super("showElementTree");
		}

		public void actionPerformed(ActionEvent e) {
			frame.getDesktopPane().add(elementTreeFrame);
			elementTreeFrame.setVisible(true);
		}
	}

	abstract class FileWorker extends SwingWorker<Object, Object> {
		final Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
		final CardLayout layout = (CardLayout) status.getLayout();
		final Document doc = editor.getReplaceDocument();
		final Cursor cursor = editor.getCursor();
		final int length;

		FileWorker(final int length) {
			addPropertyChangeListener(new PropertyChangeListener() {
				public  void propertyChange(final PropertyChangeEvent evt) {
					if ("progress".equals(evt.getPropertyName())) {
						progress.setValue((Integer) evt.getNewValue());
					}
				}
			});
			layout.show(status, "progress");
			editor.setCursor(waitCursor);
			this.length = length;
		}

		void setNumber(final int n) {
			if (length > 0) {
				setProgress(100 * n / length);
			}
		}

		@Override
		public void done() {
			try {
				get();
				doDone();
			} catch (final InterruptedException e) {
				e.printStackTrace();
			} catch (final ExecutionException e) {
				e.printStackTrace();
			}
			editor.requestFocus();
			editor.setCursor(cursor);
			layout.show(status, "label");
			progress.setValue(0);
		}

		protected void doDone() {
		}
	}

	private int getFileLength() {
		try {
			return (int) Files.size(file);
		} catch (final IOException ex) {
			ex.printStackTrace();
		}
		return 0;
	}

	class FileLoader extends FileWorker {
		FileLoader() {
			super(getFileLength());
			if (elementTreePanel != null) {
				elementTreePanel.setEditor(null);
			}
		}

		@Override
		public Object doInBackground() {
			try (final Reader in = Files.newBufferedReader(file, Charset.defaultCharset())) {
				// try to start reading
				final char[] buff = new char[4096];
				int nch;
				int n = 0;
				while ((nch = in.read(buff, 0, buff.length)) != -1) {
					doc.insertString(doc.getLength(), new String(buff, 0, nch), null);
					setNumber(n += nch);
				}
			} catch (final IOException e) {
				e.printStackTrace();
			} catch (final BadLocationException e) {
				System.err.println(e.getMessage());
			}
			return null;
		}

		@Override
		public void doDone() {
			if (elementTreePanel != null) {
				elementTreePanel.setEditor(editor);
			}
			reset();
		}
	}

	class FileSaver extends FileWorker {
		FileSaver() {
			super(editor.getDocument().getLength());
		}

		@Override
		public Object doInBackground() {
			try (final Writer out = Files.newBufferedWriter(file, Charset.defaultCharset())) {
				// start writing
				final Segment text = new Segment();
				text.setPartialReturn(true);
				int charsLeft = length;
				int offset = 0;
				while (charsLeft > 0) {
					doc.getText(offset, Math.min(4096, charsLeft), text);
					out.write(text.array, text.offset, text.count);
					charsLeft -= text.count;
					offset += text.count;
					setNumber(offset);
				}
				out.flush();
			} catch (final IOException e) {
				e.printStackTrace();
			} catch (final BadLocationException e) {
				System.err.println(e.getMessage());
			}
			return null;
		}

		@Override
		public void doDone() {
			modified = 0;
			update();
		}
	}
}
