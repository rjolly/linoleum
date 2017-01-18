package linoleum.xhtml;

import javax.swing.text.Element;
import javax.swing.text.AbstractDocument;
import javax.swing.text.html.ImageView;

public class MathView extends ImageView {
	public MathView(final Element elem) {
		super(elem);
		((AbstractDocument.AbstractElement) elem).dump(System.out, 0);
	}
}
