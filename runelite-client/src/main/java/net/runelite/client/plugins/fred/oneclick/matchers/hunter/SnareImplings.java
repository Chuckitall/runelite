package net.runelite.client.plugins.fred.oneclick.matchers.hunter;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.fred.oneclick.util.MenuEntryMatcher;
import net.runelite.client.plugins.fred.api.wrappers._Item;

import static net.runelite.api.ItemID.EARTH_RUNE;
import static net.runelite.api.ItemID.NATURE_RUNE;
import static net.runelite.api.ItemID.WATER_RUNE;
import static net.runelite.api.MenuOpcode.NPC_FIRST_OPTION;
import static net.runelite.api.MenuOpcode.SPELL_CAST_ON_NPC;
import static net.runelite.api.NpcID.BABY_IMPLING;
import static net.runelite.api.NpcID.BABY_IMPLING_1645;
import static net.runelite.api.NpcID.CRYSTAL_IMPLING;
import static net.runelite.api.NpcID.CRYSTAL_IMPLING_8742;
import static net.runelite.api.NpcID.CRYSTAL_IMPLING_8743;
import static net.runelite.api.NpcID.CRYSTAL_IMPLING_8744;
import static net.runelite.api.NpcID.CRYSTAL_IMPLING_8745;
import static net.runelite.api.NpcID.CRYSTAL_IMPLING_8746;
import static net.runelite.api.NpcID.CRYSTAL_IMPLING_8747;
import static net.runelite.api.NpcID.CRYSTAL_IMPLING_8748;
import static net.runelite.api.NpcID.CRYSTAL_IMPLING_8749;
import static net.runelite.api.NpcID.CRYSTAL_IMPLING_8750;
import static net.runelite.api.NpcID.CRYSTAL_IMPLING_8751;
import static net.runelite.api.NpcID.CRYSTAL_IMPLING_8752;
import static net.runelite.api.NpcID.CRYSTAL_IMPLING_8753;
import static net.runelite.api.NpcID.CRYSTAL_IMPLING_8754;
import static net.runelite.api.NpcID.CRYSTAL_IMPLING_8755;
import static net.runelite.api.NpcID.CRYSTAL_IMPLING_8756;
import static net.runelite.api.NpcID.CRYSTAL_IMPLING_8757;
import static net.runelite.api.NpcID.DRAGON_IMPLING;
import static net.runelite.api.NpcID.DRAGON_IMPLING_1654;
import static net.runelite.api.NpcID.EARTH_IMPLING;
import static net.runelite.api.NpcID.EARTH_IMPLING_1648;
import static net.runelite.api.NpcID.ECLECTIC_IMPLING;
import static net.runelite.api.NpcID.ECLECTIC_IMPLING_1650;
import static net.runelite.api.NpcID.ESSENCE_IMPLING;
import static net.runelite.api.NpcID.ESSENCE_IMPLING_1649;
import static net.runelite.api.NpcID.GOURMET_IMPLING;
import static net.runelite.api.NpcID.GOURMET_IMPLING_1647;
import static net.runelite.api.NpcID.LUCKY_IMPLING;
import static net.runelite.api.NpcID.LUCKY_IMPLING_7302;
import static net.runelite.api.NpcID.MAGPIE_IMPLING;
import static net.runelite.api.NpcID.MAGPIE_IMPLING_1652;
import static net.runelite.api.NpcID.NATURE_IMPLING;
import static net.runelite.api.NpcID.NATURE_IMPLING_1651;
import static net.runelite.api.NpcID.NINJA_IMPLING;
import static net.runelite.api.NpcID.NINJA_IMPLING_1653;
import static net.runelite.api.NpcID.YOUNG_IMPLING;
import static net.runelite.api.NpcID.YOUNG_IMPLING_1646;

@Singleton
@Slf4j
public class SnareImplings extends MenuEntryMatcher
{
	private final ImmutableSet implingIds = ImmutableSet.of(
		BABY_IMPLING, BABY_IMPLING_1645,
		YOUNG_IMPLING, YOUNG_IMPLING_1646,
		GOURMET_IMPLING, GOURMET_IMPLING_1647,
		EARTH_IMPLING, EARTH_IMPLING_1648,
		ESSENCE_IMPLING, ESSENCE_IMPLING_1649,
		ECLECTIC_IMPLING, ECLECTIC_IMPLING_1650,
		NATURE_IMPLING, NATURE_IMPLING_1651,
		MAGPIE_IMPLING, MAGPIE_IMPLING_1652,
		NINJA_IMPLING, NINJA_IMPLING_1653,
		CRYSTAL_IMPLING, CRYSTAL_IMPLING_8742, CRYSTAL_IMPLING_8743, CRYSTAL_IMPLING_8744, CRYSTAL_IMPLING_8745, CRYSTAL_IMPLING_8746, CRYSTAL_IMPLING_8747, CRYSTAL_IMPLING_8748, CRYSTAL_IMPLING_8749, CRYSTAL_IMPLING_8750, CRYSTAL_IMPLING_8751, CRYSTAL_IMPLING_8752, CRYSTAL_IMPLING_8753, CRYSTAL_IMPLING_8754, CRYSTAL_IMPLING_8755, CRYSTAL_IMPLING_8756, CRYSTAL_IMPLING_8757,
		DRAGON_IMPLING, DRAGON_IMPLING_1654,
		LUCKY_IMPLING, LUCKY_IMPLING_7302
	);

	private _Item earthRuneItem = null;
	private _Item waterRuneItem = null;
	private _Item natureRuneItem = null;

//	private NPC implingNPC = null;

	@Override
	public boolean addedMatches(MenuEntryAdded added)
	{
		//new MenuEntry("Catch", "<col=ffff00>Eclectic impling", 7815, 9, 0, 0, false)
		//log.debug("shift {}", plugin.isShiftPressed());
		if (natureRuneItem == null || earthRuneItem == null || waterRuneItem == null)
		{
			return false;
		}
		if (!plugin.isShiftPressed() || !added.getMenuOpcode().equals(NPC_FIRST_OPTION) || natureRuneItem.getQty() < 3 || waterRuneItem.getQty() < 4 || earthRuneItem.getQty() < 4)
		{
			return false;
		}
		NPC impling = client.getCachedNPCs()[added.getIdentifier()];
		return impling != null && implingIds.contains(impling.getId());
	}

	@Override
	public MenuEntry generate(MenuEntryAdded added)
	{
		NPC impling = client.getCachedNPCs()[added.getIdentifier()];
		if (impling != null)
		{
			//new MenuEntry("Cast", "<col=00ff00>Snare</col><col=ffffff> -> <col=ffff00>Eclectic impling", 7815, 8, 0, 0, false)
			return new MenuEntry(
				"CAST",
				"<col=00ff00>Snare</col><col=ffffff> -> <col=ffff00>" + client.getNpcDefinition(impling.getId()).getName(),
				impling.getIndex(),
				SPELL_CAST_ON_NPC.getId(),
				0,
				0,
				true
			);
		}
		return null;
	}

	@Override
	public boolean clickedMatches(MenuOptionClicked clicked)
	{
		return clicked.getOption().equals("CAST");
	}

	@Override
	public void clicked(MenuOptionClicked clicked)
	{
		switch (clicked.getOption())
		{
			case "CAST":
				client.setSelectedSpellName("<col=00ff00>Snare</col>");
				client.setSelectedSpellWidget(WidgetInfo.PACK(WidgetID.SPELLBOOK_GROUP_ID, 34));
				clicked.setOption("Cast");
				break;
			default:
				clicked.consume();
				break;
		}
	}

	@Override
	public void onInventoryChanged(final List<_Item> items)
	{
		waterRuneItem = items.stream().filter(f -> f.getId()  == WATER_RUNE).findFirst().orElse(null);
		earthRuneItem = items.stream().filter(f -> f.getId()  == EARTH_RUNE).findFirst().orElse(null);
		natureRuneItem = items.stream().filter(f -> f.getId()  == NATURE_RUNE).findFirst().orElse(null);
	}

	@Override
	public void onGameTick()
	{

	}
}