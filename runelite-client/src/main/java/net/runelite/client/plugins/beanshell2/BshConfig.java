package net.runelite.client.plugins.beanshell2;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("bsh")
public interface BshConfig extends Config
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
		name = "Bsh Root",
		description = "Beanshell scripts root",
		section = "generalSettings",
		keyName = "bshRoot"
	)
	default String bshRoot()
	{
		return "";
	}

	@ConfigItem(
		position = 1,
		name = "Bsh Scripts",
//		description = "Comma separated list of absolute paths to scripts",
		description = "Add custom bsh plugins here, 1 per line. Syntax: \'filename.bsh | [true/false]\'",
		section = "generalSettings",
		keyName = "bshScripts",
		parse = true,
		clazz = BshLocationsParse.class,
		method = "parse"
	)
	default String bshScripts()
	{
		return "";
	}
}
