package linoleum.calendar;

import java.util.Date;
import javax.swing.ImageIcon;
import linoleum.application.Frame;

public class Calendar extends Frame {

	public Calendar() {
		initComponents();
		setDescription("calendar");
		setIcon(new ImageIcon(getClass().getResource("JCalendarColor24.png")));
	}

	@Override
	public void open() {
		jCalendar1.setDate(new Date());
	}

	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                jCalendar1 = new com.toedter.calendar.JCalendar();

                setClosable(true);
                setIconifiable(true);
                setTitle("Calendar");
                setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/com/toedter/calendar/images/JCalendarColor16.gif"))); // NOI18N

                jCalendar1.setDecorationBordersVisible(true);

                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
                getContentPane().setLayout(layout);
                layout.setHorizontalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jCalendar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                );
                layout.setVerticalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jCalendar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                );

                pack();
        }// </editor-fold>//GEN-END:initComponents

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private com.toedter.calendar.JCalendar jCalendar1;
        // End of variables declaration//GEN-END:variables
}
