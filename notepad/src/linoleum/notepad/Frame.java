package linoleum.notepad;

import java.awt.Dimension;
import java.awt.Point;
import java.beans.PropertyVetoException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import linoleum.application.FileChooser;

public class Frame extends linoleum.application.Frame {
	private final FileChooser chooser = new FileChooser();
	private final Notepad notepad = new Notepad(this);
	protected Frame parent;
	private boolean found;

	public Frame() {
		this(null);
	}

	public Frame(final linoleum.application.Frame parent) {
		super(parent, Notepad.resources.getString("Title"));
		initComponents();
		setIcon(new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Edit24.gif")));
		setMimeType("text/*");
		jInternalFrame1.pack();
		getContentPane().add("Center", notepad);
		setJMenuBar(notepad.createMenubar());
		setSize(500, 400);
		chooser.setFileFilter(new FileNameExtensionFilter("Text", "txt"));
		this.parent = (Frame) parent;
	}

	@Override
	public void open() {
		notepad.open();
	}

	@Override
	public void setURI(final URI uri) {
		notepad.setFile(Paths.get(uri));
	}

	@Override
	public URI getURI() {
		final Path file = notepad.getFile();
		return file == null?null:file.toUri();
	}

	@Override
	public Frame getFrame(final linoleum.application.Frame parent) {
		return new Frame(parent);
	}

	private void openDialog(final String title) {
		if (jInternalFrame1.getDesktopPane() == null) {
			getDesktopPane().add(jInternalFrame1);
			setDialogLocation(jInternalFrame1);
			jInternalFrame1.setLayer(2);
		}
		jInternalFrame1.setTitle(Notepad.resources.getString(title));
		jInternalFrame1.setVisible(true);
	}

	private void setDialogLocation(final JInternalFrame dialog) {
		final Dimension s = dialog.getSize();
		final Dimension size = getSize();
		final int x = (size.width - s.width) / 2;
		final int y = (size.height - s.height) / 2;
		final Point point = SwingUtilities.convertPoint(this, x, y, getDesktopPane());
		dialog.setLocation(point.x, point.y);
	}

	void find() {
		openDialog("FindTitle");
		jTextField2.setEnabled(false);
		jButton2.setEnabled(false);
		jButton3.setEnabled(false);
	}

	void replace() {
		openDialog("ReplaceTitle");
		jTextField2.setEnabled(true);
		jButton2.setEnabled(true);
		jButton3.setEnabled(true);
	}

	void closeDialog() {
		if (jInternalFrame1.isVisible()) {
			jInternalFrame1.setVisible(false);
		}
	}

	FileChooser getFileChooser() {
		return parent == null?chooser:parent.chooser;
	}

	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                jInternalFrame1 = new javax.swing.JInternalFrame();
                jLabel1 = new javax.swing.JLabel();
                jTextField1 = new javax.swing.JTextField();
                jLabel2 = new javax.swing.JLabel();
                jTextField2 = new javax.swing.JTextField();
                jButton1 = new javax.swing.JButton();
                jButton2 = new javax.swing.JButton();
                jButton3 = new javax.swing.JButton();
                jButton4 = new javax.swing.JButton();

                jInternalFrame1.setClosable(true);
                jInternalFrame1.setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);

                jLabel1.setText("Find :");

                jLabel2.setText("Replace with :");

                jButton1.setText("Next");
                jButton1.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jButton1ActionPerformed(evt);
                        }
                });

                jButton2.setText("Replace");
                jButton2.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jButton2ActionPerformed(evt);
                        }
                });

                jButton3.setText("Replace all");
                jButton3.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jButton3ActionPerformed(evt);
                        }
                });

                jButton4.setText("Done");
                jButton4.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jButton4ActionPerformed(evt);
                        }
                });

                javax.swing.GroupLayout jInternalFrame1Layout = new javax.swing.GroupLayout(jInternalFrame1.getContentPane());
                jInternalFrame1.getContentPane().setLayout(jInternalFrame1Layout);
                jInternalFrame1Layout.setHorizontalGroup(
                        jInternalFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jInternalFrame1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jInternalFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jTextField1)
                                        .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jTextField2)
                                        .addGroup(jInternalFrame1Layout.createSequentialGroup()
                                                .addComponent(jButton1)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jButton2)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jButton3)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                .addContainerGap())
                );
                jInternalFrame1Layout.setVerticalGroup(
                        jInternalFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jInternalFrame1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jInternalFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jButton1)
                                        .addComponent(jButton2)
                                        .addComponent(jButton3)
                                        .addComponent(jButton4))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );

                setName(Notepad.class.getSimpleName());
                addInternalFrameListener(new javax.swing.event.InternalFrameListener() {
                        public void internalFrameOpened(javax.swing.event.InternalFrameEvent evt) {
                        }
                        public void internalFrameClosing(javax.swing.event.InternalFrameEvent evt) {
                        }
                        public void internalFrameClosed(javax.swing.event.InternalFrameEvent evt) {
                        }
                        public void internalFrameIconified(javax.swing.event.InternalFrameEvent evt) {
                                formInternalFrameIconified(evt);
                        }
                        public void internalFrameDeiconified(javax.swing.event.InternalFrameEvent evt) {
                        }
                        public void internalFrameActivated(javax.swing.event.InternalFrameEvent evt) {
                        }
                        public void internalFrameDeactivated(javax.swing.event.InternalFrameEvent evt) {
                        }
                });
                addVetoableChangeListener(new java.beans.VetoableChangeListener() {
                        public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                                formVetoableChange(evt);
                        }
                });

                pack();
        }// </editor-fold>//GEN-END:initComponents

        private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
		try {
			found = notepad.getEditor().findNext(jTextField1.getText(), true);
			setSelected(true);
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
        }//GEN-LAST:event_jButton1ActionPerformed

        private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
		if (found) try {
			found = notepad.getEditor().replace(jTextField1.getText(), jTextField2.getText());
			setSelected(true);
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
        }//GEN-LAST:event_jButton2ActionPerformed

        private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
		try {
			notepad.getEditor().replaceAll(jTextField1.getText(), jTextField2.getText());
			setSelected(true);
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
        }//GEN-LAST:event_jButton3ActionPerformed

        private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
		try {
			jInternalFrame1.setVisible(false);
			setSelected(true);
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
        }//GEN-LAST:event_jButton4ActionPerformed

        private void formInternalFrameIconified(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameIconified
		closeDialog();
        }//GEN-LAST:event_formInternalFrameIconified

        private void formVetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {//GEN-FIRST:event_formVetoableChange
		if (IS_CLOSED_PROPERTY.equals(evt.getPropertyName()) && (Boolean) evt.getNewValue()) {
			closeDialog();
			if (!notepad.proceed()) {
				throw new PropertyVetoException("aborted", evt);
			} else {
				jInternalFrame1.setClosed(true);
			}
		}
        }//GEN-LAST:event_formVetoableChange

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JButton jButton1;
        private javax.swing.JButton jButton2;
        private javax.swing.JButton jButton3;
        private javax.swing.JButton jButton4;
        private javax.swing.JInternalFrame jInternalFrame1;
        private javax.swing.JLabel jLabel1;
        private javax.swing.JLabel jLabel2;
        private javax.swing.JTextField jTextField1;
        private javax.swing.JTextField jTextField2;
        // End of variables declaration//GEN-END:variables
}
