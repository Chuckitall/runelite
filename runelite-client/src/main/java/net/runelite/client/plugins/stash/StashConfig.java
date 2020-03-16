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
	}
