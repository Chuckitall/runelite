package net.runelite.client.plugins.fred.oneclick.matchers;

import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.fred.oneclick.OneclickConfig;
import net.runelite.client.plugins.fred.oneclick.matchers.crafting.FillBucketSand;
import net.runelite.client.plugins.fred.oneclick.util.MenuEntryMatcher;
import net.runelite.client.plugins.fred.oneclick.util.MenuMatchSet;

@Singleton
@Slf4j
public class CraftingMatches extends MenuMatchSet
{
	private boolean fillBucketSandEnabled = false;
	@Inject
	private FillBucketSand fillBucketSand;

	@Override
	protected List<MenuEntryMatcher> getMatchers()
	{
		List<MenuEntryMatcher> toRet = new ArrayList<>();
		if (fillBucketSandEnabled) toRet.add(fillBucketSand);
		return toRet;
	}

	@Override
	public void onConfigChanged(ConfigChanged event)
	{
		log.debug("event -> {}", event);
		if (event.getKey().equals("fillBucketSand"))
		{
			fillBucketSandEnabled = Boolean.parseBoolean(event.getNewValue());
		}
	}

	@Override
	public void init(OneclickConfig config)
	{
		fillBucketSandEnabled = config.fillBucketSand();
		super.init(config);

	}
}
