package linoleum.mail;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedInputStream;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import javax.swing.text.BadLocationException;
import javax.swing.text.ChangedCharSetException;

class EditorPane extends JEditorPane {
    void read(InputStream in, Document doc) throws IOException {
        if (! Boolean.TRUE.equals(doc.getProperty("IgnoreCharsetDirective"))) {
            final int READ_LIMIT = 1024 * 10;
            in = new BufferedInputStream(in, READ_LIMIT);
            in.mark(READ_LIMIT);
        }
        String charset = (String) getClientProperty("charset");
        try(Reader r = (charset != null) ? new InputStreamReader(in, charset) :
                new InputStreamReader(in)) {
            try {
                getEditorKit().read(r, doc, 0);
            } catch (BadLocationException e) {
                throw new IOException(e.getMessage());
            } catch (ChangedCharSetException changedCharSetException) {
                String charSetSpec = changedCharSetException.getCharSetSpec();
                if (changedCharSetException.keyEqualsCharSet()) {
                    putClientProperty("charset", charSetSpec);
                } else {
                    setCharsetFromContentTypeParameters(charSetSpec);
                }
                try {
                    in.reset();
                } catch (IOException exception) {
                    //mark was invalidated
                    in.close();
                    URL url = (URL)doc.getProperty(Document.StreamDescriptionProperty);
                    if (url != null) {
                        URLConnection conn = url.openConnection();
                        in = conn.getInputStream();
                    } else {
                        //there is nothing we can do to recover stream
                        throw changedCharSetException;
                    }
                }
                try {
                    doc.remove(0, doc.getLength());
                } catch (BadLocationException e) {}
                doc.putProperty("IgnoreCharsetDirective", Boolean.valueOf(true));
                read(in, doc);
            }
        }
    }

    private void setCharsetFromContentTypeParameters(String paramlist) {
        String charset;
        try {
            // paramlist is handed to us with a leading ';', strip it.
            int semi = paramlist.indexOf(';');
            if (semi > -1 && semi < paramlist.length()-1) {
                paramlist = paramlist.substring(semi + 1);
            }

            if (paramlist.length() > 0) {
                // parse the paramlist into attr-value pairs & get the
                // charset pair's value
                HeaderParser hdrParser = new HeaderParser(paramlist);
                charset = hdrParser.findValue("charset");
                if (charset != null) {
                    putClientProperty("charset", charset);
                }
            }
        }
        catch (IndexOutOfBoundsException e) {
            // malformed parameter list, use charset we have
        }
        catch (NullPointerException e) {
            // malformed parameter list, use charset we have
        }
        catch (Exception e) {
            // malformed parameter list, use charset we have; but complain
            System.err.println("JEditorPane.getCharsetFromContentTypeParameters failed on: " + paramlist);
            e.printStackTrace();
        }
    }
}
