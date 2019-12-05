package net.runelite.client.plugins.fred.wintertodt;

import net.runelite.api.GameObject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

import static java.awt.Color.PINK;
import static java.awt.Color.YELLOW;

/**
 * Created by npruff on 8/7/2019.
 */
@Singleton
public class FlexoOverlay extends Overlay
{
//	@Inject
//	private Client client;

	private final FredWinterPlugin plugin;
	private final FlexoController flexoController;

	@Inject
	private FlexoOverlay(final FredWinterPlugin plugin, final FlexoController flexoController)
	{
		super(plugin);
		this.plugin = plugin;
		this.flexoController = flexoController;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!plugin.isInWintertodt())
		{
			return null;
		}
		renderHullOverlay(graphics, flexoController.getTarget_brazier(), PINK);
		renderHullOverlay(graphics, flexoController.getTarget_root(), YELLOW);
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
