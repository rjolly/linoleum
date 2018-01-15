package linoleum.console;

import java.awt.BorderLayout;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Constructor;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import linoleum.application.Frame;
import sun.tools.jconsole.LocalVirtualMachine;
import sun.tools.jconsole.ProxyClient;
import sun.tools.jconsole.VMPanel;

public class JConsole extends Frame {
	final VMPanel panel = getPanel();

	static {
		System.setProperty("swing.defaultlaf", "javax.swing.plaf.metal.MetalLookAndFeel");
	}

	public JConsole() {
		setClosable(true);
		setIconifiable(true);
		setMaximizable(true);
		setResizable(true);
		getContentPane().add(panel, BorderLayout.CENTER);
		pack();
		setTitle("JConsole");
		setFrameIcon(new ImageIcon(getClass().getResource("JavaCup16.png")));
		setIcon(new ImageIcon(getClass().getResource("JavaCup24.png")));
	}

	private VMPanel getPanel() {
		String name = ManagementFactory.getRuntimeMXBean().getName();
		name = name.substring(0, name.indexOf("@"));
		final LocalVirtualMachine lvm = LocalVirtualMachine.getAllVirtualMachines().get(Integer.valueOf(name));
		try {
			final ProxyClient c = ProxyClient.getProxyClient(lvm);
			return getPanel(c, 4000);
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private VMPanel getPanel(final ProxyClient proxyClient, final int updateInterval) {
		try {
			final Constructor<VMPanel> c = VMPanel.class.getDeclaredConstructor(ProxyClient.class, int.class);
			c.setAccessible(true);
			return c.newInstance(proxyClient, updateInterval);
		} catch (final ReflectiveOperationException e) {
			e.printStackTrace();
		}
		return null;
	}
}
