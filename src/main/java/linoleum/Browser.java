package linoleum;

import java.awt.CardLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JInternalFrame;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.Document;
import linoleum.html.EditorKit;

public class Browser extends JInternalFrame {
	private final CardLayout layout;

	public static class Application implements linoleum.application.Application {
		public String getName() {
			return Browser.class.getSimpleName();
		}

		public ImageIcon getIcon() {
			return new ImageIcon(getClass().getResource("/toolbarButtonGraphics/development/WebComponent24.gif"));
		}

		public String getMimeType() {
			return "text/html";
		}

		public JInternalFrame open(final URI uri) {
			return new Browser(uri);
		}
	}

	public Browser(final URI uri) {
		initComponents();
		layout = (CardLayout)jPanel2.getLayout();
		jEditorPane1.setEditorKitForContentType("text/html", new EditorKit());
		jEditorPane1.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		try {
			if (uri != null) linkActivated(uri.toURL());
		} catch (final MalformedURLException ex) {
			throw new RuntimeException(ex);
		}
	}

	private void open(final String str) {
		try {
			linkActivated(new URL(str));
		} catch (final MalformedURLException ex) {
			throw new RuntimeException(ex);
		}
	}

	protected void linkActivated(final URL u) {
		layout.show(jPanel2, "progressBar");
		final Cursor c = jEditorPane1.getCursor();
		final Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
		jEditorPane1.setCursor(waitCursor);
		SwingUtilities.invokeLater(new PageLoader(u, c));
	}

	class PageLoader implements Runnable {
		private URL url;
		private final Cursor cursor;

		PageLoader(final URL u, final Cursor c) {
			url = u;
			cursor = c;
		}

		public void run() {
			if (url == null) {
				// restore the original cursor
				jEditorPane1.setCursor(cursor);
				layout.show(jPanel2, "label");
				// PENDING(prinz) remove this hack when
				// automatic validation is activated.
				final Container parent = jEditorPane1.getParent();
				parent.repaint();
			} else {
				final Document doc = jEditorPane1.getDocument();
				try {
					jEditorPane1.setPage(url);
				} catch (IOException ioe) {
					jEditorPane1.setDocument(doc);
					getToolkit().beep();
				} finally {
					// schedule the cursor to revert after
					// the paint has happended.
					url = null;
					SwingUtilities.invokeLater(this);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                jPanel1 = new javax.swing.JPanel();
                jTextField1 = new javax.swing.JTextField();
                jButton1 = new javax.swing.JButton();
                jScrollPane1 = new javax.swing.JScrollPane();
                jEditorPane1 = new javax.swing.JEditorPane();
                jPanel2 = new javax.swing.JPanel();
                jLabel1 = new javax.swing.JLabel();
                jProgressBar1 = new javax.swing.JProgressBar();

                setClosable(true);
                setIconifiable(true);
                setMaximizable(true);
                setResizable(true);
                setTitle("Browser");

                jTextField1.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jTextField1ActionPerformed(evt);
                        }
                });

                jButton1.setText("Go");
                jButton1.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jButton1ActionPerformed(evt);
                        }
                });

                jEditorPane1.setEditable(false);
                jEditorPane1.addHyperlinkListener(new javax.swing.event.HyperlinkListener() {
                        public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {
                                jEditorPane1HyperlinkUpdate(evt);
                        }
                });
                jScrollPane1.setViewportView(jEditorPane1);

                javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
                jPanel1.setLayout(jPanel1Layout);
                jPanel1Layout.setHorizontalGroup(
                        jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 311, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton1)
                                .addContainerGap())
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                );
                jPanel1Layout.setVerticalGroup(
                        jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jButton1))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE))
                );

                getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

                jPanel2.setLayout(new java.awt.CardLayout());
                jPanel2.add(jLabel1, "label");
                jPanel2.add(jProgressBar1, "progressBar");

                getContentPane().add(jPanel2, java.awt.BorderLayout.PAGE_END);

                pack();
        }// </editor-fold>//GEN-END:initComponents

        private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
		open(jTextField1.getText());
        }//GEN-LAST:event_jButton1ActionPerformed

        private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
		open(evt.getActionCommand());
        }//GEN-LAST:event_jTextField1ActionPerformed

        private void jEditorPane1HyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {//GEN-FIRST:event_jEditorPane1HyperlinkUpdate
		if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			linkActivated(evt.getURL());
		}
        }//GEN-LAST:event_jEditorPane1HyperlinkUpdate

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JButton jButton1;
        private javax.swing.JEditorPane jEditorPane1;
        private javax.swing.JLabel jLabel1;
        private javax.swing.JPanel jPanel1;
        private javax.swing.JPanel jPanel2;
        private javax.swing.JProgressBar jProgressBar1;
        private javax.swing.JScrollPane jScrollPane1;
        private javax.swing.JTextField jTextField1;
        // End of variables declaration//GEN-END:variables
}
