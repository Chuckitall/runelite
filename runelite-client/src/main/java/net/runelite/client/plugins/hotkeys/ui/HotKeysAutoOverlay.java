package net.runelite.client.plugins.hotkeys.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.List;
import javax.inject.Inject;
import net.runelite.client.plugins.hotkeys.HotKeysPlugin;
import net.runelite.client.plugins.hotkeys.script.HotKeysAutoScript;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;

public class HotKeysAutoOverlay extends Overlay
{

	private final PanelComponent panelComponent;
	private final HotKeysPlugin plugin;

	@Inject
	public HotKeysAutoOverlay(final HotKeysPlugin plugin)
	{
		panelComponent = new PanelComponent();
		this.plugin = plugin;
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setPriority(OverlayPriority.HIGHEST);
		setPosition(OverlayPosition.BOTTOM_LEFT);
		setPreferredSize(new Dimension(85, 0));
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!plugin.isAutoOverlayOn())
		{
			return null;
		}
		panelComponent.getChildren().clear();
		List<HotKeysAutoScript> autoScripts = plugin.getAutoScripts();
		graphics.setFont(FontManager.getRunescapeSmallFont());
		for (HotKeysAutoScript autoScript : autoScripts)
		{
			Color leftColor = autoScript.isEnabled() ? Color.GREEN : Color.RED;
			Color rightColor = Color.GRAY;
			String leftText = autoScript.getName();
			String rightText = "[ " + autoScript.getHotkey().toString() + " ]";
			panelComponent.getChildren().add(LineComponent.builder().left(leftText).leftColor(leftColor).right(rightText).rightColor(rightColor).build());
		}
		return panelComponent.render(graphics);
	}
}
