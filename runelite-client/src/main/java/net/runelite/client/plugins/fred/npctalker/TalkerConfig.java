package net.runelite.client.plugins.fred.npctalker;

import java.awt.Color;
import net.runelite.client.config.Alpha;
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
		position = 10,
		keyName = "logDialogs",
		name = "Record dialog",
		description = "Records dialog text as chat messages."
	)
	default boolean logDialogs()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		position = 11,
		keyName = "playerColor",
		name = "Player color",
		description = "Configure the color of player dialog messages",
		hidden = true,
		unhide = "logDialogs"
	)
	default Color playerColor()
	{
		return new Color(192, 192, 192);
	}

	@Alpha
	@ConfigItem(
		position = 12,
		keyName = "npcColor",
		name = "NPC color",
		description = "Configure the color of npc dialog messages",
		hidden = true,
		unhide = "logDialogs"
	)
	default Color npcColor()
	{
		return new Color(0, 0, 255);
	}

	@Alpha
	@ConfigItem(
		position = 13,
		keyName = "notificationColor",
		name = "Notification color",
		description = "Configure the color of notification dialog messages",
		hidden = true,
		unhide = "logDialogs"
	)
	default Color notificationColor()
	{
		return new Color(200, 128, 64);
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
