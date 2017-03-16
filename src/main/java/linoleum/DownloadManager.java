package linoleum;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.MalformedURLException;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.ListCellRenderer;
import linoleum.application.Frame;

public class DownloadManager extends Frame {
	private final DefaultListModel<URL> model = new DefaultListModel<>();
	private final ListCellRenderer renderer = new Renderer();

	private class Renderer extends JPanel implements ListCellRenderer {
		final JLabel label = new JLabel();
		final JProgressBar progress = new JProgressBar();

		Renderer() {
			setLayout(new GridBagLayout());
			final GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = GridBagConstraints.RELATIVE;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1.0;
			add(label, c);
			add(progress, c);
		}

		@Override
		public Component getListCellRendererComponent(final JList list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			label.setFont(list.getFont());
			label.setText(new File(((URL) value).getPath()).getName());
			return this;
		}
	}

	public DownloadManager() {
		initComponents();
		setScheme("ftp:http:https");
		setIcon(new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Import24.gif")));
	}

	@Override
	public void open() {
		final URI uri = getURI();
		if (uri != null) try {
			final URL url = uri.toURL();
			jTextField1.setText(url.toString());
		} catch (final MalformedURLException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void close() {
		clear();
	}

	private void clear() {
		setURI(null);
		jTextField1.setText("");
	}

	private void start() {
		try {
			final URL url = new URL(jTextField1.getText());
			model.addElement(url);
		} catch (final MalformedURLException ex) {
			ex.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                jTextField1 = new javax.swing.JTextField();
                jButton1 = new javax.swing.JButton();
                jButton2 = new javax.swing.JButton();
                jScrollPane1 = new javax.swing.JScrollPane();
                jList1 = new javax.swing.JList();
                jSeparator1 = new javax.swing.JSeparator();
                jLabel1 = new javax.swing.JLabel();

                setClosable(true);
                setIconifiable(true);
                setMaximizable(true);
                setResizable(true);
                setTitle("Downloads");
                setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Import16.gif"))); // NOI18N
                setName("Downloads"); // NOI18N

                jButton1.setText("Download");
                jButton1.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jButton1ActionPerformed(evt);
                        }
                });

                jButton2.setText("Clear");
                jButton2.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jButton2ActionPerformed(evt);
                        }
                });

                jList1.setModel(model);
                jList1.setCellRenderer(renderer);
                jScrollPane1.setViewportView(jList1);

                jLabel1.setText("Location :");

                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
                getContentPane().setLayout(layout);
                layout.setHorizontalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE)
                                        .addComponent(jTextField1)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addGap(0, 0, Short.MAX_VALUE)
                                                .addComponent(jButton2)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jButton1))
                                        .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(jLabel1)
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap())
                );
                layout.setVerticalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jButton1)
                                        .addComponent(jButton2))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE)
                                .addContainerGap())
                );

                pack();
        }// </editor-fold>//GEN-END:initComponents

        private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
		clear();
        }//GEN-LAST:event_jButton2ActionPerformed

        private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
		start();
        }//GEN-LAST:event_jButton1ActionPerformed


        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JButton jButton1;
        private javax.swing.JButton jButton2;
        private javax.swing.JLabel jLabel1;
        private javax.swing.JList jList1;
        private javax.swing.JScrollPane jScrollPane1;
        private javax.swing.JSeparator jSeparator1;
        private javax.swing.JTextField jTextField1;
        // End of variables declaration//GEN-END:variables
}
