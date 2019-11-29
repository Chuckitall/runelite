package net.runelite.client.plugins.fredexperimental.controller;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

@Singleton
@Slf4j
public class InterfaceOverlay extends Overlay
{
	private final ControllerPlugin plugin;

	@Setter(AccessLevel.PACKAGE)
	private boolean debugInterfaces = false;

	@Inject
	public InterfaceOverlay(final ControllerPlugin plugin)
	{
		super(plugin);
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		this.plugin = plugin;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
//		renderWidgetItemSet(graphics, plugin.getConsumedItemWidgets(), Color.ORANGE);
//		renderWidgetItemSet(graphics, plugin.getProducedItemWidgets(), Color.GREEN);
//		renderWidgetItemSet(graphics, plugin.getCatalystItemWidgets(), Color.BLUE);

		return null;
	}

	private void renderWidgetItemSet(Graphics2D graphics, Set<WidgetItem> items, Color color)
	{
		items.forEach(f -> renderWidgetItem(graphics, f, color));
	}

	private void renderWidgetItem(Graphics2D graphics, WidgetItem item, Color color)
	{
		if (item == null || item.getCanvasBounds() == null)
		{
			return;
		}
		Rectangle slotBounds = item.getCanvasBounds();

		String idText = "" + item.getId();
		FontMetrics fm = graphics.getFontMetrics();
		Rectangle2D textBounds = fm.getStringBounds(idText, graphics);

		int textX = (int) (slotBounds.getX() + (slotBounds.getWidth() / 2) - (textBounds.getWidth() / 2));
		int textY = (int) (slotBounds.getY() + (slotBounds.getHeight() / 2) + (textBounds.getHeight() / 2));

		graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 40));
		graphics.fill(slotBounds);

		graphics.setColor(Color.BLACK);
		graphics.drawString(idText, textX + 1, textY + 1);
		graphics.setColor(Color.WHITE);
		graphics.drawString(idText, textX, textY);
	}
}
