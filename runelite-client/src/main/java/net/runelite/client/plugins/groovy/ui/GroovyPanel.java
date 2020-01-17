package net.runelite.client.plugins.groovy.ui;

import java.awt.GridLayout;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.groovy.GroovyCore;
import net.runelite.client.ui.ColorScheme;
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

	private final Comparator<GroovyScriptPanel> SCRIPT_ITEM_COMPARATOR = (o1, o2) ->
	{
		return o1.getUuid() - o2.getUuid();
	};

	private final Supplier<List<GroovyScriptPanel>> scriptSupplier;
	private GroovyCore core;

	public GroovyPanel(GroovyCore plugin, Supplier<List<GroovyScriptPanel>> scriptSupplier)
	{
		super();
		this.core = plugin;
		this.scriptSupplier = scriptSupplier;
		setBorder(new EmptyBorder(10, 10, 10, 10));
		setBackground(ColorScheme.DARK_GRAY_COLOR);
		setLayout(new GridLayout(0, 1, 0, 4));
	}

	public void rebuildScriptPanels()
	{
		List<GroovyScriptPanel> scripts = scriptSupplier.get();

		if (scripts == null)
		{
			return;
		}

		SwingUtilities.invokeLater(() ->
		{
			removeAll();

			scripts.stream()
				.sorted(SCRIPT_ITEM_COMPARATOR)
				.forEach(this::addItemToPanel);
		});
	}

	private void addItemToPanel(GroovyScriptPanel item)
	{
		add(item);
	}
}
