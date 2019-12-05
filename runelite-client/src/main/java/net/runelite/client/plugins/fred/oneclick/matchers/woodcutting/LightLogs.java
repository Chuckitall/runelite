package net.runelite.client.plugins.fred.oneclick.matchers.woodcutting;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
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
public class LightLogs extends MenuEntryMatcher
{

	private List<_Item> itemTinderbox = Lists.newArrayList();
	private List<_Item> itemLogs = Lists.newArrayList();

	private final ImmutableSet logs = ImmutableSet.of(
		ItemID.LOGS, ItemID.ACHEY_TREE_LOGS, ItemID.OAK_LOGS,
		ItemID.WILLOW_LOGS, ItemID.TEAK_LOGS, ItemID.ARCTIC_PINE_LOGS, ItemID.MAPLE_LOGS,
		ItemID.MAHOGANY_LOGS, ItemID.YEW_LOGS, ItemID.MAGIC_LOGS, ItemID.REDWOOD_LOGS
	);

	private final ImmutableSet tinderbox = ImmutableSet.of(ItemID.TINDERBOX, ItemID.GOLDEN_TINDERBOX);

	@Override
	public boolean addedMatches(MenuEntryAdded added)
	{
//		List<_Item> items = plugin.getInventoryItems();
//		if(items.stream().noneMatch(f->logs.contains(f.getId())) || items.stream().noneMatch(f->tinderbox.contains(f.getId())))
		if (itemLogs.size() == 0 || itemTinderbox.size() == 0)
		{
			return false;
		}
		_Item log = getItemAtIdx(itemLogs, added.getParam0());
		return added.getOpcode() == ITEM_USE.getId() && log != null;
	}

	@Override
	public MenuEntry generate(MenuEntryAdded added)
	{
		//List<_Item> items = plugin.getInventoryItems();
//		_Item box = items.stream().filter(f->tinderbox.contains(f.getId())).findFirst().get();
//		_Item log = items.stream().filter(f->logs.contains(f.getId())).filter(f->added.getParam0() != f.getIdx()).findFirst().orElse(items.stream().filter(f->logs.contains(f.getId())).findFirst().get());
		_Item logSe = getItemAtIdx(itemLogs, added.getParam0());
		_Item box;
		if (itemTinderbox.size() > 0)
		{
			box = itemTinderbox.stream().findFirst().get();
			return new MenuEntry(
				"LIGHT",
				"<col=ff9040>" + client.getItemDefinition(box.getId()).getName() + "<col=ffffff> -> <col=ff9040>" + client.getItemDefinition(logSe.getId()).getName(),
				logSe.getId(),
				MenuOpcode.ITEM_USE_ON_WIDGET_ITEM.getId(),
				logSe.getIdx(),
				WidgetInfo.INVENTORY.getId(),
				true
			);
		}
		else
		{
			log.error("No tinderbox!");
			return null;
		}
	}

	@Override
	public boolean clickedMatches(MenuOptionClicked clicked)
	{
		return clicked.getOption().equals("LIGHT");
	}

	@Override
	public void clicked(MenuOptionClicked clicked)
	{
		Optional<_Item> box = itemTinderbox.stream().findFirst();
		if (box.isPresent())
		{
			clicked.setOption("Use");
			client.setSelectedItemWidget(clicked.getParam1());
			client.setSelectedItemSlot(box.get().getIdx());
			client.setSelectedItemID(box.get().getId());
		}
		else
		{
			clicked.consume();
		}
	}

	@Override
	public void onInventoryChanged(final List<_Item> items)
	{
		itemLogs = items.stream().filter(i -> logs.contains(i.getId())).collect(Collectors.toList());
		itemTinderbox = items.stream().filter(i -> tinderbox.contains(i.getId())).collect(Collectors.toList());
	}
}
