package net.runelite.client.plugins.fred.api.controllers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.LocatableQueryResults;
import net.runelite.api.ObjectDefinition;
import net.runelite.api.ObjectID;
import net.runelite.api.Point;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.plugins.fred.api.wrappers._Tile;
import net.runelite.client.plugins.fred.api.wrappers._WidgetItem;
import net.runelite.client.plugins.fredexperimental.striker.Striker;

@Singleton
@Slf4j
public class _Banking
{
	private static final int ITEM_EMPTY = 6512;

	private Client client;

	@Inject
	private _Vision vision;

	@Inject
	public _Banking(Client client)
	{
		this.client = client;
	}

	private final static Set<Integer> bankIds = ImmutableSet.of(ObjectID.BANK_BOOTH_16642, ObjectID.BANK_CHESTWRECK);

	//return if bank is open, not on settings page, not on pin page
	public boolean isBankOpen()
	{
		Widget bankContainer = client.getWidget(WidgetInfo.BANK_CONTENT_CONTAINER);
		Widget bankItemContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
		return bankContainer != null && !bankContainer.isHidden() && bankItemContainer != null && !bankItemContainer.isHidden();
	}

	public boolean isNearBank()
	{
		return getNearbyBanks().size() > 0;
	}

	public boolean isBankItemInView(_WidgetItem bankItem)
	{
		Widget bankItemContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
		Widget bank = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
		if (bankItem != null && bankItemContainer != null && !bankItemContainer.isHidden() && bank != null && !bank.isHidden())
		{
			return bank.getBounds().contains(bankItem.getCanvasBounds(true));
		}
		return false;
	}

	public _WidgetItem getBankItem(int id)
	{
		return getBankItems().stream().filter(f -> f.getId() == id).findFirst().orElse(null);
	}

	public _WidgetItem getInventoryItem(int id)
	{
		return getInvItems().stream().filter(f -> f.getId() == id).findFirst().orElse(null);
	}

	public List<GameObject> getNearbyBanks()
	{
		LocatableQueryResults<GameObject> results = new GameObjectQuery().filter(f ->
		{
			int id = f.getId();
			ObjectDefinition def = client.getObjectDefinition(id);
			if (def.getImpostorIds() != null)
			{
				id = def.getImpostor().getId();
			}
			return bankIds.contains(id);
		}).result(client);
		return ImmutableList.copyOf(results.list);
	}

	private List<_WidgetItem> getBankItems()
	{
		Widget bank = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
		List<_WidgetItem> bankItems = Lists.newArrayList();
		if (isBankOpen() && bank != null && !bank.isSelfHidden())
		{
			Widget[] children = bank.getDynamicChildren();
			for (int i = 0; i < children.length; i++)
			{
				Widget child = children[i];
				if (child.getItemId() == ITEM_EMPTY || child.isSelfHidden())
				{
					continue;
				}
				child.revalidate();
				// set bounds to same size as default inventory
				Rectangle bounds = child.getBounds();
				bounds.setBounds(bounds.x - 1, bounds.y - 1, 32, 32);

				// Index is set to 0 because the widget's index does not correlate to the order in the bank
				bankItems.add(_WidgetItem.from(new WidgetItem(child.getItemId(), child.getItemQuantity(), i, bounds, child)));
			}
			return ImmutableList.copyOf(bankItems);
		}
		else
		{
			return ImmutableList.of();
		}
	}

	private List<_WidgetItem> getInvItems()
	{
		Widget inv = client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER);
		List<_WidgetItem> invItems = Lists.newArrayList();
		if (isBankOpen() && inv != null && !inv.isSelfHidden())
		{
			inv.revalidate();
			Widget[] children = inv.getDynamicChildren();
			for (int i = 0; i < children.length; i++)
			{
				Widget child = children[i];
				if (child.isSelfHidden())
				{
					continue;
				}
				child.revalidate();
				// set bounds to same size as default inventory
				Rectangle bounds = child.getBounds();
				bounds.setBounds(bounds.x - 1, bounds.y - 1, 32, 32);
				invItems.add(_WidgetItem.from(new WidgetItem(child.getItemId(), child.getItemQuantity(), i, bounds, child)));
			}
			return ImmutableList.copyOf(invItems);
		}
		else
		{
			return ImmutableList.of();
		}
	}

	public boolean doDeposit(_WidgetItem item)
	{
		if (item == null)
		{
			return false;
		}
		Point loc = Point.fromNative(item.getCanvasBounds(false).getLocation());
		if (loc.getX() == 0 && loc.getY() == 0)
		{
			return false;
		}

		Widget inv = client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER);
		if (isBankOpen() && inv != null && !inv.isSelfHidden())
		{
			Point pos = vision.getClickPoint(item.getCanvasBounds(true));
			log.debug("depositing item {} requested pos {}", item, pos);
			Striker.clickMouse(pos, 1);
			Striker.delayMS(25, 100);
			return true;
		}
		else
		{
			return false;
		}
	}

	public boolean doDeposit(int itemId)
	{
		_WidgetItem item = getInventoryItem(itemId);
		if (item != null)
		{
			return doDeposit(item);
		}
		return false;
	}

	public boolean doWithdraw(_WidgetItem item)
	{
		if (isBankOpen() && isBankItemInView(item))
		{
			Point pos = vision.getClickPoint(item.getCanvasBounds(true));
			Striker.clickMouse(pos, 1);
			Striker.delayMS(25, 100);
			return true;
		}
		else
		{
			return false;
		}
	}

	public boolean doWithdraw(int itemId)
	{
		_WidgetItem item = getBankItem(itemId);
		if (item != null)
		{
			return doWithdraw(item);
		}
		return false;
	}

	public boolean doOpenBank()
	{
		final _Tile pLoc = _Tile.ofPlayer(client);
		if (pLoc == null || isBankOpen() || !isNearBank())
		{
			return false;
		}
		List<GameObject> banks = getNearbyBanks().stream().filter(f -> f.getConvexHull() != null).sorted(Comparator.comparingInt(f -> _Tile.fromGameObject(f).distanceTo(pLoc))).limit(3).collect(Collectors.toList());
		GameObject bank = banks.stream().filter(vision::isGameObjectOnScreen).findFirst().orElse(null);
		if (bank != null)
		{
			Point pos = vision.getClickPoint(bank.getConvexHull());
			Striker.clickMouse(pos, 1);
			Striker.delayMS(2000);
			return isBankOpen();
		}
		else
		{
			return false;
		}
	}

	public boolean doCloseBank()
	{
		if (isBankOpen())
		{
			Striker.typeKey(KeyEvent.VK_ESCAPE);
			Striker.delayMS(200);
			return !isBankOpen();
		}
		else
		{
			return false;
		}
	}
}
