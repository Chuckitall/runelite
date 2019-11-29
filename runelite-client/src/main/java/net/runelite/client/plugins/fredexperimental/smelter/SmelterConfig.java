package net.runelite.client.plugins.fredexperimental.smelter;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigTitleSection;
import net.runelite.client.config.Keybind;
import net.runelite.client.config.Title;

@ConfigGroup("fredsSmelter")
public interface SmelterConfig extends Config
{
	@ConfigTitleSection(
		keyName = "runtime",
		name = "Runtime Settings",
		description = "Runtime settings",
		position = 0
	)
	default Title runtime()
	{
		return new Title();
	}

	@ConfigItem(
		keyName = "hotkey",
		name = "Hotkey",
		description = "Hotkey to use to toggle controller enable/disabled.",
		position = 0,
		titleSection = "runtime"
	)
	default Keybind getHotkey()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
		keyName = "location",
		name = "Location",
		description = "Location to smelt at.",
		position = 1,
		enumClass = SmelterLocation.class,
		titleSection = "runtime"
	)
	default SmelterLocation getLocation()
	{
		return SmelterLocation.PORT_PHASMATYS;
	}

	@ConfigItem(
		keyName = "producing",
		name = "Producing",
		description = "What are we making?",
		position = 2,
		enumClass = SmelterItem.class,
		titleSection = "runtime"
	)
	default SmelterItem getProducing()
	{
		return SmelterItem.GOLD_BAR;
	}

	@ConfigTitleSection(
		keyName = "debug",
		name = "Debug",
		description = "Debug settings.",
		position = 10
	)
	default Title debug()
	{
		return new Title();
	}

	@ConfigItem(
		keyName = "debugArea",
		name = "Debug Area",
		description = "Enable area debugging overlay.",
		position = 0,
		titleSection = "debug"
	)
	default boolean debugArea()
	{
		return true;
	}

	@ConfigItem(
		keyName = "debugPath",
		name = "Debug Path",
		description = "Enable path debugging overlay.",
		position = 1,
		titleSection = "debug"
	)
	default boolean debugPath()
	{
		return true;
	}

	@ConfigItem(
		keyName = "debugFurnace",
		name = "Debug Furnace",
		description = "Enable furnace debugging overlay.",
		position = 2,
		titleSection = "debug"
	)
	default boolean debugFurnace()
	{
		return true;
	}

	@ConfigItem(
		keyName = "debugBank",
		name = "Debug Bank",
		description = "Enable bank debugging overlay.",
		position = 3,
		titleSection = "debug"
	)
	default boolean debugBank()
	{
		return true;
	}

	@ConfigItem(
		keyName = "debugInterfaces",
		name = "Debug Interfaces",
		description = "Enable interface debugging overlays.",
		position = 4,
		titleSection = "debug"
	)
	default boolean debugInterfaces()
	{
		return true;
	}
}
