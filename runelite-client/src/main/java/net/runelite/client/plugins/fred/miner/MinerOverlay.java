package net.runelite.client.plugins.fred.miner;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.util.ArrayList;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameObject;
import net.runelite.client.plugins.fred.util.WorldUtils;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

import static java.awt.Color.PINK;
import static java.awt.Color.YELLOW;

/**
 * Created by npruff on 8/24/2019.
 */

@Singleton
@Slf4j
public class MinerOverlay extends Overlay
{
	private final MinerPlugin plugin;
	private WorldUtils worldUtil;

	@Inject
	private MinerOverlay(final MinerPlugin plugin)
	{
		super(plugin);
		this.plugin = plugin;
		this.worldUtil = new WorldUtils(plugin.getClient());
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		ArrayList<GameObject> objects = worldUtil.getObjects(plugin.getRockIDs().stream().mapToInt(f -> f).toArray(), 6);
		final GameObject o = plugin.getTargetRock();
		if (objects != null) objects.forEach(
				f ->
				{
					if (o != f)
					{
						renderHullOverlay(graphics, f, YELLOW);
					}
				});
		renderHullOverlay(graphics, o, PINK);
		if (plugin.getLastRockClicked() != null && plugin.getLastRockClicked().isInScene(plugin.getClient()) && plugin.getClient().getLocalPlayer() != null)
		{
			OverlayUtil.drawTiles(graphics, plugin.getClient(), plugin.getLastRockClicked(), plugin.getClient().getLocalPlayer().getWorldLocation(), Color.RED, 2, 150, 50);
		}
		return null;
	}


	private void renderHullOverlay(Graphics2D graphics, GameObject obj, Color color)
	{
		if (obj == null)
		{
			return;
		}
		Shape objectClickbox = obj.getConvexHull();
		if (objectClickbox != null)
		{
			graphics.setColor(color);
			graphics.setStroke(new BasicStroke(2));
			graphics.draw(objectClickbox);
			graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 20));
			graphics.fill(objectClickbox);
		}
	}
}

