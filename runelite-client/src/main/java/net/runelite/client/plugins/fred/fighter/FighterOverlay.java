package net.runelite.client.plugins.fred.fighter;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.NPC;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

import static java.awt.Color.YELLOW;

/**
 * Created by npruff on 8/24/2019.
 */

@Singleton
@Slf4j
public class FighterOverlay extends Overlay
{
	private FighterPlugin plugin;

	@Inject
	FighterOverlay(FighterPlugin plugin)
	{
		super(plugin);
		this.plugin = plugin;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (plugin.isHighlightTargets() || plugin.isHighlightTarget())
		{
			List<NPC> targets = plugin.getTargets();
			for (NPC target : targets)
			{
				if (target == null || target.getName() == null)
				{
					continue;
				}

				if (plugin.isHighlightTarget() && target.equals(plugin.getTarget()))
				{
					renderNpcOverlay(graphics, target, plugin.getTargetColor());
					continue;
				}

				if (plugin.isHighlightTargets())
				{
					renderNpcOverlay(graphics, target, plugin.getTargetsColor());
				}
			}
		}

		if (plugin.getFightingWith() != null && !plugin.getTargets().contains(plugin.getFightingWith()))
		{
			renderNpcOverlay(graphics, plugin.getFightingWith(), YELLOW);
		}

		return null;
	}

	private void renderNpcOverlay(Graphics2D graphics, NPC actor, Color color)
	{
		final Point minimapLocation = actor.getMinimapLocation();

		if (minimapLocation != null)
		{
			OverlayUtil.renderMinimapLocation(graphics, minimapLocation, color.darker());
			OverlayUtil.renderTextLocation(graphics, minimapLocation, "Target", color);
		}

		Shape objectClickbox = actor.getConvexHull();
		if (objectClickbox != null)
		{
			OverlayUtil.renderPolygon(graphics, objectClickbox, color);
		}
	}
}


