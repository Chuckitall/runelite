package net.runelite.client.plugins.fred.oneclick.matchers.utility;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Singleton;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.MenuEntry;
import net.runelite.api.MenuOpcode;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.queries.NPCQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.fred.api.wrappers._Tile;
import net.runelite.client.plugins.fred.oneclick.util.MenuEntryMatcher;
import net.runelite.client.plugins.fred.api.wrappers._Item;
import net.runelite.client.plugins.fred.util.Random;
import net.runelite.client.plugins.fred.util.Tab;
import net.runelite.client.plugins.fred.util.TabUtils;

import static net.runelite.api.ItemID.COINS_995;
import static net.runelite.api.MenuOpcode.ITEM_USE;
import static net.runelite.api.MenuOpcode.WALK;
import static net.runelite.api.MenuOpcode.CC_OP;

@Singleton
@Slf4j
public class TradeCharter extends MenuEntryMatcher
{
	private _Item coins = null;

	private Set<Integer> charterCrewIds = ImmutableSet.of(NpcID.TRADER_CREWMEMBER, NpcID.TRADER_CREWMEMBER_1330, NpcID.TRADER_CREWMEMBER_1331, NpcID.TRADER_CREWMEMBER_1332, NpcID.TRADER_CREWMEMBER_1333, NpcID.TRADER_CREWMEMBER_1334);

	private Map<CharterItem, Boolean> targetItems = new HashMap<>();

	@Setter
	private CharterArea targetArea = null;

	private NPC crew = null;
	private _Tile walkTile = null;
	private GameObject depBox = null;
	private List<_Item> buyItems = Lists.newArrayList();

	public void setTargetItem(CharterItem item, boolean enabled)
	{
		targetItems.put(item, enabled);
	}

	private List<CharterItem> getTargetedItems()
	{
		if (!haveTargetItems())
		{
			return new ArrayList<>();
		}
		return targetItems.entrySet().stream().filter(Entry::getValue).map(Entry::getKey).collect(Collectors.toList());
	}

	private boolean haveWalkTile()
	{
		return walkTile != null;
	}

	private boolean haveCrew()
	{
		return crew != null;
	}

	private boolean haveTargetItems()
	{
		return targetItems != null && targetItems.containsValue(true);
	}

	private boolean haveTargetArea()
	{
		return targetArea != null;
	}

	private boolean inTargetArea(_Tile tile)
	{
		return targetArea != null && targetArea.getCharterTraderArea().contains(tile);
	}

	private boolean haveDepBox()
	{
		return depBox != null;
	}

	private boolean haveBuyItem()
	{
		return buyItems.size() > 0;
	}

	private boolean haveCoins()
	{
		return coins != null;
	}

	private void doHop()
	{
//		Striker.typeKey(KeyEvent.VK_ESCAPE);
//		Striker.delayMS(Random.nextInt(120, 200));
//		//WorldHopperPlugin.request_hop(false);
//		int count = 0;
//		while (client.getGameState().equals(GameState.LOGGED_IN) && count++ < 200)
//		{
//			Striker.delayMS(Random.nextInt(20, 50));
//		}
//		log.debug("count {}", count);
//		count = 0;
//		while (!client.getGameState().equals(GameState.LOGGED_IN) && count++ < 200)
//		{
//			Striker.delayMS(Random.nextInt(20, 50));
//		}
//		log.debug("count {}", count);
//		Striker.delayMS(Random.nextInt(100, 150));
//		Striker.typeKey(TabUtils.getTabHotkey(Tab.INVENTORY, client));
//		Striker.delayMS(Random.nextInt(20, 50));
	}

	@Override
	public boolean addedMatches(MenuEntryAdded added)
	{
		if (added.getOption().equals("TRADE") || added.getOption().equals("BUY ALL") || added.getOption().equals("DEPOSIT") || added.getOption().equals("ALL") || added.getOption().equals("HOP") || added.getOption().equals("WALK"))
		{
			return false;
		}
		if (!haveTargetItems())
		{
			log.debug("no items");
			return false;
		}
		if (added.getOpcode() != ITEM_USE.getId() && added.getOpcode() != CC_OP.getId())
		{
			return false;
		}
		if (added.getOpcode() == CC_OP.getId() && !added.getOption().equals("Value<col=ff9040>"))
		{
			return false;
		}
		return ((haveCrew() || haveWalkTile()) && haveCoins() && coins.getIdx() == added.getParam0()) || (haveDepBox() && haveBuyItem() && buyItems.stream().anyMatch(f -> f.getIdx() == added.getParam0()));
	}

	@Override
	public MenuEntry generate(MenuEntryAdded added)
	{
//		log.debug("Area contains tiles: {} -> {}", _Tile.ofPlayer(client), this.inTargetArea(_Tile.ofPlayer(client)));
		if (added.getOpcode() != ITEM_USE.getId() && added.getOpcode() != CC_OP.getId())
		{
			log.error("added.getOpcode was {}, not {} or {}", added.getOpcode(), ITEM_USE.getId(), CC_OP.getId());
			return null;
		}
		else if (!plugin.isShiftPressed() && haveCrew() && haveCoins() && added.getParam0() == coins.getIdx())
		{
			Widget w1 = client.getWidget(300, 16);
			Widget w2 = client.getWidget(300, 1);
			if (w1 == null || w1.isHidden())
			{
				return new MenuEntry(
					"TRADE",
					"<col=ffff00>" + client.getNpcDefinition(crew.getId()).getName(),
					crew.getIndex(),
					MenuOpcode.NPC_THIRD_OPTION.getId(),
					0,
					0,
					true);
			}
			else if (haveTargetItems() && w2 != null && !w2.isHidden() && w2.getDynamicChildren().length == 12 && w2.getDynamicChildren()[1].getText().equals("Trader Stan's Trading Post"))
			{
				CharterItem targetItem = getTargetedItems().stream().filter(f -> w1.getDynamicChildren()[f.getIdx()] != null && w1.getDynamicChildren()[f.getIdx()].getItemQuantity() > 0).findFirst().orElse(null);
				Widget l = targetItem != null ? w1.getDynamicChildren()[targetItem.getIdx()] : null;
				if (l != null && l.getItemQuantity() > 0)
				{
					//("Buy 50", "<col=ff9040>Soda ash</col>", 5, 57, 23, 19660816, false)
					return new MenuEntry("BUY ALL", "<col=ff9040>" + client.getItemDefinition(targetItem.getId()).getName() + "</col>", 5, MenuOpcode.CC_OP.getId(), targetItem.getIdx(), l.getId(), true);
				}
				else
				{
					return new MenuEntry("HOP", "", 0, ITEM_USE.getId(), 0, 0, true);
				}
			}
		}
		else if ((!haveCrew() || plugin.isShiftPressed()) && haveWalkTile() && haveCoins() && added.getParam0() == coins.getIdx())
		{
			WorldPoint walkTo = walkTile.toWorldPoint();
			if (walkTo.isInScene(client))
			{
				return new MenuEntry("WALK", "", 0, WALK.getId(), walkTile.getX(), walkTile.getY(), true);
			}
		}
		else if (haveDepBox() && haveBuyItem() && buyItems.stream().anyMatch(f -> f.getIdx() == added.getParam0()))
		{
			Widget w = client.getWidget(WidgetInfo.DIALOG_OPTION) != null ? client.getWidget(WidgetInfo.DIALOG_OPTION).getParent() : null;
			_Item buyItem =  buyItems.stream().filter(f -> f.getIdx() == added.getParam0()).findFirst().orElse(null);
			if (buyItem != null && (w == null || w.isHidden()))
			{
				return new MenuEntry(
					"DEPOSIT",
					"<col=ff9040>" + client.getItemDefinition(buyItem.getId()).getName() + "<col=ffffff> -> <col=ffff>" + client.getObjectDefinition(depBox.getId()).getName(),
					0,
					MenuOpcode.ITEM_USE_ON_GAME_OBJECT.getId(),
					buyItem.getId(),
					buyItem.getIdx(),
					true);
			}
			else if (buyItem != null && !w.isHidden())
			{
				Widget[] dChild = w.getDynamicChildren();
				if (dChild[0] == null || !dChild[0].getText().equalsIgnoreCase("How many would you like to deposit?"))
				{
					return null;
				}
				int idx = -1;
				switch (dChild.length)
				{
					case 7:
						idx = 4;
						break;
					case 6:
						idx = 3;
						break;
				}
				if (idx != -1)
				{
					Widget all = dChild[idx];
					if (all != null && all.getText().equalsIgnoreCase("All") && !all.isHidden())
					{
						return new MenuEntry("ALL", "", 0, MenuOpcode.WIDGET_TYPE_6.getId(), idx, all.getId(), true);
					}
				}
			}
		}
		else
		{
			log.error("FUCK");
		}
		return null;
	}

	@Override
	public boolean clickedMatches(MenuOptionClicked clicked)
	{
		return clicked.getOption().equals("TRADE") || clicked.getOption().equals("BUY ALL") || clicked.getOption().equals("DEPOSIT") || clicked.getOption().equals("ALL") || clicked.getOption().equals("HOP") || clicked.getOption().equals("WALK");
	}

	private void walkNextTile(LocalPoint temp, Point mPos)
	{
		if (temp != null)
		{
			Rectangle bounds = Perspective.getCanvasTilePoly(client, temp).getBounds();
			if (client.getCanvas().getBounds().contains(bounds))
			{
				//Striker.clickMouse(StrikerUtils.getClickPoint(bounds), 1);
				//Striker.delayMS(30, 70);
				//Striker.moveMouse(mPos);
				//Striker.delayMS(20, 40);
			}
			else
			{
				log.debug("Cant do anything! {} -> {}", bounds, client.getCanvas().getBounds());
			}
		}
	}

	@Override
	public void clicked(MenuOptionClicked clicked)
	{
		switch (clicked.getOption())
		{
			case "WALK":
				log.debug("walk {}", walkTile);
				//Striker.schedule(() -> walkNextTile(walkTile.toLocalPoint(client), client.getMouseCanvasPosition()), 25);
				clicked.consume();
				break;
			case "HOP":
				//Striker.schedule(this::doHop, 10);
				clicked.consume();
				break;
			case "TRADE":
				clicked.setOption("Trade");
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
			case "BUY ALL":
				clicked.setOption("Buy 50");
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
		coins = items.stream().filter(f -> f.getId()  == COINS_995).findFirst().orElse(null);
		List<Integer> buyIds = Arrays.stream(CharterItem.values()).map(CharterItem::getId).collect(Collectors.toList());
		buyItems = items.stream().filter(f -> buyIds.contains(f.getId())).collect(Collectors.toList());
		log.debug("buyItems: {}", buyItems);
		log.debug("buyIds: {}", buyIds);
	}

	@Override
	public void onGameTick()
	{
		if (haveTargetArea())
		{
			final NPCQuery crewQ = new NPCQuery().idEquals(charterCrewIds);
			final GameObjectQuery depBoxQ = new GameObjectQuery().idEquals(targetArea.getDepBoxId());
			depBox = depBoxQ.result(client).nearestTo(client.getLocalPlayer());
			this.crew = crewQ.result(client).nearestTo(client.getLocalPlayer());
			this.walkTile = targetArea.getPathToShips().getNextTile(client);
		}
	}
}
