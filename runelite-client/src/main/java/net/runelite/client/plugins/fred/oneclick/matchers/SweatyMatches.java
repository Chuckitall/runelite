package net.runelite.client.plugins.fred.oneclick.matchers;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.fred.oneclick.OneclickConfig;
import net.runelite.client.plugins.fred.oneclick.matchers.sweaty.Logs3T;
import net.runelite.client.plugins.fred.oneclick.util.MenuEntryMatcher;
import net.runelite.client.plugins.fred.oneclick.util.MenuMatchSet;

@Singleton
@Slf4j
public class SweatyMatches extends MenuMatchSet
{

	private boolean logs3TEnabled = false;
	@Inject
	private Logs3T logs3T;

	@Override
	protected List<MenuEntryMatcher> getMatchers()
	{
		List<MenuEntryMatcher> toRet = new ArrayList<>();
		if (logs3TEnabled) toRet.add(logs3T);
		return toRet;
	}

	@Override
	public void onConfigChanged(ConfigChanged event)
	{
		log.debug("event -> {}", event);
		if (event.getKey().equals("logs3T"))
		{
			logs3TEnabled = Boolean.parseBoolean(event.getNewValue());
		}
	}

	@Override
	public void init(OneclickConfig config)
	{
		logs3TEnabled = config.logs3T();
		super.init(config);
	}
}
