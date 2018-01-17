package linoleum.console;

import java.awt.BorderLayout;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import linoleum.application.Frame;
import sun.tools.jconsole.LocalVirtualMachine;
import sun.tools.jconsole.ProxyClient;
import sun.tools.jconsole.VMPanel;

public class JConsole extends Frame {
	private final ProxyClient client = getProxyClient();
	private VMPanel panel;

	static {
		System.setProperty("swing.defaultlaf", "javax.swing.plaf.metal.MetalLookAndFeel");
	}

	public JConsole() {
		setClosable(true);
		setIconifiable(true);
		setMaximizable(true);
		setResizable(true);
		setSize(640, 480);
		setTitle("JConsole");
		setFrameIcon(new ImageIcon(getClass().getResource("JavaCup16.png")));
		setIcon(new ImageIcon(getClass().getResource("JavaCup24.png")));
	}

	private ProxyClient getProxyClient() {
		String name = ManagementFactory.getRuntimeMXBean().getName();
		name = name.substring(0, name.indexOf("@"));
		final LocalVirtualMachine lvm = LocalVirtualMachine.getAllVirtualMachines().get(Integer.valueOf(name));
		try {
			return ProxyClient.getProxyClient(lvm);
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

	private void setShouldUseSSL(final boolean shouldUseSSL) {
		try {
			final Field f = VMPanel.class.getDeclaredField("shouldUseSSL");
			f.setAccessible(true);
			f.setBoolean(panel, shouldUseSSL);
		} catch (final ReflectiveOperationException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void open() {
		panel = getPanel(client, 4000);
		getContentPane().add(panel, BorderLayout.CENTER);
		setShouldUseSSL(false);
		panel.connect();
	}

	@Override
	public void close() {
		panel.disconnect();
		panel.cleanUp();
		getContentPane().remove(panel);
		panel = null;
	}
}
