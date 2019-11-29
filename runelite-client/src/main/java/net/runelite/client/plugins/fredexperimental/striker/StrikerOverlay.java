package net.runelite.client.plugins.fredexperimental.striker;

import com.google.inject.Inject;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

import static java.awt.Color.PINK;

public class StrikerOverlay extends Overlay
{
	private final StrikerPlugin plugin;

	@Inject
	private StrikerOverlay(final StrikerPlugin plugin)
	{
		super(plugin);
		this.plugin = plugin;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (plugin.getClient().getCanvas().contains(plugin.getClient().getMouseCanvasPosition().asNativePoint()))
		{
			Shape shape = new Ellipse2D.Double(plugin.getClient().getMouseCanvasPosition().getX() + 0.0d, plugin.getClient().getMouseCanvasPosition().getY() + 0.0d, 8.0d, 4.0d);
			graphics.setColor(PINK);
			graphics.setStroke(new BasicStroke(2));
			graphics.draw(shape);
			graphics.setColor(new Color(PINK.getRed(), PINK.getGreen(), PINK.getBlue(), 40));
			graphics.fill(shape);
		}
		return null;
	}
}
