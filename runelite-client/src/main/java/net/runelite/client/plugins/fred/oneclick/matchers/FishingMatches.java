package net.runelite.client.plugins.fred.oneclick.matchers;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.fred.oneclick.OneclickConfig;
import net.runelite.client.plugins.fred.oneclick.matchers.fishing.FishNearest;
import net.runelite.client.plugins.fred.oneclick.util.MenuEntryMatcher;
import net.runelite.client.plugins.fred.oneclick.util.MenuMatchSet;

@Singleton
@Slf4j
public class FishingMatches extends MenuMatchSet
{

	private boolean fishNearestEnabled = false;
	@Inject
	private FishNearest fishNearest;

	@Override
	protected List<MenuEntryMatcher> getMatchers()
	{
		List<MenuEntryMatcher> toRet = new ArrayList<>();
		if (fishNearestEnabled) toRet.add(fishNearest);
		return toRet;
	}

	@Override
	public void onConfigChanged(ConfigChanged event)
	{
		log.debug("event -> {}", event);
		if (event.getKey().equals("fishNearest"))
		{
			fishNearestEnabled = Boolean.parseBoolean(event.getNewValue());
		}
	}

	@Override
	public void init(OneclickConfig config)
	{
		fishNearestEnabled = config.fishNearest();
		super.init(config);
	}
}
