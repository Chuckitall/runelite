package net.runelite.client.plugins.fredexperimental.oakplankmake;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("oakplankmake")
public interface OakPlankMakeConfig extends Config
{
	@ConfigItem(
		position = 0,
		keyName = "delay",
		name = "Delay",
		description = "Configure delay."
	)
	default int delay()
	{
		return 50;
	}
}
