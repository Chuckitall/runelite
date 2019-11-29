package net.runelite.client.plugins.fred.oneclick.matchers;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.fred.oneclick.OneclickConfig;
import net.runelite.client.plugins.fred.oneclick.matchers.construction.UnnotePhiles;
import net.runelite.client.plugins.fred.oneclick.util.MenuEntryMatcher;
import net.runelite.client.plugins.fred.oneclick.util.MenuMatchSet;

@Singleton
@Slf4j
public class ConstructionMatches extends MenuMatchSet
{

	private boolean unnotePhilesEnabled = false;
	@Inject private UnnotePhiles unnotePhiles;

	@Override
	protected List<MenuEntryMatcher> getMatchers()
	{
		List<MenuEntryMatcher> toRet = new ArrayList<>();
		if (unnotePhilesEnabled) toRet.add(unnotePhiles);
//		if (makePlankEnabled) toRet.add(makePlank);
		return toRet;
	}

	@Override
	public void onConfigChanged(ConfigChanged event)
	{
		log.debug("event -> {}", event);
		if (event.getKey().equals("philesUnnote"))
		{
			unnotePhilesEnabled = Boolean.parseBoolean(event.getNewValue());
		}
//		if (event.getKey().equals("makeplank"))
//		{
//			makePlankEnabled = Boolean.parseBoolean(event.getNewValue());
//		}
	}

	@Override
	public void init(OneclickConfig config)
	{
		unnotePhilesEnabled = config.philesUnnote();
//		makePlankEnabled = config.makePlank();
		super.init(config);
	}
}
