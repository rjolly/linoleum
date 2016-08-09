package linoleum;

import java.awt.Component;
import java.util.List;
import javax.swing.ImageIcon;
import linoleum.application.Frame;
import linoleum.application.OptionPanel;
import linoleum.application.event.ClassPathChangeEvent;

public class PreferenceManager extends Frame {
	private OptionPanel current;

	public PreferenceManager() {
		initComponents();
		setIcon(new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Properties24.gif")));
	}

	@Override
	public void open() {
		final List<OptionPanel> list = getApplicationManager().getOptionPanels();
		for(final OptionPanel panel : list) {
			if (jTabbedPane1.indexOfTabComponent(panel) < 0) {
				jTabbedPane1.addTab(panel.getName(), panel);
			}
		}
		load();
	}

	private void load() {
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
		if (current != null) {
			current.save();
		}
		current = null;
	}

	@Override
	public void classPathChanged(final ClassPathChangeEvent e) {
		open();
	}

	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                jTabbedPane1 = new javax.swing.JTabbedPane();

                setClosable(true);
                setIconifiable(true);
                setMaximizable(true);
                setResizable(true);
                setTitle("Preferences");
                setName("Preferences"); // NOI18N

                jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
                        public void stateChanged(javax.swing.event.ChangeEvent evt) {
                                jTabbedPane1StateChanged(evt);
                        }
                });

                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
                getContentPane().setLayout(layout);
                layout.setHorizontalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 377, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jTabbedPane1))
                );
                layout.setVerticalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 304, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jTabbedPane1))
                );

                pack();
        }// </editor-fold>//GEN-END:initComponents

        private void jTabbedPane1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTabbedPane1StateChanged
		load();
        }//GEN-LAST:event_jTabbedPane1StateChanged

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JTabbedPane jTabbedPane1;
        // End of variables declaration//GEN-END:variables
}
