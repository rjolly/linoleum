package linoleum;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.beans.PropertyVetoException;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import linoleum.application.FileChooser;
import linoleum.application.Frame;
import linoleum.theme.MetalThemeModel;

public class Background extends Frame {
	private final Preferences prefs = Preferences.userNodeForPackage(getClass());
	private final MetalThemeModel model = new MetalThemeModel();
	private final FileChooser chooser = new FileChooser();
	private final Color zero = new Color(0, 0, 0, 0);

	public Background() {
		initComponents();
		chooser.setFileFilter(new FileNameExtensionFilter("Images", "jpg", "gif", "png"));
	}

	@Override
	public void init() {
		prefs.addPreferenceChangeListener(new PreferenceChangeListener() {
			@Override
			public void preferenceChange(final PreferenceChangeEvent evt) {
				if (evt.getKey().equals(getKey("image"))) {
					jLabel1.setIcon(getImage());
				} else if (evt.getKey().equals(getKey("theme"))) {
					update();
				}
			}
		});
	}

	@Override
	public void select() {
		try {
			setSelected(true);
		} catch (final PropertyVetoException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void load() {
		jTextField1.setText(prefs.get(getKey("image"), ""));
		model.setSelectedItem(getTheme());
	}

	@Override
	public void save() {
		prefs.put(getKey("image"), jTextField1.getText());
		prefs.put(getKey("theme"), model.getSelectedItem());
	}

	private Icon getImage() {
		final String str = prefs.get(getKey("image"), "");
		return !str.isEmpty()?new ImageIcon(str):new ImageIcon(getClass().getResource("Wave.png"));
	}

	private String getTheme() {
		return prefs.get(getKey("theme"), "Ocean");
	}

	void update() {
		model.select(getTheme());
	}

	void resize() {
		final Dimension size = getDesktopPane().getSize();
		final Insets insets = getInsets();
		final Container panel = getContentPane();
		final int width = getWidth() - panel.getWidth();
		final int height = getHeight() - panel.getHeight();
		setBounds(-insets.left, insets.bottom - height, size.width + width, size.height + height);
	}

	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                optionPanel1 = new linoleum.application.OptionPanel();
                jLabel2 = new javax.swing.JLabel();
                jTextField1 = new javax.swing.JTextField();
                jButton1 = new javax.swing.JButton();
                jLabel3 = new javax.swing.JLabel();
                jComboBox1 = new javax.swing.JComboBox<>();
                jLabel1 = new javax.swing.JLabel();

                jLabel2.setText("Image :");

                jButton1.setText("Choose...");
                jButton1.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jButton1ActionPerformed(evt);
                        }
                });

                jLabel3.setText("Theme :");

                jComboBox1.setModel(model);
                jComboBox1.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jComboBox1ActionPerformed(evt);
                        }
                });

                javax.swing.GroupLayout optionPanel1Layout = new javax.swing.GroupLayout(optionPanel1);
                optionPanel1.setLayout(optionPanel1Layout);
                optionPanel1Layout.setHorizontalGroup(
                        optionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(optionPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(optionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jLabel3)
                                        .addComponent(jLabel2))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(optionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(optionPanel1Layout.createSequentialGroup()
                                                .addComponent(jTextField1)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(jComboBox1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addContainerGap())
                );
                optionPanel1Layout.setVerticalGroup(
                        optionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(optionPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(optionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel2)
                                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jButton1))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(optionPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel3)
                                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );

                setBackground(zero);
                setOptionPanel(optionPanel1);

                jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                jLabel1.setIcon(getImage());
                getContentPane().add(jLabel1, java.awt.BorderLayout.CENTER);

                pack();
        }// </editor-fold>//GEN-END:initComponents

        private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
		final int returnVal = chooser.showInternalOpenDialog(this);
		switch (returnVal) {
		case JFileChooser.APPROVE_OPTION:
			jTextField1.setText(chooser.getSelectedFile().getPath());
			break;
		default:
		}
        }//GEN-LAST:event_jButton1ActionPerformed

        private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
		optionPanel1.setDirty(true);
        }//GEN-LAST:event_jComboBox1ActionPerformed

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JButton jButton1;
        private javax.swing.JComboBox<String> jComboBox1;
        private javax.swing.JLabel jLabel1;
        private javax.swing.JLabel jLabel2;
        private javax.swing.JLabel jLabel3;
        private javax.swing.JTextField jTextField1;
        private linoleum.application.OptionPanel optionPanel1;
        // End of variables declaration//GEN-END:variables
}
