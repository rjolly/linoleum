package linoleum.mail;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import linoleum.application.Frame;

public class SimpleClient extends Frame {
	public SimpleClient(final boolean first) {
		initComponents();
		setIcon(new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/ComposeMail24.gif")));
		if (first) try {
			final File capfile = new File("simple.mailcap");
			final InputStream is = capfile.isFile()?new FileInputStream(capfile):getClass().getResourceAsStream("simple.mailcap");
			CommandMap.setDefaultCommandMap(new MailcapCommandMap(is));
		} catch (final IOException ex) {
			ex.printStackTrace();
		}
	}

	public SimpleClient() {
		this(true);
	}

	@Override
	public Frame getFrame() {
		return new SimpleClient(false);
	}

	@Override
	protected void open() {
		final SimpleAuthenticator auth = new SimpleAuthenticator(this);
		final Session session = Session.getInstance(System.getProperties(), auth);
		final DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
		try {
			final Store store = session.getStore(new URLName(getURI().toString()));
			final StoreTreeNode storenode = new StoreTreeNode(store);
			root.add(storenode);
		} catch (final NoSuchProviderException e) {
			e.printStackTrace();
		}
		jTree1.setModel(new DefaultTreeModel(root));
	}

	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                jSplitPane1 = new javax.swing.JSplitPane();
                jSplitPane2 = new javax.swing.JSplitPane();
                jScrollPane1 = new javax.swing.JScrollPane();
                jTree1 = new javax.swing.JTree();
                folderViewer = new linoleum.mail.FolderViewer();
                messageViewer = new linoleum.mail.MessageViewer();

                setClosable(true);
                setIconifiable(true);
                setMaximizable(true);
                setResizable(true);
                setTitle("Simple JavaMail Client");
                setName("Mail"); // NOI18N

                jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

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
                jSplitPane1.setRightComponent(messageViewer);

                getContentPane().add(jSplitPane1, java.awt.BorderLayout.CENTER);

                pack();
        }// </editor-fold>//GEN-END:initComponents

        private void jTree1ValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_jTree1ValueChanged
		final TreePath path = evt.getNewLeadSelectionPath();
		if (path != null) {
			final Object o = path.getLastPathComponent();
			if (o instanceof FolderTreeNode) {
				final FolderTreeNode node = (FolderTreeNode)o;
				final Folder folder = node.getFolder();
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

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private linoleum.mail.FolderViewer folderViewer;
        private javax.swing.JScrollPane jScrollPane1;
        private javax.swing.JSplitPane jSplitPane1;
        private javax.swing.JSplitPane jSplitPane2;
        private javax.swing.JTree jTree1;
        private linoleum.mail.MessageViewer messageViewer;
        // End of variables declaration//GEN-END:variables
}
