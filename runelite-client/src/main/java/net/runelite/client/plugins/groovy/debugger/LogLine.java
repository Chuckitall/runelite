package net.runelite.client.plugins.groovy.debugger;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import javax.swing.plaf.LabelUI;
import net.runelite.client.plugins.groovy.debugger.DebuggerWindow.LogLevel;
import org.pushingpixels.substance.api.UiThreadingViolationException;

public class LogLine
{
	private static final Toolkit toolkit = Toolkit.getDefaultToolkit();
	private static final Clipboard clipboard = toolkit.getSystemClipboard();

	String owner;
	LogLevel severity;
	String message;
	JLogLineLabel label;

	public JLabel getJLabel()
	{
		if (label == null)
		{
			try
			{
				label = new JLogLineLabel();
			}
			catch(UiThreadingViolationException e)
			{
				e.printStackTrace();
			}
		}
		return label;
	}

	public LogLine(LogLevel severity, String owner, String message)
	{
		this.severity = severity;
		this.owner = owner;
		this.message = message;
	}

	class JLogLineLabel extends JLabel
	{
		boolean mouseOver = false;

		/**
		 * Returns the L&amp;F object that renders this component.
		 *
		 * @return LabelUI object
		 */
		public LabelUI getUI() {
			return (LabelUI)ui;
		}

		JLogLineLabel()
		{
			super(String.format("%s: %s", owner, message));
			this.setForeground(Color.WHITE);
			this.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mouseClicked(MouseEvent e)
				{
					clipboard.setContents(new StringSelection(message), null);
					super.mouseClicked(e);
				}

				@Override
				public void mouseEntered(MouseEvent e)
				{
					mouseOver = true;
					repaint();
					super.mouseEntered(e);
				}

				@Override
				public void mouseExited(MouseEvent e)
				{
					mouseOver = false;
					repaint();
					super.mouseExited(e);
				}
			});
		}

		@Override
		protected void paintComponent(Graphics g)
		{
			//You may also need to account for some offsets here:
			int startX = getFontMetrics(getFont()).stringWidth(owner + ": ");
			int startY = 0; //You probably have some vertical offset to add here.
			int length = getFontMetrics(getFont()).stringWidth(message);
			int height = getFontMetrics(getFont()).getHeight();
			Color color = new Color(severity.getColor().getRed(), severity.getColor().getGreen(), severity.getColor().getBlue(), mouseOver ? 80 : 40);
			g.setColor(color);
			g.fillRect(startX, startY, length, height);
			super.paintComponent(g);
		}
	}
}
