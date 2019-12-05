package net.runelite.client.plugins.fred.util;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Created by npruff on 8/25/2019.
 */
@Slf4j
public class WidgetUtils
{
	final static int MAX_INVENTORY_SPACE = 28;

	public static List<WidgetItem> getItemWidgets(int[] itemIds, Client client)
	{
		Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);

		ArrayList<Integer> itemIDs = new ArrayList<>();

		for (int i : itemIds)
		{
			itemIDs.add(i);
		}

		List<WidgetItem> listToReturn = new ArrayList<>();

		for (WidgetItem item : inventoryWidget.getWidgetItems())
		{
			if (itemIDs.contains(item.getId()))
			{
				listToReturn.add(item);
			}
		}

		return listToReturn;
	}

	public static List<Widget> getEquippedWidgets(int[] itemIds, Client client)
	{
		Widget equipmentWidget = client.getWidget(WidgetInfo.EQUIPMENT);
		ArrayList<Integer> equippedIds = new ArrayList<>();
		for (int i : itemIds)
		{
			equippedIds.add(i);
		}

		List<Widget> equipped = new ArrayList<>();
		if (equipmentWidget.getStaticChildren() != null)
		{
			for (Widget widgets : equipmentWidget.getStaticChildren())
			{
				for (Widget items : widgets.getDynamicChildren())
				{
					if (equippedIds.contains(items.getItemId()))
					{
						equipped.add(items);
					}
				}
			}
		}
		else
		{
			log.error("Static Children is Null!");
		}
		return equipped;
	}

	public static <E> E getRandomElement(List<E> options)
	{
		return (options != null && options.size() > 0) ? options.get(Random.nextInt(0, options.size())) : null;
	}

	public static WidgetItem getItemWidget(int[] itemIds, Client client, boolean first)
	{
		List<WidgetItem> items = getItemWidgets(itemIds, client);
		if (items.size() == 0)
		{
			return null;
		}
		return (first ? items.get(0) : getRandomElement(items));
	}

	public static Widget getEquippedWidget(int[] itemIds, Client client, boolean first)
	{
		List<Widget> items = getEquippedWidgets(itemIds, client);
		if (items.size() == 0)
		{
			return null;
		}
		return (first ? items.get(0) : getRandomElement(items));
	}

	public static int getFreeInventorySpaces(Client client)
	{
		ItemContainer inventoryItemContainer = client.getItemContainer(InventoryID.INVENTORY);
		Item[] inventoryItems = new Item[0];
		if (inventoryItemContainer != null)
		{
			inventoryItems = inventoryItemContainer.getItems();
		}
		int emptyInventorySpaceCount = (int) Arrays.stream(inventoryItems).filter(i -> i.getId() != -1).count();
		return MAX_INVENTORY_SPACE - emptyInventorySpaceCount;
	}

	public static int getInventoryItemCount(Client client, final int[] ids)
	{
		ItemContainer inventoryItemContainer = client.getItemContainer(InventoryID.INVENTORY);
		Item[] inventoryItems = new Item[0];
		if (inventoryItemContainer != null)
		{
			inventoryItems = inventoryItemContainer.getItems();
		}
		return (int) Arrays.stream(inventoryItems).mapToInt(Item :: getId).filter(i -> ArrayUtils.contains(ids, i)).count();
	}

	public static int getInventoryItemCount(Client client, Set<Integer> ids)
	{
		if (ids == null || ids.size() == 0)
		{
			return 0;
		}
		return getInventoryItemCount(client, ids.stream().mapToInt(f -> f).toArray());
	}

	public static boolean hasItem(Client client, final int[] itemIds)
	{
		return hasItemEquipped(client, itemIds) || hasItemInInventory(client, itemIds);
	}

	public static boolean hasItemEquipped(Client client, final int[] itemids)
	{
		ItemContainer container = client.getItemContainer(InventoryID.EQUIPMENT);
		if (container != null && container.getItems() != null)
		{
			return Arrays.stream(container.getItems()).mapToInt(Item::getId).filter(f -> ArrayUtils.contains(itemids, f)).toArray().length > 0;
		}
		return false;
	}

	public static boolean hasItemInInventory(Client client, final int[] itemIds)
	{
		ItemContainer container = client.getItemContainer(InventoryID.INVENTORY);
		if (container != null && container.getItems() != null)
		{
			return Arrays.stream(container.getItems()).mapToInt(Item::getId).filter(f -> ArrayUtils.contains(itemIds, f)).toArray().length > 0;
		}
		return false;
	}
}
