package net.runelite.client.plugins.hotkeys.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.client.plugins.hotkeys.HotKeysPlugin;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;

public class HotKeysOverlay extends Overlay
{

	private final PanelComponent panelComponent;
	private final HotKeysPlugin plugin;

	@Inject
	public HotKeysOverlay(final HotKeysPlugin plugin)
	{
		panelComponent = new PanelComponent();
		this.plugin = plugin;
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setPriority(OverlayPriority.HIGHEST);
		setPosition(OverlayPosition.TOP_LEFT);
		panelComponent.setPreferredSize(new Dimension(115, 0));
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!plugin.isMainOverlayOn())
		{
			return null;
		}
		panelComponent.getChildren().clear();
		Color leftColor = plugin.isHotkeysOn() ? Color.GREEN : Color.RED;
		String leftString = plugin.isHotkeysOn() ? "HOTKEYS: ON" : "HOTKEYS: OFF";
		Color rightColor = Color.GRAY;
		String rightString = plugin.isPrayFlicking() ? "p" : "  ";
		panelComponent.getChildren().add(LineComponent.builder().left(leftString).leftColor(leftColor).right(rightString).rightColor(rightColor).build());
		return panelComponent.render(graphics);
	}

}
