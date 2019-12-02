package net.runelite.client.plugins.fred.artifact;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameObject;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
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
		if(plugin.getState() != null)
		{
			List<GameObject> objs = plugin.getInterestingObjects();
			if (objs != null)
			{
				for (GameObject obj : objs)
				{
					if(obj.getPlane() == plugin.getClient().getPlane())
					{
						renderHullOverlay(graphics, obj.getConvexHull(), BLUE);
					}
				}
			}
		}
		plugin.getGuardVision().forEach(tile -> drawTile(graphics, tile, RED, 1, 255, 50));
		//renderScaryTiles(graphics, plugin.getGuards(), RED);
		//plugin.getGuards().forEach(f -> renderGuard(graphics, f, RED));
		return null;
	}

	private void renderScaryTiles(Graphics2D graphics, List<NPC> actors, Color color)
	{
		final Set<WorldPoint> wp = new HashSet<>();

		actors.stream().flatMap(f -> getGuardLineOfSight(f, 3).stream()).distinct().forEach(tile -> drawTile(graphics, tile, color, 1, 255, 50));
	}

	private List<WorldPoint> getGuardLineOfSight(NPC guard, int range)
	{
		List<WorldPoint> toRet = new ArrayList<>();

		WorldArea area = guard.getWorldArea();
		for (int x = area.getX() - range; x <= area.getX() + range; x++)
		{
			for (int y = area.getY() - range; y <= area.getY() + range; y++)
			{
				WorldPoint targetLocation = new WorldPoint(x, y, area.getPlane());
				if (toRet.contains(targetLocation))
				{
					continue;
				}
				if (area.hasLineOfSightTo(plugin.getClient(), targetLocation))
				{
					toRet.add(targetLocation);
				}
			}
		}
		return toRet;
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

	private void renderPoly(Graphics2D graphics, Color color, Polygon polygon)
	{
		if (polygon != null)
		{
			graphics.setColor(color);
			graphics.setStroke(new BasicStroke(2));
			graphics.draw(polygon);
			graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 20));
			graphics.fill(polygon);
		}
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