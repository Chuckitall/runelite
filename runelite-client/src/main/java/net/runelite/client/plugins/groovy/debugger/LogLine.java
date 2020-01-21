package net.runelite.client.plugins.groovy.debugger;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.print.attribute.standard.Severity;
import javax.swing.JLabel;
import net.runelite.client.RuneLite;
import net.runelite.client.plugins.fred.util.Random;
import net.runelite.client.plugins.groovy.debugger.DebuggerWindow.LogLevel;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.skin.SubstanceRuneLiteLookAndFeel;

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
		return label;
	}

	public LogLine(LogLevel severity, String owner, String message)
	{
		this.severity = severity;
		this.owner = owner;
		this.message = message;
		this.label = new JLogLineLabel();

	}

	public LogLine(GroovyLogEvent e)
	{
		this(e.getLogLevel(), e.getName(), e.getMessage());
	}

	class JLogLineLabel extends JLabel
	{
//		JLabel line = new JLabel(String.format("%s: %s", e.getName(), e.getMessage()));
//		line.setOpaque(true);
//		line.setForeground(e.getLogLevel().getColor());
//		line.setBackground(new Color(Random.nextInt(120, 255), Random.nextInt(120, 255), Random.nextInt(120, 255),

		boolean mouseOver = false;

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
			super.paintComponent(g);
			//You may also need to account for some offsets here:
			int startX = getFontMetrics(getFont()).stringWidth(owner + ": ");
			int startY = 0; //You probably have some vertical offset to add here.
			int length = getFontMetrics(getFont()).stringWidth(message);
			int height = getFontMetrics(getFont()).getHeight();
			Color color = new Color(severity.getColor().getRed(), severity.getColor().getGreen(), severity.getColor().getBlue(), mouseOver ? 80 : 40);
			g.setColor(color);
			g.fillRect(startX, startY, length, height);
		}
	}
}
