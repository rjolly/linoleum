package linoleum;

import java.awt.Component;
import java.util.List;
import javax.swing.ImageIcon;
import linoleum.application.Frame;
import linoleum.application.OptionPanel;
import linoleum.application.event.ClassPathChangeEvent;

public class PreferenceManager extends Frame {
	private final OptionPanel desktop = Desktop.instance.getBackgroundFrame().getOptionPanel();
	private OptionPanel current;
	private boolean save;

	public PreferenceManager() {
		initComponents();
		setIcon(new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Properties24.gif")));
	}

	@Override
	public void open() {
		if (jTabbedPane1.indexOfTabComponent(desktop) < 0) {
			jTabbedPane1.addTab(desktop.getName(), desktop);
		}
		final List<OptionPanel> list = getApplicationManager().getOptionPanels();
		for(final OptionPanel panel : list) {
			if (jTabbedPane1.indexOfTabComponent(panel) < 0) {
				jTabbedPane1.addTab(panel.getName(), panel);
			}
		}
		next();
	}

	private void next() {
		final Component comp = jTabbedPane1.getSelectedComponent();
		final OptionPanel panel = comp instanceof OptionPanel?(OptionPanel) comp:null;
		if (panel != null && panel != current) {
			if (current != null) {
				current.save();
			}
			(current = panel).load();
		}
	}

	@Override
	public void close() {
		if (save && current != null) {
			current.save();
		}
		current = null;
		save = false;
	}

	public void classPathChanged(final ClassPathChangeEvent e) {
		open();
	}

	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                jButton1 = new javax.swing.JButton();
                jButton2 = new javax.swing.JButton();
                jTabbedPane1 = new javax.swing.JTabbedPane();

                setClosable(true);
                setIconifiable(true);
                setMaximizable(true);
                setResizable(true);
                setTitle("Preferences");
                setName("Preferences"); // NOI18N

                jButton1.setText("Ok");
                jButton1.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jButton1ActionPerformed(evt);
                        }
                });

                jButton2.setText("Cancel");
                jButton2.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jButton2ActionPerformed(evt);
                        }
                });

                jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
                        public void stateChanged(javax.swing.event.ChangeEvent evt) {
                                jTabbedPane1StateChanged(evt);
                        }
                });

                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
                getContentPane().setLayout(layout);
                layout.setHorizontalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap(271, Short.MAX_VALUE)
                                .addComponent(jButton1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton2)
                                .addContainerGap())
                        .addComponent(jTabbedPane1)
                );
                layout.setVerticalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 255, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jButton1)
                                        .addComponent(jButton2))
                                .addContainerGap())
                );

                pack();
        }// </editor-fold>//GEN-END:initComponents

        private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
		save = true;
		doDefaultCloseAction();
        }//GEN-LAST:event_jButton1ActionPerformed

        private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
		doDefaultCloseAction();
        }//GEN-LAST:event_jButton2ActionPerformed

        private void jTabbedPane1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTabbedPane1StateChanged
		next();
        }//GEN-LAST:event_jTabbedPane1StateChanged

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JButton jButton1;
        private javax.swing.JButton jButton2;
        private javax.swing.JTabbedPane jTabbedPane1;
        // End of variables declaration//GEN-END:variables
}
