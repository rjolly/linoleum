package linoleum.html;

import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

public class EditorKit extends HTMLEditorKit {
	private final ViewFactory factory = new Factory();

	public Document createDefaultDocument() {
		final Document doc = new Document();
		final StyleSheet styles = getStyleSheet();
		doc.getStyleSheet().addStyleSheet(styles);
		doc.setParser(getParser());
		doc.setTokenThreshold(100);
		return doc;
	}

	public ViewFactory getViewFactory() {
		return factory;
	}
}
