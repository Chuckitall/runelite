package net.runelite.client.plugins.hotkeys.utils.data;

import net.runelite.api.MenuEntry;
import net.runelite.api.MenuOpcode;

public class Entries
{
	public static final MenuEntry equipItem = new MenuEntry("Wear", "Wear", -1, MenuOpcode.ITEM_SECOND_OPTION.getId(), -1, 9764864, false);
	public static final MenuEntry spec = new MenuEntry("Use <col=00ff00>Special Attack</col>", "", 1, MenuOpcode.CC_OP.getId(), -1, 38862884, false);
	public static final MenuEntry consume = new MenuEntry("Consume", "Digestible Object", -1, MenuOpcode.ITEM_FIRST_OPTION.getId(), -1, 9764864, false);
	public static final MenuEntry quickPrayer = new MenuEntry("Activate", "Quick-prayers", 1, MenuOpcode.CC_OP.getId(), -1, 10485774, false);

	public static final MenuEntry AttackPlayer = new MenuEntry("Attack", "Player who is about to sit", -1, MenuOpcode.PLAYER_SECOND_OPTION.getId(), 0, 0, false);
	public static final MenuEntry AttackNPC = new MenuEntry("Attack", "NPC who is about to sit", -1, MenuOpcode.NPC_SECOND_OPTION.getId(), 0, 0, false);
	public static final MenuEntry CastPlayer = new MenuEntry("Cast", "Spells and shit", -1, MenuOpcode.SPELL_CAST_ON_PLAYER.getId(), 0, 0, false);
	public static final MenuEntry CastNPC = new MenuEntry("Cast", "Spells and shit", -1, MenuOpcode.SPELL_CAST_ON_NPC.getId(), 0, 0, false);

	public static final MenuEntry FollowPlayer = new MenuEntry("Follow", "Person being followed", -1, 46, 0, 0, false);

	public static final MenuEntry itemUseOnItem = new MenuEntry("Use", "item->other item", -1, 31, -1, 9764864, false);
}
