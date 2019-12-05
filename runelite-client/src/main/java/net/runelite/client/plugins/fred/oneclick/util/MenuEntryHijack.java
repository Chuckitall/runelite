package net.runelite.client.plugins.fred.oneclick.util;

import com.google.inject.Inject;

import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.eventbus.EventBus;

public abstract class MenuEntryHijack
{
	@Getter
	private boolean enabled = false;

	@Inject
	protected EventBus eventBus;

	@Inject
	protected Client client;

	final public void setEnabled(boolean enable)
	{
		if (enabled == enable)
		{
			return;
		}
		if (enable)
		{
			enabled = true;
			onEnabled();
			eventBus.subscribe(MenuEntryAdded.class, this, this::onMenuEntryAdded);
			eventBus.subscribe(MenuOptionClicked.class, this, this::onMenuOptionClicked);
		}
		else
		{
			enabled = false;
			onDisabled();
			eventBus.unregister(this);
		}
	}

	protected abstract void onEnabled();
	protected abstract void onDisabled();

	//called when menu is clicked added
	protected abstract void onMenuEntryAdded(MenuEntryAdded event);

	//called when menu is clicked
	protected abstract void onMenuOptionClicked(MenuOptionClicked event);
}
