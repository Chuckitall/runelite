package net.runelite.client.plugins.fred.artifact;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameObject;
import net.runelite.api.NPC;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

import static java.awt.Color.BLUE;
import static java.awt.Color.GREEN;
import static java.awt.Color.PINK;
import static java.awt.Color.RED;

@Slf4j
@Singleton
public class SceneOverlay extends Overlay
{
	private final ArtifactPlugin plugin;

	@Inject
	private SceneOverlay(final ArtifactPlugin plugin)
	{
		super(plugin);
		this.plugin = plugin;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (plugin.shouldRunPlugin())
		{
			GameObject obj = plugin.getState().getTargetObject(plugin.getClient());
			if(obj != null)
			{
				renderHullOverlay(graphics, obj.getConvexHull(), BLUE);
			}
			plugin.getGuards().stream().forEach(f -> renderHullOverlay(graphics, f.getConvexHull(), RED));
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