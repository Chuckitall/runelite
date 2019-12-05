package net.runelite.client.plugins.fred.npctalker;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

/**
 * Created by npruff on 9/2/2019.
 */
@ConfigGroup(TalkerPlugin.CONFIG_GROUP)
public interface TalkerConfig extends Config
{

	@ConfigItem(
			position = 0,
			keyName = "speedThroughDialog",
			name = "Hit space automatically",
			description = "Clicks through dialogs where the only option is space."
	)
	default boolean speedThroughDialog()
	{
		return false;
	}

	@ConfigItem(
		position = 21,
		keyName = "randLow",
		name = "Minimum Delay",
		description = "Minimum delay for keypress response."
	)
	default int randLow()
	{
		return 60;
	}

	@ConfigItem(
		position = 22,
		keyName = "randHigh",
		name = "Maximum Delay",
		description = "Maximum delay for keypress response."
	)
	default int randHigh()
	{
		return 140;
	}

}
