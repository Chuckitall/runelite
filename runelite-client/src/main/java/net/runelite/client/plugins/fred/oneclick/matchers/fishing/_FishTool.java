package net.runelite.client.plugins.fred.oneclick.matchers.fishing;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemID;

import static net.runelite.api.NpcID.FISHING_SPOT_1542;
import static net.runelite.api.NpcID.FISHING_SPOT_7323;

@Getter(AccessLevel.PACKAGE)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@AllArgsConstructor
@ToString
@Slf4j
enum _FishTool
{
	BARBARIAN_ROD("Use-rod", ItemID.BARBARIAN_ROD, new int[] {FISHING_SPOT_1542, FISHING_SPOT_7323});

	String option;
	int[] toolIds;
	int[] spotIds;

	_FishTool(final String option, final int tool, final int[] spots)
	{
		this(option, new int[] {tool}, spots);
	}
}
