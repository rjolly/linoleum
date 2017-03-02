package linoleum;

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import linoleum.application.Frame;

public class Clock extends Frame {
	private final Preferences prefs = Preferences.userNodeForPackage(getClass());

	public Clock() {
		initComponents();
		prefs.addPreferenceChangeListener(new PreferenceChangeListener() {
			@Override
			public void preferenceChange(final PreferenceChangeEvent evt) {
				if (evt.getKey().equals(getKey("analog"))) {
					refresh();
				}
			}
		});
		refresh();
	}

	private void refresh() {
		clockPanel1.setAnalog(prefs.getBoolean(getKey("analog"), false));
		pack();
	}

	@Override
	public void load() {
		jRadioButton1.setSelected(prefs.getBoolean(getKey("analog"), false));
	}

	@Override
	public void save() {
		prefs.putBoolean(getKey("analog"), jRadioButton1.isSelected());
	}

	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                optionPanel1 = new linoleum.application.OptionPanel();
                jRadioButton1 = new javax.swing.JRadioButton();
                jRadioButton2 = new javax.swing.JRadioButton();
                buttonGroup1 = new javax.swing.ButtonGroup();
                clockPanel1 = new linoleum.ClockPanel();

                buttonGroup1.add(jRadioButton1);
                jRadioButton1.setText("Analog");
                jRadioButton1.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jRadioButton1ActionPerformed(evt);
                        }
                });

                buttonGroup1.add(jRadioButton2);
                jRadioButton2.setText("Digital");
                jRadioButton2.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jRadioButton2ActionPerformed(evt);
                        }
                });

                javax.swing.GroupLayout optionPanel1Layout = new javax.swing.GroupLayout(optionPanel1);
                optionPanel1.setLayout(optionPanel1Layout);
                optionPanel1Layout.setHorizontalGroup(
                        optionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(optionPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(optionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jRadioButton1)
                                        .addComponent(jRadioButton2))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
                optionPanel1Layout.setVerticalGroup(
                        optionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(optionPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jRadioButton1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jRadioButton2)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );

                setClosable(true);
                setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
                setTitle("Clock");
                setOptionPanel(optionPanel1);
                addComponentListener(new java.awt.event.ComponentAdapter() {
                        public void componentShown(java.awt.event.ComponentEvent evt) {
                                formComponentShown(evt);
                        }
                        public void componentHidden(java.awt.event.ComponentEvent evt) {
                                formComponentHidden(evt);
                        }
                });
                getContentPane().add(clockPanel1, java.awt.BorderLayout.CENTER);

                pack();
        }// </editor-fold>//GEN-END:initComponents

        private void formComponentHidden(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentHidden
		clockPanel1.stop();
        }//GEN-LAST:event_formComponentHidden

        private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
		clockPanel1.start();
        }//GEN-LAST:event_formComponentShown

        private void jRadioButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton1ActionPerformed
		optionPanel1.setDirty(true);
        }//GEN-LAST:event_jRadioButton1ActionPerformed

        private void jRadioButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton2ActionPerformed
		optionPanel1.setDirty(true);
        }//GEN-LAST:event_jRadioButton2ActionPerformed

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.ButtonGroup buttonGroup1;
        private linoleum.ClockPanel clockPanel1;
        private javax.swing.JRadioButton jRadioButton1;
        private javax.swing.JRadioButton jRadioButton2;
        private linoleum.application.OptionPanel optionPanel1;
        // End of variables declaration//GEN-END:variables
}
