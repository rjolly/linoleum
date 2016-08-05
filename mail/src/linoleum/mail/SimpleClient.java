package linoleum.mail;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.prefs.Preferences;
import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
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
	private final boolean debug = props.getProperty("linoleum.mail.debug") != null;
	private final DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
	private final DefaultTreeModel model = new DefaultTreeModel(root);
	private final Map<URLName, StoreTreeNode> map = new HashMap<>();
	private final Session session;
	private final Compose frame;
	private Folder folder;
	static final String name = "Mail";

	private class ComposeAction extends AbstractAction {
		public ComposeAction() {
			super("Compose", composeIcon);
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			compose("?");
		}
	}

	public Action getComposeAction() {
		return composeAction;
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

	public Action getExpungeAction() {
		return expungeAction;
	}

	private class SettingsAction extends AbstractAction {
		public SettingsAction() {
			super("Settings");
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			settings.show(SimpleClient.this);
			final String str = prefs.get(name + ".url", null);
			if (str != null && !str.isEmpty()) {
				open(str);
			}
		}
	}

	public Action getSettingsAction() {
		return settingsAction;
	}

	public SimpleClient() {
		initComponents();
		setIcon(new ImageIcon(getClass().getResource("Mail24.png")));
		try {
			final File capfile = new File("simple.mailcap");
			final InputStream is = capfile.isFile()?new FileInputStream(capfile):getClass().getResourceAsStream("simple.mailcap");
			CommandMap.setDefaultCommandMap(new MailcapCommandMap(is));
		} catch (final FileNotFoundException ex) {
			ex.printStackTrace();
		}
		final String mailhost = prefs.get(name + ".mailhost", null);
		if (mailhost != null) {
			props.put("mail.smtp.host", mailhost);
		}
		session = Session.getInstance(props, auth);
		if (debug) {
			session.setDebug(true);
		}
		frame = new Compose(session);
		final String str = prefs.get(name + ".url", null);
		if (str != null && !str.isEmpty()) {
			open(str);
		}
	}

	public void compose(final String str) {
		try {
			frame.open(getApplicationManager(), new URI("mailto", str, null));
		} catch (final URISyntaxException e) {
			e.printStackTrace();
		}
	}

	public FolderViewer getFolderViewer() {
		return folderViewer;
	}

	@Override
	protected void open() {
		open(getURI().toString());
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

                settings = new linoleum.mail.Settings();
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
                jMenu2 = new javax.swing.JMenu();
                jMenuItem5 = new javax.swing.JMenuItem();
                jMenu3 = new javax.swing.JMenu();
                jMenuItem6 = new javax.swing.JMenuItem();

                setClosable(true);
                setIconifiable(true);
                setMaximizable(true);
                setResizable(true);
                setTitle("Simple JavaMail Client");
                setName(name);

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

                jMenuItem1.setAction(getComposeAction());
                jMenu1.add(jMenuItem1);

                jMenuItem2.setAction(messageViewer.getReplyAction());
                jMenu1.add(jMenuItem2);

                jMenuItem3.setAction(messageViewer.getReplyToAllAction());
                jMenu1.add(jMenuItem3);

                jMenuItem4.setAction(messageViewer.getDeleteAction());
                jMenu1.add(jMenuItem4);

                jMenuBar1.add(jMenu1);

                jMenu2.setText("Folder");

                jMenuItem5.setAction(getExpungeAction());
                jMenu2.add(jMenuItem5);

                jMenuBar1.add(jMenu2);

                jMenu3.setText("Account");

                jMenuItem6.setAction(getSettingsAction());
                jMenu3.add(jMenuItem6);

                jMenuBar1.add(jMenu3);

                setJMenuBar(jMenuBar1);

                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
                getContentPane().setLayout(layout);
                layout.setHorizontalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 582, Short.MAX_VALUE)
                );
                layout.setVerticalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 415, Short.MAX_VALUE)
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

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private linoleum.mail.FolderViewer folderViewer;
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
        private javax.swing.JScrollPane jScrollPane1;
        private javax.swing.JSplitPane jSplitPane1;
        private javax.swing.JSplitPane jSplitPane2;
        private javax.swing.JTree jTree1;
        private linoleum.mail.MessageViewer messageViewer;
        private linoleum.mail.Settings settings;
        // End of variables declaration//GEN-END:variables
}
