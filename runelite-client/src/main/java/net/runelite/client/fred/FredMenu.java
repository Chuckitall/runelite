package net.runelite.client.fred;

import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.ScriptCallbackEvent;
import net.runelite.client.eventbus.EventBus;

@Singleton
@Slf4j
public class FredMenu
{
	private final Client client;
	private final EventBus eventBus;

	//Used to manage custom non-player menu options

	@Inject
	private FredMenu(Client client, EventBus eventBus)
	{
		this.client = client;
		this.eventBus = eventBus;
	}

	private void onMenuEntryAdded(MenuEntryAdded event)
	{

	}


	private void onMenuEntryClicked(MenuEntryAdded event)
	{

	}
}
