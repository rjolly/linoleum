package linoleum.html;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.Document;
import linoleum.application.App;
import linoleum.application.Frame;

public class Browser extends Frame {
	private final Icon goIcon = new ImageIcon(getClass().getResource("/linoleum/html/Go16.png"));
	private final Icon stopIcon = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Stop16.gif"));
	private final Icon reloadIcon = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Refresh16.gif"));
	private final DefaultComboBoxModel<Integer> model = new DefaultComboBoxModel<>(new Integer[] { 8, 10, 12, 14, 18, 24, 36 });
	private final PreferenceChangeListener listener = new PreferenceChangeListener() {
		@Override
		public void preferenceChange(final PreferenceChangeEvent evt) {
			if (evt.getKey().equals(getKey("fontSize"))) {
				resize();
			}
		}
	};
	private final Preferences prefs = Preferences.userNodeForPackage(getClass());
	private final Action copyLinkLocationAction = new CopyLinkLocationAction();
	private List<FrameURL> history = new ArrayList<>();
	private PageLoader loader;
	private FrameURL previous;
	private FrameURL current;
	private boolean reload;
	private boolean open;
	private int delta;
	private int index;
	private URL url;

	private class CopyLinkLocationAction extends AbstractAction {
		public CopyLinkLocationAction() {
			super("Copy link location");
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final StringSelection selection = new StringSelection(url.toString());
			getToolkit().getSystemClipboard().setContents(selection, selection);
		}
	}

	public Browser() {
		initComponents();
		setIcon(new ImageIcon(getClass().getResource("/toolbarButtonGraphics/development/WebComponent24.gif")));
		setMimeType("text/html");
		setScheme("http:https");
		resize();
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
				if (loader == null && current != null) {
					reload = jTextField1.getText().equals(current.getURL().toString());
					jButton1.setIcon(reload?reloadIcon:goIcon);
				}
			}
		});
		update();
		setURL(getHome());
	}

	private void setURL(final String str) {
		try {
			setURL(new URL(str));
		} catch (final MalformedURLException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public Component getFocusOwner() {
		return current == null?jTextField1:jEditorPane1;
	}

	public JEditorPane getEditorPane() {
		return jEditorPane1;
	}

	private void open(final String str) {
		if (!reload && !str.isEmpty()) {
			setURL(str);
		}
		open();
	}

	@Override
	public void load() {
		jTextField2.setText(getHome());
		jComboBox1.setSelectedItem(getFontSize());
	}

	private String getHome() {
		return prefs.get(getKey("home"), "file:");
	}

	private int getFontSize() {
		return prefs.getInt(getKey("fontSize"), jEditorPane1.getFont().getSize());
	}

	private void resize() {
		jEditorPane1.setFont(jEditorPane1.getFont().deriveFont((float) getFontSize()));
	}

	@Override
	public void save() {
		prefs.put(getKey("home"), jTextField2.getText());
		prefs.putInt(getKey("fontSize"), (Integer) jComboBox1.getSelectedItem());
	}

	@Override
	public Frame getFrame() {
		return new Browser();
	}

	@Override
	public void setURI(final URI uri) {
		try {
			setURL(uri.toURL());
		} catch (final MalformedURLException ex) {
			ex.printStackTrace();
		}
	}

	private void setURL(final URL url) {
		delta = 1;
		setURL(new FrameURL(url));
	}

	private void setURL(final FrameURL dest) {
		reload = false;
		current = dest;
	}

	private void setURL(final int delta) {
		this.delta = delta;
		setURL(history.get(index + delta));
	}

	@Override
	public URI getURI() {
		if (current != null) try {
			return current.getURL().toURI();
		} catch (final URISyntaxException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	@Override
	public void open() {
		if (!open) {
			prefs.addPreferenceChangeListener(listener);
			open = true;
		}
		final URI uri = getURI();
		if (uri != null) {
			final String scheme = uri.getScheme();
			final MimeType type = getMimeType(uri);
			if (type == null?canOpen(scheme):canOpen(type)) {
				doOpen();
			} else if (canOpen(scheme)) {
				getApplicationManager().open(uri, type);
			} else {
				getApplicationManager().open(uri);
			}
		}
	}

	@Override
	public void close() {
		prefs.removePreferenceChangeListener(listener);
	}

	private void linkActivated(final HyperlinkEvent evt) {
		delta = 1;
		setURL(FrameURL.create(current, evt));
		open();
	}

	@Override
	public MimeType getMimeType(final URI uri) {
		if (canOpen(uri.getScheme())) try {
			final URLConnection conn = uri.toURL().openConnection();
			if (conn instanceof HttpURLConnection) {
				final HttpURLConnection hconn = (HttpURLConnection) conn;
				hconn.setInstanceFollowRedirects(false);
				final int response = hconn.getResponseCode();
				if (response >= 300 && response <= 399) {
					final String loc = conn.getHeaderField("Location");
					return getMimeType(uri.resolve(loc));
				}
			}
			final String str = conn.getContentType();
			if (str != null) {
				return new MimeType(str);
			}
		} catch (final MimeTypeParseException | IOException ex) {
			ex.printStackTrace();
		}
		return super.getMimeType(uri);
	}

	private boolean reuseFor(final URL that) {
		if (current != null) {
			return that == null?false:current.getURL().sameFile(that);
		}
		return true;
	}

	@Override
	public boolean reuseFor(final URI that) {
		try {
			return reuseFor(that == null?new URL(getHome()):that.toURL());
		} catch (final MalformedURLException ex) {
			ex.printStackTrace();
		}
		return false;
	}

	private void doOpen() {
		if (loader != null) {
			loader.cancel(true);
		}
		if (loader == null) {
			loader = new PageLoader();
			jEditorPane1.setLoader(loader);
			loader.execute();
		}
	}

	private class PageLoader extends linoleum.html.PageLoader {
		private final CardLayout layout = (CardLayout) jPanel2.getLayout();
		private final Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
		private final Cursor cursor = jEditorPane1.getCursor();

		PageLoader() {
			addPropertyChangeListener(new PropertyChangeListener() {
				public  void propertyChange(final PropertyChangeEvent evt) {
					if ("progress".equals(evt.getPropertyName())) {
						jProgressBar1.setValue((Integer) evt.getNewValue());
					}
				}
			});
			jButton1.setIcon(stopIcon);
			layout.show(jPanel2, "progressBar");
			jEditorPane1.setCursor(waitCursor);
		}

		public Boolean doInBackground() throws IOException {
			jEditorPane1.setPage(current, reload);
			return null;
		}

		public void done() {
			try {
				get();
				jTextField1.setText(current.getURL().toString());
				setTitle((String) jEditorPane1.getDocument().getProperty(Document.TitleProperty));
				if (previous != null) {
					record();
				}
				previous = current;
				reload = true;
			} catch (final InterruptedException e) {
				e.printStackTrace();
			} catch (final ExecutionException e) {
				e.printStackTrace();
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

	private void record() {
		if (index == history.size()) {
			history.add(previous);
		}
		if (!current.equals(history.get(index))) {
			index += delta;
		}
		if (index < history.size() && !current.equals(history.get(index))) {
			history = new ArrayList<>(history.subList(0, index));
		}
		update();
	}

	private void update() {
		jButton2.setEnabled(index > 0);
		jButton3.setEnabled(index < history.size() - 1);
	}

	private void prepare() {
		jPopupMenu1.removeAll();
		boolean sep0 = false;
		if (url != null) try {
			sep0 = getApplicationManager().populate(url.toURI(), jPopupMenu1);
		} catch (final URISyntaxException ex) {
			ex.printStackTrace();
		}
		if (sep0) {
			jPopupMenu1.addSeparator();
		}
		jPopupMenu1.add(copyLinkLocationAction);
	}

	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                jPopupMenu1 = new javax.swing.JPopupMenu();
                optionPanel1 = new linoleum.application.OptionPanel();
                jLabel2 = new javax.swing.JLabel();
                jTextField2 = new javax.swing.JTextField();
                jLabel3 = new javax.swing.JLabel();
                jComboBox1 = new javax.swing.JComboBox();
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

                jPopupMenu1.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
                        public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
                        }
                        public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                        }
                        public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                                jPopupMenu1PopupMenuWillBecomeVisible(evt);
                        }
                });

                jLabel2.setText("Home page :");

                jLabel3.setText("Font size :");

                jComboBox1.setModel(model);
                jComboBox1.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jComboBox1ActionPerformed(evt);
                        }
                });

                javax.swing.GroupLayout optionPanel1Layout = new javax.swing.GroupLayout(optionPanel1);
                optionPanel1.setLayout(optionPanel1Layout);
                optionPanel1Layout.setHorizontalGroup(
                        optionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(optionPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(optionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jLabel3)
                                        .addComponent(jLabel2))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(optionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jTextField2, javax.swing.GroupLayout.DEFAULT_SIZE, 283, Short.MAX_VALUE)
                                        .addGroup(optionPanel1Layout.createSequentialGroup()
                                                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap())
                );
                optionPanel1Layout.setVerticalGroup(
                        optionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(optionPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(optionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel2)
                                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(optionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel3)
                                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );

                setClosable(true);
                setIconifiable(true);
                setMaximizable(true);
                setResizable(true);
                setTitle("Browser");
                setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/development/WebComponent16.gif"))); // NOI18N
                setOptionPanel(optionPanel1);

                jPanel3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));

                jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/linoleum/html/Back16.png"))); // NOI18N
                jButton2.setPreferredSize(new java.awt.Dimension(28, 28));
                jButton2.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jButton2ActionPerformed(evt);
                        }
                });
                jPanel3.add(jButton2);

                jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/linoleum/html/Forward16.png"))); // NOI18N
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

                jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/linoleum/html/Go16.png"))); // NOI18N
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
		if (jPopupMenu1.isShowing()) {
			return;
		}
		if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			if (url != null) {
				linkActivated(evt);
			}
		} else if (evt.getEventType() == HyperlinkEvent.EventType.ENTERED) {
			((JEditorPane) evt.getSource()).setComponentPopupMenu(jPopupMenu1);
			url = evt.getURL();
			if (url != null) {
				jLabel1.setText(url.toString());
			}
		} else if (evt.getEventType() == HyperlinkEvent.EventType.EXITED) {
			((JEditorPane) evt.getSource()).setComponentPopupMenu(null);
			url = null;
			jLabel1.setText("");
		}
        }//GEN-LAST:event_jEditorPane1HyperlinkUpdate

        private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
		setURL(-1);
		doOpen();
        }//GEN-LAST:event_jButton2ActionPerformed

        private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
		setURL(1);
		doOpen();
        }//GEN-LAST:event_jButton3ActionPerformed

        private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
		optionPanel1.setDirty(true);
        }//GEN-LAST:event_jComboBox1ActionPerformed

        private void jPopupMenu1PopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_jPopupMenu1PopupMenuWillBecomeVisible
		prepare();
        }//GEN-LAST:event_jPopupMenu1PopupMenuWillBecomeVisible

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JButton jButton1;
        private javax.swing.JButton jButton2;
        private javax.swing.JButton jButton3;
        private javax.swing.JComboBox jComboBox1;
        private linoleum.html.EditorPane jEditorPane1;
        private javax.swing.JLabel jLabel1;
        private javax.swing.JLabel jLabel2;
        private javax.swing.JLabel jLabel3;
        private javax.swing.JPanel jPanel2;
        private javax.swing.JPanel jPanel3;
        private javax.swing.JPopupMenu jPopupMenu1;
        private javax.swing.JProgressBar jProgressBar1;
        private javax.swing.JScrollPane jScrollPane1;
        private javax.swing.JTextField jTextField1;
        private javax.swing.JTextField jTextField2;
        private linoleum.application.OptionPanel optionPanel1;
        // End of variables declaration//GEN-END:variables
}
