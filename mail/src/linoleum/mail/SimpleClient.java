package linoleum.mail;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import linoleum.application.Frame;

public class SimpleClient extends Frame {
	private final Preferences prefs = Preferences.userNodeForPackage(getClass());
	private final SimpleAuthenticator auth = new SimpleAuthenticator(this);
	private final Properties props = System.getProperties();
	private final boolean debug = props.getProperty("linoleum.mail.debug") != null;
	private final DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
	private final DefaultTreeModel model = new DefaultTreeModel(root);
	private final Session session;
	private final Compose frame;
	private StoreTreeNode node;
	private FolderTreeNode foldernode;
	static final String name = "Mail";

	public SimpleClient() {
		initComponents();
		setIcon(new ImageIcon(getClass().getResource("Mail24.png")));
		try {
			final File capfile = new File("simple.mailcap");
			final InputStream is = capfile.isFile()?new FileInputStream(capfile):getClass().getResourceAsStream("simple.mailcap");
			CommandMap.setDefaultCommandMap(new MailcapCommandMap(is));
		} catch (final IOException ex) {
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
		if (str != null) {
			open(str);
		}
	}

	public void compose() {
		compose("?");
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

	void open(final String str) {
		try {
			final Store store = session.getStore(new URLName(str));
			final StoreTreeNode storenode = new StoreTreeNode(store);
			model.insertNodeInto(storenode, root, root.getChildCount());
			jTree1.scrollPathToVisible(new TreePath(storenode.getPath()));
		} catch (final NoSuchProviderException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                jPopupMenu1 = new javax.swing.JPopupMenu();
                jMenuItem1 = new javax.swing.JMenuItem();
                jPopupMenu2 = new javax.swing.JPopupMenu();
                jMenuItem2 = new javax.swing.JMenuItem();
                jPopupMenu3 = new javax.swing.JPopupMenu();
                jMenuItem3 = new javax.swing.JMenuItem();
                jSplitPane1 = new javax.swing.JSplitPane();
                jSplitPane2 = new javax.swing.JSplitPane();
                jScrollPane1 = new javax.swing.JScrollPane();
                jTree1 = new javax.swing.JTree();
                folderViewer = new linoleum.mail.FolderViewer();
                messageViewer = new linoleum.mail.MessageViewer();

                jMenuItem1.setText("Add...");
                jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jMenuItem1ActionPerformed(evt);
                        }
                });
                jPopupMenu1.add(jMenuItem1);

                jMenuItem2.setText("Remove");
                jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jMenuItem2ActionPerformed(evt);
                        }
                });
                jPopupMenu2.add(jMenuItem2);

                jMenuItem3.setText("Expunge");
                jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jMenuItem3ActionPerformed(evt);
                        }
                });
                jPopupMenu3.add(jMenuItem3);

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
                jTree1.setComponentPopupMenu(jPopupMenu1);
                jTree1.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
                        public void mouseMoved(java.awt.event.MouseEvent evt) {
                                jTree1MouseMoved(evt);
                        }
                });
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

                folderViewer.setMv(messageViewer);
                jSplitPane2.setRightComponent(folderViewer);

                jSplitPane1.setTopComponent(jSplitPane2);

                messageViewer.setClient(this);
                jSplitPane1.setBottomComponent(messageViewer);

                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
                getContentPane().setLayout(layout);
                layout.setHorizontalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 582, Short.MAX_VALUE)
                );
                layout.setVerticalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 437, Short.MAX_VALUE)
                );

                pack();
        }// </editor-fold>//GEN-END:initComponents

        private void jTree1ValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_jTree1ValueChanged
		final TreePath path = evt.getNewLeadSelectionPath();
		if (path != null) {
			final Object o = path.getLastPathComponent();
			if (o instanceof FolderTreeNode) {
				final Folder folder = ((FolderTreeNode)o).getFolder();
				try {
					if ((folder.getType() & Folder.HOLDS_MESSAGES) != 0) {
						folderViewer.setFolder(folder);
					}
				} catch (final MessagingException me) {
					me.printStackTrace();
				}
			}
		}
        }//GEN-LAST:event_jTree1ValueChanged

        private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
		final String str = JOptionPane.showInternalInputDialog(this, "Enter URL:");
		if (str != null) {
			open(str);
		}
        }//GEN-LAST:event_jMenuItem1ActionPerformed

        private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
                model.removeNodeFromParent(node);
        }//GEN-LAST:event_jMenuItem2ActionPerformed

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

        private void jTree1MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTree1MouseMoved
		final TreePath path = jTree1.getPathForLocation(evt.getX(), evt.getY());
		jTree1.setComponentPopupMenu(jPopupMenu1);
		if (path != null) {
			final Object o = path.getLastPathComponent();
			if (o instanceof FolderTreeNode) {
				foldernode = (FolderTreeNode)o;
				jTree1.setComponentPopupMenu(jPopupMenu3);
			} else if (o instanceof StoreTreeNode) {
				node = (StoreTreeNode)o;
				jTree1.setComponentPopupMenu(jPopupMenu2);
			}
		}
        }//GEN-LAST:event_jTree1MouseMoved

        private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
		final Folder folder = foldernode.getFolder();
		try {
			folder.expunge();
			folderViewer.setFolder(folder);
		} catch (final MessagingException me) {
			me.printStackTrace();
		}
        }//GEN-LAST:event_jMenuItem3ActionPerformed

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private linoleum.mail.FolderViewer folderViewer;
        private javax.swing.JMenuItem jMenuItem1;
        private javax.swing.JMenuItem jMenuItem2;
        private javax.swing.JMenuItem jMenuItem3;
        private javax.swing.JPopupMenu jPopupMenu1;
        private javax.swing.JPopupMenu jPopupMenu2;
        private javax.swing.JPopupMenu jPopupMenu3;
        private javax.swing.JScrollPane jScrollPane1;
        private javax.swing.JSplitPane jSplitPane1;
        private javax.swing.JSplitPane jSplitPane2;
        private javax.swing.JTree jTree1;
        private linoleum.mail.MessageViewer messageViewer;
        // End of variables declaration//GEN-END:variables
}
