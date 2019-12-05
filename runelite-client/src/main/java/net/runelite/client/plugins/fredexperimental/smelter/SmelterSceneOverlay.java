package net.runelite.client.plugins.fredexperimental.smelter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameObject;
import net.runelite.api.Perspective;
import net.runelite.client.plugins.fred.api.interfaces._TileContainer;
import net.runelite.client.plugins.fred.api.wrappers._Area;
import net.runelite.client.plugins.fred.api.wrappers._Tile;
import net.runelite.client.plugins.fred.api.wrappers._TilePath;
import net.runelite.client.plugins.fredexperimental.striker.StrikerUtils;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

@Singleton
@Slf4j
public class SmelterSceneOverlay extends Overlay
{
	private final SmelterPlugin plugin;


	@Setter(AccessLevel.PACKAGE)
	private boolean debugArea = false;

	@Setter(AccessLevel.PACKAGE)
	private boolean debugFurnace = false;

	@Setter(AccessLevel.PACKAGE)
	private boolean debugBank = false;

	@Setter(AccessLevel.PACKAGE)
	private boolean debugPath = false;


	@Inject
	public SmelterSceneOverlay(final SmelterPlugin plugin)
	{
		super(plugin);
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		this.plugin = plugin;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		SmelterLocation location = plugin.getLocation();
		if (location == null)
		{
			return null;
		}
		if (debugArea)
		{
			_Area area = location.getBoundsArea();
			if (area != null)
			{
				renderTiles(graphics, area, (area.contains(_Tile.ofPlayer(plugin.getClient())) ? Color.YELLOW : Color.PINK));
			}
		}
		if (debugFurnace)
		{
			GameObject booth = plugin.getBankBooth();
			if (booth != null && booth.getConvexHull() != null)
			{
				renderHullOverlay(graphics, booth.getConvexHull(), Color.BLUE);
			}
		}
		if (debugBank)
		{
			GameObject furnace = plugin.getFurnace();
			if (furnace != null && furnace.getConvexHull() != null)
			{
				renderHullOverlay(graphics, furnace.getConvexHull(), Color.RED);
			}
		}

		if (debugPath)
		{
			_TilePath path = location.getPathToFurnace();
			if (path != null)
			{
				_Tile next = path.getNextTile(plugin.getClient());
				renderTiles(graphics, path, Color.PINK, next);
				renderTile(graphics, next, Color.ORANGE);
			}
		}
		return null;
	}

	private void renderTiles(Graphics2D graphics, _TileContainer tileContainer, Color color)
	{
		this.renderTiles(graphics, tileContainer, color, null);
	}

	private void renderTiles(Graphics2D graphics, _TileContainer tileContainer, Color color, _Tile exclude)
	{
		if (tileContainer == null || tileContainer.getTiles() == null || tileContainer.getTiles().length == 0)
		{
			return;
		}
		for (_Tile t : tileContainer.getTiles())
		{
			if (exclude == null || !exclude.equals(t))
			{
				renderTile(graphics, t, color);
			}
		}
	}

	private void renderTile(Graphics2D graphics, _Tile tile, Color color)
	{
		if (tile == null || tile.toLocalPoint(plugin.getClient()) == null)
		{
			return;
		}
		Polygon poly = Perspective.getCanvasTilePoly(plugin.getClient(), tile.toLocalPoint(plugin.getClient()));
		if (poly != null)
		{
			OverlayUtil.renderPolygon(graphics, poly, color);
		}
	}

	private void renderHullOverlay(Graphics2D graphics, Shape shape, Color color)
	{
		if (shape == null)
		{
			return;
		}

		Rectangle temp = StrikerUtils.getScaledRect(shape.getBounds(), 0.5);
		graphics.setColor(color);
		graphics.setStroke(new BasicStroke(2));
		graphics.draw(temp);
		graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 20));
		graphics.fill(temp);
	}
}