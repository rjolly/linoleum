package linoleum.mail;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.mail.*;

public class StoreTreeNode extends DefaultMutableTreeNode {
	protected Store store = null;
	protected Folder folder = null;
	protected String display = null;

	public StoreTreeNode(Store what) {
		super(what);
		store = what;
	}

	public boolean isLeaf() {
		return false;
	}

	public int getChildCount() {
		if (folder == null) {
			loadChildren();
		}
		return super.getChildCount();
	}

	public boolean close() {
		try {
			if (store.isConnected()) {
				store.close();
			}
			return true;
		} catch (MessagingException me) {
			me.printStackTrace();
		}
		return false;
	}

	public boolean open() {
		try {
			// connect to the Store if we need to
			if (!store.isConnected()) {
				store.connect();
			}
			return true;
		} catch (MessagingException me) {
			me.printStackTrace();
		}
		return false;
	}

	protected void loadChildren() {
		try {
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
				insert(node, i);
			}
		} catch (MessagingException me) {
			me.printStackTrace();
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
