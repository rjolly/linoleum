package linoleum.mail;

import java.awt.*;
import javax.mail.*;
import javax.activation.*;
import java.util.Date;
import java.io.IOException;

public class MessageViewer extends javax.swing.JPanel implements Viewer {
	Message displayed;
	Component mainbody;

	public MessageViewer() {
		initComponents();
		setMessage(null);
	}

	public final void setMessage(final Message what) {
		displayed = what;

		if (mainbody != null) {
			jPanel1.remove(mainbody);
		}
		if (what != null) {
			loadHeaders();

			// add the main body
			jPanel1.add(mainbody = getBodyComponent());
		} else {
			headers.setText("");
		}

		invalidate();
		validate();
		scrollToOrigin();
	}

	public void scrollToOrigin() {
		if (mainbody instanceof Viewer) {
			((Viewer)mainbody).scrollToOrigin();
		}
	}

	private void loadHeaders() {
		// setup what we want in our viewer
		final StringBuffer sb = new StringBuffer();

		// date
		sb.append("Date: ");
		try {
			final Date duh = displayed.getSentDate();
			if (duh != null) {
				sb.append(duh.toString());
			} else {
				sb.append("Unknown");
			}

			sb.append("\n");

			// from
			sb.append("From: ");
			Address[] adds = displayed.getFrom();
			if (adds != null && adds.length > 0) {
				sb.append(adds[0].toString());
			}
			sb.append("\n");

			// to
			sb.append("To: ");
			adds = displayed.getRecipients(Message.RecipientType.TO);
			if (adds != null && adds.length > 0) {
				sb.append(adds[0].toString());
			}
			sb.append("\n");

			// subject
			sb.append("Subject: ");
			sb.append(displayed.getSubject());

			headers.setText(sb.toString());
		} catch (final MessagingException me) {
			headers.setText("");
		}
	}

	private Component getBodyComponent() {
		//------------
		// now get a content viewer for the main type...
		//------------
		try {
			final DataHandler dh = displayed.getDataHandler();
			final CommandInfo ci = dh.getCommand("view");
			if (ci == null) {
				throw new MessagingException("view command failed on: " + displayed.getContentType());
			}
			final Object bean = dh.getBean(ci);
			if (bean instanceof Component) {
				return (Component)bean;
			} else {
				throw new MessagingException("bean is not a component: " + bean);
			}
		} catch (final MessagingException me) {
			return new Label(me.toString());
		}
	}

	public void setCommandContext(final String verb, final DataHandler dh) throws IOException {
		final Object o = dh.getContent();
		if (o instanceof Message) {
			setMessage((Message)o);
		} else {
			System.out.println("MessageViewer - content not a Message object, " + o);
			if (o != null) {
				System.out.println(o.getClass());
			}
		}
	}

	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                jButton1 = new javax.swing.JButton();
                jScrollPane1 = new javax.swing.JScrollPane();
                headers = new javax.swing.JTextArea();
                jButton2 = new javax.swing.JButton();
                jButton3 = new javax.swing.JButton();
                jPanel1 = new javax.swing.JPanel();
                jButton4 = new javax.swing.JButton();
                jButton5 = new javax.swing.JButton();

                jButton1.setText("Compose");
                jButton1.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jButton1ActionPerformed(evt);
                        }
                });

                headers.setEditable(false);
                headers.setRows(4);
                jScrollPane1.setViewportView(headers);

                jButton2.setText("Reply");
                jButton2.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jButton2ActionPerformed(evt);
                        }
                });

                jButton3.setText("Reply to all");
                jButton3.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jButton3ActionPerformed(evt);
                        }
                });

                jPanel1.setLayout(new java.awt.CardLayout());

                jButton4.setText("Delete");
                jButton4.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jButton4ActionPerformed(evt);
                        }
                });

                jButton5.setText("Structure");
                jButton5.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jButton5ActionPerformed(evt);
                        }
                });

                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
                this.setLayout(layout);
                layout.setHorizontalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane1)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(jButton1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton5)
                                .addContainerGap(32, Short.MAX_VALUE))
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                );
                layout.setVerticalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jButton1)
                                        .addComponent(jButton2)
                                        .addComponent(jButton3)
                                        .addComponent(jButton4)
                                        .addComponent(jButton5))
                                .addGap(1, 1, 1)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE))
                );
        }// </editor-fold>//GEN-END:initComponents

        private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
		System.out.println("Compose");
        }//GEN-LAST:event_jButton1ActionPerformed

        private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
		System.out.println("Reply");
        }//GEN-LAST:event_jButton2ActionPerformed

        private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
		System.out.println("Replay to all");
        }//GEN-LAST:event_jButton3ActionPerformed

        private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
		System.out.println("Delete");
        }//GEN-LAST:event_jButton4ActionPerformed

        private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
		System.out.println("\nMessage Structure");
		if (displayed != null) {
			dumpPart("", displayed);
		}
        }//GEN-LAST:event_jButton5ActionPerformed

	private void dumpPart(final String prefix, final Part p) {
		try {
			System.out.println(prefix + "----------------");
			System.out.println(prefix + "Content-Type: " + p.getContentType());
			System.out.println(prefix + "Class: " + p.getClass());

			final Object o = p.getContent();
			System.out.println(prefix + "Content: " + (o == null?"is null":o.getClass()));
			if (o instanceof Multipart) {
				final String newpref = prefix + "\t";
				final Multipart mp = (Multipart)o;
				final int count = mp.getCount();
				for (int i = 0; i < count; i++) {
					dumpPart(newpref, mp.getBodyPart(i));
				}
			}
		} catch (final MessagingException e) {
			e.printStackTrace();
		} catch (final IOException ioex) {
			System.out.println("Cannot get content" + ioex.getMessage());
		}
	}

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JTextArea headers;
        private javax.swing.JButton jButton1;
        private javax.swing.JButton jButton2;
        private javax.swing.JButton jButton3;
        private javax.swing.JButton jButton4;
        private javax.swing.JButton jButton5;
        private javax.swing.JPanel jPanel1;
        private javax.swing.JScrollPane jScrollPane1;
        // End of variables declaration//GEN-END:variables
}
