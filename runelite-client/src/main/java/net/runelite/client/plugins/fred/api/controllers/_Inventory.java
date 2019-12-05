package net.runelite.client.plugins.fred.api.controllers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.awt.Rectangle;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.plugins.fred.api.wrappers._WidgetItem;
import net.runelite.client.plugins.fredexperimental.striker.Striker;

@Singleton
@Slf4j
public class _Inventory
{
	private Client client;

	@Inject
	private _Vision vision;

	@Inject
	public _Inventory(Client client)
	{
		this.client = client;
	}

	public _WidgetItem getInventoryItem(int id)
	{
		return getInvItems().stream().filter(f -> f.getId() == id).findFirst().orElse(null);
	}

	public List<_WidgetItem> getInvItems()
	{
		Widget inv = client.getWidget(WidgetInfo.INVENTORY);
		List<_WidgetItem> invItems = Lists.newArrayList();
		if (inv != null && !inv.isHidden())
		{
			log.debug("Inventory visible");
			inv.revalidate();
			WidgetItem[] children = inv.getWidgetItems().toArray(new WidgetItem[0]);
			for (WidgetItem child : children)
			{
				if (child.getWidget().isSelfHidden())
				{
					continue;
				}
				child.getWidget().revalidate();
				Rectangle bounds = child.getCanvasBounds();
				//log.debug("child[{}] -> bounds: {}", child.getIndex(), bounds);
				invItems.add(_WidgetItem.from(new WidgetItem(child.getId(), child.getQuantity(), child.getIndex(), bounds, child.getWidget())));
			}
			return ImmutableList.copyOf(invItems);
		}
		else
		{
			log.debug("Inventory hidden");
			return ImmutableList.of();
		}
	}

	public boolean isInventoryVisible()
	{
		Widget inv = client.getWidget(WidgetInfo.INVENTORY);
		return (inv != null && !inv.isHidden());
	}

	public boolean doClick(_WidgetItem item)
	{
		if (item != null && isInventoryVisible())
		{
			Point pos = vision.getClickPoint(item.getCanvasBounds(true));
			Striker.clickMouse(pos, 1);
			Striker.delayMS(25, 100);
			return true;
		}
		else
		{
			return false;
		}
	}

	public boolean doClick(int id)
	{
		return doClick(getInventoryItem(id));
	}

}
