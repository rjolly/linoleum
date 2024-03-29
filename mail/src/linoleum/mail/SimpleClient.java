package linoleum.mail;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.HashMap;
import java.util.prefs.Preferences;
import java.util.prefs.PreferenceChangeEvent;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import linoleum.application.FileChooser;
import linoleum.application.PreferenceSupport;

public class SimpleClient extends PreferenceSupport {
	private final Icon composeIcon = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/ComposeMail16.gif"));
	private final Action composeAction = new ComposeAction();
	private final Action expungeAction = new ExpungeAction();
	private final Action settingsAction = new SettingsAction();
	private final Action removeAction = new RemoveAction();
	private final Preferences prefs = Preferences.userNodeForPackage(getClass());
	private final DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
	private final DefaultTreeModel model = new DefaultTreeModel(root);
	private final Map<URLName, StoreTreeNode> map = new HashMap<>();
	private final Session session = Session.getInstance(System.getProperties(), new SimpleAuthenticator(this));
	private final FileChooser chooser = new FileChooser();
	private Folder folder;
	private Store store;

	private class ComposeAction extends AbstractAction {
		public ComposeAction() {
			super("Compose", composeIcon);
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			getApplicationManager().get(Compose.class).open(getDesktopPane());
		}
	}

	private class ExpungeAction extends AbstractAction {
		public ExpungeAction() {
			super("Expunge");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK));
			setEnabled(false);
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			(new SwingWorker<Object, Object>() {
				public Object doInBackground() throws MessagingException  {
					folder.expunge();
					return null;
				}

				public void done() {
					try {
						get();
						folderViewer.setFolder(folder);
					} catch (final Exception e) {
						e.printStackTrace();
					}
				}
			}).execute();
		}
	}

	private class SettingsAction extends AbstractAction {
		public SettingsAction() {
			super("Settings");
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			try {
				getApplicationManager().open(new URI("prefs", getName(), null));
			} catch (final URISyntaxException ex) {
				ex.printStackTrace();
			}
		}
	}

	private class RemoveAction extends AbstractAction {
		public RemoveAction() {
			super("Remove");
			setEnabled(false);
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final URLName name = store.getURLName();
			final StoreTreeNode storenode = map.get(name);
			model.removeNodeFromParent(storenode);
			map.remove(name);
			setEnabled(false);
		}
	}

	SimpleClient getDialogParent() {
		if (!isShowing()) {
			open(getApplicationManager().getDesktopPane());
		}
		return this;
	}

	public SimpleClient() {
		initComponents();
		setDescription("mail");
		setScheme("imap:imaps:mstor");
		setIcon(new ImageIcon(getClass().getResource("Mail24.png")));
		session.getProperties().put("mail.smtps.auth", "true");
		session.getProperties().put("mail.mime.decodefilename", "true");
		session.getProperties().put("mstor.mbox.metadataStrategy", "none");
		refresh();
	}

	private void refresh() {
		session.setDebug(getDebug());
	}

	public Session getSession() {
		return session;
	}

	public boolean getDebug() {
		return getBooleanPref("debug");
	}

	public String getFrom() {
		return getPref("from");
	}

	public String getURL() {
		return getPref("url");
	}

	public String getRecord() {
		return getPref("record");
	}

	public String getTransport() {
		return getPref("transport");
	}

	void compose(final String str) throws URISyntaxException {
		getApplicationManager().open(new URI("mailto", str, null));
	}

	public FolderViewer getFolderViewer() {
		return folderViewer;
	}

	public MessageViewer getMessageViewer() {
		return messageViewer;
	}

	FileChooser getFileChooser() {
		return chooser;
	}

	@Override
	public void load() {
		jTextField1.setText(getPref("url"));
		jTextField2.setText(getPref("transport"));
		jTextField3.setText(getPref("from"));
		jTextField4.setText(getPref("record"));
		jCheckBox1.setSelected(getBooleanPref("debug"));
	}

	@Override
	public void save() {
		putPref("url", jTextField1.getText());
		putPref("transport", jTextField2.getText());
		putPref("from", jTextField3.getText());
		putPref("record", jTextField4.getText());
		putBooleanPref("debug", jCheckBox1.isSelected());
	}

	@Override
	public void init() {
		prefs.addPreferenceChangeListener(this);
	}

	@Override
	public void preferenceChange(final PreferenceChangeEvent evt) {
		if (evt.getKey().equals(getKey("url"))) {
			open();
		} else if (evt.getKey().equals(getKey("debug"))) {
			refresh();
		}
	}

	@Override
	public void open() {
		final URI uri = getURI();
		if (uri != null) {
			open(uri.toString());
		} else {
			final String str = getURL();
			if (!str.isEmpty()) {
				open(str);
			}
		}
	}

	@Override
	public void close() {
		setURI(null);
	}

	private void open(final String str) {
		final URLName name = new URLName(str);
		(new SwingWorker<StoreTreeNode, Object>() {
			public StoreTreeNode doInBackground() throws NoSuchProviderException {
				StoreTreeNode storenode = map.get(name);
				if (storenode == null) {
					final Store store = session.getStore(name);
					map.put(store.getURLName(), storenode = new StoreTreeNode(store));
					model.insertNodeInto(storenode, root, root.getChildCount());
				}
				return storenode;
			}

			@Override
			protected void done() {
				try {
					jTree1.scrollPathToVisible(new TreePath(get().getPath()));
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		}).execute();
	}

	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                optionPanel1 = new linoleum.application.OptionPanel();
                jLabel1 = new javax.swing.JLabel();
                jTextField1 = new javax.swing.JTextField();
                jLabel2 = new javax.swing.JLabel();
                jTextField2 = new javax.swing.JTextField();
                jLabel3 = new javax.swing.JLabel();
                jTextField3 = new javax.swing.JTextField();
                jLabel4 = new javax.swing.JLabel();
                jTextField4 = new javax.swing.JTextField();
                jCheckBox1 = new javax.swing.JCheckBox();
                jSplitPane1 = new javax.swing.JSplitPane();
                jSplitPane2 = new javax.swing.JSplitPane();
                jScrollPane1 = new javax.swing.JScrollPane();
                jTree1 = new javax.swing.JTree();
                folderViewer = new linoleum.mail.FolderViewer();
                messageViewer = new linoleum.mail.MessageViewer();
                jMenuBar1 = new javax.swing.JMenuBar();
                jMenu1 = new javax.swing.JMenu();
                jMenuItem1 = new javax.swing.JMenuItem();
                jMenuItem2 = new javax.swing.JMenuItem();
                jMenuItem3 = new javax.swing.JMenuItem();
                jMenuItem4 = new javax.swing.JMenuItem();
                jSeparator1 = new javax.swing.JPopupMenu.Separator();
                jMenuItem5 = new javax.swing.JMenuItem();
                jMenu2 = new javax.swing.JMenu();
                jMenuItem6 = new javax.swing.JMenuItem();
                jMenu3 = new javax.swing.JMenu();
                jMenuItem7 = new javax.swing.JMenuItem();
                jSeparator2 = new javax.swing.JPopupMenu.Separator();
                jMenuItem8 = new javax.swing.JMenuItem();

                jLabel1.setText("URL :");

                jLabel2.setText("Transport :");

                jLabel3.setText("From :");

                jLabel4.setText("Record :");

                jCheckBox1.setText("Debug");
                jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jCheckBox1ActionPerformed(evt);
                        }
                });

                javax.swing.GroupLayout optionPanel1Layout = new javax.swing.GroupLayout(optionPanel1);
                optionPanel1.setLayout(optionPanel1Layout);
                optionPanel1Layout.setHorizontalGroup(
                        optionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(optionPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(optionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jLabel2)
                                        .addComponent(jLabel1)
                                        .addComponent(jLabel3)
                                        .addComponent(jLabel4))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(optionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(optionPanel1Layout.createSequentialGroup()
                                                .addComponent(jCheckBox1)
                                                .addGap(0, 0, Short.MAX_VALUE))
                                        .addComponent(jTextField4)
                                        .addComponent(jTextField3)
                                        .addComponent(jTextField1)
                                        .addComponent(jTextField2, javax.swing.GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE))
                                .addContainerGap())
                );
                optionPanel1Layout.setVerticalGroup(
                        optionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(optionPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(optionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel1)
                                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(optionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel2)
                                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(optionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel3)
                                        .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(optionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel4)
                                        .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jCheckBox1)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );

                setClosable(true);
                setIconifiable(true);
                setMaximizable(true);
                setResizable(true);
                setTitle("Simple JavaMail Client");
                setName("Mail"); // NOI18N
                setOptionPanel(optionPanel1);
                addInternalFrameListener(new javax.swing.event.InternalFrameListener() {
                        public void internalFrameActivated(javax.swing.event.InternalFrameEvent evt) {
                        }
                        public void internalFrameClosed(javax.swing.event.InternalFrameEvent evt) {
                        }
                        public void internalFrameClosing(javax.swing.event.InternalFrameEvent evt) {
                        }
                        public void internalFrameDeactivated(javax.swing.event.InternalFrameEvent evt) {
                        }
                        public void internalFrameDeiconified(javax.swing.event.InternalFrameEvent evt) {
                        }
                        public void internalFrameIconified(javax.swing.event.InternalFrameEvent evt) {
                        }
                        public void internalFrameOpened(javax.swing.event.InternalFrameEvent evt) {
                                formInternalFrameOpened(evt);
                        }
                });

                jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
                jSplitPane1.setResizeWeight(0.3);
                jSplitPane1.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
                        public void propertyChange(java.beans.PropertyChangeEvent evt) {
                                jSplitPane1PropertyChange(evt);
                        }
                });

                jSplitPane2.setResizeWeight(0.5);
                jSplitPane2.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
                        public void propertyChange(java.beans.PropertyChangeEvent evt) {
                                jSplitPane2PropertyChange(evt);
                        }
                });

                jTree1.setModel(model);
                jTree1.addTreeWillExpandListener(new javax.swing.event.TreeWillExpandListener() {
                        public void treeWillCollapse(javax.swing.event.TreeExpansionEvent evt)throws javax.swing.tree.ExpandVetoException {
                                jTree1TreeWillCollapse(evt);
                        }
                        public void treeWillExpand(javax.swing.event.TreeExpansionEvent evt)throws javax.swing.tree.ExpandVetoException {
                                jTree1TreeWillExpand(evt);
                        }
                });
                jTree1.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
                        public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                                jTree1ValueChanged(evt);
                        }
                });
                jScrollPane1.setViewportView(jTree1);

                jSplitPane2.setLeftComponent(jScrollPane1);
                jSplitPane2.setRightComponent(folderViewer);

                jSplitPane1.setTopComponent(jSplitPane2);
                jSplitPane1.setBottomComponent(messageViewer);

                jMenu1.setText("Message");

                jMenuItem1.setAction(composeAction);
                jMenu1.add(jMenuItem1);

                jMenuItem2.setAction(messageViewer.getReplyAction());
                jMenu1.add(jMenuItem2);

                jMenuItem3.setAction(messageViewer.getReplyToAllAction());
                jMenu1.add(jMenuItem3);

                jMenuItem4.setAction(messageViewer.getForwardAction());
                jMenu1.add(jMenuItem4);
                jMenu1.add(jSeparator1);

                jMenuItem5.setAction(messageViewer.getDeleteAction());
                jMenu1.add(jMenuItem5);

                jMenuBar1.add(jMenu1);

                jMenu2.setText("Folder");

                jMenuItem6.setAction(expungeAction);
                jMenu2.add(jMenuItem6);

                jMenuBar1.add(jMenu2);

                jMenu3.setText("Account");

                jMenuItem7.setAction(settingsAction);
                jMenu3.add(jMenuItem7);
                jMenu3.add(jSeparator2);

                jMenuItem8.setAction(removeAction);
                jMenu3.add(jMenuItem8);

                jMenuBar1.add(jMenu3);

                setJMenuBar(jMenuBar1);

                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
                getContentPane().setLayout(layout);
                layout.setHorizontalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 415, Short.MAX_VALUE)
                );
                layout.setVerticalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 329, Short.MAX_VALUE)
                );

                pack();
        }// </editor-fold>//GEN-END:initComponents

        private void jTree1ValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_jTree1ValueChanged
		final TreePath path = evt.getNewLeadSelectionPath();
		if (path != null) {
			final Object o = path.getLastPathComponent();
			if (o instanceof FolderTreeNode) {
				folder = ((FolderTreeNode) o).getFolder();
				expungeAction.setEnabled(true);
				try {
					if ((folder.getType() & Folder.HOLDS_MESSAGES) != 0) {
						folderViewer.setFolder(folder);
					}
				} catch (final MessagingException me) {
					me.printStackTrace();
				}
			} else {
				expungeAction.setEnabled(false);
			}
			if (o instanceof StoreTreeNode) {
				store = ((StoreTreeNode) o).getStore();
				removeAction.setEnabled(!store.isConnected());
			} else {
				removeAction.setEnabled(false);
			}
		}
        }//GEN-LAST:event_jTree1ValueChanged

        private void jTree1TreeWillExpand(javax.swing.event.TreeExpansionEvent evt)throws javax.swing.tree.ExpandVetoException {//GEN-FIRST:event_jTree1TreeWillExpand
		final TreePath path = evt.getPath();
		if (path != null) {
			final Object o = path.getLastPathComponent();
			if (o instanceof StoreTreeNode) {
				(new SwingWorker<Object, Object>() {
					public Object doInBackground() throws MessagingException  {
						((StoreTreeNode) o).open(model);
						return null;
					}

					public void done() {
						try {
							get();
							removeAction.setEnabled(false);
						} catch (final Exception e) {
							e.printStackTrace();
						}
					}
				}).execute();
			}
		}
        }//GEN-LAST:event_jTree1TreeWillExpand

        private void jTree1TreeWillCollapse(javax.swing.event.TreeExpansionEvent evt)throws javax.swing.tree.ExpandVetoException {//GEN-FIRST:event_jTree1TreeWillCollapse
		final TreePath path = evt.getPath();
		if (path != null) {
			final Object o = path.getLastPathComponent();
			if (o instanceof StoreTreeNode) {
				(new SwingWorker<Object, Object>() {
					public Object doInBackground() throws MessagingException {
						folderViewer.setFolder(null);
						((StoreTreeNode) o).close();
						return null;
					}

					public void done() {
						try {
							get();
							removeAction.setEnabled(true);
						} catch (final Exception e) {
							e.printStackTrace();
						}
					}
				}).execute();
			}
		}
        }//GEN-LAST:event_jTree1TreeWillCollapse

        private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
		optionPanel1.setDirty(true);
        }//GEN-LAST:event_jCheckBox1ActionPerformed

        private void jSplitPane1PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jSplitPane1PropertyChange
		if (isShowing() && !isMaximum() && isRecording() && JSplitPane.DIVIDER_LOCATION_PROPERTY.equals(evt.getPropertyName())) {
			prefs.putInt(getKey("horizontalDividerLocation"), jSplitPane1.getDividerLocation());
		}
        }//GEN-LAST:event_jSplitPane1PropertyChange

        private void jSplitPane2PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jSplitPane2PropertyChange
		if (isShowing() && !isMaximum() && isRecording() && JSplitPane.DIVIDER_LOCATION_PROPERTY.equals(evt.getPropertyName())) {
			prefs.putInt(getKey("verticalDividerLocation"), jSplitPane2.getDividerLocation());
		}
        }//GEN-LAST:event_jSplitPane2PropertyChange

        private void formInternalFrameOpened(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameOpened
		jSplitPane1.setDividerLocation(prefs.getInt(getKey("horizontalDividerLocation"), jSplitPane1.getDividerLocation()));
		jSplitPane2.setDividerLocation(prefs.getInt(getKey("verticalDividerLocation"), jSplitPane2.getDividerLocation()));
        }//GEN-LAST:event_formInternalFrameOpened

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private linoleum.mail.FolderViewer folderViewer;
        private javax.swing.JCheckBox jCheckBox1;
        private javax.swing.JLabel jLabel1;
        private javax.swing.JLabel jLabel2;
        private javax.swing.JLabel jLabel3;
        private javax.swing.JLabel jLabel4;
        private javax.swing.JMenu jMenu1;
        private javax.swing.JMenu jMenu2;
        private javax.swing.JMenu jMenu3;
        private javax.swing.JMenuBar jMenuBar1;
        private javax.swing.JMenuItem jMenuItem1;
        private javax.swing.JMenuItem jMenuItem2;
        private javax.swing.JMenuItem jMenuItem3;
        private javax.swing.JMenuItem jMenuItem4;
        private javax.swing.JMenuItem jMenuItem5;
        private javax.swing.JMenuItem jMenuItem6;
        private javax.swing.JMenuItem jMenuItem7;
        private javax.swing.JMenuItem jMenuItem8;
        private javax.swing.JScrollPane jScrollPane1;
        private javax.swing.JPopupMenu.Separator jSeparator1;
        private javax.swing.JPopupMenu.Separator jSeparator2;
        private javax.swing.JSplitPane jSplitPane1;
        private javax.swing.JSplitPane jSplitPane2;
        private javax.swing.JTextField jTextField1;
        private javax.swing.JTextField jTextField2;
        private javax.swing.JTextField jTextField3;
        private javax.swing.JTextField jTextField4;
        private javax.swing.JTree jTree1;
        private linoleum.mail.MessageViewer messageViewer;
        private linoleum.application.OptionPanel optionPanel1;
        // End of variables declaration//GEN-END:variables
}
