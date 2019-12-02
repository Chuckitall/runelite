package net.runelite.client.plugins.fred.artifact;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameObject;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

import static java.awt.Color.BLUE;
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
		if (!plugin.shouldRunPlugin())
		{
			return null;
		}
		Optional<Shape> shape =  plugin.getTargetObject().map(GameObject::getConvexHull);
		if (!shape.isPresent())
		{
			shape = plugin.getTargetNPC().map(NPC::getConvexHull);
		}
		shape.ifPresent(value -> renderHullOverlay(graphics, value, BLUE));
		plugin.getGuardVision().forEach(tile -> drawTile(graphics, tile, RED, 1, 255, 50));
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

	private void drawTile(Graphics2D graphics, WorldPoint point, Color color, int strokeWidth, int outlineAlpha, int fillAlpha)
	{
		WorldPoint playerLocation = Objects.requireNonNull(plugin.getClient().getLocalPlayer()).getWorldLocation();

		if (point.distanceTo(playerLocation) >= 32)
		{
			return;
		}

		LocalPoint lp = LocalPoint.fromWorld(plugin.getClient(), point);

		if (lp == null)
		{
			return;
		}

		Polygon poly = Perspective.getCanvasTilePoly(plugin.getClient(), lp);

		if (poly == null)
		{
			return;
		}

		graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), outlineAlpha));
		graphics.setStroke(new BasicStroke(strokeWidth));
		graphics.draw(poly);
		graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), fillAlpha));
		graphics.fill(poly);
	}
}
