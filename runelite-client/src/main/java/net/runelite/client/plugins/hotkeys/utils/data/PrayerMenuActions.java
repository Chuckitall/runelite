package net.runelite.client.plugins.hotkeys.utils.data;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.MenuEntry;
import net.runelite.api.Prayer;

@Getter
@AllArgsConstructor
public enum PrayerMenuActions
{
	THICK_SKIN("Thick Skin", Prayer.THICK_SKIN,
		new MenuEntry("Activate", "<col=ff9040>Thick Skin</col>", 1, 57, -1, 35454981, false)),
	BURST_OF_STRENGTH("Burst of Strength", Prayer.BURST_OF_STRENGTH,
		new MenuEntry("Activate", "<col=ff9040>Burst of Strength</col>", 1, 57, -1, 35454982, false)),
	CLARITY_OF_THOUGHT("Clarity Of Thought", Prayer.CLARITY_OF_THOUGHT,
		new MenuEntry("Activate", "<col=ff9040>Clarity of Thought</col>", 1, 57, -1, 35454983, false)),
	SHARP_EYE("Sharp Eye", Prayer.SHARP_EYE,
		new MenuEntry("Activate", "<col=ff9040>Sharp Eye</col>", 1, 57, -1, 35454999, false)),
	MYSTIC_WILL("Mystic Will", Prayer.MYSTIC_WILL,
		new MenuEntry("Activate", "<col=ff9040>Mystic Will</col>", 1, 57, -1, 35455000, false)),
	ROCK_SKIN("Rock Skin", Prayer.ROCK_SKIN,
		new MenuEntry("Activate", "<col=ff9040>Rock Skin</col>", 1, 57, -1, 35454984, false)),
	SUPERHUMAN_STRENGTH("Superhuman Strength", Prayer.SUPERHUMAN_STRENGTH,
		new MenuEntry("Activate", "<col=ff9040>Superhuman Strength</col>", 1, 57, -1, 35454985, false)),
	IMPROVED_REFLEXES("Improved Reflexes", Prayer.IMPROVED_REFLEXES,
		new MenuEntry("Activate", "<col=ff9040>Improved Reflexes</col>", 1, 57, -1, 35454986, false)),
	RAPID_RESTORE("Rapid Restore", Prayer.RAPID_RESTORE,
		new MenuEntry("Activate", "<col=ff9040>Rapid Restore</col>", 1, 57, -1, 35454987, false)),
	RAPID_HEAL("Rapid Heal", Prayer.RAPID_HEAL,
		new MenuEntry("Activate", "<col=ff9040>Rapid Heal</col>", 1, 57, -1, 35454988, false)),
	PROTECT_ITEM("Protect Item", Prayer.PROTECT_ITEM,
		new MenuEntry("Activate", "<col=ff9040>Protect Item</col>", 1, 57, -1, 35454989, false)),
	HAWK_EYE("Hawk Eye", Prayer.HAWK_EYE,
		new MenuEntry("Activate", "<col=ff9040>Hawk Eye</col>", 1, 57, -1, 35455001, false)),
	MYSTIC_LORE("Mystic Lore", Prayer.MYSTIC_LORE,
		new MenuEntry("Activate", "<col=ff9040>Mystic Lore</col>", 1, 57, -1, 35455002, false)),
	STEEL_SKIN("Steel Skin", Prayer.STEEL_SKIN,
		new MenuEntry("Activate", "<col=ff9040>Steel Skin</col>", 1, 57, -1, 35454990, false)),
	ULTIMATE_STRENGTH("Ultimate Strength", Prayer.ULTIMATE_STRENGTH,
		new MenuEntry("Activate", "<col=ff9040>Ultimate Strength</col>", 1, 57, -1, 35454991, false)),
	INCREDIBLE_REFLEXES("Incredible Reflexes", Prayer.INCREDIBLE_REFLEXES,
		new MenuEntry("Activate", "<col=ff9040>Incredible Reflexes</col>", 1, 57, -1, 35454992, false)),
	PROTECT_FROM_MAGIC("Protect from Magic", Prayer.PROTECT_FROM_MAGIC,
		new MenuEntry("Activate", "<col=ff9040>Protect from Magic</col>", 1, 57, -1, 35454993, false)),
	PROTECT_FROM_MISSILES("Protect from Missiles", Prayer.PROTECT_FROM_MISSILES,
		new MenuEntry("Activate", "<col=ff9040>Protect from Missiles</col>", 1, 57, -1, 35454994, false)),
	PROTECT_FROM_MELEE("Protect from Melee", Prayer.PROTECT_FROM_MELEE,
		new MenuEntry("Activate", "<col=ff9040>Protect from Melee</col>", 1, 57, -1, 35454995, false)),
	EAGLE_EYE("Eagle Eye", Prayer.EAGLE_EYE,
		new MenuEntry("Activate", "<col=ff9040>Eagle Eye</col>", 1, 57, -1, 35455003, false)),
	MYSTIC_MIGHT("Mystic Might", Prayer.MYSTIC_MIGHT,
		new MenuEntry("Activate", "<col=ff9040>Mystic Might</col>", 1, 57, -1, 35455004, false)),
	RETRIBUTION("Retribution", Prayer.RETRIBUTION,
		new MenuEntry("Activate", "<col=ff9040>Retribution</col>", 1, 57, -1, 35454996, false)),
	REDEMPTION("Redemption", Prayer.REDEMPTION,
		new MenuEntry("Activate", "<col=ff9040>Redemption</col>", 1, 57, -1, 35454997, false)),
	SMITE("Smite", Prayer.SMITE,
		new MenuEntry("Activate", "<col=ff9040>Smite</col>", 1, 57, -1, 35454998, false)),
	CHIVALRY("Chivalry", Prayer.CHIVALRY,
		new MenuEntry("Activate", "<col=ff9040>Chivalry</col>", 1, 57, -1, 35455005, false)),
	PIETY("Piety", Prayer.PIETY,
		new MenuEntry("Activate", "<col=ff9040>Piety</col>", 1, 57, -1, 35455006, false)),
	PRESERVE("Preserve", Prayer.PRESERVE,
		new MenuEntry("Activate", "<col=ff9040>Preserve</col>", 1, 57, -1, 35455009, false)),
	RIGOUR("Rigour", Prayer.RIGOUR,
		new MenuEntry("Activate", "<col=ff9040>Rigour</col>", 1, 57, -1, 35455007, false)),
	AUGURY("Augury", Prayer.AUGURY,
		new MenuEntry("Activate", "<col=ff9040>Augury</col>", 1, 57, -1, 35455008, false));

	private final String name;
	private final Prayer prayer;
	private final MenuEntry menuEntry;
	private static final Map<Prayer, MenuEntry> map;

	static
	{
		ImmutableMap.Builder<Prayer, MenuEntry> builder = ImmutableMap.builder();

		for (PrayerMenuActions prayer : values())
		{
			builder.put(prayer.prayer, prayer.menuEntry);
		}

		map = builder.build();
	}

	public static MenuEntry getEntry(Prayer prayer)
	{
		return map.get(prayer);
	}
}