package net.runelite.client.plugins.fred.fighter;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;

import static java.awt.Color.BLUE;
import static java.awt.Color.RED;

/**
 * Created by npruff on 8/24/2019.
 */
@ConfigGroup("FredFighter")
public interface FighterConfig extends Config
{
	@ConfigItem(
		position = 0,
		keyName = "toggleScript",
		name = "Enable/Disable Keybind",
		description = "Enables/Disables script depending on situation."
	)
	default Keybind toggleScript()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
		position = 1,
		keyName = "togglePaused",
		name = "Pause/unpause Keybind",
		description = "Pauses the script."
	)
	default Keybind togglePaused()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
		position = 2,
		keyName = "debugKey1",
		name = "Debug1 Keybind",
		description = "triggers debug1 task flag."
	)
	default Keybind debugKey1()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
		position = 3,
		keyName = "debugKey2",
		name = "Debug2 Keybind",
		description = "triggers debug2 task flag."
	)
	default Keybind debugKey2()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
		position = 4,
		keyName = "debugKey3",
		name = "Debug3 Keybind",
		description = "triggers debug3 task flag."
	)
	default Keybind debugKey3()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
		position = 5,
		keyName = "debugKey4",
		name = "Debug4 Keybind",
		description = "triggers debug4 task flag."
	)
	default Keybind debugKey4()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
		position = 10,
		keyName = "npcName",
		name = "NPC Name",
		description = "Name of NPC to attack."
	)
	default String npcName()
	{
		return "";
	}

	@ConfigItem(
		position = 11,
		keyName = "npcLevel",
		name = "NPC Level",
		description = "Level of npc to attack."
	)
	default int npcLevel()
	{
		return -1;
	}

	@ConfigItem(
		position = 20,
		keyName = "highlightTarget",
		name = "Highlight Target",
		description = "Highlight monster you are fighting."
	)
	default boolean highlightTarget()
	{
		return false;
	}

	@ConfigItem(
		position = 21,
		keyName = "highlightTargets",
		name = "Highlight Targets",
		description = "Highlight monsters you can kill for your current slayer assignment"
	)
	default boolean highlightTargets()
	{
		return false;
	}

	@ConfigItem(
		position = 30,
		keyName = "targetColor",
		name = "Target Color",
		description = "Color of npc being attacked"
	)
	default Color targetColor()
	{
		return RED;
	}

	@ConfigItem(
		position = 31,
		keyName = "targetsColor",
		name = "Targets Color",
		description = "Color of the highlighted targets"
	)
	default Color targetsColor()
	{
		return BLUE;
	}
}
