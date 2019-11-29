package net.runelite.client.plugins.fred.oneclick.matchers.crafting;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameObject;
import net.runelite.api.ItemID;
import net.runelite.api.MenuEntry;
import net.runelite.api.MenuOpcode;
import net.runelite.api.ObjectID;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.fred.oneclick.util.MenuEntryMatcher;
import net.runelite.client.plugins.fred.api.wrappers._Item;

import static net.runelite.api.MenuOpcode.ITEM_USE;

@Singleton
@Slf4j
public class FillBucketSand extends MenuEntryMatcher
{
	private Set<Integer> emptyBucketIds = ImmutableSet.of(ItemID.BUCKET);
	private List<_Item> emptyBucketItems = Lists.newArrayList();

	private Set<Integer> sandpitIds = ImmutableSet.of(ObjectID.SAND_PIT);
	private GameObject sandpit = null;

	private boolean haveSandpit()
	{
		return sandpit != null;
	}

	@Override
	public boolean addedMatches(MenuEntryAdded added)
	{
		if (emptyBucketItems.size() == 0 || !haveSandpit())
		{
			return false;
		}
		return added.getOpcode() == ITEM_USE.getId() && getItemAtIdx(emptyBucketItems, added.getParam0()) != null;
	}

	@Override
	public MenuEntry generate(MenuEntryAdded added)
	{
		_Item bucket = getItemAtIdx(emptyBucketItems, added.getParam0());
		if (bucket != null && haveSandpit())
		{
			return new MenuEntry("FILL", "<col=ff9040>" + client.getItemDefinition(bucket.getId()).getName() + "<col=ffffff> -> <col=ffff>" + client.getObjectDefinition(sandpit.getId()).getName(), WidgetInfo.INVENTORY.getId(), MenuOpcode.ITEM_USE_ON_GAME_OBJECT.getId(), bucket.getId(), bucket.getIdx(), true);
		}
		else
		{
			log.error("FUCK me bloody {}, {}", bucket, haveSandpit());
			return null;
		}
	}

	@Override
	public boolean clickedMatches(MenuOptionClicked clicked)
	{
		return clicked.getOption().equals("FILL");
	}

	@Override
	public void clicked(MenuOptionClicked clicked)
	{
		switch (clicked.getOption())
		{
			case "FILL":
				client.setSelectedItemWidget(clicked.getIdentifier());
				client.setSelectedItemID(clicked.getParam0());
				client.setSelectedItemSlot(clicked.getParam1());
				clicked.setOption("Use");
				clicked.setIdentifier(sandpit.getId());
				clicked.setParam0(sandpit.getSceneMinLocation().getX());
				clicked.setParam1(sandpit.getSceneMinLocation().getY());
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
		emptyBucketItems = items.stream().filter(f -> emptyBucketIds.contains(f.getId())).collect(Collectors.toList());
	}

	@Override
	public void onGameTick()
	{
		final GameObjectQuery sandpitQ = new GameObjectQuery().idEquals(sandpitIds);
		sandpit = sandpitQ.result(client).nearestTo(client.getLocalPlayer());
	}
}
