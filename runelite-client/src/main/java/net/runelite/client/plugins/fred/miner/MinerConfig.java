package net.runelite.client.plugins.fred.miner;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;

/**
 * Created by npruff on 8/24/2019.
 */
@ConfigGroup("FredMiner")
public interface MinerConfig extends Config
{
	@ConfigItem(
		position = 0,
		keyName = "triggerMine",
		name = "Mine",
		description = "Starts Mining Rock"
	)
	default Keybind triggerMine()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
		position = 1,
		keyName = "triggerTeleport",
		name = "Teleport",
		description = "Triggers teleport action."
	)
	default Keybind triggerTeleport()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
		position = 2,
		keyName = "triggerBank",
		name = "Bank",
		description = "Triggers bank action."
	)
	default Keybind triggerBank()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
		position = 10,
		keyName = "oreItemIDs",
		name = "Ore IDs",
		description = "Comma separated list of item IDs."
	)
	default String oreItemIDs()
	{
		return "";
	}

	@ConfigItem(
		position = 11,
		keyName = "oreRockIDs",
		name = "Rock IDs",
		description = "Comma separated list of rock IDs."
	)
	default String oreRockIDs()
	{
		return "";
	}

	@ConfigItem(
			position = 20,
			keyName = "autoAction",
			name = "Auto action",
			description = "Triggers actions automatically."
	)
	default boolean autoAction()
	{
		return false;
	}

//	@ConfigItem(
//		position = 1,
//		keyName = "triggerFletch",
//		name = "Fletch",
//		description = "Starts fletching Logs"
//	)
//	default Keybind triggerFletch()
//	{
//		return Keybind.NOT_SET;
//	}

//	@ConfigItem(
//		position = 1,
//		keyName = "triggerFletch",
//		name = "Fletch",
//		description = "Starts fletching Logs"
//	)
//	default Keybind triggerFletch()
//	{
//		return Keybind.NOT_SET;
//	}
}
