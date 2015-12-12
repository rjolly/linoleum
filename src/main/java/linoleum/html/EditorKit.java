package linoleum.html;

import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import javax.swing.text.StyleConstants;

public class EditorKit extends HTMLEditorKit {

	public Document createDefaultDocument() {
		final Document doc = new Document();
		final StyleSheet styles = getStyleSheet();
		styles.getStyle("code").removeAttribute(StyleConstants.FontSize);
		doc.getStyleSheet().addStyleSheet(styles);
		doc.setParser(getParser());
		doc.setTokenThreshold(100);
		return doc;
	}
}
