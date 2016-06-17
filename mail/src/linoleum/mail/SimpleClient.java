package linoleum.mail;

import java.awt.Dimension;
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
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.ImageIcon;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import linoleum.application.Frame;

public class SimpleClient extends Frame {
	private final MessageViewer mv = new MessageViewer();
	private final FolderViewer fv = new FolderViewer(mv);
	private final JScrollPane sp;
	private final JSplitPane jsp;
	private final JSplitPane jsp2;

	public SimpleClient(final boolean first) {
		initComponents();
		setTitle("Simple JavaMail Client");
		setIcon(new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/ComposeMail24.gif")));
		if (first) try {
			final File capfile = new File("simple.mailcap");
			final InputStream is = capfile.isFile()?new FileInputStream(capfile):getClass().getResourceAsStream("simple.mailcap");
			CommandMap.setDefaultCommandMap(new MailcapCommandMap(is));
		} catch (final IOException ex) {
			ex.printStackTrace();
		}
		sp = new JScrollPane();
		sp.setPreferredSize(new Dimension(250, 300));
		jsp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sp, fv);
		jsp.setOneTouchExpandable(true);
		jsp2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, jsp, mv);
		jsp2.setOneTouchExpandable(true);
		getContentPane().add(jsp2);
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
		final DefaultTreeModel treeModel = new DefaultTreeModel(root);
		final JTree tree = new JTree(treeModel);
		tree.addTreeSelectionListener(new TreePress());
		sp.getViewport().add(tree);
	}

	class TreePress implements TreeSelectionListener {
		public void valueChanged(final TreeSelectionEvent e) {
			final TreePath path = e.getNewLeadSelectionPath();
			if (path != null) {
				final Object o = path.getLastPathComponent();
				if (o instanceof FolderTreeNode) {
					final FolderTreeNode node = (FolderTreeNode)o;
					final Folder folder = node.getFolder();
					try {
						if ((folder.getType() & Folder.HOLDS_MESSAGES) != 0) {
							fv.setFolder(folder);
						}
					} catch (final MessagingException me) {
						me.printStackTrace();
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                setClosable(true);
                setIconifiable(true);
                setMaximizable(true);
                setResizable(true);
                setName("Mail");

                pack();
        }// </editor-fold>//GEN-END:initComponents

        // Variables declaration - do not modify//GEN-BEGIN:variables
        // End of variables declaration//GEN-END:variables
}
