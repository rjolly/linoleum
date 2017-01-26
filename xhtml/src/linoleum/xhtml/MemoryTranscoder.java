package linoleum.xhtml;

import java.awt.Image;
import java.awt.image.BufferedImage;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;

public class MemoryTranscoder extends ImageTranscoder {
	private Image image;

	public BufferedImage createImage(final int width, final int height) {
		return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}

	public void writeImage(final BufferedImage image, final TranscoderOutput output) {
		this.image = image;
	}

	public Image getImage() {
		return image;
	}
}
