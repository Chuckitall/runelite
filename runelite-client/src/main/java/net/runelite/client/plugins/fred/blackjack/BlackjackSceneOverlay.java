package net.runelite.client.plugins.fred.blackjack;

import com.google.inject.Inject;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Point;
import net.runelite.client.flexo.Flexo;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;


import static java.awt.Color.GREEN;
import static java.awt.Color.PINK;
import static java.awt.Color.RED;

@Slf4j
public class BlackjackSceneOverlay extends Overlay
{
	private final FredsBlackjackPlugin plugin;

	@Inject
	private BlackjackSceneOverlay(final FredsBlackjackPlugin plugin)
	{
		super(plugin);
		this.plugin = plugin;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (plugin.isFuck())
		{
			final Color color = graphics.getColor();
			graphics.setColor(new Color(255, 0, 0, 70));
			graphics.fill(new Rectangle(Flexo.client.getCanvas().getSize()));
			graphics.setColor(color);
		}
		else if (plugin.getTarget() != null)
		{
			if (plugin.getTarget().getConvexHull() != null)
			{
				renderHullOverlay(graphics, plugin.getTarget().getConvexHull(), PINK);
			}
			if (plugin.getClickArea() != null)
			{
				renderHullOverlay(graphics, plugin.getClickArea(), (plugin.getClickArea() != null && plugin.getClickArea().contains(Point.toNative(Flexo.client.getMouseCanvasPosition()))) ? GREEN : RED);
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
