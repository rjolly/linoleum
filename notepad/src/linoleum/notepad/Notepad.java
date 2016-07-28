package linoleum.notepad;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.*;
import javax.swing.undo.*;
import javax.swing.text.*;
import javax.swing.event.*;

@SuppressWarnings("serial")
public class Notepad extends JPanel {

	private static Properties properties;
	public static ResourceBundle resources;

	private static final String[] MENUBAR_KEYS = {"file", "edit", "debug"};
	private static final String[] TOOLBAR_KEYS = {"new", "open", "save", "-", "cut", "copy", "paste"};
	private static final String[] FILE_KEYS = {"new", "open", "save"};
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

		// Add this as a listener for undoable edits.
		editor.getDocument().addUndoableEditListener(undoHandler);

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
		update();
	}

	public Editor getEditor() {
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
			final URL url = getResource(cmd + imageSuffix);
			if (url != null) {
				a.putValue(Action.SMALL_ICON, new ImageIcon(url));
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

	private Component createStatusbar() {
		// need to do something reasonable here
		status = new StatusBar();
		return status;
	}

	private void resetUndoManager() {
		undo.discardAllEdits();
		modified = 0;
		undoAction.update();
		redoAction.update();
	}

	private Component createToolbar() {
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
	private JComponent status;
	private JInternalFrame elementTreeFrame;
	private ElementTreePanel elementTreePanel;
	private Frame frame;
	private int modified;
	private File parent;
	private File file;

	private UndoableEditListener undoHandler = new UndoHandler();
	private UndoManager undo = new UndoManager();

	public static final String imageSuffix = "Image";
	public static final String labelSuffix = "Label";
	public static final String actionSuffix = "Action";
	public static final String tipSuffix = "Tooltip";
	public static final String acceleratorSuffix = "Accelerator";

	class UndoHandler implements UndoableEditListener {

		public void undoableEditHappened(UndoableEditEvent e) {
			undo.addEdit(e.getEdit());
			if (modified >= 0) modified += 1;
			undoAction.update();
			redoAction.update();
		}
	}

	class StatusBar extends JComponent {

		public StatusBar() {
			super();
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);
		}
	}
	// --- action implementations -----------------------------------
	private UndoAction undoAction = new UndoAction();
	private RedoAction redoAction = new RedoAction();

	private Action[] defaultActions = {
		new NewAction(),
		new OpenAction(),
		new SaveAction(),
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

	class OpenAction extends AbstractAction {

		OpenAction() {
			super("open");
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			if (parent == null) {
				parent = new File(".");
			}
			frame.getApplicationManager().open(parent.toURI());
		}
	}

	public void open(final File file) {
		if (modified != 0) {
			final int option = JOptionPane.showInternalConfirmDialog(frame, resources.getString("Warning"), resources.getString("WarningTitle"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
			switch (option) {
			case JOptionPane.OK_OPTION:
				break;
			default:
				return;
			}
		}
		setFile(file);
		if (file.exists()) {
			new FileLoader().execute();
		} else {
			editor.getDocument().addUndoableEditListener(undoHandler);
			resetUndoManager();
		}
	}

	private void setFile(final File file) {
		final Document doc = editor.getReplaceDocument();
		if (doc != null) {
			doc.removeUndoableEditListener(undoHandler);
		}
		editor.setDocument(new Document());
		if (file != null) {
			parent = file.getParentFile();
		}
		this.file = file;
	}

	public File getFile() {
		return file;
	}

	class SaveAction extends AbstractAction {

		SaveAction() {
			super("save");
		}

		public void actionPerformed(final ActionEvent e) {
			save();
		}
	}

	private void save() {
		new FileSaver().execute();
		modified = 0;
		update();
	}

	class NewAction extends AbstractAction {

		NewAction() {
			super("new");
		}

		NewAction(String nm) {
			super(nm);
		}

		public void actionPerformed(final ActionEvent e) {
			if (modified != 0) {
				final int option = JOptionPane.showInternalConfirmDialog(frame, resources.getString("Warning"), resources.getString("WarningTitle"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
				switch (option) {
				case JOptionPane.OK_OPTION:
					break;
				default:
					return;
				}
			}
			setFile(null);
			editor.getDocument().addUndoableEditListener(undoHandler);
			resetUndoManager();
			revalidate();
		}
	}

	private void update() {
		String title = file == null?resources.getString("Title"):file.getName();
		if (modified != 0) {
			title += " (modified)";
		}
		frame.setTitle(title);
		getAction("save").setEnabled(file != null && modified != 0);
	}

	public void close() {
		if (file != null && modified != 0) {
			final int option = JOptionPane.showInternalConfirmDialog(frame, resources.getString("Question"), resources.getString("QuestionTitle"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			switch (option) {
			case JOptionPane.YES_OPTION:
				save();
			}
		}
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
		final Document doc = editor.getReplaceDocument();
		final int length;

		FileWorker(final int length) {
			this.length = length;

			// initialize the statusbar
			status.removeAll();
			final JProgressBar progress = new JProgressBar();
			addPropertyChangeListener(new PropertyChangeListener() {
				public  void propertyChange(final PropertyChangeEvent evt) {
					if ("progress".equals(evt.getPropertyName())) {
						progress.setValue((Integer)evt.getNewValue());
					}
				}
			});
			status.add(progress);
			status.revalidate();
		}

		void setNumber(final int n) {
			if (length > 0) {
				setProgress(100 * n / length);
			}
		}

		@Override
		public void done() {

			// we are done... get rid of progressbar
			status.removeAll();
			status.revalidate();
		}
	}

	class FileLoader extends FileWorker {
		FileLoader() {
			super((int)file.length());
			if (elementTreePanel != null) {
				elementTreePanel.setEditor(null);
			}
		}

		@Override
		public Document doInBackground() {
			try (final Reader in = new FileReader(file)) {

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
			try (final Writer out = new FileWriter(file)) {

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
