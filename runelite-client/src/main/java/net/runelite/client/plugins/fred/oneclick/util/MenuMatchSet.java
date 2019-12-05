package net.runelite.client.plugins.fred.oneclick.util;

import java.util.List;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.client.events.ConfigChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.plugins.fred.api.wrappers._Item;
import net.runelite.client.plugins.fred.oneclick.OneclickConfig;
import net.runelite.client.plugins.fred.oneclick.OneclickPlugin;

@Slf4j
public abstract class MenuMatchSet
{
	@Inject
	protected Client client;
	@Inject
	protected OneclickPlugin plugin;

	protected abstract List<MenuEntryMatcher> getMatchers();
	public abstract void onConfigChanged(ConfigChanged event);

	private int matchedIdx = -1;
	public boolean onMenuAdded(MenuEntryAdded added)
	{
		List<MenuEntryMatcher> matches = getMatchers();
		for (int i = 0; i < matches.size(); i++)
		{
			MenuEntryMatcher matcher = matches.get(i);
			if (matcher.addedMatches(added))
			{
				MenuEntry e = matcher.generate(added);
				if (e != null)
				{
					client.insertMenuItem(e.getOption(), e.getTarget(), e.getOpcode(), e.getIdentifier(), e.getParam0(), e.getParam1(), e.isForceLeftClick());
					matchedIdx = i;
					break;
				}
				else
				{
					log.debug("matcher {} matched {}, but generated null", matcher.getClass().getSimpleName(), added);
				}
			}
		}
		return matchedIdx != -1;
	}

	public boolean onMenuClicked(MenuOptionClicked clicked)
	{
		boolean retVal = false;
		List<MenuEntryMatcher> matches = getMatchers();
		if (matchedIdx == -1 || matches.size() <= matchedIdx)
		{
			//log.debug("matchedIdx: '{}', matches size: '{}' -> is invalid", matchedIdx, matches.size());
		}
		else
		{
			MenuEntryMatcher match = matches.get(matchedIdx);
			if (match.clickedMatches(clicked))
			{
				match.clicked(clicked);
				retVal = true;
			}
		}
		matchedIdx = -1;
		return retVal;
	}

	public void init(OneclickConfig config)
	{
		List<MenuEntryMatcher> matches = getMatchers();
		for (MenuEntryMatcher entryMatcher : matches)
		{
			entryMatcher.init();
		}
	}


	public final void onGameTick()
	{
		matchedIdx = -1;
		List<MenuEntryMatcher> matches = getMatchers();
		for (MenuEntryMatcher entryMatcher : matches)
		{
			entryMatcher.onGameTick();
		}
	}

	public void onInventoryChanged(final List<_Item> items)
	{
		List<MenuEntryMatcher> matches = getMatchers();
		for (MenuEntryMatcher entryMatcher : matches)
		{
			entryMatcher.onInventoryChanged(items);
		}
	}
}
