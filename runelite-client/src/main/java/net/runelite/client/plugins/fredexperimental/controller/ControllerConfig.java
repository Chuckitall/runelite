package net.runelite.client.plugins.fredexperimental.controller;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigTitleSection;
import net.runelite.client.config.Keybind;
import net.runelite.client.config.Title;

@ConfigGroup("fredsController")
public interface ControllerConfig extends Config
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
		keyName = "debugTileAreas",
		name = "Debug Areas",
		description = "Enable area debugging overlay.",
		position = 0,
		titleSection = "debug"
	)
	default boolean debugTileAreas()
	{
		return true;
	}

	@ConfigItem(
		keyName = "debugTilePaths",
		name = "Debug Paths",
		description = "Enable path debugging overlay.",
		position = 1,
		titleSection = "debug"
	)
	default boolean debugTilePaths()
	{
		return true;
	}

	@ConfigItem(
		keyName = "debugGameObjects",
		name = "Debug GameObjects",
		description = "Enable GameObjects debugging overlay.",
		position = 2,
		titleSection = "debug"
	)
	default boolean debugGameObjects()
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
