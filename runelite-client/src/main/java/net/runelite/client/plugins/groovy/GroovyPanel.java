package net.runelite.client.plugins.groovy;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.plugins.groovy.debugger.DebuggerWindow;
import net.runelite.client.plugins.groovy.script.ScriptWrapper.ScriptPanel;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;


@Slf4j
public class GroovyPanel extends PluginPanel
{
	public static final ImageIcon ON_SWITCHER;
	public static final ImageIcon OFF_SWITCHER;
	public static final ImageIcon GEAR_ICON;
	public static final int CONTENT_WIDTH = 148;
	public static final int TIME_WIDTH = 20;


	public static DebuggerWindow debuggerWindow;

	public final JLabel title_label  = new JLabel("Groovy");

	static
	{
		ON_SWITCHER = new ImageIcon(ImageUtil.recolorImage(ImageUtil.getResourceStreamFromClass(GroovyCore.class, "script_on.png"), ColorScheme.BRAND_BLUE));
		OFF_SWITCHER = new ImageIcon(ImageUtil.flipImage(
			ImageUtil.luminanceScale(
				ImageUtil.grayscaleImage(ImageUtil.getResourceStreamFromClass(GroovyCore.class, "script_on.png")),
				0.61f
			),
			true,
			false
		));
		GEAR_ICON = new ImageIcon(ImageUtil.recolorImage(ImageUtil.getResourceStreamFromClass(GroovyCore.class, "config_edit_icon.png"), ColorScheme.LIGHT_GRAY_COLOR));
	}

	private final Comparator<ScriptPanel> SCRIPT_ITEM_COMPARATOR = (o1, o2) ->
	{
		return o1.getName().compareTo(o2.getName());
	};

	private final Supplier<List<ScriptPanel>> scriptSupplier;
	private GroovyCore core;

	public GroovyPanel(GroovyCore plugin, Client client, EventBus eventBus, Supplier<List<ScriptPanel>> scriptSupplier)
	{
		super();
		this.core = plugin;
		this.scriptSupplier = scriptSupplier;

		if (debuggerWindow == null)
		{
			debuggerWindow = new DebuggerWindow(client, eventBus);
		}

		setBorder(new EmptyBorder(10, 10, 10, 10));
		setBackground(ColorScheme.DARK_GRAY_COLOR);
		setLayout(new GridLayout(0, 1, 0, 4));


		title_label.setFont(FontManager.getRunescapeBoldFont());
		title_label.setBackground(Color.BLACK);
		title_label.setForeground(UIManager.getColor("Label.foreground").darker());
		title_label.setPreferredSize(new Dimension(CONTENT_WIDTH - TIME_WIDTH, 20));

		title_label.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				super.mousePressed(e);
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				super.mouseReleased(e);
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				super.mouseEntered(e);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				super.mouseExited(e);
			}
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (debuggerWindow.isVisible())
				{
					debuggerWindow.close();
				}
				else
				{
					debuggerWindow.open();
				}
				super.mouseClicked(e);
			}
		});
	}

	public void rebuildScriptPanels()
	{
		List<ScriptPanel> scripts = scriptSupplier.get();

		SwingUtilities.invokeLater(() ->
		{
			removeAll();
			this.add(title_label);

			if (scripts == null)
			{
				return;
			}

			scripts.stream()
				.sorted(SCRIPT_ITEM_COMPARATOR)
				.forEach(this::addItemToPanel);
		});
	}

	private void addItemToPanel(ScriptPanel item)
	{
		add(item);
	}
}
