package net.runelite.client.plugins.fred.wintertodt;


import java.awt.*;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;
import net.runelite.client.config.Range;

@ConfigGroup("FredWinter")
public interface FredWinterConfig extends Config
{

	@ConfigItem(
			position = 0,
			keyName = "autoTrigger",
			name = "Automatic Triggers",
			description = "Triggers specific actions automatically"
	)
	default boolean autoTrigger()
	{
		return false;
	}

	@ConfigItem(
			position = 1,
			keyName = "triggerFletch",
			name = "Fletch",
			description = "Starts fletching Logs"
	)
	default Keybind triggerFletch()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
			position = 2,
			keyName = "triggerEat",
			name = "Eat",
			description = "Eats food"
	)
	default Keybind triggerEat()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
			position = 3,
			keyName = "triggerFeed",
			name = "Feed",
			description = "Starts feeding brazier"
	)
	default Keybind triggerFeed()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
			position = 4,
			keyName = "triggerFix",
			name = "Fix",
			description = "Starts fixing brazier"
	)
	default Keybind triggerFix()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
			position = 5,
			keyName = "triggerLight",
			name = "Light",
			description = "Starts lighting brazier"
	)
	default Keybind triggerLight()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
			position = 6,
			keyName = "triggerChop",
			name = "Chop",
			description = "Starts chopping root"
	)
	default Keybind triggerChop()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
			position = 10,
			keyName = "damageNotificationColor",
			name = "Damage Notification Color",
			description = "Color of damage notification text in chat"
	)
	default Color damageNotificationColor()
	{
		return Color.CYAN;
	}

	@ConfigItem(
			position = 11,
			keyName = "roundNotification",
			name = "Wintertodt round notification",
			description = "Notifies you before the round starts (in seconds)"
	)
	@Range(
			max = 60
	)
	default int roundNotification()
	{
		return 5;
	}
}
