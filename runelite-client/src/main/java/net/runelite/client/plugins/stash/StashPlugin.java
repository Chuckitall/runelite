package net.runelite.client.plugins.stash;

import com.google.inject.Inject;
import com.google.inject.Provides;
import java.util.List;
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
		List<StashUnit> units;
		if(config.showBeginner())
		{
			units = StashUnit.getTier(0);
			log.debug("beginner has {} entries", units.size());
			units.forEach(f -> log.debug("stash: {} -> {}", f.getLocation(), f.check(client)));
		}

		if(config.showEasy())
		{
			units = StashUnit.getTier(1);
			log.debug("easy has {} entries", units.size());
			units.forEach(f -> log.debug("stash: {} -> {}", f.getLocation(), f.check(client)));
		}

		if(config.showMedium())
		{
			units = StashUnit.getTier(2);
			log.debug("medium has {} entries", units.size());
			units.forEach(f -> log.debug("stash: {} -> {}", f.getLocation(), f.check(client)));
		}

		if(config.showHard())
		{
			units = StashUnit.getTier(3);
			log.debug("hard has {} entries", units.size());
			units.forEach(f -> log.debug("stash: {} -> {}", f.getLocation(), f.check(client)));
		}

		if(config.showElite())
		{
			units = StashUnit.getTier(4);
			log.debug("elite has {} entries", units.size());
			units.forEach(f -> log.debug("stash: {} -> {}", f.getLocation(), f.check(client)));
		}

		if(config.showMaster())
		{
			units = StashUnit.getTier(5);
			log.debug("master has {} entries", units.size());
			units.forEach(f -> log.debug("stash: {} -> {}", f.getLocation(), f.check(client)));
		}
//		overlayManager.add(stashOverlay);
		//
	}

	@Override
	protected void shutDown()
	{
		eventBus.unregister(this);
	}
}
