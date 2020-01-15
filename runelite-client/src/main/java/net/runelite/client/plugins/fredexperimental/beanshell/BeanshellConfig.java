package net.runelite.client.plugins.fredexperimental.beanshell;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.plugins.fredexperimental.oneclick2.ScriptLocationsParse;


@ConfigGroup("beanshell")
public interface BeanshellConfig extends Config
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
		name = "Scripts Root",
		description = "Beanshell scripts root",
		section = "generalSettings",
		keyName = "beanshellPath"
	)
	default String beanshellPath()
	{
		return "";
	}

	@ConfigItem(
		position = 2,
		name = "Scripts",
//		description = "Comma separated list of absolute paths to scripts",
		description = "Add custom lua scripts here, 1 per line. Syntax: \'filename | enabled\'",
		section = "generalSettings",
		keyName = "beanshells",
		parse = true,
		clazz = BeanshellLocationsParse.class,
		method = "parse"
	)
	default String beanshells()
	{
		return "";
	}
}
