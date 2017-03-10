package linoleum;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import linoleum.application.Frame;

public class Console extends Frame {
	private final Preferences prefs = Preferences.userNodeForPackage(getClass());
	private final ConsolePanel panel = new ConsolePanel(prefs.getBoolean(getKey("visible"), false));
	private final DefaultComboBoxModel<Level> model = new DefaultComboBoxModel<>();
	private final Logger logger = Logger.getLogger("");

	public Console() {
		initComponents();
		setContentPane(panel);
		setIcon(new ImageIcon(getClass().getResource("/toolbarButtonGraphics/development/Host24.gif")));
		prefs.addPreferenceChangeListener(new PreferenceChangeListener() {
			@Override
			public void preferenceChange(final PreferenceChangeEvent evt) {
				if (evt.getKey().equals(getKey("level")) || evt.getKey().equals(getKey("visible"))) {
					refresh();
				}
			}
		});
		model.addElement(Level.CONFIG);
		model.addElement(Level.INFO);
		model.addElement(Level.WARNING);
		model.addElement(Level.SEVERE);
		final Handler[] handlers = logger.getHandlers();
		if (handlers.length > 0) {
			final Handler handler = handlers[0];
			if (handler.getLevel().intValue() > Level.CONFIG.intValue()) {
				handler.setLevel(Level.CONFIG);
			}
		}
	}

	@Override
	public void init() {
		refresh();
	}

	private void refresh() {
		logger.setLevel(getLevel());
		setVisible(prefs.getBoolean(getKey("visible"), false));
	}

	@Override
	public void load() {
		model.setSelectedItem(getLevel());
		jCheckBox1.setSelected(prefs.getBoolean(getKey("visible"), false));
	}

	@Override
	public void save() {
		prefs.put(getKey("level"), model.getSelectedItem().toString());
		prefs.putBoolean(getKey("visible"), jCheckBox1.isSelected());
	}

	private Level getLevel() {
		return Level.parse(prefs.get(getKey("level"), Level.INFO.toString()));
	}

	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                optionPanel1 = new linoleum.application.OptionPanel();
                jLabel1 = new javax.swing.JLabel();
                jComboBox1 = new javax.swing.JComboBox();
                jCheckBox1 = new javax.swing.JCheckBox();

                jLabel1.setText("Log level :");

                jComboBox1.setModel(model);
                jComboBox1.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jComboBox1ActionPerformed(evt);
                        }
                });

                jCheckBox1.setText("Visible");
                jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jCheckBox1ActionPerformed(evt);
                        }
                });

                javax.swing.GroupLayout optionPanel1Layout = new javax.swing.GroupLayout(optionPanel1);
                optionPanel1.setLayout(optionPanel1Layout);
                optionPanel1Layout.setHorizontalGroup(
                        optionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(optionPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(optionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(optionPanel1Layout.createSequentialGroup()
                                                .addComponent(jLabel1)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jComboBox1, 0, 295, Short.MAX_VALUE))
                                        .addGroup(optionPanel1Layout.createSequentialGroup()
                                                .addComponent(jCheckBox1)
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap())
                );
                optionPanel1Layout.setVerticalGroup(
                        optionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, optionPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jCheckBox1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(optionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel1)
                                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );

                setIconifiable(true);
                setMaximizable(true);
                setResizable(true);
                setTitle("Console");
                setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/development/Host16.gif"))); // NOI18N
                setOptionPanel(optionPanel1);

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

        private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
		optionPanel1.setDirty(true);
        }//GEN-LAST:event_jComboBox1ActionPerformed

        private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
		optionPanel1.setDirty(true);
        }//GEN-LAST:event_jCheckBox1ActionPerformed

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JCheckBox jCheckBox1;
        private javax.swing.JComboBox jComboBox1;
        private javax.swing.JLabel jLabel1;
        private linoleum.application.OptionPanel optionPanel1;
        // End of variables declaration//GEN-END:variables
}
