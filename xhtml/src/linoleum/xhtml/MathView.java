package linoleum.xhtml;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import javax.swing.text.Element;
import javax.swing.text.BadLocationException;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.ImageView;

public class MathView extends ImageView {
	public MathView(final Element elem) {
		super(elem);
		final int n = elem.getElementCount();
		for (int i = 0; i < n; i++) {
			dump((AbstractDocument.AbstractElement) elem.getElement(i));
		}
		System.out.println();
	}

	public void dump(final AbstractDocument.AbstractElement elem) {
		final String name = elem.getName();
		if (!"content".equals(name)) {
			final AttributeSet attrs = elem.getAttributes();
			final boolean end = "true".equals(attrs.getAttribute(HTML.Attribute.ENDTAG));
			System.out.print("<" + (end?"/":"") + name);
			if (!end) {
				for (final Object obj : Collections.list(attrs.getAttributeNames())) {
					if (obj != StyleConstants.NameAttribute) {
						System.out.print(" " + obj + "=" + elem.getAttribute(obj));
					}
				}
			}
			System.out.print(">");
		}
		if (elem.isLeaf()) {
			try {
				System.out.print(elem.getDocument().getText(elem.getStartOffset(), elem.getEndOffset() - elem.getStartOffset()).trim());
			} catch (final BadLocationException e) {
				e.printStackTrace();
			}
		} else {
			final int n = elem.getElementCount();
			for (int i = 0; i < n; i++) {
				dump((AbstractDocument.AbstractElement) elem.getElement(i));
			}
		}
	}
}
