package net.runelite.client.plugins.fred.oneclick;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("fredsOneclick")
public interface OneclickConfig extends Config
{
	@ConfigSection(
		position = 0,
		name = "Crafting",
		description = "Crafting enhancements",
		keyName = "crafting"
	)
	default boolean crafting()
	{
		return true;
	}

	@ConfigItem(
		keyName = "glassblowing",
		name = "Glass blowing",
		description = "Makes left click glassblowing pipe select the next required action.",
		position = 0,
		section = "crafting"
	)
	default boolean glassblowing()
	{
		return false;
	}

	@ConfigItem(
		keyName = "fillBucketSand",
		name = "Fill Bucket with Sand",
		description = "Makes left click empty bucket fill at sand pit",
		position = 0,
		section = "crafting"
	)
	default boolean fillBucketSand()
	{
		return false;
	}

	@ConfigSection(
		position = 0,
		name = "Construction",
		description = "Construction enhancements",
		keyName = "construction"
	)
	default boolean construction()
	{
		return false;
	}

	@ConfigItem(
		keyName = "philesUnnote",
		name = "Philes Unnote Planks",
		description = "Make left click of noted planks \"use on philes\" if near philes.</br>Makes left click of noted planks \"Exchange all\" if dialog is open.",
		position = 0,
		section = "construction"
	)
	default boolean philesUnnote()
	{
		return false;
	}

	@ConfigSection(
		position = 0,
		name = "Smithing",
		description = "Smithing enhancements",
		keyName = "smithing"
	)
	default boolean smithing()
	{
		return true;
	}

	@ConfigItem(
		keyName = "leftClickSmith",
		name = "Smithing helpers",
		description = "Single click on bank un-notes bars.</br>Single click on anvil uses bar.",
		position = 0,
		section = "smithing"
	)
	default boolean leftClickSmith()
	{
		return false;
	}

	@ConfigSection(
		position = 0,
		name = "Hunter",
		description = "Hunter enhancements",
		keyName = "hunter"
	)
	default boolean hunter()
	{
		return true;
	}

	@ConfigItem(
		keyName = "snareImplings",
		name = "Snare Implings",
		description = "Adds shift+left click on impling to cast snare.",
		position = 0,
		section = "hunter"
	)
	default boolean snareImplings()
	{
		return false;
	}

	@ConfigItem(
		keyName = "birdhouse",
		name = "Birdhouse helper",
		description = "",
		position = 0,
		section = "hunter"
	)
	default boolean birdhouse()
	{
		return false;
	}

	@ConfigSection(
		position = 0,
		name = "Magic",
		description = "Magic enhancements",
		keyName = "magic"
	)
	default boolean magic()
	{
		return true;
	}

	@ConfigItem(
		keyName = "superheat",
		name = "Superheat helper",
		description = "",
		position = 0,
		section = "magic"
	)
	default boolean superheat()
	{
		return true;
	}

	@ConfigSection(
		position = 0,
		name = "Woodcutting",
		description = "Woodcutting enhancements",
		keyName = "woodcutting"
	)
	default boolean woodcutting()
	{
		return true;
	}

	@ConfigItem(
		keyName = "makeplank",
		name = "Make Plank",
		description = "Swaps use w/ 'Buy Plank' if a sawmill operator is nearby.",
		position = 0,
		section = "woodcutting"
	)
	default boolean makePlank()
	{
		return false;
	}

	@ConfigSection(
		position = 0,
		name = "Firemaking",
		description = "Firemaking enhancements",
		keyName = "firemaking"
	)
	default boolean firemaking()
	{
		return true;
	}

	@ConfigItem(
		keyName = "lightlogs",
		name = "Light logs",
		description = "Swaps use w/ light on logs when inventory has a tinderbox.",
		position = 0,
		section = "firemaking"
	)
	default boolean lightLogs()
	{
		return false;
	}

	@ConfigSection(
		position = 0,
		name = "Fletching",
		description = "Fletching Enhancements",
		keyName = "fletching"
	)
	default boolean fletching()
	{
		return true;
	}

	@ConfigItem(
		keyName = "fletchDarts",
		name = "Fletch Darts",
		description = "Swaps use dart tips w/ use feathers on dart tips.",
		position = 0,
		section = "fletching"
	)
	default boolean fletchDarts()
	{
		return false;
	}

	@ConfigSection(
		position = 0,
		name = "Fishing",
		description = "Fishing Enhancements",
		keyName = "fishing"
	)
	default boolean fishing()
	{
		return true;
	}

	@ConfigItem(
		keyName = "fishNearest",
		name = "Fish Nearest",
		description = "Swaps \"use [fishing tool]\" with \"[action] fishing spot\" on the nearest appropriate target.",
		position = 0,
		section = "fishing"
	)
	default boolean fishNearest()
	{
		return true;
	}

	@ConfigSection(
		position = 1,
		name = "Other",
		description = "Other enhancements",
		keyName = "other"
	)
	default boolean other()
	{
		return true;
	}

	@ConfigItem(
		keyName = "logs3T",
		name = "Logs 3 tick",
		description = "Single click on logs results in using knife on logs",
		position = 0,
		section = "other"
	)
	default boolean logs3T()
	{
		return true;
	}

	@ConfigItem(
		keyName = "leftClickHerbPaste",
		name = "Mix Herbtar",
		description = "Single click on tar results in making herb tar",
		position = 10,
//		hidden = true,
		section = "other"
	)
	default boolean leftClickHerbPaste()
	{
		return false;
	}

	@ConfigItem(
		keyName = "threeTickFishing",
		name = "3t Fish helper",
		description = "Only allow this event on 3rd ticks, double click to drop fish.",
		position = 10,
//		hidden = true,
		section = "other",
		disabledBy = "leftClickHerbPaste"
	)
	default boolean threeTickFishing()
	{
		return false;
	}

	@ConfigSection(
		position = 10,
		name = "Twisted",
		description = "Twisted enhancements",
		keyName = "twisted"
	)
	default boolean twisted()
	{
		return true;
	}

	@ConfigItem(
		keyName = "harvesterDrop",
		name = "Harvester Drop",
		description = "Make click on resource drop item if inv is full.",
		position = 10,
		section = "twisted"
	)
	default boolean harvesterDrop()
	{
		return false;
	}
}
