package linoleum.xhtml;

import java.net.URL;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;

public class Document extends linoleum.html.Document {
	public HTMLEditorKit.ParserCallback getReader(final int pos) {
		final Object desc = getProperty(StreamDescriptionProperty);
		if (desc instanceof URL) {
			setBase((URL)desc);
		}
		final Reader reader = new Reader(pos);
		return reader;
	}

	public HTMLEditorKit.ParserCallback getReader(final int pos, final int popDepth, final int pushDepth, final HTML.Tag insertTag) {
		final Object desc = getProperty(StreamDescriptionProperty);
		if (desc instanceof URL) {
			setBase((URL)desc);
		}
		final Reader reader = new Reader(pos, popDepth, pushDepth, insertTag);
		return reader;
	}

	public class Reader extends HTMLReader {
		public Reader(final int offset) {
			this(offset, 0, 0, null);
		}

		public Reader(final int offset, final int popDepth, final int pushDepth, final HTML.Tag insertTag) {
			super(offset, popDepth, pushDepth, insertTag);
			registerTag(EditorKit.Tag.MATH, new SpecialAction());
		}
	}
}
