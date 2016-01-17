package linoleum;

import java.awt.CardLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.datatransfer.StringSelection;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JInternalFrame;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.Document;
import linoleum.html.EditorKit;
import linoleum.html.FrameURL;

public class Browser extends JInternalFrame {
	private final Icon goIcon = new javax.swing.ImageIcon(getClass().getResource("Go16.png"));
	private final Icon stopIcon = new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Stop16.gif"));
	private final Icon reloadIcon = new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Refresh16.gif"));
	private List<FrameURL> history = new ArrayList<>();
	private final CardLayout layout;
	private PageLoader loader;
	private FrameURL current;
	private boolean reload;
	private int index;
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
		update();
		layout = (CardLayout)jPanel2.getLayout();
		jEditorPane1.setEditorKitForContentType("text/html", new EditorKit());
		jEditorPane1.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		jTextField1.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void insertUpdate(final DocumentEvent e) {
				update();
			}

			@Override
			public void removeUpdate(final DocumentEvent e) {
				update();
			}

			@Override
			public void changedUpdate(final DocumentEvent e) {
				update();
			}

			private void update() {
				if (loader == null) {
					reload = false;
					jButton1.setIcon(goIcon);
				}
			}
		});
		try {
			if (uri != null) open(uri.toURL());
		} catch (final MalformedURLException ex) {
			throw new RuntimeException(ex);
		}
	}

	private void open(final String str) {
		try {
			open(new URL(str));
		} catch (final MalformedURLException ex) {
			throw new RuntimeException(ex);
		}
	}

	private void open(final URL url) {
		open(reload?current:new FrameURL(url), 1, reload);
	}

	private void linkActivated(final HyperlinkEvent evt) {
		open(FrameURL.create(current, evt), 1, false);
	}

	private void open(final int delta) {
		open(history.get(index + delta), delta, false);
	}

	private void open(final FrameURL url, final int delta, final boolean force) {
		if (loader != null) {
			loader.cancel(true);
		}
		if (loader == null) {
			loader = new PageLoader(url, delta, force);
			loader.execute();
		}
	}

	private class PageLoader extends linoleum.html.PageLoader {
		private final Cursor cursor = jEditorPane1.getCursor();
		private final FrameURL dest;
		private final boolean force;
		private final int delta;
		private boolean success;

		PageLoader(final FrameURL dest, final int delta, final boolean force) {
			this.dest = dest;
			this.delta = delta;
			this.force = force;
			addPropertyChangeListener(new PropertyChangeListener() {
				public  void propertyChange(final PropertyChangeEvent evt) {
					if ("progress".equals(evt.getPropertyName())) {
						jProgressBar1.setValue((Integer)evt.getNewValue());
					}
				}
			});
			jButton1.setIcon(stopIcon);
			layout.show(jPanel2, "progressBar");
			jEditorPane1.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		}

		public URL doInBackground() {
			success = false;
			try {
				jEditorPane1.setPage(dest, this, force);
				success = true;
			} catch (final IOException ioe) {
				getToolkit().beep();
			}
			return jEditorPane1.getPage();
		}

		public void done() {
			if (success) {
				jTextField1.setText(dest.getURL().toString());
				setTitle((String)jEditorPane1.getDocument().getProperty(Document.TitleProperty));
				if (current != null) {
					record(dest, delta);
				}
				current = dest;
				reload = true;
			}
			// restore the original cursor
			jEditorPane1.setCursor(cursor);
			layout.show(jPanel2, "label");
			jProgressBar1.setValue(0);
			jButton1.setIcon(reload?reloadIcon:goIcon);
			// PENDING(prinz) remove this hack when
			// automatic validation is activated.
			final Container parent = jEditorPane1.getParent();
			parent.repaint();
			loader = null;
		}
	}

	private void record(final FrameURL dest, final int delta) {
		if (index == history.size()) {
			history.add(current);
		}
		if (!dest.equals(history.get(index))) {
			index += delta;
		}
		if (index < history.size() && !dest.equals(history.get(index))) {
			history = new ArrayList<>(history.subList(0, index));
		}
		update();
	}

	private void update() {
		jButton2.setEnabled(index > 0);
		jButton3.setEnabled(index < history.size() - 1);
	}

	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                jPopupMenu1 = new javax.swing.JPopupMenu();
                jMenuItem1 = new javax.swing.JMenuItem();
                jPanel3 = new javax.swing.JPanel();
                jButton2 = new javax.swing.JButton();
                jButton3 = new javax.swing.JButton();
                jTextField1 = new javax.swing.JTextField();
                jButton1 = new javax.swing.JButton();
                jScrollPane1 = new javax.swing.JScrollPane();
                jEditorPane1 = new linoleum.html.EditorPane();
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

                jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/linoleum/Back16.png"))); // NOI18N
                jButton2.setPreferredSize(new java.awt.Dimension(28, 28));
                jButton2.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jButton2ActionPerformed(evt);
                        }
                });
                jPanel3.add(jButton2);

                jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/linoleum/Forward16.png"))); // NOI18N
                jButton3.setPreferredSize(new java.awt.Dimension(28, 28));
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

                jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/linoleum/Go16.png"))); // NOI18N
                jButton1.setPreferredSize(new java.awt.Dimension(28, 28));
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

                jPanel2.setLayout(new java.awt.CardLayout());
                jPanel2.add(jLabel1, "label");
                jPanel2.add(jProgressBar1, "progressBar");

                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
                getContentPane().setLayout(layout);
                layout.setHorizontalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 394, Short.MAX_VALUE)
                );
                layout.setVerticalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(9, 9, 9)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                );

                pack();
        }// </editor-fold>//GEN-END:initComponents

        private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
		if (loader == null) {
			open(jTextField1.getText());
		} else {
			loader.cancel(true);
		}
        }//GEN-LAST:event_jButton1ActionPerformed

        private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
		open(evt.getActionCommand());
        }//GEN-LAST:event_jTextField1ActionPerformed

        private void jEditorPane1HyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {//GEN-FIRST:event_jEditorPane1HyperlinkUpdate
		if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			linkActivated(evt);
		} else if (evt.getEventType() == HyperlinkEvent.EventType.ENTERED) {
			url = evt.getURL();
			jLabel1.setText(url.toString());
			((JEditorPane)evt.getSource()).setComponentPopupMenu(jPopupMenu1);
		} else if (evt.getEventType() == HyperlinkEvent.EventType.EXITED) {
			((JEditorPane)evt.getSource()).setComponentPopupMenu(null);
			jLabel1.setText("");
		}
        }//GEN-LAST:event_jEditorPane1HyperlinkUpdate

        private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
		open(-1);
        }//GEN-LAST:event_jButton2ActionPerformed

        private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
		open(1);
        }//GEN-LAST:event_jButton3ActionPerformed

        private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
		final StringSelection selection = new StringSelection(url.toString());
		getToolkit().getSystemClipboard().setContents(selection, selection);
        }//GEN-LAST:event_jMenuItem1ActionPerformed

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JButton jButton1;
        private javax.swing.JButton jButton2;
        private javax.swing.JButton jButton3;
        private linoleum.html.EditorPane jEditorPane1;
        private javax.swing.JLabel jLabel1;
        private javax.swing.JMenuItem jMenuItem1;
        private javax.swing.JPanel jPanel2;
        private javax.swing.JPanel jPanel3;
        private javax.swing.JPopupMenu jPopupMenu1;
        private javax.swing.JProgressBar jProgressBar1;
        private javax.swing.JScrollPane jScrollPane1;
        private javax.swing.JTextField jTextField1;
        // End of variables declaration//GEN-END:variables
}
