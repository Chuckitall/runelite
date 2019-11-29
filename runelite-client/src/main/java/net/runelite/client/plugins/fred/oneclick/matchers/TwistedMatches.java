package net.runelite.client.plugins.fred.oneclick.matchers;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.fred.oneclick.OneclickConfig;
import net.runelite.client.plugins.fred.oneclick.matchers.twisted.HarvesterDropper;
import net.runelite.client.plugins.fred.oneclick.util.MenuEntryMatcher;
import net.runelite.client.plugins.fred.oneclick.util.MenuMatchSet;

@Singleton
@Slf4j
public class TwistedMatches extends MenuMatchSet
{

	private boolean harvesterDropEnabled = false;
	@Inject
	private HarvesterDropper harvesterDropper;

	@Override
	protected List<MenuEntryMatcher> getMatchers()
	{
		List<MenuEntryMatcher> toRet = new ArrayList<>();
		if (harvesterDropEnabled) toRet.add(harvesterDropper);
		return toRet;
	}

	@Override
	public void onConfigChanged(ConfigChanged event)
	{
		log.debug("event -> {}", event);
		if (event.getKey().equals("harvesterDrop"))
		{
			harvesterDropEnabled = Boolean.parseBoolean(event.getNewValue());
		}
	}

	@Override
	public void init(OneclickConfig config)
	{
		harvesterDropEnabled = config.harvesterDrop();
		super.init(config);
	}
}
