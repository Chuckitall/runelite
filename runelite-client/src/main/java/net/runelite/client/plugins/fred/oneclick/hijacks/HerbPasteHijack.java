package net.runelite.client.plugins.fred.oneclick.hijacks;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Singleton;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.MenuOpcode;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.fred.oneclick.util.MenuEntryHijack;

@Slf4j
@Singleton
public class HerbPasteHijack extends MenuEntryHijack
{
	private static final String MAKE = "Make";
	private static final Set<Integer> HERBS = ImmutableSet.of(
		ItemID.GUAM_LEAF, ItemID.MARRENTILL, ItemID.TARROMIN, ItemID.HARRALANDER
	);

	private int tarIdx;
	private int tarId;
	private boolean tar;
	private boolean pestle;

	@Override
	protected void onEnabled()
	{
		this.tarIdx = -1;
		this.tarId = -1;
		this.tar = false;
		this.pestle = false;
		eventBus.subscribe(ItemContainerChanged.class, this, this::onItemContainerChanged);
		ItemContainer temp = client.getItemContainer(InventoryID.INVENTORY);
		this.onItemContainerChanged(new ItemContainerChanged(InventoryID.INVENTORY.getId(), temp));
	}

	@Override
	protected void onDisabled()
	{
		this.tarIdx = -1;
		this.tarId = -1;
		this.tar = false;
		this.pestle = false;
		eventBus.unregister(this);
	}

	@Override
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		final int id = event.getIdentifier();
		if (tar && pestle && event.getOpcode() == MenuOpcode.ITEM_USE.getId() && HERBS.contains(id))
		{
			event.setOption(MAKE);
			event.setModified();
		}
	}

	@Override
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (!this.isEnabled())// should be unnecessary
		{
			return;
		}
//		final MenuEntry entry = event.getMenuEntry();

		if (tar && pestle && event.getOpcode() == MenuOpcode.ITEM_USE.getId() && event.getOption().equals(MAKE))
		{
			event.setOpcode(MenuOpcode.ITEM_USE_ON_WIDGET_ITEM.getId());
			event.setOption("Use");
			client.setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
			client.setSelectedItemSlot(tarIdx);
			client.setSelectedItemID(tarId);
		}
	}

	private void onItemContainerChanged(ItemContainerChanged event)
	{
		if (!this.isEnabled()) //should be unnecessary
		{
			return;
		}

		final ItemContainer itemContainer = event.getItemContainer();
		final List<Item> items = Arrays.asList(itemContainer.getItems());

		if (itemContainer != client.getItemContainer(InventoryID.INVENTORY) || !Collections.disjoint(items, HERBS))
		{
			return;
		}
		tarIdx = -1;
		tarId = -1;
		tar = false;
		pestle = false;

		for (int i = 0; i < items.size(); i++)
		{
			final int itemId = items.get(i).getId();
			if (!tar && ItemID.SWAMP_TAR == itemId)
			{
				tarId = itemId;
				tarIdx = i;
				tar = true;
			}
			if (ItemID.PESTLE_AND_MORTAR == itemId)
			{
				pestle = true;
			}
		}
	}
}
