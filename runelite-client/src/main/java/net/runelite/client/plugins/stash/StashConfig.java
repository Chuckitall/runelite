package net.runelite.client.plugins.stash;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

/**
 * Created by npruff on 9/2/2019.
 */
@ConfigGroup(StashPlugin.CONFIG_GROUP)
public interface StashConfig extends Config
{
	@ConfigItem(
		position = 0,
		keyName = "placeholder",
		name = "Placeholder",
		description = "Placeholder config"
	)
	default boolean placeholder()
	{
		return false;
	}

	@ConfigItem(
		position = 10,
		keyName = "showBeginner",
		name = "Show beginner",
		description = "Show beginner stashes"
	)
	default boolean showBeginner()
	{
		return true;
	}

	@ConfigItem(
		position = 11,
		keyName = "showEasy",
		name = "Show easy",
		description = "Show easy stashes"
	)
	default boolean showEasy()
	{
		return true;
	}
	@ConfigItem(
		position = 12,
		keyName = "showMedium",
		name = "Show medium",
		description = "Show medium stashes"
	)
	default boolean showMedium()
	{
		return false;
	}

	@ConfigItem(
		position = 13,
		keyName = "showHard",
		name = "Show hard",
		description = "Show hard stashes"
	)
	default boolean showHard()
	{
		return false;
	}

	@ConfigItem(
		position = 14,
		keyName = "showElite",
		name = "Show elite",
		description = "Show elite stashes"
	)
	default boolean showElite()
	{
		return false;
	}

	@ConfigItem(
		position = 15,
		keyName = "showMaster",
		name = "Show master",
		description = "Show master stashes"
	)
	default boolean showMaster()
	{
		return false;
	}


}
