package net.runelite.client.plugins.fredexperimental.controller.listeners;

import java.util.List;
import net.runelite.client.plugins.fred.api.wrappers._Item;

public interface InventoryItemsListener
{
	void onInventoryItemsChanged(List<_Item> inventory);
}
