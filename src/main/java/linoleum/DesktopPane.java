package linoleum;

import java.awt.Component;
import java.awt.Dimension;
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
import javax.swing.ListSelectionModel;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import linoleum.application.Frame;

public class DesktopPane extends JDesktopPane {
	public static final int DEFAULT_LAYER = 1;
	public static final int DIALOG_LAYER = 2;
	public static final int ICON_LAYER = 3;
	private final Icon defaultIcon = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/development/Application24.gif"));
	private final DefaultListModel<Frame> model = new DefaultListModel<>();
	private final ListCellRenderer<Frame> renderer = new Renderer();
	private final LayoutManager layout = new GridBagLayout();
	private final JList<Frame> list = new JList<>();
	private final InputMap inputMap = list.getInputMap();
	private final InputMap map = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
	private final Action selectAction = new SelectAction();
	private final KeyEventDispatcher dispatcher = new KeyEventDispatcher() {
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
					setRecording(state);
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
	};
	private boolean recording;
	private boolean searching;
	private boolean selecting;
	private boolean reopen;
	private Background bkg;

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
			if (str == null || str.isEmpty()) {
				str = value.getName();
			}
			if (str != null && str.length() > 16) {
				str = str.substring(0, 13) + "...";
			}
			setText(str);
			setFont(list.getFont());
			return this;
		}
	}

	private class SelectAction extends AbstractAction {
		public SelectAction() {
			super("select");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.ALT_MASK));
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			if (!selecting) {
				select();
			}
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
		list.requestFocus();
		if (1 < model.getSize()) {
			list.setSelectedIndex(1);
		} else if (0 < model.getSize()) {
			list.setSelectedIndex(0);
		}
		selecting = true;
	}

	private void commit() {
		final JComponent comp = (JComponent) getRootPane().getGlassPane();
		comp.setVisible(false);
		comp.remove(list);
		final Frame frame = list.getSelectedValue();
		final int index = list.getSelectedIndex();
		if (index > -1 && index < model.getSize()) {
			model.removeElementAt(index);
		}
		if (frame != null) {
			model.add(0, frame);
			frame.select();
		}
		selecting = false;
	}

	public DesktopPane() {
		setPreferredSize(new Dimension(640, 451));
		map.put((KeyStroke) selectAction.getValue(Action.ACCELERATOR_KEY), selectAction.getValue(Action.NAME));
		map.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.ALT_DOWN_MASK), selectAction.getValue(Action.NAME));
		getActionMap().put(selectAction.getValue(Action.NAME), selectAction);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.ALT_DOWN_MASK), inputMap.get(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0)));
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.ALT_DOWN_MASK), inputMap.get(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0)));
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.ALT_DOWN_MASK), inputMap.get(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0)));
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.ALT_DOWN_MASK), inputMap.get(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0)));
		inputMap.put((KeyStroke) selectAction.getValue(Action.ACCELERATOR_KEY), inputMap.get(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0)));
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.ALT_DOWN_MASK), inputMap.get(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0)));
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.SHIFT_DOWN_MASK | InputEvent.ALT_DOWN_MASK), inputMap.get(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0)));
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK | InputEvent.ALT_DOWN_MASK), inputMap.get(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0)));
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher);
		list.setModel(model);
		list.setCellRenderer(renderer);
		list.setLayoutOrientation(JList.VERTICAL_WRAP);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

	void destroy() {
		KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(dispatcher);
	}

	void setReopen(final boolean reopen) {
		putClientProperty("DesktopPane.reopen", this.reopen = reopen);
	}

	public boolean isReopen() {
		return reopen;
	}

	private void setRecording(final boolean recording) {
		putClientProperty("DesktopPane.recording", this.recording = recording);
	}

	public boolean isRecording() {
		return recording;
	}

	private void update() {
		list.setVisibleRowCount(Math.min(model.getSize(), 8));
	}

	@Override
	public void addImpl(final Component comp, Object constraints, int index) {
		if (comp instanceof Background) {
			bkg = (Background) comp;
		} else if (comp instanceof Frame) {
			final Frame frame = (Frame) comp;
			frame.addInternalFrameListener(new InternalFrameAdapter() {
				@Override
				public void internalFrameOpened(final InternalFrameEvent e) {
					model.add(0, frame);
					update();
				}

				@Override
				public void internalFrameActivated(final InternalFrameEvent e) {
					model.removeElement(frame);
					model.add(0, frame);
					update();
				}

				@Override
				public void internalFrameClosing(final InternalFrameEvent e) {
					model.removeElement(frame);
					update();
				}

				@Override
				public void internalFrameClosed(final InternalFrameEvent e) {
					model.removeElement(frame);
					update();
				}

				@Override
				public void internalFrameIconified(final InternalFrameEvent e) {
					update();
				}
			});
		}
		super.addImpl(comp, constraints == null?DEFAULT_LAYER:constraints, index);
	}

	public int getLayer(final Component c) {
		final int layer = super.getLayer(c);
		return c instanceof JInternalFrame.JDesktopIcon?ICON_LAYER:layer;
	}
}
