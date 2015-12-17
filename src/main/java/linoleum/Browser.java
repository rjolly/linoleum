package linoleum;

import java.awt.CardLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.datatransfer.StringSelection;
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
import javax.swing.SwingWorker;
import linoleum.html.EditorKit;

public class Browser extends JInternalFrame {
	private final CardLayout layout;
	private URL url;

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

	protected void linkActivated(final URL url) {
		new PageLoader(url).execute();
	}

	private class PageLoader extends SwingWorker<Object, Object> {
		private final Cursor cursor = jEditorPane1.getCursor();
		private final URL url;

		PageLoader(final URL url) {
			this.url = url;
			layout.show(jPanel2, "progressBar");
			jEditorPane1.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		}

		public Object doInBackground() {
			final Document doc = jEditorPane1.getDocument();
			try {
				jEditorPane1.setPage(url);
			} catch (final IOException ioe) {
				jEditorPane1.setDocument(doc);
				getToolkit().beep();
			}
			return doc;
		}

		public void done() {
			// restore the original cursor
			jEditorPane1.setCursor(cursor);
			layout.show(jPanel2, "label");
			// PENDING(prinz) remove this hack when
			// automatic validation is activated.
			final Container parent = jEditorPane1.getParent();
			parent.repaint();
		}
	}

	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                jPopupMenu1 = new javax.swing.JPopupMenu();
                jMenuItem1 = new javax.swing.JMenuItem();
                jPanel1 = new javax.swing.JPanel();
                jPanel3 = new javax.swing.JPanel();
                jButton2 = new javax.swing.JButton();
                jButton3 = new javax.swing.JButton();
                jTextField1 = new javax.swing.JTextField();
                jButton1 = new javax.swing.JButton();
                jScrollPane1 = new javax.swing.JScrollPane();
                jEditorPane1 = new javax.swing.JEditorPane();
                jPanel4 = new javax.swing.JPanel();
                jPanel2 = new javax.swing.JPanel();
                jLabel1 = new javax.swing.JLabel();
                jProgressBar1 = new javax.swing.JProgressBar();

                jMenuItem1.setText("Copy link location");
                jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jMenuItem1ActionPerformed(evt);
                        }
                });
                jPopupMenu1.add(jMenuItem1);

                setClosable(true);
                setIconifiable(true);
                setMaximizable(true);
                setResizable(true);
                setTitle("Browser");

                jPanel3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));

                jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/navigation/Back16.gif"))); // NOI18N
                jButton2.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jButton2ActionPerformed(evt);
                        }
                });
                jPanel3.add(jButton2);

                jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/navigation/Forward16.gif"))); // NOI18N
                jButton3.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jButton3ActionPerformed(evt);
                        }
                });
                jPanel3.add(jButton3);

                jTextField1.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jTextField1ActionPerformed(evt);
                        }
                });

                jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/media/Play16.gif"))); // NOI18N
                jButton1.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jButton1ActionPerformed(evt);
                        }
                });

                javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
                jPanel1.setLayout(jPanel1Layout);
                jPanel1Layout.setHorizontalGroup(
                        jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 252, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton1)
                                .addContainerGap())
                );
                jPanel1Layout.setVerticalGroup(
                        jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jButton1)
                                        .addComponent(jTextField1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                );

                jEditorPane1.setEditable(false);
                jEditorPane1.addHyperlinkListener(new javax.swing.event.HyperlinkListener() {
                        public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {
                                jEditorPane1HyperlinkUpdate(evt);
                        }
                });
                jScrollPane1.setViewportView(jEditorPane1);

                jPanel2.setLayout(new java.awt.CardLayout());
                jPanel2.add(jLabel1, "label");
                jPanel2.add(jProgressBar1, "progressBar");

                javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
                jPanel4.setLayout(jPanel4Layout);
                jPanel4Layout.setHorizontalGroup(
                        jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 372, Short.MAX_VALUE))
                );
                jPanel4Layout.setVerticalGroup(
                        jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 20, Short.MAX_VALUE)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                                        .addGap(0, 0, Short.MAX_VALUE)
                                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                );

                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
                getContentPane().setLayout(layout);
                layout.setHorizontalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                );
                layout.setVerticalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                );

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
		} else if (evt.getEventType() == HyperlinkEvent.EventType.ENTERED) {
			url = evt.getURL();
			jLabel1.setText(url.toString());
			jEditorPane1.setComponentPopupMenu(jPopupMenu1);
		} else if (evt.getEventType() == HyperlinkEvent.EventType.EXITED) {
			jEditorPane1.setComponentPopupMenu(null);
			jLabel1.setText("");
		}
        }//GEN-LAST:event_jEditorPane1HyperlinkUpdate

        private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        }//GEN-LAST:event_jButton2ActionPerformed

        private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        }//GEN-LAST:event_jButton3ActionPerformed

        private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
		final StringSelection selection = new StringSelection(url.toString());
		getToolkit().getSystemClipboard().setContents(selection, selection);
        }//GEN-LAST:event_jMenuItem1ActionPerformed

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JButton jButton1;
        private javax.swing.JButton jButton2;
        private javax.swing.JButton jButton3;
        private javax.swing.JEditorPane jEditorPane1;
        private javax.swing.JLabel jLabel1;
        private javax.swing.JMenuItem jMenuItem1;
        private javax.swing.JPanel jPanel1;
        private javax.swing.JPanel jPanel2;
        private javax.swing.JPanel jPanel3;
        private javax.swing.JPanel jPanel4;
        private javax.swing.JPopupMenu jPopupMenu1;
        private javax.swing.JProgressBar jProgressBar1;
        private javax.swing.JScrollPane jScrollPane1;
        private javax.swing.JTextField jTextField1;
        // End of variables declaration//GEN-END:variables
}
