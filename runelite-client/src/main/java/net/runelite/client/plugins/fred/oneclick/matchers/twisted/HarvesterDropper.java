package net.runelite.client.plugins.fred.oneclick.matchers.twisted;

import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
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

import static net.runelite.api.MenuOpcode.GAME_OBJECT_FIRST_OPTION;
import static net.runelite.api.MenuOpcode.NPC_FIRST_OPTION;
import static net.runelite.api.NpcID.ROD_FISHING_SPOT;
import static net.runelite.api.NpcID.ROD_FISHING_SPOT_1508;
import static net.runelite.api.NpcID.ROD_FISHING_SPOT_1509;
import static net.runelite.api.NpcID.ROD_FISHING_SPOT_1513;
import static net.runelite.api.NpcID.ROD_FISHING_SPOT_1515;
import static net.runelite.api.NpcID.ROD_FISHING_SPOT_1516;
import static net.runelite.api.NpcID.ROD_FISHING_SPOT_1526;
import static net.runelite.api.NpcID.ROD_FISHING_SPOT_1527;
import static net.runelite.api.NpcID.ROD_FISHING_SPOT_7463;
import static net.runelite.api.NpcID.ROD_FISHING_SPOT_7464;
import static net.runelite.api.NpcID.ROD_FISHING_SPOT_7468;
import static net.runelite.api.NpcID.ROD_FISHING_SPOT_8524;
import static net.runelite.api.ObjectID.ROCKS_11364;
import static net.runelite.api.ObjectID.ROCKS_11365;
import static net.runelite.api.ObjectID.ROCKS_11366;
import static net.runelite.api.ObjectID.ROCKS_11367;
import static net.runelite.api.ObjectID.ROCKS_11372;
import static net.runelite.api.ObjectID.ROCKS_11373;

@Singleton
@Slf4j
public class HarvesterDropper extends MenuEntryMatcher
{
	private final Map<_ItemObjEntry, List<_Item>> invItems = new HashMap<>();
	private _ItemContainer invContainer = null;
	private boolean flag = false;

	@Override
	public void init()
	{
		//ROCKS_11390
		log.debug("init");
		invItems.clear();
		invItems.put(_ItemObjEntry.builder("IRON", GAME_OBJECT_FIRST_OPTION).withObjects(ROCKS_11364, ROCKS_11365).withItem(ItemID.IRON_ORE).build(), new ArrayList<>());
		invItems.put(_ItemObjEntry.builder("COAL", GAME_OBJECT_FIRST_OPTION).withObjects(ROCKS_11366, ROCKS_11367).withItem(ItemID.COAL).build(), new ArrayList<>());
		invItems.put(_ItemObjEntry.builder("MITHRIL", GAME_OBJECT_FIRST_OPTION).withObjects(ROCKS_11372, ROCKS_11373).withItem(ItemID.MITHRIL_ORE).build(), new ArrayList<>());
		invItems.put(_ItemObjEntry.builder("FLY FISHING", NPC_FIRST_OPTION)
			.withNPCs(ROD_FISHING_SPOT, ROD_FISHING_SPOT_1508, ROD_FISHING_SPOT_1509, ROD_FISHING_SPOT_1513,
				ROD_FISHING_SPOT_1515, ROD_FISHING_SPOT_1516, ROD_FISHING_SPOT_1526, ROD_FISHING_SPOT_1527,
				ROD_FISHING_SPOT_7463, ROD_FISHING_SPOT_7464, ROD_FISHING_SPOT_7468, ROD_FISHING_SPOT_8524)
			.withItems(ItemID.RAW_TROUT, ItemID.RAW_SALMON).build(), new ArrayList<>());
	}

	@Override
	public boolean addedMatches(MenuEntryAdded added)
	{
		if (invContainer == null || invItems.size() == 0 || flag)
		{
			return false;
		}
		return plugin.getFreeInvSpaces() == 0 && invItems.keySet().stream().anyMatch(f -> f.matches(added, client));
	}

	@Nullable
	@Override
	public MenuEntry generate(MenuEntryAdded added)
	{
		//Optional<_ItemObjEntry> matchedT = invItems.keySet().stream().filter(f -> Arrays.stream(f.getObjIds()).anyMatch(j -> j == added.getIdentifier())).findFirst();
		Optional<_ItemObjEntry> matchedT = invItems.keySet().stream().filter(f -> f.matches(added, client)).findFirst();
		Optional<_Item> targetT = matchedT.flatMap(f -> invContainer.getItemNearSlot(invContainer.getSlotIdx(1, 1), f.getItemIds()));
		log.debug("Matched {}", matchedT);
		log.debug("targetT {}", targetT);
		if (matchedT.isPresent() && targetT.isPresent())
		{
			//_ItemObjEntry matched = matchedT.get();
			_Item target = targetT.get();
			return new MenuEntry(
				"DROP",
				client.getItemDefinition(target.getId()).getName(),
				target.getId(),
				MenuOpcode.ITEM_DROP.getId(),
				target.getIdx(),
				WidgetInfo.INVENTORY.getId(),
				true
			);
		}
		return null;
	}

	@Override
	public boolean clickedMatches(MenuOptionClicked clicked)
	{
		return clicked.getOption().equals("DROP");
	}

	@Override
	public void clicked(MenuOptionClicked clicked)
	{
		if (clicked.getOption().equals("DROP"))
		{
			flag = true;
			clicked.setOption("Drop");
		}
		else
		{
			clicked.consume();
		}
	}

	@Override
	public void onGameTick()
	{

	}

	@Override
	public void onInventoryChanged(List<_Item> items)
	{
		log.debug("Items count = {}", items.size());

		for (_ItemObjEntry key : invItems.keySet())
		{
			List<_Item> temp = invItems.get(key);
			temp.clear();
			temp.addAll(items.stream().filter(f -> Arrays.stream(key.getItemIds()).anyMatch(j -> j == f.getId())).collect(Collectors.toList()));
		}

		invContainer = _ItemContainer.create(client, InventoryID.INVENTORY);
		if (plugin.getFreeInvSpaces() > 0 && flag)
		{
			flag = false;
		}

	}
}
