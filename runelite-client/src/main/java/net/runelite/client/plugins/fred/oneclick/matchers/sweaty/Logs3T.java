package net.runelite.client.plugins.fred.oneclick.matchers.sweaty;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemID;
import net.runelite.api.MenuEntry;
import net.runelite.api.MenuOpcode;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.fred.oneclick.api._Item;
import net.runelite.client.plugins.fred.oneclick.api._ItemContainer;
import net.runelite.client.plugins.fred.oneclick.util.MenuEntryMatcher;

import static net.runelite.api.MenuOpcode.ITEM_USE;

@Singleton
@Slf4j
public class Logs3T extends MenuEntryMatcher
{
	private _ItemContainer inventory = null;

	private Set<Integer> logIds = null;
	private Set<Integer> knifeIds = null;

	private _Item targetLogs = null;
	private _Item targetKnife = null;

	@Override
	public void init()
	{
		logIds = ImmutableSet.of(ItemID.MAHOGANY_LOGS, ItemID.TEAK_LOGS);
		knifeIds = ImmutableSet.of(ItemID.KNIFE);
		inventory = _ItemContainer.create(client, InventoryID.INVENTORY);
		targetLogs = null;
		targetKnife = null;
		super.init();
	}

	@Override
	public boolean addedMatches(MenuEntryAdded added)
	{
		if (inventory == null || logIds == null || knifeIds == null || added.getOpcode() != ITEM_USE.getId())
		{
			return false;
		}
		Optional<_Item> logs = inventory.getItemAtIdx(added.getParam0()).filter(f -> logIds.contains(f.getId()));
		Optional<_Item> knife = inventory.getItemNearSlot(logs.map(_Item::getIdx).orElse(0), knifeIds.stream().mapToInt(j -> j).toArray());//getItemAtIdx(dartTipItems, added.getParam0());
		if (logs.isPresent() && knife.isPresent())
		{
			targetLogs = logs.get();
			targetKnife = knife.get();
			return true;
		}
		else
		{
			return false;
		}
	}

	@Override
	public MenuEntry generate(MenuEntryAdded added)
	{
		return new MenuEntry(
			"<col=0090f6>3T",
			"<col=ff9040>" + client.getItemDefinition(targetKnife.getId()).getName() + "<col=ffffff> -> <col=ff9040>" + client.getItemDefinition(targetLogs.getId()).getName(),
			targetLogs.getId(),
			MenuOpcode.ITEM_USE_ON_WIDGET_ITEM.getId(),
			targetLogs.getIdx(),
			WidgetInfo.INVENTORY.getId(),
			false
		);
	}

	@Override
	public boolean clickedMatches(MenuOptionClicked clicked)
	{
		return clicked.getOption().equals("<col=0090f6>3T");
	}

	@Override
	public void clicked(MenuOptionClicked clicked)
	{
		if (clicked.getOption().equals("<col=0090f6>3T"))
		{
			clicked.setOption("Use");
			client.setSelectedItemWidget(clicked.getParam1());
			client.setSelectedItemSlot(targetKnife.getIdx());
			client.setSelectedItemID(targetKnife.getId());
		}
		else
		{
			clicked.consume();
		}
	}

	@Override
	public void onInventoryChanged(final List<_Item> items)
	{
		inventory = _ItemContainer.create(client, InventoryID.INVENTORY);
	}
}
