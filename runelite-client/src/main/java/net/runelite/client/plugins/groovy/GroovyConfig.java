package net.runelite.client.plugins.groovy;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("groovy")
public interface GroovyConfig extends Config
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
		name = "Groovy Root",
		description = "Groovy scripts root",
		section = "generalSettings",
		keyName = "groovyRoot"
	)
	default String groovyRoot()
	{
		return "";
	}

	@ConfigItem(
		position = 1,
		name = "Groovy Scripts",
//		description = "Comma separated list of absolute paths to scripts",
		description = "Add custom groovy plugins here, 1 per line. Syntax: \'[filename] | [true/false]\'",
		section = "generalSettings",
		keyName = "groovyScripts",
		parse = true,
		clazz = GroovyScriptsParse.class,
		method = "parse"
	)
	default String groovyScripts()
	{
		return "";
	}
}
