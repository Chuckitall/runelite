package net.runelite.client.plugins.fredexperimental.talker2;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.fred.npctalker.TalkerPlugin;
/**
 * Created by npruff on 9/2/2019.
 */

@PluginDescriptor(
	name = "Fred Talker 2",
	description = "Talker/Interactor bot",
	tags = {"flexo", "utility", "fred"},
	type = PluginType.FRED
)
@Slf4j
public class TalkerCore extends Plugin
{
	public static final String TALKER_GROUP = "talker";
	@ConfigGroup(TalkerCore.TALKER_GROUP)
	public interface TalkerConfig extends Config
	{

		@ConfigItem(
			position = 0,
			keyName = "autoSpace",
			name = "Hit space automatically",
			description = "Clicks through dialogs where the only option is space."
		)
		default boolean autoSpace()
		{
			return false;
		}
	}
}
