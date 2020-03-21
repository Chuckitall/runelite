package net.runelite.client.plugins.fred.oneclick.matchers.construction;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemID;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.queries.NPCQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.fred.oneclick.util.MenuEntryMatcher;
import net.runelite.client.plugins.fred.oneclick.api._Item;

import static net.runelite.api.ItemID.COINS_995;
import static net.runelite.api.MenuOpcode.ITEM_USE;
import static net.runelite.api.MenuOpcode.ITEM_USE_ON_NPC;
import static net.runelite.api.MenuOpcode.WIDGET_TYPE_6;

@Singleton
@Slf4j
public class UnnotePhiles extends MenuEntryMatcher
{

	private List<_Item> notedPlankItems = Lists.newArrayList();

	private final ImmutableSet notedPlankIds = ImmutableSet.of(
		ItemID.PLANK + 1, ItemID.OAK_PLANK + 1, ItemID.TEAK_PLANK + 1, ItemID.MAHOGANY_PLANK + 1
	);
	private _Item coinsItem = null;

	private NPC philes = null;

	@Override
	public boolean addedMatches(MenuEntryAdded added)
	{
		if (notedPlankItems.size() == 0)
		{
			return false;
		}
		_Item notedPlank = getItemAtIdx(notedPlankItems, added.getParam0());
		return added.getOpcode() == ITEM_USE.getId() && philes != null && notedPlank != null && plugin.getFreeInvSpaces() > 0 && coinsItem != null && coinsItem.getQty() >= (5 * plugin.getFreeInvSpaces());
	}

	@Override
	public MenuEntry generate(MenuEntryAdded added)
	{
		_Item notedPlank = getItemAtIdx(notedPlankItems, added.getParam0());
		Widget w = client.getWidget(WidgetInfo.DIALOG_OPTION) != null ? client.getWidget(WidgetInfo.DIALOG_OPTION).getParent() : null;
		if (w == null || w.isHidden())
		{
			//new MenuEntry("Use", "<col=ff9040>Oak plank<col=ffffff> -> <col=ffff00>Phials", 15533, 7, 0, 0, false)
			return new MenuEntry(
				"UNNOTE",
				"<col=ff9040>" + client.getItemDefinition(notedPlank.getId()).getName() + "<col=ffffff> -> <col=ffff00>" + client.getNpcDefinition(philes.getId()).getName(),
				philes.getIndex(),
				ITEM_USE_ON_NPC.getId(),
				notedPlank.getIdx(),
				notedPlank.getId(),
				true
			);
		}
		else
		{	Widget[] dChild = w.getDynamicChildren();
			//Menuaction -> new MenuEntry("Continue", "", 0, 30, 3, 14352385, false)
			if (dChild.length == 8 && dChild[0] != null && dChild[0].getText().equalsIgnoreCase("Select an Option"))
			{
				Widget all = dChild[3];
				if (all != null && all.getText().equalsIgnoreCase("Exchange All: 120 coins") && !all.isHidden())
				{
					return new MenuEntry("ALL", "", 0, WIDGET_TYPE_6.getId(), 3, all.getId(), true);
				}
			}
		}
		return null;
	}

	@Override
	public boolean clickedMatches(MenuOptionClicked clicked)
	{
//		if (!clicked.getMenuOpcode().equals(ITEM_USE_ON_NPC) && !clicked.getMenuOpcode().equals(WIDGET_TYPE_6))
//		{
//			return false;
//		}
		return clicked.getOption().equals("ALL") || clicked.getOption().equals("UNNOTE");
	}

	@Override
	public void clicked(MenuOptionClicked clicked)
	{
		switch (clicked.getOption())
		{
			case "ALL":
				clicked.setOption("Continue");
				break;
			case "UNNOTE":
				client.setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
				client.setSelectedItemSlot(clicked.getParam0());
				client.setSelectedItemID(clicked.getParam1());
				clicked.setOption("Use");
				clicked.setIdentifier(philes.getIndex());
				clicked.setParam0(0);
				clicked.setParam1(0);
				break;
			default:
				clicked.consume();
				break;
		}
	}

	@Override
	public void onInventoryChanged(final List<_Item> items)
	{
		notedPlankItems = items.stream().filter(i -> notedPlankIds.contains(i.getId())).collect(Collectors.toList());
		coinsItem = items.stream().filter(f -> f.getId()  == COINS_995).findFirst().orElse(null);
	}

	@Override
	public void onGameTick()
	{
		final NPCQuery philesQ = new NPCQuery().idEquals(1614);
		philes = philesQ.result(client).nearestTo(client.getLocalPlayer());
	}
}
