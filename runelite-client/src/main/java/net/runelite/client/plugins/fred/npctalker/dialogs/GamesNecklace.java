package net.runelite.client.plugins.fred.npctalker.dialogs;

import net.runelite.api.MenuOpcode;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.fred.npctalker.DialogTree;
import org.apache.commons.lang3.ArrayUtils;

import static net.runelite.api.ItemID.GAMES_NECKLACE1;
import static net.runelite.api.ItemID.GAMES_NECKLACE2;
import static net.runelite.api.ItemID.GAMES_NECKLACE3;
import static net.runelite.api.ItemID.GAMES_NECKLACE4;
import static net.runelite.api.ItemID.GAMES_NECKLACE5;
import static net.runelite.api.ItemID.GAMES_NECKLACE6;
import static net.runelite.api.ItemID.GAMES_NECKLACE7;
import static net.runelite.api.ItemID.GAMES_NECKLACE8;

public class GamesNecklace implements DialogTree
{
	private final String[] options = new String[] { "Burthorpe", "Barbarian Outpost", "Corporeal Beast", "Tears of Guthix", "Wintertodt Camp" };
	private final int[][] paths = new int[][]{{1}, {2}, {3}, {4}, {5}};
	private final int[] item_ids = new int[] {GAMES_NECKLACE1, GAMES_NECKLACE2, GAMES_NECKLACE3, GAMES_NECKLACE4, GAMES_NECKLACE5, GAMES_NECKLACE6, GAMES_NECKLACE7, GAMES_NECKLACE8};

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
