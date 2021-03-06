package linoleum.xhtml;

import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.ImageIcon;
import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.swing.SwingReplacedElementFactory;
import linoleum.application.Frame;

public class FlyingSaucer extends Frame {
	private final ChainedReplacedElementFactory cef = new ChainedReplacedElementFactory();

	static {
		System.setProperty("xr.css.user-agent-default-css", "/linoleum/xhtml/");
	}

	public FlyingSaucer() {
		initComponents();
		setIcon(new ImageIcon(getClass().getResource("/toolbarButtonGraphics/development/WebComponent24.gif")));
		setMimeType("text/html:application/xhtml+xml");
		cef.addFactory(new SwingReplacedElementFactory());
		cef.addFactory(new JEuclidReplacedElementFactory());
		cef.addFactory(new SVGBatikReplacedElementFactory());
		xHTMLPanel1.getSharedContext().setReplacedElementFactory(cef);
	}

	@Override
	public Frame getFrame() {
		return new FlyingSaucer();
	}

	@Override
	public void setURI(final URI uri) {
		xHTMLPanel1.setDocument(uri.toString());
	}

	@Override
	public URI getURI() {
		final String str = xHTMLPanel1.getSharedContext().getBaseURL();
		if (str != null) try {
			return new URI(str);
		} catch (final URISyntaxException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                fSScrollPane1 = new org.xhtmlrenderer.simple.FSScrollPane();
                xHTMLPanel1 = new org.xhtmlrenderer.simple.XHTMLPanel();

                setClosable(true);
                setIconifiable(true);
                setMaximizable(true);
                setResizable(true);
                setTitle("Flying Saucer");

                fSScrollPane1.setViewportView(xHTMLPanel1);

                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
                getContentPane().setLayout(layout);
                layout.setHorizontalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(fSScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 394, Short.MAX_VALUE)
                );
                layout.setVerticalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(fSScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE)
                );

                pack();
        }// </editor-fold>//GEN-END:initComponents


        // Variables declaration - do not modify//GEN-BEGIN:variables
        private org.xhtmlrenderer.simple.FSScrollPane fSScrollPane1;
        private org.xhtmlrenderer.simple.XHTMLPanel xHTMLPanel1;
        // End of variables declaration//GEN-END:variables
}
