package net.runelite.client.plugins.fredexperimental.buychartershop;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("chartertrader")
public interface CharterTraderConfig extends Config
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
