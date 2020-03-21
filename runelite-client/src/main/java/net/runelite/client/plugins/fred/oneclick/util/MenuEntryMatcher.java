package net.runelite.client.plugins.fred.oneclick.util;


import java.util.List;
import javax.annotation.Nullable;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.plugins.fred.oneclick.OneclickPlugin;
import net.runelite.client.plugins.fred.oneclick.api._Item;

public abstract class MenuEntryMatcher
{
	@Inject
	protected Client client;
	@Inject
	protected OneclickPlugin plugin;
	protected _Item getItemAtIdx(List<_Item> items, int idx)
	{
		return items.stream().filter(f -> f.getIdx() == idx).findFirst().orElse(null);
	}

	public abstract boolean addedMatches(MenuEntryAdded added);

	@Nullable
	public abstract MenuEntry generate(MenuEntryAdded added);

	public abstract boolean clickedMatches(MenuOptionClicked clicked);

	public abstract void clicked(MenuOptionClicked clicked);

	public void init()
	{

	}

	public void onInventoryChanged(final List<_Item> items)
	{

	}

	public void onGameTick()
	{

	}
}
