package linoleum.mail;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.prefs.Preferences;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
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
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import linoleum.application.Frame;

public class SimpleClient extends Frame {
	private final Icon composeIcon = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/ComposeMail16.gif"));
	private final Action composeAction = new ComposeAction();
	private final Action expungeAction = new ExpungeAction();
	private final Action settingsAction = new SettingsAction();
	private final Preferences prefs = Preferences.userNodeForPackage(getClass());
	private final SimpleAuthenticator auth = new SimpleAuthenticator(this);
	private final Properties props = System.getProperties();
	private final DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
	private final DefaultTreeModel model = new DefaultTreeModel(root);
	private final Map<URLName, StoreTreeNode> map = new HashMap<>();
	private final Session session = Session.getInstance(props, auth);
	private final Compose frame = new Compose(session);
	private Folder folder;
	static SimpleClient instance;

	private class ComposeAction extends AbstractAction {
		public ComposeAction() {
			super("Compose", composeIcon);
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			frame.open(null);
		}
	}

	private class ExpungeAction extends AbstractAction {
		public ExpungeAction() {
			super("Expunge");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK));
			setEnabled(false);
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			try {
				folder.expunge();
				folderViewer.setFolder(folder);
			} catch (final MessagingException me) {
				me.printStackTrace();
			}		}
	}

	private class SettingsAction extends AbstractAction {
		public SettingsAction() {
			super("Settings");
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			optionPanel1.load();
			final int result = JOptionPane.showInternalConfirmDialog(SimpleClient.this, optionPanel1, "Account settings", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			switch (result) {
			case JOptionPane.OK_OPTION:
				optionPanel1.save();
				break;
			default:
			}
		}
	}

	public SimpleClient() {
		initComponents();
		setIcon(new ImageIcon(getClass().getResource("Mail24.png")));
		props.put("mail.mime.decodefilename", "true");
		props.put("mstor.mbox.metadataStrategy", "none");
		prefs.addPreferenceChangeListener(new PreferenceChangeListener() {
			@Override
			public void preferenceChange(final PreferenceChangeEvent evt) {
				if (evt.getKey().startsWith(getName())) {
					open();
				}
			}
		});
		if (instance == null) {
			instance = this;
		}
		frame.setJMenuBar(getJMenuBar());
	}

	void compose(final String str) {
		try {
			frame.open(new URI("mailto", str, null));
		} catch (final URISyntaxException e) {
			e.printStackTrace();
		}
	}

	public FolderViewer getFolderViewer() {
		return folderViewer;
	}

	@Override
	public void init() {
		frame.setApplicationManager(getApplicationManager());
	}

	@Override
	public void load() {
		jTextField1.setText(prefs.get(getKey("url"), ""));
		jTextField2.setText(prefs.get(getKey("mailhost"), ""));
		jTextField3.setText(prefs.get(getKey("from"), ""));
		jTextField4.setText(prefs.get(getKey("record"), "Sent"));
		jCheckBox1.setSelected(prefs.getBoolean(getKey("debug"), false));
	}

	@Override
	public void save() {
		prefs.put(getKey("url"), jTextField1.getText());
		prefs.put(getKey("mailhost"), jTextField2.getText());
		prefs.put(getKey("from"), jTextField3.getText());
		prefs.put(getKey("record"), jTextField4.getText());
		prefs.putBoolean(getKey("debug"), jCheckBox1.isSelected());
	}

	@Override
	public void open() {
		final String mailhost = prefs.get(getKey("mailhost"), "");
		if (!mailhost.isEmpty()) {
			props.put("mail.smtp.host", mailhost);
		}
		final boolean debug = prefs.getBoolean(getKey("debug"), false);
		if (debug) {
			session.setDebug(true);
		}
		final URI uri = getURI();
		if (uri != null) {
			open(uri.toString());
		} else {
			final String str = prefs.get(getKey("url"), "");
			if (!str.isEmpty()) {
				open(str);
			}
		}
	}

	final void open(final String str) {
		final URLName name = new URLName(str);
		StoreTreeNode storenode = map.get(name);
		if (storenode == null) {
			try {
				final Store store = session.getStore(name);
				map.put(name, storenode = new StoreTreeNode(store));
				model.insertNodeInto(storenode, root, root.getChildCount());
			} catch (final NoSuchProviderException e) {
				e.printStackTrace();
			}
		}
		if (storenode != null) {
			jTree1.scrollPathToVisible(new TreePath(storenode.getPath()));
		}
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

                optionPanel1.setFrame(this);

                jLabel1.setText("URL :");

                jLabel2.setText("Mailhost :");

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
                setName("Mail");

                jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
                jSplitPane1.setResizeWeight(0.4);

                jSplitPane2.setResizeWeight(0.4);

                jTree1.setModel(model);
                jTree1.addTreeWillExpandListener(new javax.swing.event.TreeWillExpandListener() {
                        public void treeWillExpand(javax.swing.event.TreeExpansionEvent evt)throws javax.swing.tree.ExpandVetoException {
                                jTree1TreeWillExpand(evt);
                        }
                        public void treeWillCollapse(javax.swing.event.TreeExpansionEvent evt)throws javax.swing.tree.ExpandVetoException {
                                jTree1TreeWillCollapse(evt);
                        }
                });
                jTree1.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
                        public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                                jTree1ValueChanged(evt);
                        }
                });
                jScrollPane1.setViewportView(jTree1);

                jSplitPane2.setLeftComponent(jScrollPane1);

                folderViewer.setMv(messageViewer);
                jSplitPane2.setRightComponent(folderViewer);

                jSplitPane1.setTopComponent(jSplitPane2);

                messageViewer.setClient(this);
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
				folder = ((FolderTreeNode)o).getFolder();
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
		}
        }//GEN-LAST:event_jTree1ValueChanged

        private void jTree1TreeWillExpand(javax.swing.event.TreeExpansionEvent evt)throws javax.swing.tree.ExpandVetoException {//GEN-FIRST:event_jTree1TreeWillExpand
		final TreePath path = evt.getPath();
		if (path != null) {
			final Object o = path.getLastPathComponent();
			if (o instanceof StoreTreeNode) {
				try {
					((StoreTreeNode)o).open();
				} catch (final MessagingException me) {
					throw new ExpandVetoException(evt);
				}
			}
		}
        }//GEN-LAST:event_jTree1TreeWillExpand

        private void jTree1TreeWillCollapse(javax.swing.event.TreeExpansionEvent evt)throws javax.swing.tree.ExpandVetoException {//GEN-FIRST:event_jTree1TreeWillCollapse
		final TreePath path = evt.getPath();
		if (path != null) {
			final Object o = path.getLastPathComponent();
			if (o instanceof StoreTreeNode) {
				try {
					folderViewer.setFolder(null);
					((StoreTreeNode)o).close();
				} catch (final MessagingException me) {
					throw new ExpandVetoException(evt);
				}
			}
		}
        }//GEN-LAST:event_jTree1TreeWillCollapse

        private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
		optionPanel1.setDirty(true);
        }//GEN-LAST:event_jCheckBox1ActionPerformed

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
        private javax.swing.JScrollPane jScrollPane1;
        private javax.swing.JPopupMenu.Separator jSeparator1;
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
