package net.runelite.client.plugins.fred.npctalker;

import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;

public interface DialogTree
{
	//return all the potential paths this tree can walk
	String[] getPaths();
	boolean shouldShowOptions(MenuEntryAdded added); //is this an entry we should piggy back onto?
	int[] getPath(int j); //returns a list of options it needs to click to reach a specific outcome

	MenuOptionClicked transform(MenuOptionClicked clicked);
}
