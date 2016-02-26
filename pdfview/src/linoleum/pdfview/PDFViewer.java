package linoleum.pdfview;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import linoleum.application.Frame;

public class PDFViewer extends Frame {
	public final static String TITLE = "SwingLabs PDF Viewer";
	private int curpage = -1;
	private PDFFile curFile;
	private String docName;
	private PagePreparer pagePrep;
	private PagePanel page;

	public PDFViewer() {
		initComponents();
		setMimeType("application/pdf");
		setEnabling();
	}

	@Override
	public Frame getFrame() {
		return new PDFViewer();
	}

	@Override
	protected void open() {
		try {
			openFile(Paths.get(getURI()).toFile());
		} catch (final IOException ex) {
			ex.printStackTrace();
		}
	}

	public final void openFile(final File file) throws IOException {
		// first open the file for random access
		RandomAccessFile raf = new RandomAccessFile(file, "r");

		// extract a file channel
		try (final FileChannel channel = raf.getChannel()) {

			// now memory-map a byte-buffer
			ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
			openPDFByteBuffer(buf, file.getPath(), file.getName());
		}
	}

	private void openPDFByteBuffer(final ByteBuffer buf, final String path, final String name) throws IOException {
		final PDFFile newfile = new PDFFile(buf);

		// set up our document
		curFile = newfile;
		docName = name;
		setTitle(TITLE + ": " + docName);
		page = new PagePanel(jScrollPane1.getSize());
		jScrollPane1.setViewportView(page);

		// display page 1.
		gotoPage(0);
	}

	class PagePreparer extends Thread {
		int waitforPage;
		int prepPage;

		public PagePreparer(int waitforPage) {
			setDaemon(true);
			setName(getClass().getName());

			this.waitforPage = waitforPage;
			this.prepPage = waitforPage + 1;
		}

		public void quit() {
			waitforPage = -1;
		}

		public void run() {
			Dimension size = null;
			Rectangle2D clip = null;

			if (page != null) {
				page.waitForCurrentPage();
				size = page.getCurSize();
				clip = page.getCurClip();
			}

			if (waitforPage == curpage) {
				PDFPage pdfPage = curFile.getPage(prepPage + 1, true);
				if (pdfPage != null && waitforPage == curpage) {
					pdfPage.getImage(size.width, size.height, clip, null, true, true);
				}
			}
		}
	}

	public final void setEnabling() {
		boolean fileavailable = curFile != null;
		boolean pageshown = page != null && page.getPage() != null;

		pageField.setEnabled(fileavailable);
		prevButton.setEnabled(pageshown);
		nextButton.setEnabled(pageshown);
		firstButton.setEnabled(fileavailable);
		lastButton.setEnabled(fileavailable);
	}

	public void gotoPage(int pagenum) {
		if (pagenum <= 0) {
			pagenum = 0;
		} else if (pagenum >= curFile.getNumPages()) {
			pagenum = curFile.getNumPages() - 1;
		}

		curpage = pagenum;

		// update the page text field
		pageField.setText(String.valueOf(curpage + 1));

		// fetch the page and show it in the appropriate place
		PDFPage pg = curFile.getPage(pagenum + 1);
		page.showPage(pg);
		page.requestFocus();

		// stop any previous page prepper, and start a new one
		if (pagePrep != null) {
			pagePrep.quit();
		}
		pagePrep = new PagePreparer(pagenum);
		pagePrep.start();

		setEnabling();
	}

	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                jScrollPane1 = new javax.swing.JScrollPane();
                jPanel1 = new javax.swing.JPanel();
                firstButton = new javax.swing.JButton();
                prevButton = new javax.swing.JButton();
                pageField = new javax.swing.JTextField();
                nextButton = new javax.swing.JButton();
                lastButton = new javax.swing.JButton();

                setClosable(true);
                setIconifiable(true);
                setMaximizable(true);
                setResizable(true);
                setTitle("PDF Viewer");
                addComponentListener(new java.awt.event.ComponentAdapter() {
                        public void componentResized(java.awt.event.ComponentEvent evt) {
                                formComponentResized(evt);
                        }
                });

                firstButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/sun/pdfview/gfx/first.gif"))); // NOI18N
                firstButton.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                firstButtonActionPerformed(evt);
                        }
                });
                jPanel1.add(firstButton);

                prevButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/sun/pdfview/gfx/prev.gif"))); // NOI18N
                prevButton.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                prevButtonActionPerformed(evt);
                        }
                });
                jPanel1.add(prevButton);

                pageField.setColumns(3);
                pageField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
                pageField.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                pageFieldActionPerformed(evt);
                        }
                });
                jPanel1.add(pageField);

                nextButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/sun/pdfview/gfx/next.gif"))); // NOI18N
                nextButton.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                nextButtonActionPerformed(evt);
                        }
                });
                jPanel1.add(nextButton);

                lastButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/sun/pdfview/gfx/last.gif"))); // NOI18N
                lastButton.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                lastButtonActionPerformed(evt);
                        }
                });
                jPanel1.add(lastButton);

                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
                getContentPane().setLayout(layout);
                layout.setHorizontalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 394, Short.MAX_VALUE)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                );
                layout.setVerticalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                );

                pack();
        }// </editor-fold>//GEN-END:initComponents

        private void prevButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prevButtonActionPerformed
                gotoPage(curpage - 1);
        }//GEN-LAST:event_prevButtonActionPerformed

        private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextButtonActionPerformed
                gotoPage(curpage + 1);
        }//GEN-LAST:event_nextButtonActionPerformed

        private void firstButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_firstButtonActionPerformed
                gotoPage(0);
        }//GEN-LAST:event_firstButtonActionPerformed

        private void lastButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lastButtonActionPerformed
                gotoPage(curFile.getNumPages() - 1);
        }//GEN-LAST:event_lastButtonActionPerformed

        private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
                if (page != null) page.showPage();
        }//GEN-LAST:event_formComponentResized

        private void pageFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pageFieldActionPerformed
                gotoPage(Integer.parseInt(evt.getActionCommand()) - 1);
        }//GEN-LAST:event_pageFieldActionPerformed

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JButton firstButton;
        private javax.swing.JPanel jPanel1;
        private javax.swing.JScrollPane jScrollPane1;
        private javax.swing.JButton lastButton;
        private javax.swing.JButton nextButton;
        private javax.swing.JTextField pageField;
        private javax.swing.JButton prevButton;
        // End of variables declaration//GEN-END:variables
}
