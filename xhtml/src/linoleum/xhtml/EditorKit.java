package linoleum.xhtml;

import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTML;
import javax.swing.text.html.StyleSheet;

public class EditorKit extends linoleum.html.EditorKit {
	public static class Tag extends HTML.Tag {
		public static final Tag MATH = new Tag("math");

		public Tag(final String id) {
			super(id);
		}
	}

	private final ViewFactory factory = new Factory();

	@Override
	public ViewFactory getViewFactory() {
		return factory;
	}

	@Override
	public Document createDefaultDocument() {
		final Document doc = new Document();
		final StyleSheet styles = getStyleSheet();
		doc.getStyleSheet().addStyleSheet(styles);
		doc.setParser(getParser());
		doc.setTokenThreshold(100);
		return doc;
	}

	public static class Factory extends HTMLFactory {
		public View create(final Element elem) {
			final AttributeSet attrs = elem.getAttributes();
			final Object elementName = attrs.getAttribute(AbstractDocument.ElementNameAttribute);
			final Object o = (elementName != null) ? null : attrs.getAttribute(StyleConstants.NameAttribute);
			if (o instanceof Tag) {
				final Tag kind = (Tag) o;
				if (kind == Tag.MATH) {
					return new MathView(elem);
				}
			}
			return super.create(elem);
		}
	}
}
