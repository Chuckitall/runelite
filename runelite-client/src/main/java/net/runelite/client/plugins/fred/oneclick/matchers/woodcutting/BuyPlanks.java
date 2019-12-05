package net.runelite.client.plugins.fred.oneclick.matchers.woodcutting;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameObject;
import net.runelite.api.ItemID;
import net.runelite.api.MenuEntry;
import net.runelite.api.MenuOpcode;
import net.runelite.api.NPC;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.queries.NPCQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.fred.oneclick.util.MenuEntryMatcher;
import net.runelite.client.plugins.fred.api.wrappers._Item;

import static net.runelite.api.ItemID.COINS_995;
import static net.runelite.api.ItemID.LOGS;
import static net.runelite.api.ItemID.MAHOGANY_LOGS;
import static net.runelite.api.ItemID.OAK_LOGS;
import static net.runelite.api.ItemID.TEAK_LOGS;
import static net.runelite.api.MenuOpcode.ITEM_USE;
import static net.runelite.api.ObjectID.OAK_10820;

@Singleton
@Slf4j
public class BuyPlanks extends MenuEntryMatcher
{
	private final ImmutableSet plank_logs = ImmutableSet.of(
		LOGS, OAK_LOGS,
		TEAK_LOGS, MAHOGANY_LOGS
	);

	private List<_Item> plank_logs_items = Lists.newArrayList();

	private final ImmutableSet planks = ImmutableSet.of(
		ItemID.PLANK, ItemID.OAK_PLANK,
		ItemID.TEAK_PLANK, ItemID.MAHOGANY_PLANK
	);

	private List<_Item> plank_items = Lists.newArrayList();
	private _Item coins = null;

	private NPC operator = null;
	private GameObject depBox = null;
	private GameObject oakTree = null;

	private boolean haveLog()
	{
		return plank_logs_items.size() > 0;
	}

	private boolean havePlank()
	{
		return plank_items.size() > 0;
	}

	private boolean haveOperator()
	{
		return operator != null;
	}

	private boolean haveDepBox()
	{
		return depBox != null;
	}

	private boolean haveTree()
	{
		return oakTree != null;
	}

	@Override
	public boolean addedMatches(MenuEntryAdded added)
	{
		if (!(havePlank() && haveDepBox()) && !(haveLog() && haveOperator()) && !(haveTree() && coins != null))
		{
			return false;
		}
		return added.getOpcode() == ITEM_USE.getId() && ((getItemAtIdx(plank_items, added.getParam0()) != null && haveDepBox()) || (getItemAtIdx(plank_logs_items, added.getParam0()) != null && haveOperator()) || (coins != null && coins.getIdx() == added.getParam0() && haveTree()));
	}

	@Override
	public MenuEntry generate(MenuEntryAdded added)
	{
		_Item log = getItemAtIdx(plank_logs_items, added.getParam0());
		_Item plank = getItemAtIdx(plank_items, added.getParam0());
		_Item goldPieces = (coins != null && coins.getIdx() == added.getParam0()) ? coins : null;
		if (log != null)
		{
			Widget w = client.getWidget(403, 88);
			if (w == null || w.isHidden())
			{
				return new MenuEntry(
					"SAWMILL",
					"<col=ffff00>Sawmill operator",
					operator.getIndex(),
					MenuOpcode.NPC_THIRD_OPTION.getId(),
					0,
					0,
					false);
			}
			else if (!w.isHidden() && w.getText().equals("What wood do you want converting to planks?"))
			{
				Widget l = null;
				if (log.getId() == LOGS)
				{
					l = client.getWidget(403, 93);
				}
				else if (log.getId() == OAK_LOGS)
				{
					l = client.getWidget(403, 94);
				}
				else if (log.getId() == TEAK_LOGS)
				{
					l = client.getWidget(403, 95);
				}
				else if (log.getId() == MAHOGANY_LOGS)
				{
					l = client.getWidget(403, 96);
				}
				if (l != null)
				{
					return new MenuEntry("BUY ALL", "", 0, MenuOpcode.WIDGET_TYPE_1.getId(), 0, l.getId(), true);
				}
			}
		}
		else if (plank != null)
		{
			Widget w = client.getWidget(WidgetInfo.DIALOG_OPTION) != null ? client.getWidget(WidgetInfo.DIALOG_OPTION).getParent() : null;
			if (w == null || w.isHidden())
			{
				return new MenuEntry(
					"DEPOSIT",
					"<col=ff9040>" + client.getItemDefinition(plank.getId()).getName() + "<col=ffffff> -> <col=ffff>" + client.getObjectDefinition(depBox.getId()).getName(),
					0,
					MenuOpcode.ITEM_USE_ON_GAME_OBJECT.getId(),
					plank.getId(),
					plank.getIdx(),
					true);
			}
			else if (!w.isHidden())
			{
				Widget[] dChild = w.getDynamicChildren();
				if (dChild.length == 7 && dChild[0] != null && dChild[0].getText().equalsIgnoreCase("How many would you like to deposit?"))
				{
					Widget all = dChild[4];
					if (all != null && all.getText().equalsIgnoreCase("All") && !all.isHidden())
					{
						return new MenuEntry("ALL", "", 0, MenuOpcode.WIDGET_TYPE_6.getId(), 4, all.getId(), true);
					}
				}
			}
		}
		else if (goldPieces != null && oakTree != null)
		{
			return new MenuEntry("CHOP", "<col=ffff>Oak", oakTree.getId(), MenuOpcode.GAME_OBJECT_FIRST_OPTION.getId(), oakTree.getSceneMinLocation().getX(), oakTree.getSceneMinLocation().getY(), true);
		}
		return null;
	}

	@Override
	public boolean clickedMatches(MenuOptionClicked clicked)
	{
		return clicked.getOption().equals("SAWMILL") || clicked.getOption().equals("DEPOSIT") || clicked.getOption().equals("ALL") || clicked.getOption().equals("BUY ALL") || clicked.getOption().equals("CHOP");
	}

	@Override
	public void clicked(MenuOptionClicked clicked)
	{
		switch (clicked.getOption())
		{
			case "CHOP":
				clicked.setOption("Chop down");
				break;
			case "DEPOSIT":
				client.setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
				client.setSelectedItemSlot(clicked.getParam1());
				client.setSelectedItemID(clicked.getParam0());
				clicked.setOption("Use");
				clicked.setIdentifier(depBox.getId());
				clicked.setParam0(depBox.getLocalLocation().getSceneX());
				clicked.setParam1(depBox.getLocalLocation().getSceneY());
				break;
			case "ALL":
				clicked.setOption("Continue");
				break;
			case "SAWMILL":
				clicked.setOption("Buy-plank");
				break;
			case "BUY ALL":
				clicked.setOption("Buy <col=ff7000>All");
				break;
			default:
				log.error("NonMatched!");
				clicked.consume();
				break;
		}
	}

	@Override
	public void onInventoryChanged(List<_Item> items)
	{
		plank_logs_items.clear();
		plank_logs_items = items.stream().filter(f -> plank_logs.contains(f.getId())).collect(Collectors.toList());
		plank_items.clear();
		plank_items = items.stream().filter(f -> planks.contains(f.getId())).collect(Collectors.toList());
		coins = items.stream().filter(f -> f.getId()  == COINS_995).findFirst().orElse(null);
	}

	@Override
	public void onGameTick()
	{
		final NPCQuery sawmillOperatorQ = new NPCQuery().idEquals(5422);
		final GameObjectQuery depBoxQ = new GameObjectQuery().idEquals(26254);
		final GameObjectQuery tree = new GameObjectQuery().idEquals(OAK_10820).isWithinArea(LocalPoint.fromWorld(client, 1620, 3506), 1024);
		operator = sawmillOperatorQ.result(client).nearestTo(client.getLocalPlayer());
		depBox = depBoxQ.result(client).nearestTo(client.getLocalPlayer());
		oakTree = tree.result(client).nearestTo(client.getLocalPlayer());
	}
}
