package net.runelite.client.plugins.stash;

import com.google.inject.Inject;
import com.google.inject.Provides;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.ui.overlay.OverlayManager;
/**
 * Created by npruff on 9/2/2019.
 */

@PluginDescriptor(
	name = "Stash",
	description = "Tracks which stash units you have build, and which have been filled.",
	tags = {"uim", "fred", "stash"},
	type = PluginType.FRED
)
@Slf4j
public class StashPlugin extends Plugin
{
	static final String CONFIG_GROUP = "Stash";

	@Getter(AccessLevel.PACKAGE)
	@Inject
	private Client client;

	@Getter(AccessLevel.PUBLIC)
	@Inject
	private StashConfig config;

	@Getter(AccessLevel.PACKAGE)
	@Inject
	private OverlayManager overlayManager;

	@Getter(AccessLevel.PACKAGE)
	@Inject
	private EventBus eventBus;

	@Inject
	@Getter(AccessLevel.PUBLIC)
	private ConfigManager configManager;

	@Provides
	StashConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(StashConfig.class);
	}

	@Override
	protected void startUp()
	{
//		overlayManager.add(stashOverlay);
		//
	}

	@Override
	protected void shutDown()
	{
		eventBus.unregister(this);
	}
}
