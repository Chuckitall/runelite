package net.runelite.client.plugins.fredexperimental.controller;

import com.google.common.collect.ImmutableSet;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.GameObject;
import net.runelite.api.Perspective;
import net.runelite.client.plugins.fred.api.interfaces._TileContainer;
import net.runelite.client.plugins.fred.api.wrappers._Area;
import net.runelite.client.plugins.fred.api.wrappers._Tile;
import net.runelite.client.plugins.fred.api.wrappers._TilePath;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

@Singleton
@Slf4j
public class SceneOverlay extends Overlay
{
	private final ControllerPlugin plugin;

	@Setter(AccessLevel.PACKAGE)
	private boolean debugAreas = false;

	@Setter(AccessLevel.PACKAGE)
	private boolean debugPaths = false;

	@Setter(AccessLevel.PACKAGE)
	private boolean debugGameObjects = false;

	@Setter(AccessLevel.PACKAGE)
	private boolean debugBank = false;


	@Inject
	public SceneOverlay(final ControllerPlugin plugin)
	{
		super(plugin);
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		this.plugin = plugin;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		Script s = plugin.getScript();
		if (debugAreas)
		{
			Set<_Area> areas = s.debugGetAreas(plugin.getContext());
			if (areas != null)
			{
				for (_Area area : areas)
				{
					if (area != null)
					{
						renderTiles(graphics, area, (area.contains(_Tile.ofPlayer(plugin.getClient())) ? Color.YELLOW : Color.PINK));
					}
				}
			}
		}
		if (debugPaths)
		{
			Set<_TilePath> paths = s.debugGetTilePaths(plugin.getContext());
			if (paths != null)
			{
				for (_TilePath path : paths)
				{
					_Tile next = path.getNextTile(plugin.getClient());
					renderTiles(graphics, path, Color.PINK, next);
					renderTile(graphics, next, Color.ORANGE);
				}
			}
		}

		Set<GameObject> banks = ImmutableSet.of();

		if (debugBank)
		{
			banks = plugin.getNearbyBanks();
			for (GameObject bank : banks)
			{
				renderHullOverlay(graphics, bank, Color.RED);
			}
		}

		if (debugGameObjects)
		{
			Set<GameObject> gameObjects = s.debugGetGameObject(plugin.getContext()).stream().filter(f ->
				!(f instanceof Actor)
			).collect(Collectors.toSet());
//			if (gameObjects != null)
//			{
				for (GameObject gameObject : gameObjects)
				{
					if (!debugBank || !banks.contains(gameObject))
					{
						renderHullOverlay(graphics, gameObject, new Color(205, 29, 160));
					}
				}
//			}
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

	private void renderHullOverlay(Graphics2D graphics, GameObject obj, Color color)
	{
		if (obj == null || obj.getConvexHull() == null)
		{
			return;
		}

		Shape temp = obj.getConvexHull();
		graphics.setColor(color);
		graphics.setStroke(new BasicStroke(4));
		graphics.draw(temp);
		graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 60));
		graphics.fill(temp);
	}
}