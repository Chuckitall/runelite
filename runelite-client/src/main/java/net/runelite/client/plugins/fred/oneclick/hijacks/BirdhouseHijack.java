package net.runelite.client.plugins.fred.oneclick.hijacks;

import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.MenuOpcode;
import net.runelite.api.ObjectDefinition;
import net.runelite.api.ObjectID;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.queries.GameObjectQuery;

import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.fred.oneclick.util.ItemSelector;
import net.runelite.client.plugins.fred.oneclick.util.MenuEntryHijack;

import static net.runelite.api.MenuOpcode.ITEM_USE;

@Slf4j
@Singleton
public class BirdhouseHijack extends MenuEntryHijack
{
	private ItemSelector birdSeed = new ItemSelector(ImmutableSet.of(ItemID.BARLEY_SEED, ItemID.HAMMERSTONE_SEED, ItemID.ASGARNIAN_SEED, ItemID.JUTE_SEED, ItemID.YANILLIAN_SEED, ItemID.KRANDORIAN_SEED, ItemID.WILDBLOOD_SEED));
	private ItemSelector birdHouse = new ItemSelector(ImmutableSet.of(ItemID.BIRD_HOUSE, ItemID.OAK_BIRD_HOUSE, ItemID.WILLOW_BIRD_HOUSE, ItemID.TEAK_BIRD_HOUSE, ItemID.MAPLE_BIRD_HOUSE, ItemID.MAHOGANY_BIRD_HOUSE, ItemID.YEW_BIRD_HOUSE, ItemID.MAGIC_BIRD_HOUSE, ItemID.REDWOOD_BIRD_HOUSE));

	private static final Set<Integer> EMPTY_BIRDHOUSE_IDS = ImmutableSet.of(
		ObjectID.BIRDHOUSE_EMPTY, ObjectID.OAK_BIRDHOUSE_EMPTY, ObjectID.WILLOW_BIRDHOUSE_EMPTY,
		ObjectID.TEAK_BIRDHOUSE_EMPTY, ObjectID.MAPLE_BIRDHOUSE_EMPTY, ObjectID.MAHOGANY_BIRDHOUSE_EMPTY,
		ObjectID.YEW_BIRDHOUSE_EMPTY, ObjectID.MAGIC_BIRDHOUSE_EMPTY, ObjectID.REDWOOD_BIRDHOUSE_EMPTY,
		ObjectID.BIRDHOUSE, ObjectID.OAK_BIRDHOUSE, ObjectID.WILLOW_BIRDHOUSE,
		ObjectID.TEAK_BIRDHOUSE, ObjectID.MAPLE_BIRDHOUSE, ObjectID.MAHOGANY_BIRDHOUSE,
		ObjectID.YEW_BIRDHOUSE, ObjectID.MAGIC_BIRDHOUSE, ObjectID.REDWOOD_BIRDHOUSE
	);
	private static final Set<Integer> IMPOSTER_IDS = ImmutableSet.of(30568, 30567, 30566, 30565);

	@Override
	protected void onEnabled()
	{
		birdSeed.clear();
		birdHouse.clear();
		eventBus.subscribe(ItemContainerChanged.class, this, this::onItemContainerChanged);
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			this.onItemContainerChanged(new ItemContainerChanged(InventoryID.INVENTORY.getId(), client.getItemContainer(InventoryID.INVENTORY)));
		}
	}

	@Override
	protected void onDisabled()
	{
		birdSeed.clear();
		birdHouse.clear();
		eventBus.unregister(this);
	}

	@Override
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (!this.isEnabled())// should be unnecessary
		{
			return;
		}
		if (!birdSeed.found() && !birdHouse.found())
		{
			return;
		}
		if (event.getMenuOpcode() == ITEM_USE && birdSeed.idxMatches(event.getParam0()))
		{
			GameObject obj = (new GameObjectQuery().idEquals(IMPOSTER_IDS).result(client).nearestTo(client.getLocalPlayer()));
			if (obj != null)
			{
				ObjectDefinition def = client.getObjectDefinition(obj.getId());
				if (def.getImpostor() != null) def = def.getImpostor();
				if (EMPTY_BIRDHOUSE_IDS.contains(def.getId()))
				{
					birdSeed.setSelectedIdx(event.getParam0());
					client.insertMenuItem("FILL", "<col=ff9040>" + client.getItemDefinition(event.getIdentifier()).getName() + "<col=ffffff> -> <col=ffff>" + def.getName(), MenuOpcode.ITEM_USE_ON_GAME_OBJECT.getId(), obj.getId(), obj.getLocalLocation().getSceneX(), obj.getLocalLocation().getSceneY(), true);
				}
			}
		}
		else if (event.getMenuOpcode() == ITEM_USE && birdHouse.idxMatches(event.getParam0()))
		{
			GameObject obj = (new GameObjectQuery().idEquals(IMPOSTER_IDS).result(client).nearestTo(client.getLocalPlayer()));
			if (obj != null)
			{
				ObjectDefinition def = client.getObjectDefinition(obj.getId());
				if (def.getImpostor() != null) def = def.getImpostor();
				if (ObjectID.SPACE == def.getId())
				{
					birdHouse.setSelectedIdx(event.getParam0());
					client.insertMenuItem("BUILD", "<col=ff9040>" + client.getItemDefinition(event.getIdentifier()).getName() + "<col=ffffff> -> <col=ffff>" + def.getName(), MenuOpcode.ITEM_USE_ON_GAME_OBJECT.getId(), obj.getId(), obj.getLocalLocation().getSceneX(), obj.getLocalLocation().getSceneY(), true);
				}
			}
		}
	}

	@Override
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (!this.isEnabled())// should be unnecessary
		{
			return;
		}
		if (!(event.getOption().equals("BUILD") || event.getOption().equals("FILL")))
		{
			return;
		}
		if (event.getOption().equals("FILL") && birdSeed.getSelectedIdx() != -1)
		{
			event.setOption("Use");
			client.setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
			client.setSelectedItemSlot(birdSeed.getSelectedIdx());
			client.setSelectedItemID(birdSeed.getSelectedId());
		}
		else if (event.getOption().equals("BUILD"))
		{
			event.setOption("Build");
			client.setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
			client.setSelectedItemSlot(birdHouse.getSelectedIdx());
			client.setSelectedItemID(birdHouse.getSelectedId());
		}
		else
		{
			event.consume();
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

		if (itemContainer != client.getItemContainer(InventoryID.INVENTORY))
		{
			return;
		}

		birdHouse.clear();
		birdSeed.clear();

		for (int idx = 0; idx < items.size(); idx++)
		{
			final int id = items.get(idx).getId();
			birdSeed.testAndAdd(id, idx);
			birdHouse.testAndAdd(id, idx);
		}
	}
}