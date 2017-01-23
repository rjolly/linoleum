package linoleum.xhtml;

import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;

import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import org.xhtmlrenderer.simple.extend.FormSubmissionListener;

public class ChainedReplacedElementFactory implements ReplacedElementFactory {
	private List<ReplacedElementFactory> factoryList = new ArrayList<>();

	public ReplacedElement createReplacedElement(final LayoutContext c, final BlockBox box, final UserAgentCallback uac, final int cssWidth, final int cssHeight) {
		ReplacedElement re = null;
		for (final ReplacedElementFactory ref : factoryList) {
			re = ref.createReplacedElement(c, box, uac, cssWidth, cssHeight);
			if ( re != null) break;
		}
		return re;
	}

	public void addFactory(final ReplacedElementFactory ref) {
		factoryList.add(ref);
	}

	public void reset() {
		for (final ReplacedElementFactory factory : factoryList) {
			factory.reset();
		}	   
	}

	public void remove(final Element e) {
		for (final ReplacedElementFactory factory : factoryList) {
			factory.remove(e);
		}	   
	}

	public void setFormSubmissionListener(final FormSubmissionListener listener) {
		// nothing to do ?
	}
}
