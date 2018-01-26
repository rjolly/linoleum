package linoleum.mail;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.mail.*;

public class StoreTreeNode extends DefaultMutableTreeNode {
	protected Store store = null;
	protected Folder folder = null;
	protected String display = null;

	public StoreTreeNode(Store what) {
		super(what);
		store = what;
	}

	public Store getStore() {
		return store;
	}

	public boolean isLeaf() {
		return false;
	}

	public void close() throws MessagingException {
		if (store.isConnected()) {
			store.close();
		}
	}

	public void open(final DefaultTreeModel model) throws MessagingException  {
		// connect to the Store if we need to
		if (!store.isConnected()) {
			store.connect();
			if (folder == null) {
				loadChildren(model);
			}
		}
	}

	private void loadChildren(final DefaultTreeModel model) throws MessagingException {
		// get the default folder, and list the
		// subscribed folders on it
		folder = store.getDefaultFolder();
		// Folder[] sub = folder.listSubscribed();
		Folder[] sub = folder.list();

		// add a FolderTreeNode for each Folder
		int num = sub.length;
		for(int i = 0; i < num; i++) {
			FolderTreeNode node = new FolderTreeNode(sub[i]);
			// we used insert here, since add() would make
			// another recursive call to getChildCount();
			model.insertNodeInto(node, this, i);
		}
	}

	public String toString() {
		if (display == null) {
			URLName url = store.getURLName();
			if (url == null) {
				display = store.toString();
			} else {
				// don't show the password
				URLName too = new URLName( url.getProtocol(), url.getHost(), url.getPort(), url.getFile(), url.getUsername(), null);
				display = too.toString();
			}
		}
		return display;
	}
}
