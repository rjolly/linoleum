package linoleum;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.GridBagLayout;
import java.awt.KeyboardFocusManager;
import java.awt.KeyEventDispatcher;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DefaultListModel;
import javax.swing.KeyStroke;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import linoleum.application.Frame;

public class DesktopPane extends JDesktopPane {
	public static final int DEFAULT_LAYER = 1;
	public static final int DIALOG_LAYER = 2;
	public static final int ICON_LAYER = 3;
	private final Icon defaultIcon = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/development/Application24.gif"));
	private final Action searchNextFrameAction = new SearchNextFrameAction();
	private final Action searchPreviousFrameAction = new SearchPreviousFrameAction();
	private final DefaultListModel<Frame> model = new DefaultListModel<>();
	private final ListCellRenderer<Frame> renderer = new Renderer();
	private final LayoutManager layout = new GridBagLayout();
	private final JList<Frame> list = new JList<>();
	private boolean recording;
	private boolean searching;
	private Background bkg;
	private int index;

	private class Renderer extends JLabel implements ListCellRenderer<Frame> {
		public Renderer() {
			setOpaque(true);
			setHorizontalAlignment(CENTER);
			setVerticalAlignment(CENTER);
			setVerticalTextPosition(BOTTOM);
			setHorizontalTextPosition(CENTER);
		}

		@Override
		public Component getListCellRendererComponent(final JList<? extends Frame> list, final Frame value, final int index, final boolean isSelected, final boolean cellHasFocus) {
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			final Icon icon = value.getIcon();
			setIcon(icon == null?defaultIcon:icon);
			String str = value.getTitle();
			if (str != null && str.length() > 16) {
				str = str.substring(0, 13) + "...";
			}
			setText(str == null?"untitled":str);
			setFont(list.getFont());
			return this;
		}
	}

	private int getKeyCode() {
		return Desktop.isDesktopSupported()?KeyEvent.VK_A:KeyEvent.VK_ALT;
	}

	private class SearchNextFrameAction extends AbstractAction {
		public SearchNextFrameAction() {
			super("searchNextFrame");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(getKeyCode(), InputEvent.ALT_DOWN_MASK));
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			index++;
			if (index == model.getSize()) {
				index = 0;
			}
			select();
		}
	}

	private class SearchPreviousFrameAction extends AbstractAction {
		public SearchPreviousFrameAction() {
			super("searchPreviousFrame");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(getKeyCode(), InputEvent.SHIFT_DOWN_MASK | InputEvent.ALT_DOWN_MASK));
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			index--;
			if (index == -1) {
				index = model.getSize() - 1;
			}
			select();
		}
	}

	private void select() {
		final JComponent comp = (JComponent) getRootPane().getGlassPane();
		if (comp.getLayout() != layout) {
			comp.setLayout(layout);
		}
		if (!comp.isVisible()) {
			comp.add(list);
			comp.setVisible(true);
		}
		if (index > -1 && index < model.getSize()) {
			list.setSelectedIndex(index);
		}
	}

	private void commit() {
		final JComponent comp = (JComponent) getRootPane().getGlassPane();
		comp.setVisible(false);
		comp.remove(list);
		if (index > -1 && index < model.getSize()) {
			final Frame frame = model.get(index);
			model.removeElementAt(index);
			model.add(0, frame);
			frame.select();
		}
		index = 0;
	}

	public DesktopPane() {
		final ActionMap actionMap = getActionMap();
		final InputMap inputMap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		inputMap.put((KeyStroke) searchNextFrameAction.getValue(Action.ACCELERATOR_KEY), searchNextFrameAction.getValue(Action.NAME));
		actionMap.put(searchNextFrameAction.getValue(Action.NAME), searchNextFrameAction);
		inputMap.put((KeyStroke) searchPreviousFrameAction.getValue(Action.ACCELERATOR_KEY), searchPreviousFrameAction.getValue(Action.NAME));
		actionMap.put(searchPreviousFrameAction.getValue(Action.NAME), searchPreviousFrameAction);
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
			public boolean dispatchKeyEvent(final KeyEvent e) {
				final boolean state;
				switch (e.getID()) {
				case KeyEvent.KEY_PRESSED:
					state = true;
					break;
				case KeyEvent.KEY_RELEASED:
				default:
					state = false;
				}
				switch (e.getKeyCode()) {
				case KeyEvent.VK_CONTROL:
					if (recording != state) {
						putClientProperty("DesktopPane.recording", recording = state);
					}
					break;
				case KeyEvent.VK_ALT:
					if (searching != state) {
						if (searching) {
							commit();
						}
						searching = state;
					}
					break;
				default:
				}
				return false;
			}
		});
		list.setModel(model);
		list.setCellRenderer(renderer);
		list.setLayoutOrientation(JList.VERTICAL_WRAP);
	}

	private void update() {
		list.setVisibleRowCount(Math.min(model.getSize(), 8));
	}

	@Override
	public Component add(final Component comp) {
		if (comp instanceof Background) {
			bkg = (Background) comp;
		} else if (comp instanceof Frame) {
			final Frame frame = (Frame) comp;
			frame.addInternalFrameListener(new InternalFrameAdapter() {
				private void open() {
					model.add(0, frame);
					index = 0;
					update();
				}

				@Override
				public void internalFrameOpened(final InternalFrameEvent e) {
					open();
				}

				@Override
				public void internalFrameActivated(final InternalFrameEvent e) {
					model.removeElement(frame);
					open();
				}

				private void close() {
					bkg.select();
					index = -1;
					update();
				}

				@Override
				public void internalFrameClosing(final InternalFrameEvent e) {
					model.removeElement(frame);
					close();
				}

				@Override
				public void internalFrameClosed(final InternalFrameEvent e) {
					model.removeElement(frame);
					close();
				}

				@Override
				public void internalFrameIconified(final InternalFrameEvent e) {
					close();
				}
			});
		}
		addImpl(comp, DEFAULT_LAYER, -1);
		return comp;
	}

	public int getLayer(final Component c) {
		final int layer = super.getLayer(c);
		return c instanceof JInternalFrame.JDesktopIcon?ICON_LAYER:layer;
	}
}
