package linoleum;

import java.net.URI;
import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import linoleum.application.Application;

public class Console extends javax.swing.JInternalFrame implements Application {

	public Console() {
		initComponents();
		setContentPane(new ConsolePanel());
	}

	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                setClosable(true);
                setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
                setIconifiable(true);
                setMaximizable(true);
                setResizable(true);
                setTitle("Console");
                setName(Console.class.getSimpleName());

                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
                getContentPane().setLayout(layout);
                layout.setHorizontalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 394, Short.MAX_VALUE)
                );
                layout.setVerticalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 277, Short.MAX_VALUE)
                );

                pack();
        }// </editor-fold>//GEN-END:initComponents


        // Variables declaration - do not modify//GEN-BEGIN:variables
        // End of variables declaration//GEN-END:variables

	public ImageIcon getIcon() {
		return new ImageIcon(getClass().getResource("/toolbarButtonGraphics/development/Host24.gif"));
	}

	public JInternalFrame open(final URI uri) {
		return this;
	}
}
