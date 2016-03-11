package linoleum.notepad;

import javax.swing.event.UndoableEditEvent;
import javax.swing.text.PlainDocument;
import javax.swing.text.BadLocationException;
import javax.swing.undo.CompoundEdit;

public class Document extends PlainDocument {
	CompoundEdit undo = null;

	void hold() {
		undo = new CompoundEdit();
	}

	void release() {
		undo.end();
		super.fireUndoableEditUpdate(new UndoableEditEvent(this, undo));
		undo = null;
	}

	protected void fireUndoableEditUpdate(final UndoableEditEvent e) {
		if(undo==null) super.fireUndoableEditUpdate(e);
		else undo.addEdit(e.getEdit());
	}

	public String getText() throws BadLocationException {
		return getText(0, getLength());
	}
}
