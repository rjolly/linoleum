package linoleum;

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.Preferences;
import linoleum.application.PreferenceSupport;

public class Clock extends PreferenceSupport {
	public Clock() {
		initComponents();
		setDescription("clock");
		refresh();
	}

	private void refresh() {
		clockPanel1.setAnalog(getBooleanPref("analog"));
		pack();
	}

	@Override
	public void init() {
		Preferences.userNodeForPackage(getClass()).addPreferenceChangeListener(this);
	}

	@Override
	public void preferenceChange(final PreferenceChangeEvent evt) {
		if (evt.getKey().equals(getKey("analog"))) {
			refresh();
		}
	}

	@Override
	public void load() {
		jRadioButton1.setSelected(getBooleanPref("analog"));
	}

	@Override
	public void save() {
		putBooleanPref("analog", jRadioButton1.isSelected());
	}

	@Override
	public void open() {
		clockPanel1.start();
	}

	@Override
	public void close() {
		clockPanel1.stop();
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
                setTitle("Clock");
                setOptionPanel(optionPanel1);
                getContentPane().add(clockPanel1, java.awt.BorderLayout.CENTER);

                pack();
        }// </editor-fold>//GEN-END:initComponents

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
