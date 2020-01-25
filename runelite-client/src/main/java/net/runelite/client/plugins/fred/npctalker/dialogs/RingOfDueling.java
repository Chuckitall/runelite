package net.runelite.client.plugins.fred.npctalker.dialogs;

import net.runelite.api.MenuOpcode;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.fred.npctalker.DialogTree;
import org.apache.commons.lang3.ArrayUtils;

import static net.runelite.api.ItemID.RING_OF_DUELING1;
import static net.runelite.api.ItemID.RING_OF_DUELING2;
import static net.runelite.api.ItemID.RING_OF_DUELING3;
import static net.runelite.api.ItemID.RING_OF_DUELING4;
import static net.runelite.api.ItemID.RING_OF_DUELING5;
import static net.runelite.api.ItemID.RING_OF_DUELING6;
import static net.runelite.api.ItemID.RING_OF_DUELING7;
import static net.runelite.api.ItemID.RING_OF_DUELING8;

public class RingOfDueling implements DialogTree
{
	private final String[] options = new String[] { "Sand Casino", "Castle Wars", "Clan Wars"};
	private final int[][] paths = new int[][]{{1}, {2}, {3}};
	private final int[] item_ids = new int[] {RING_OF_DUELING1,RING_OF_DUELING2,RING_OF_DUELING3,RING_OF_DUELING4,RING_OF_DUELING5,RING_OF_DUELING6,RING_OF_DUELING7,RING_OF_DUELING8};

	@Override
	public String[] getPaths()
	{
		return options;
	}

	@Override
	public boolean shouldShowOptions(MenuEntryAdded added)
	{
		if (WidgetID.INVENTORY_GROUP_ID == WidgetInfo.TO_GROUP(added.getParam1()) && added.getOpcode() == MenuOpcode.ITEM_FOURTH_OPTION.getId() && ArrayUtils.contains(item_ids, added.getIdentifier())) //check item is in inventory
		{
			return true;
		}
		return false;
	}

	@Override
	public int[] getPath(int j)
	{
		if(paths.length <= j || j < 0)
		{
			return null;
		}
		return paths[j];
	}

	@Override
	public MenuOptionClicked transform(MenuOptionClicked clicked)
	{
		clicked.setOpcode(MenuOpcode.ITEM_FOURTH_OPTION.getId());
		clicked.setOption("Rub");
		return clicked;
	}
}
