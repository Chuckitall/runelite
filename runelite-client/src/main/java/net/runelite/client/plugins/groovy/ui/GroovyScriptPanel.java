package net.runelite.client.plugins.groovy.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.runelite.client.ui.FontManager;

import static net.runelite.client.plugins.groovy.ui.GroovyPanel.CONTENT_WIDTH;
import static net.runelite.client.plugins.groovy.ui.GroovyPanel.OFF_SWITCHER;
import static net.runelite.client.plugins.groovy.ui.GroovyPanel.ON_SWITCHER;
import static net.runelite.client.plugins.groovy.ui.GroovyPanel.TIME_WIDTH;

public class GroovyScriptPanel extends JPanel
{
	static Color ENABLED_BACKGROUND_COLOR = new Color(30, 125, 30);
	static Color DISABLED_BACKGROUND_COLOR = new Color(125, 30, 30);

	@Getter(AccessLevel.PUBLIC)
	private final int uuid;

	@Getter(AccessLevel.PUBLIC)
	@Setter(AccessLevel.PUBLIC)
	private Color color = DISABLED_BACKGROUND_COLOR;

	public JLabel avatar; //on off toggle
	public JLabel gear; //configure

	public JLabel titleLabel;
	private JPanel upAndContent;
	private JPanel buttonsContent;



	public GroovyScriptPanel(int uuid, String name)
	{
		super(new BorderLayout());
		this.uuid = uuid;
		this.setPreferredSize(new Dimension(0, 56));

		buttonsContent = new JPanel();
		buttonsContent.setLayout(new BorderLayout());
		buttonsContent.setBorder(new EmptyBorder(4, 8, 4, 4));
		buttonsContent.setBackground(null);

		avatar = new JLabel();
		// width = 48+4 to compensate for the border
		avatar.setPreferredSize(new Dimension(29, 15));
		avatar.setBorder(new EmptyBorder(0, 4, 0, 0));

		gear = new JLabel();
		// width = 48+4 to compensate for the border
		gear.setPreferredSize(new Dimension(29, 15));
		gear.setBorder(new EmptyBorder(0, 4, 0, 0));

		buttonsContent.add(avatar, BorderLayout.WEST);
		buttonsContent.add(gear, BorderLayout.EAST);


		upAndContent = new JPanel();
		upAndContent.setLayout(new BoxLayout(upAndContent, BoxLayout.Y_AXIS));
		upAndContent.setBorder(new EmptyBorder(4, 8, 4, 4));
		upAndContent.setBackground(null);

		Color darkerForeground = UIManager.getColor("Label.foreground").darker();

		titleLabel = new JLabel(name);
		titleLabel.setFont(FontManager.getRunescapeSmallFont());
		titleLabel.setBackground(null);
		titleLabel.setForeground(darkerForeground);
		titleLabel.setPreferredSize(new Dimension(CONTENT_WIDTH - TIME_WIDTH, 0));

		upAndContent.add(titleLabel);
		upAndContent.add(new Box.Filler(new Dimension(0, 0),
			new Dimension(0, Short.MAX_VALUE),
			new Dimension(0, Short.MAX_VALUE)));
		upAndContent.add(buttonsContent);

		this.add(upAndContent, BorderLayout.CENTER);
//		this.add(buttonsContent, BorderLayout.CENTER);
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e)
			{
				setBackground(color.brighter().brighter());
				super.mouseEntered(e);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				setBackground(color);
				super.mouseExited(e);
			}

			@Override
			public void mousePressed(MouseEvent e)
			{
				setBackground(color.brighter().brighter().brighter());
				super.mousePressed(e);
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				setBackground(color.brighter().brighter());
				super.mouseReleased(e);
			}
		});
	}

	public void setEnabled(boolean enabled)
	{
		if (enabled)
		{
			setColor(ENABLED_BACKGROUND_COLOR);
			avatar.setIcon(ON_SWITCHER);
		}
		else
		{
			setColor(DISABLED_BACKGROUND_COLOR);
			avatar.setIcon(OFF_SWITCHER);
		}
		setBackground(color);
	}
}
