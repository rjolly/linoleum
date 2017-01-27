package linoleum.xhtml;

import java.awt.Image;
import org.w3c.dom.Document;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;

public class SVGBatikReplacedElementFactory extends IconReplacedElementFactory {
	public SVGBatikReplacedElementFactory() {
		super("svg");
	}

	public Image createImage(final Document doc) throws Exception {
		final MemoryTranscoder transcoder = new MemoryTranscoder();
		transcoder.transcode(new TranscoderInput(doc), new TranscoderOutput());
		return transcoder.getImage();
	}
}
