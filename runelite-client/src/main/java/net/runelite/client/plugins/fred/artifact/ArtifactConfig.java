package net.runelite.client.plugins.fred.artifact;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("artifact")
public interface ArtifactConfig extends Config
{
	@ConfigItem(
		position = 10,
		keyName = "markHouse",
		name = "Mark Target",
		description = "Marks the target house and dresser."
	)
	default boolean markHouse()
	{
		return true;
	}
}
