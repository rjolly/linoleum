package linoleum.application;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.accessibility.AccessibleContext;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.plaf.FileChooserUI;

public class FileChooser extends JFileChooser {
	private JInternalFrame dialog;
	private int returnValue = ERROR_OPTION;

	public int showInternalOpenDialog(final Component parent) {
		setDialogType(OPEN_DIALOG);
		return showInternalDialog(parent, null);
	}

	public int showInternalSaveDialog(final Component parent) {
		setDialogType(SAVE_DIALOG);
		return showInternalDialog(parent, null);
	}

	public int showInternalDialog(final Component parent, final String approveButtonText) {
		if (dialog != null) {
			return ERROR_OPTION;
		}

		final Component fo = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();

		if(approveButtonText != null) {
			setApproveButtonText(approveButtonText);
			setDialogType(CUSTOM_DIALOG);
		}
		dialog = createInternalDialog(parent);
		dialog.addInternalFrameListener(new InternalFrameAdapter() {
			public void internalFrameClosing(final InternalFrameEvent e) {
				invoke(dialog, "stopLWModal");
			}
		});
		returnValue = ERROR_OPTION;
		rescanCurrentDirectory();

		dialog.setVisible(true);
		invoke(dialog, "startLWModal");
		firePropertyChange("JFileChooserDialogIsClosingProperty", dialog, null);

		if (parent instanceof JInternalFrame) {
			try {
				((JInternalFrame)parent).setSelected(true);
			} catch (final PropertyVetoException e) {
			}
		}

		if (fo != null && fo.isShowing()) {
			fo.requestFocus();
		}

		dialog.getContentPane().removeAll();
		dialog.dispose();
		dialog = null;
		return returnValue;
	}

	protected JInternalFrame createInternalDialog(final Component parent) {
		final FileChooserUI ui = getUI();
		final String title = ui.getDialogTitle(this);
		putClientProperty(AccessibleContext.ACCESSIBLE_DESCRIPTION_PROPERTY, title);

		final Container root = JOptionPane.getDesktopPaneForComponent(parent);
		final JInternalFrame dialog = new JInternalFrame(title, false, true, false, false);
		dialog.setComponentOrientation(getComponentOrientation());

		final Container contentPane = dialog.getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(this, BorderLayout.CENTER);

		if (JDialog.isDefaultLookAndFeelDecorated() && UIManager.getLookAndFeel().getSupportsWindowDecorations()) {
			dialog.getRootPane().setWindowDecorationStyle(JRootPane.FILE_CHOOSER_DIALOG);
		}
		dialog.getRootPane().setDefaultButton(ui.getDefaultButton(this));

		root.add(dialog, JLayeredPane.MODAL_LAYER);

		final Dimension dialogSize = dialog.getPreferredSize();
		final Dimension rootSize = root.getSize();
		final Dimension parentSize = parent.getSize();

		final Point dialogCoord = SwingUtilities.convertPoint(parent, 0, 0, root);
		int x = (parentSize.width - dialogSize.width) / 2 + dialogCoord.x;
		int y = (parentSize.height - dialogSize.height) / 2 + dialogCoord.y;

		final int ovrx = x + dialogSize.width - rootSize.width;
		final int ovry = y + dialogSize.height - rootSize.height;
		x = Math.max((ovrx > 0? x - ovrx: x), 0);
		y = Math.max((ovry > 0? y - ovry: y), 0);
		dialog.setBounds(x, y, dialogSize.width, dialogSize.height);

		root.validate();
		try {
			dialog.setSelected(true);
		} catch (PropertyVetoException e) {
		}

		return dialog;
	}

	@Override
	public void approveSelection() {
		returnValue = APPROVE_OPTION;
		if (dialog != null) {
			hide(dialog);
		}
		super.approveSelection();
	}

	@Override
	public void cancelSelection() {
		returnValue = CANCEL_OPTION;
		if (dialog != null) {
			hide(dialog);
		}
		super.cancelSelection();
	}

	private static void hide(final JInternalFrame dialog) {
		try {
			dialog.setClosed(true);
		} catch (final PropertyVetoException e) {
		}
		dialog.setVisible(false);
	}

	private static void invoke(final JInternalFrame dialog, final String str) {
		try {
			Method method = null;
			try {
				method = Container.class.getDeclaredMethod(str, (Class[])null);
			} catch (final NoSuchMethodException ex) {
			}
			if (method != null) {
				method.setAccessible(true);
				method.invoke(dialog, (Object[])null);
			}
		} catch (final IllegalAccessException ex) {
		} catch (final IllegalArgumentException ex) {
		} catch (final InvocationTargetException ex) {
		}
	}
}
