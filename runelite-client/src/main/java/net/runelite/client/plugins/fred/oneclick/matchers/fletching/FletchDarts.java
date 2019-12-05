package net.runelite.client.plugins.fred.oneclick.matchers.fletching;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemID;
import net.runelite.api.MenuEntry;
import net.runelite.api.MenuOpcode;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.fred.oneclick.util.MenuEntryMatcher;
import net.runelite.client.plugins.fred.api.wrappers._Item;

import static net.runelite.api.MenuOpcode.ITEM_USE;

@Singleton
@Slf4j
public class FletchDarts extends MenuEntryMatcher
{

	private List<_Item> dartTipItems = Lists.newArrayList();
	private List<_Item> featherItems = Lists.newArrayList();

	private final Set<Integer> dartTipIds = ImmutableSet.of(
		ItemID.BRONZE_DART_TIP, ItemID.IRON_DART_TIP, ItemID.STEEL_DART_TIP, ItemID.MITHRIL_DART_TIP, ItemID.ADAMANT_DART_TIP, ItemID.RUNE_DART_TIP
	);

	private final Set<Integer> featherIds = ImmutableSet.of(ItemID.FEATHER);

	@Override
	public boolean addedMatches(MenuEntryAdded added)
	{
		if (dartTipItems.size() == 0 || featherItems.size() == 0)
		{
			return false;
		}
		_Item dart = getItemAtIdx(dartTipItems, added.getParam0());
		return added.getOpcode() == ITEM_USE.getId() && dart != null;
	}

	@Override
	public MenuEntry generate(MenuEntryAdded added)
	{
		_Item dart = getItemAtIdx(dartTipItems, added.getParam0());
		_Item feather;
		if (featherItems.size() > 0)
		{
			feather = featherItems.stream().findFirst().get();
			return new MenuEntry(
				"FLETCH",
				"<col=ff9040>" + client.getItemDefinition(feather.getId()).getName() + "<col=ffffff> -> <col=ff9040>" + client.getItemDefinition(dart.getId()).getName(),
				dart.getId(),
				MenuOpcode.ITEM_USE_ON_WIDGET_ITEM.getId(),
				dart.getIdx(),
				WidgetInfo.INVENTORY.getId(),
				true
			);
		}
		else
		{
			log.error("No feathers!");
			return null;
		}
	}

	@Override
	public boolean clickedMatches(MenuOptionClicked clicked)
	{
		return clicked.getOption().equals("FLETCH");
	}

	@Override
	public void clicked(MenuOptionClicked clicked)
	{

		Optional<_Item> feather = featherItems.stream().findFirst();
		if (feather.isPresent())
		{
			clicked.setOption("Use");
			client.setSelectedItemWidget(clicked.getParam1());
			client.setSelectedItemSlot(feather.get().getIdx());
			client.setSelectedItemID(feather.get().getId());
		}
		else
		{
			clicked.consume();
		}
	}

	@Override
	public void onInventoryChanged(final List<_Item> items)
	{
		featherItems = items.stream().filter(i -> featherIds.contains(i.getId())).collect(Collectors.toList());
		dartTipItems = items.stream().filter(i -> dartTipIds.contains(i.getId())).collect(Collectors.toList());
	}
}
