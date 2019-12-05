package net.runelite.client.plugins.fredexperimental.oneclick2;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("oneclick2")
public interface Oneclick2Config extends Config
{

	@ConfigSection(
		position = 0,
		name = "General Settings",
		description = "General settings",
		keyName = "generalSettings"
	)
	default boolean generalSettings()
	{
		return false;
	}

	@ConfigItem(
		position = 0,
		name = "Placeholder",
		description = "Placeholder description!",
		section = "generalSettings",
		keyName = "placeholder"
	)
	default boolean placeholder()
	{
		return false;
	}

	@ConfigItem(
		position = 1,
		name = "Scripts",
		description = "Lua scripts root",
		section = "generalSettings",
		keyName = "scriptsPath"
	)
	default String scriptsPath()
	{
		return "";
	}

	@ConfigItem(
		position = 2,
		name = "Scripts",
//		description = "Comma separated list of absolute paths to scripts",
		description = "Add custom lua scripts here, 1 per line. Syntax: \'filename | enabled\'",
		section = "generalSettings",
		keyName = "scripts",
		parse = true,
		clazz = ScriptLocationsParse.class,
		method = "parse"
	)
	default String scripts()
	{
		return "";
	}
}
