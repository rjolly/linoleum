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

	private static Properties properties;
	public static ResourceBundle resources;

	private static final String[] MENUBAR_KEYS = {"file", "edit", "debug"};
	private static final String[] TOOLBAR_KEYS = {"new", "open", "save", "-", "cut", "copy", "paste"};
	private static final String[] FILE_KEYS = {"new", "open", "save", "saveAs", "-", "exit"};
	private static final String[] EDIT_KEYS = {"cut", "copy", "paste", "-", "undo", "redo", "-", "find", "replace"};
	private static final String[] DEBUG_KEYS = {"dump", "showElementTree"};

	static {
		try {
			properties = new Properties();
			properties.load(Notepad.class.getResourceAsStream("NotepadSystem.properties"));
			resources = ResourceBundle.getBundle("linoleum.notepad.Notepad", Locale.getDefault());
		} catch (MissingResourceException | IOException  e) {
			System.err.println("Notepad.properties or NotepadSystem.properties not found");
			System.exit(1);
		}
	}

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public Notepad(final Frame frame) {
		super(true);
		this.frame = frame;
		setBorder(BorderFactory.createEtchedBorder());
		setLayout(new BorderLayout());

		// create the embedded JTextComponent
		editor = new Editor();
		editor.addCaretListener(caretListener);

		// install the command table
		final ActionMap commands = editor.getActionMap();
		for (final Action a : defaultActions) {
			commands.put(a.getValue(Action.NAME), a);
		}

		JScrollPane scroller = new JScrollPane();
		JViewport port = scroller.getViewport();
		port.add(editor);

		String vpFlag = getProperty("ViewportBackingStore");
		if (vpFlag != null) {
			Boolean bs = Boolean.valueOf(vpFlag);
			port.setScrollMode(bs
					? JViewport.BACKINGSTORE_SCROLL_MODE
					: JViewport.BLIT_SCROLL_MODE);
		}

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add("North", createToolbar());
		panel.add("Center", scroller);
		add("Center", panel);
		add("South", createStatusbar());
	}

	@Override
	public void requestFocus() {
		editor.requestFocus();
	}

	Editor getEditor() {
		return editor;
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

	private String getProperty(String key) {
		return properties.getProperty(key);
	}

	private String getResourceString(String nm) {
		String str;
		try {
			str = resources.getString(nm);
		} catch (MissingResourceException mre) {
			str = null;
		}
		return str;
	}

	private URL getResource(String key) {
		String name = getResourceString(key);
		if (name != null) {
			return getClass().getResource(name);
		}
		return null;
	}

	private URL getSmallResource(String key) {
		String name = getResourceString(key);
		if (name != null) {
			return getClass().getResource(name.replace("24", "16"));
		}
		return null;
	}

	private JPanel createStatusbar() {
		status = new JPanel();
                status.setLayout(new CardLayout());
		label = new JLabel();
		progress = new JProgressBar();
                status.add(label, "label");
                status.add(progress, "progress");
		return status;
	}

	private void resetUndoManager() {
		undo.discardAllEdits();
		modified = 0;
		undoAction.update();
		redoAction.update();
	}

	private JToolBar createToolbar() {
		toolbar = new JToolBar();
		for (String toolKey: getToolBarKeys()) {
			if (toolKey.equals("-")) {
				toolbar.add(Box.createHorizontalStrut(5));
			} else {
				toolbar.add(getAction(toolKey));
			}
		}
		toolbar.add(Box.createHorizontalGlue());
		return toolbar;
	}

	public JMenuBar createMenubar() {
		JMenuBar mb = new JMenuBar();
		for(String menuKey: getMenuBarKeys()){
			JMenu m = createMenu(menuKey);
			if (m != null) {
				mb.add(m);
			}
		}
		return mb;
	}

	private JMenu createMenu(String key) {
		JMenu menu = new JMenu(getResourceString(key + labelSuffix));
		for (String itemKey: getItemKeys(key)) {
			if (itemKey.equals("-")) {
				menu.addSeparator();
			} else {
				menu.add(getAction(itemKey));
			}
		}
		return menu;
	}

	private String[] getItemKeys(String key) {
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

	private Editor editor;
	private JToolBar toolbar;
	private JPanel status;
	private JLabel label;
	private JProgressBar progress;
	private JInternalFrame elementTreeFrame;
	private ElementTreePanel elementTreePanel;
	private Frame frame;
	private int modified;
	private Path file;
	private UndoManager undo = new UndoManager();

	public static final String imageSuffix = "Image";
	public static final String labelSuffix = "Label";
	public static final String actionSuffix = "Action";
	public static final String tipSuffix = "Tooltip";
	public static final String acceleratorSuffix = "Accelerator";

	private UndoableEditListener undoHandler = new UndoableEditListener() {
		public void undoableEditHappened(UndoableEditEvent e) {
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
	private UndoAction undoAction = new UndoAction();
	private RedoAction redoAction = new RedoAction();

	private Action[] defaultActions = {
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

		public void actionPerformed(ActionEvent e) {
			try {
				undo.undo();
				modified -= 1;
			} catch (CannotUndoException ex) {
				Logger.getLogger(UndoAction.class.getName()).log(Level.SEVERE,
						"Unable to undo", ex);
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

		public void actionPerformed(ActionEvent e) {
			try {
				undo.redo();
				modified += 1;
			} catch (CannotRedoException ex) {
				Logger.getLogger(RedoAction.class.getName()).log(Level.SEVERE,
						"Unable to redo", ex);
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

	class FindAction extends AbstractAction {
		FindAction() {
			super("find");
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			frame.find();
		}
	}

	class ReplaceAction extends AbstractAction {
		ReplaceAction() {
			super("replace");
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			frame.replace();
		}
	}

	class NewAction extends AbstractAction {
		NewAction() {
			super("new");
		}

		NewAction(String nm) {
			super(nm);
		}

		public void actionPerformed(final ActionEvent e) {
			frame.closeDialog();
			if (clean() || proceed("Warning")) {
				setFile(null);
				open();
			}
		}
	}

	class OpenAction extends AbstractAction {
		OpenAction() {
			super("open");
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			frame.closeDialog();
			if (clean() || proceed("Warning")) {
				final FileChooser chooser = frame.getFileChooser();
				switch (chooser.showInternalOpenDialog(Notepad.this)) {
				case JFileChooser.APPROVE_OPTION:
					setFile(chooser.getSelectedFile().toPath());
					open();
					break;
				default:
				}
			}
		}
	}

	class SaveAction extends AbstractAction {
		SaveAction() {
			super("save");
		}

		public void actionPerformed(final ActionEvent e) {
			save();
		}
	}

	class SaveAsAction extends AbstractAction {
		SaveAsAction() {
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

	class ExitAction extends AbstractAction {
		ExitAction() {
			super("exit");
		}

		public void actionPerformed(final ActionEvent e) {
			frame.doDefaultCloseAction();
		}
	}

	void setFile(final Path file) {
		final Document doc = editor.getReplaceDocument();
		if (doc != null) {
			doc.removeUndoableEditListener(undoHandler);
		}
		editor.setDocument(new Document());
		this.file = file;
	}

	Path getFile() {
		return file;
	}

	void open() {
		if (file != null && Files.exists(file)) {
			new FileLoader().execute();
		} else {
			editor.getDocument().addUndoableEditListener(undoHandler);
			resetUndoManager();
		}
	}

	private void save(final Path file) {
		if (!Files.exists(file) || file.equals(this.file) || proceed("Overwrite")) {
			this.file = file;
			save();
		}
	}

	private void save() {
		new FileSaver().execute();
		modified = 0;
		update();
	}

	private void update() {
		String title = file == null?resources.getString("Title"):file.getFileName().toString();
		if (modified != 0) {
			title += " (modified)";
		}
		frame.setTitle(title);
		getAction("save").setEnabled(file != null && modified != 0);
	}

	boolean clean() {
		return modified == 0;
	}

	boolean proceed(final String key) {
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
			if (elementTreeFrame == null) {
				// Create a frame containing an instance of
				// ElementTreePanel.
				try {
					String title = resources.getString("ElementTreeFrameTitle");
					elementTreeFrame = new JInternalFrame(title);
				} catch (MissingResourceException mre) {
					elementTreeFrame = new JInternalFrame();
				}

				frame.addInternalFrameListener(new InternalFrameAdapter() {

					@Override
					public void internalFrameClosing(InternalFrameEvent weeee) {
						elementTreeFrame.setVisible(false);
					}
				});
				Container fContentPane = elementTreeFrame.getContentPane();

				fContentPane.setLayout(new BorderLayout());
				elementTreePanel = new ElementTreePanel(editor);
				fContentPane.add(elementTreePanel);
				elementTreeFrame.pack();
			}
			frame.getDesktopPane().add(elementTreeFrame);
			elementTreeFrame.setClosable(true);
			elementTreeFrame.setVisible(true);
		}
	}

	abstract class FileWorker extends SwingWorker<Document, Object> {
		final CardLayout layout = (CardLayout) status.getLayout();
		final Document doc = editor.getReplaceDocument();
		final Cursor cursor = editor.getCursor();
		final int length;

		FileWorker(final int length) {
			this.length = length;
			addPropertyChangeListener(new PropertyChangeListener() {
				public  void propertyChange(final PropertyChangeEvent evt) {
					if ("progress".equals(evt.getPropertyName())) {
						progress.setValue((Integer)evt.getNewValue());
					}
				}
			});
			layout.show(status, "progress");
			editor.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		}

		void setNumber(final int n) {
			if (length > 0) {
				setProgress(100 * n / length);
			}
		}

		@Override
		public void done() {
			editor.setCursor(cursor);
			layout.show(status, "label");
			progress.setValue(0);
		}
	}

	private int getFileLength() {
		int n = 0;
		try {
			n = (int) Files.size(file);
		} catch (final IOException ex) {
			ex.printStackTrace();
		}
		return n;
	}

	class FileLoader extends FileWorker {
		FileLoader() {
			super(getFileLength());
			if (elementTreePanel != null) {
				elementTreePanel.setEditor(null);
			}
		}

		@Override
		public Document doInBackground() {
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
			return doc;
		}

		@Override
		public void done() {
			super.done();
			if (elementTreePanel != null) {
				elementTreePanel.setEditor(editor);
			}
			doc.addUndoableEditListener(undoHandler);
			resetUndoManager();
		}
	}

	class FileSaver extends FileWorker {
		FileSaver() {
			super(editor.getDocument().getLength());
		}

		@Override
		public Document doInBackground() {
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
			return doc;
		}
	}
}
