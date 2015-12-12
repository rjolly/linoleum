package linoleum;

import java.awt.Container;
import java.awt.Cursor;
import java.io.IOException;
import java.net.URL;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;
import linoleum.html.EditorKit;

public class HtmlPane extends JScrollPane implements HyperlinkListener {

    JEditorPane html;

    @SuppressWarnings("LeakingThisInConstructor")
    public HtmlPane() {
        html = new JEditorPane();
        html.setEditorKitForContentType("text/html", new EditorKit());
        html.setEditable(false);
        html.addHyperlinkListener(this);
        html.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        JViewport vp = getViewport();
        vp.add(html);
    }

    /**
     * Notification of a change relative to a
     * hyperlink.
     */
    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            linkActivated(e.getURL());
        }
    }

    /**
     * Follows the reference in an
     * link.  The given url is the requested reference.
     * By default this calls <a href="#setPage">setPage</a>,
     * and if an exception is thrown the original previous
     * document is restored and a beep sounded.  If an
     * attempt was made to follow a link, but it represented
     * a malformed url, this method will be called with a
     * null argument.
     *
     * @param u the URL to follow
     */
    protected void linkActivated(URL u) {
        Cursor c = html.getCursor();
        Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
        html.setCursor(waitCursor);
        SwingUtilities.invokeLater(new PageLoader(u, c));
    }


    /**
     * temporary class that loads synchronously (although
     * later than the request so that a cursor change
     * can be done).
     */
    class PageLoader implements Runnable {

        PageLoader(URL u, Cursor c) {
            url = u;
            cursor = c;
        }

        public void run() {
            if (url == null) {
                // restore the original cursor
                html.setCursor(cursor);

                // PENDING(prinz) remove this hack when
                // automatic validation is activated.
                Container parent = html.getParent();
                parent.repaint();
            } else {
                Document doc = html.getDocument();
                try {
                    html.setPage(url);
                } catch (IOException ioe) {
                    html.setDocument(doc);
                    getToolkit().beep();
                } finally {
                    // schedule the cursor to revert after
                    // the paint has happended.
                    url = null;
                    SwingUtilities.invokeLater(this);
                }
            }
        }
        URL url;
        Cursor cursor;
    }
}
