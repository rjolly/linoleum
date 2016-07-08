package linoleum.mail;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.mail.Folder;
import javax.mail.MessagingException;

public class FolderTreeNode extends DefaultMutableTreeNode {
	protected Folder folder = null;
	protected boolean hasLoaded = false;

	public FolderTreeNode(Folder what) {
		super(what);
		folder = what;
	}

	public boolean isLeaf() {
		try {
			if ((folder.getType() & Folder.HOLDS_FOLDERS) == 0) {
				return true;
			}
		} catch (MessagingException me) {
		}

		// otherwise it does hold folders, and therefore not
		// a leaf
		return false;
	}

	public Folder getFolder() {
		return folder;
	}

	public int getChildCount() {
		if (!hasLoaded) {
			loadChildren();
		}
		return super.getChildCount();
	}

	protected void loadChildren() {
		// if it is a leaf, just say we have loaded them
		if (isLeaf()) {
			hasLoaded = true;
			return;
		}

		try {
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
		return folder.getName();
	}
}

