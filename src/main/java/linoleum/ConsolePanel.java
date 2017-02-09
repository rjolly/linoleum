package linoleum;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

public class ConsolePanel extends JPanel {
	private final JTextArea editor;
	private final DocumentListener listener;
	private final PipedOutputStream src = new PipedOutputStream();
	private final PrintStream out = new PrintStream(src);
	private boolean updating;
	private InputStream is;

	public ConsolePanel(final boolean visible) {
		try {
			is = new PipedInputStream(src, 128);
		} catch (final IOException e) {
		}

		setLayout(new BorderLayout());
		this.editor = new JTextArea();
		editor.setFont(new Font("monospaced", Font.PLAIN, 12));
		editor.setDocument(new EditableAtEndDocument());
		editor.setLineWrap(true);
		JScrollPane scroller = new JScrollPane();
		scroller.getViewport().add(editor);
		add(scroller, BorderLayout.CENTER);

		listener = new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				if (updating) return;
				beginUpdate();
				editor.setCaretPosition(editor.getDocument().getLength());
				if (insertContains(e, '\n')) {
					String cmd = getMarkedText();
					// Handle multi-line input
					if ((cmd.length() == 0) ||
						(cmd.charAt(cmd.length() - 1) != '\\')) {
						// Trim "\\n" combinations
						final String cmd1 = trimContinuations(cmd);
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								out.print(cmd1 + "\n");
								out.flush();
								setMark();
								endUpdate();
							}
						});
					} else {
						endUpdate();
					}
				} else {
					endUpdate();
				}
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
			}
		};

		editor.getDocument().addDocumentListener(listener);
		editor.addCaretListener(new CaretListener() {
			@Override
			public void caretUpdate(CaretEvent e) {
				int len = editor.getDocument().getLength();
				if (e.getDot() > len) {
					editor.setCaretPosition(len);
				}
			}
		});

		Box hbox = Box.createHorizontalBox();
		hbox.add(Box.createGlue());
		JButton button = new JButton("Clear"); // FIXME: i18n ?
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				clear();
			}
		});
		hbox.add(button);
		hbox.add(Box.createGlue());
		add(hbox, BorderLayout.SOUTH);
		if (visible) {
			System.setIn(new BufferedInputStream(getInputStream()));
			System.setOut(new PrintStream(new BufferedOutputStream(getOutputStream(), 128), true));
			System.setErr(new PrintStream(new BufferedOutputStream(getOutputStream(), 128), true));
		}
	}

	@Override
	public void requestFocus() {
		editor.requestFocus();
	}

	public void clear() {
		EditableAtEndDocument d = (EditableAtEndDocument)editor.getDocument();
		d.clear();
		setMark();
		editor.requestFocus();
	}

	public void setMark() {
		((EditableAtEndDocument)editor.getDocument()).setMark();
	}

	public String getMarkedText() {
		try {
			String s = ((EditableAtEndDocument)editor.getDocument()).getMarkedText();
			int i = s.length();
			while ((i > 0) && (s.charAt(i - 1) == '\n')) {
				i--;
			}
			return s.substring(0, i);
		} catch (BadLocationException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void print(String s) {
		Document d = editor.getDocument();
		d.removeDocumentListener(listener);
		try {
			d.insertString(d.getLength(), s, null);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		d.addDocumentListener(listener);
	}

	public InputStream getInputStream() {
		return is;
	}

	OutputStream os = new OutputStream() {
		public void write(final int b) throws IOException {
			write(new byte[] {(byte)b});
		}

		@Override
		public void write(byte b[], int off, int len) {
			byte buf[] = new byte[len];
			System.arraycopy(b, off, buf, 0, len);
			print(new String(buf));
			setMark();
			editor.setCaretPosition(editor.getDocument().getLength());
		}
	};

	public OutputStream getOutputStream() {
		return os;
	}

	private void beginUpdate() {
		editor.setEditable(false);
		updating = true;
	}

	private void endUpdate() {
		editor.setEditable(true);
		updating = false;
	}

	private boolean insertContains(DocumentEvent e, char c) {
		String s = null;
		try {
			s = editor.getText(e.getOffset(), e.getLength());
			for (int i = 0; i < e.getLength(); i++) {
				if (s.charAt(i) == c) {
					return true;
				}
			}
		} catch (BadLocationException ex) {
			ex.printStackTrace();
		}
		return false;
	}

	private String trimContinuations(String text) {
		int i;
		while ((i = text.indexOf("\\\n")) >= 0) {
			text = text.substring(0, i) + text.substring(i+1, text.length());
		}
		return text;
	}
}
