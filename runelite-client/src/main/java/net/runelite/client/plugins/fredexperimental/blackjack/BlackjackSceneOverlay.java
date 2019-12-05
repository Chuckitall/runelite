package net.runelite.client.plugins.fredexperimental.blackjack;

import com.google.inject.Inject;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

import static java.awt.Color.BLUE;
import static java.awt.Color.GREEN;
import static java.awt.Color.ORANGE;
import static java.awt.Color.RED;

@Slf4j
public class BlackjackSceneOverlay extends Overlay
{
	private final FredsBlackjackPlugin2 plugin;

	@Inject
	private BlackjackSceneOverlay(final FredsBlackjackPlugin2 plugin)
	{
		super(plugin);
		this.plugin = plugin;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (plugin.getTarget() != null)
		{
			if (plugin.getTarget().getConvexHull() != null)
			{
				renderHullOverlay(graphics, plugin.getTarget().getConvexHull(), plugin.getTarget().getConvexHull().contains(plugin.getClient().getMouseCanvasPosition().asNativePoint()) ? BLUE : ORANGE);
			}
			if (plugin.getTargetArea() != null)
			{
				renderHullOverlay(graphics, plugin.getTargetArea(), plugin.getTargetArea().contains(plugin.getClient().getMouseCanvasPosition().asNativePoint()) ? GREEN : RED);
			}
		}
		return null;
	}

	private void renderHullOverlay(Graphics2D graphics, Shape shape, Color color)
	{
		if (shape == null)
		{
			return;
		}
		graphics.setColor(color);
		graphics.setStroke(new BasicStroke(2));
		graphics.draw(shape);
		graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 20));
		graphics.fill(shape);
	}
}
