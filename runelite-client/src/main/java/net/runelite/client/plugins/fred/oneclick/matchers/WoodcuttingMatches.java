package net.runelite.client.plugins.fred.oneclick.matchers;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.fred.oneclick.OneclickConfig;
import net.runelite.client.plugins.fred.oneclick.matchers.woodcutting.BuyPlanks;
import net.runelite.client.plugins.fred.oneclick.matchers.woodcutting.LightLogs;
import net.runelite.client.plugins.fred.oneclick.util.MenuEntryMatcher;
import net.runelite.client.plugins.fred.oneclick.util.MenuMatchSet;

@Singleton
@Slf4j
public class WoodcuttingMatches extends MenuMatchSet
{

	private boolean lightLogsEnabled = false;
	@Inject private LightLogs lightLogs;

	private boolean makePlankEnabled = false;
	@Inject private BuyPlanks makePlank;

	@Override
	protected List<MenuEntryMatcher> getMatchers()
	{
		List<MenuEntryMatcher> toRet = new ArrayList<>();
		if (lightLogsEnabled) toRet.add(lightLogs);
		if (makePlankEnabled) toRet.add(makePlank);
		return toRet;
	}

	@Override
	public void onConfigChanged(ConfigChanged event)
	{
		log.debug("event -> {}", event);
		if (event.getKey().equals("lightlogs"))
		{
			lightLogsEnabled = Boolean.parseBoolean(event.getNewValue());
		}
		if (event.getKey().equals("makeplank"))
		{
			makePlankEnabled = Boolean.parseBoolean(event.getNewValue());
		}
	}

	@Override
	public void init(OneclickConfig config)
	{
		lightLogsEnabled = config.lightLogs();
		makePlankEnabled = config.makePlank();
		super.init(config);

	}
}
