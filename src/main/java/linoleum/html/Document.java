package linoleum.html;

import java.net.URL;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.MutableAttributeSet;

public class Document extends HTMLDocument {

	public HTMLEditorKit.ParserCallback getReader(final int pos) {
		final Object desc = getProperty(javax.swing.text.Document.StreamDescriptionProperty);
		if (desc instanceof URL) { 
			setBase((URL)desc);
		}
		final Reader reader = new Reader(pos);
		return reader;
	}

	public HTMLEditorKit.ParserCallback getReader(final int pos, final int popDepth, final int pushDepth, final HTML.Tag insertTag) {
		final Object desc = getProperty(Document.StreamDescriptionProperty);
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
			registerTag(HTML.Tag.HEAD, new EmptyAction());
		}

		class EmptyAction extends TagAction {

			public void start(final HTML.Tag t, final MutableAttributeSet attr) {}

			public void end(final HTML.Tag t) {}
		}
	}
}
