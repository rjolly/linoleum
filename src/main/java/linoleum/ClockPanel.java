package linoleum;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JPanel;

public class ClockPanel extends JPanel {
	private Timer timer; // The thread that displays clock
	private final SimpleDateFormat sf = new SimpleDateFormat("s");
	private final SimpleDateFormat mf = new SimpleDateFormat("m");
	private final SimpleDateFormat hf = new SimpleDateFormat("h");
	private final SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale.ENGLISH); // Formats the date displayed
	private final Font clockFaceFont = new Font("Serif", Font.PLAIN, 14); // Font for number display on clock
	private Date currentDate; // Used to get date to display
	private Color handColor = Color.blue; // Color of main hands and dial
	private Color numberColor = Color.darkGray; // Color of second hand and numbers
	private int xcenter = 55, ycenter = 55; // Center position
	private int width = 188;
	private int height = 17;
	private boolean analog;

	public ClockPanel() {
		try {
			setBackground(new Color(Integer.parseInt(getParameter("bgcolor"), 16)));
		} catch (NullPointerException e) {
		} catch (NumberFormatException e) {
		}
		try {
			handColor = new Color(Integer.parseInt(getParameter("fgcolor1"), 16));
		} catch (NullPointerException e) {
		} catch (NumberFormatException e) {
		}
		try {
			numberColor = new Color(Integer.parseInt(getParameter("fgcolor2"), 16));
		} catch (NullPointerException e) {
		} catch (NumberFormatException e) {
		}
	}

	public void setAnalog(final boolean analog) {
		this.analog = analog;
	}

	@Override
	public Dimension getPreferredSize() {
		return analog?new Dimension(110, 110):new Dimension(width + 10, height + 10); // Set clock window size
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setFont(clockFaceFont);
		currentDate = new Date();

		if (analog) {
			int xh, yh, xm, ym, xs, ys;
			int s = 0, m = 10, h = 10;

			// Draw the circle and numbers
			g.setColor(handColor);
			g.drawArc(xcenter - 50, ycenter - 50, 100, 100, 0, 360);
			g.setColor(numberColor);
			g.drawString("9", xcenter - 45, ycenter + 3);
			g.drawString("3", xcenter + 40, ycenter + 3);
			g.drawString("12", xcenter - 5, ycenter - 37);
			g.drawString("6", xcenter - 3, ycenter + 45);

			try {
				s = Integer.parseInt(sf.format(currentDate));
			} catch (NumberFormatException n) {
			}
			try {
				m = Integer.parseInt(mf.format(currentDate));
			} catch (NumberFormatException n) {
			}
			try {
				h = Integer.parseInt(hf.format(currentDate));
			} catch (NumberFormatException n) {
			}

			// Set position of the ends of the hands
			xs = (int) (Math.cos(s * Math.PI / 30 - Math.PI / 2) * 45 + xcenter);
			ys = (int) (Math.sin(s * Math.PI / 30 - Math.PI / 2) * 45 + ycenter);
			xm = (int) (Math.cos(m * Math.PI / 30 - Math.PI / 2) * 40 + xcenter);
			ym = (int) (Math.sin(m * Math.PI / 30 - Math.PI / 2) * 40 + ycenter);
			xh = (int) (Math.cos((h * 30 + m / 2) * Math.PI / 180 - Math.PI / 2)
					* 30
					+ xcenter);
			yh = (int) (Math.sin((h * 30 + m / 2) * Math.PI / 180 - Math.PI / 2)
					* 30
					+ ycenter);

			// Draw hands
			g.setColor(numberColor);
			g.drawLine(xcenter, ycenter, xs, ys);
			g.setColor(handColor);
			g.drawLine(xcenter, ycenter - 1, xm, ym);
			g.drawLine(xcenter - 1, ycenter, xm, ym);
			g.drawLine(xcenter, ycenter - 1, xh, yh);
			g.drawLine(xcenter - 1, ycenter, xh, yh);
		} else {
			// Get the date to print at the bottom
			final String today = formatter.format(currentDate);

			final FontMetrics fm = g.getFontMetrics();
			final int ascent = fm.getAscent();
			width = fm.stringWidth(today);
			height = fm.getHeight();

			// Draw date
			g.setColor(numberColor);
			g.drawString(today, 5, ascent + 5);
		}
	}

	public void start() {
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				repaint();
			}
		}, 0, 1000);
	}

	public void stop() {
		timer.cancel();
	}

	public String getAppletInfo() {
		return "Title: A Clock \n"
				+ "Author: Rachel Gollub, 1995 \n"
				+ "An analog clock.";
	}

	public String[][] getParameterInfo() {
		String[][] info = {
			{ "bgcolor", "hexadecimal RGB number",
				"The background color. Default is the color of your browser." },
			{ "fgcolor1", "hexadecimal RGB number",
				"The color of the hands and dial. Default is blue." },
			{ "fgcolor2", "hexadecimal RGB number",
				"The color of the second hand and numbers. Default is dark gray." }
		};
		return info;
	}

	private String getParameter(final String name) {
		return null;
	}
}
