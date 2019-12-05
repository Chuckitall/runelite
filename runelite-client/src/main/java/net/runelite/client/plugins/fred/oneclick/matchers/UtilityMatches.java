package net.runelite.client.plugins.fred.oneclick.matchers;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.fred.oneclick.OneclickConfig;
import net.runelite.client.plugins.fred.oneclick.matchers.utility.CharterArea;
import net.runelite.client.plugins.fred.oneclick.matchers.utility.CharterItem;
import net.runelite.client.plugins.fred.oneclick.matchers.utility.TradeCharter;
import net.runelite.client.plugins.fred.oneclick.util.MenuEntryMatcher;
import net.runelite.client.plugins.fred.oneclick.util.MenuMatchSet;

@Singleton
@Slf4j
public class UtilityMatches extends MenuMatchSet
{
	private boolean tradeCharterEnabled = false;
	@Inject private TradeCharter tradeCharter;

	@Override
	protected List<MenuEntryMatcher> getMatchers()
	{
		List<MenuEntryMatcher> toRet = new ArrayList<>();
		if (tradeCharterEnabled) toRet.add(tradeCharter);
		return toRet;
	}

	@Override
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getKey().equals("charterCrewEnabled"))
		{
			tradeCharterEnabled = Boolean.parseBoolean(event.getNewValue());
		}
		if (event.getKey().equals("charterCrewSodaAsh"))
		{
			tradeCharter.setTargetItem(CharterItem.SODA_ASH, Boolean.parseBoolean(event.getNewValue()));
		}
		if (event.getKey().equals("charterCrewBucketOfSand"))
		{
			tradeCharter.setTargetItem(CharterItem.BUCKET_OF_SAND, Boolean.parseBoolean(event.getNewValue()));
		}
		if (event.getKey().equals("charterCrewSeaweed"))
		{
			tradeCharter.setTargetItem(CharterItem.SEAWEED, Boolean.parseBoolean(event.getNewValue()));
		}
		if (event.getKey().equals("charterCrewBucketOfSlime"))
		{
			tradeCharter.setTargetItem(CharterItem.BUCKET_OF_SLIME, Boolean.parseBoolean(event.getNewValue()));
		}
		if (event.getKey().equals("charterCrewPineapple"))
		{
			tradeCharter.setTargetItem(CharterItem.PINEAPPLE, Boolean.parseBoolean(event.getNewValue()));
		}
		if (event.getKey().equals("charterCrewArea"))
		{
			tradeCharter.setTargetArea(CharterArea.valueOf(event.getNewValue()));
		}
	}

	@Override
	public void init(OneclickConfig config)
	{
		tradeCharterEnabled = config.charterCrewEnabled();
		tradeCharter.setTargetArea(config.charterCrewArea());
		tradeCharter.setTargetItem(CharterItem.PINEAPPLE, config.charterCrewPineapple());
		tradeCharter.setTargetItem(CharterItem.BUCKET_OF_SLIME, config.charterCrewBucketOfSlime());
		tradeCharter.setTargetItem(CharterItem.BUCKET_OF_SAND, config.charterCrewBucketOfSand());
		tradeCharter.setTargetItem(CharterItem.SEAWEED, config.charterCrewSeaweed());
		tradeCharter.setTargetItem(CharterItem.SODA_ASH, config.charterCrewSodaAsh());
		super.init(config);

	}
}
