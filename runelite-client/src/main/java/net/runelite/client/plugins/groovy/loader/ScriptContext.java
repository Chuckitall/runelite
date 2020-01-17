package net.runelite.client.plugins.groovy.loader;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.events.Event;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.ui.overlay.OverlayManager;

public class ScriptContext
{
	@Getter(AccessLevel.PUBLIC)
	private Client client;

	@Getter(AccessLevel.PUBLIC)
	private MenuManager menuManager;

	@Getter(AccessLevel.PUBLIC)
	private OverlayManager overlayManager;

	@Getter(AccessLevel.PUBLIC)
	private EventBus eventBus;

	public ScriptContext(Client client, EventBus eventBus, MenuManager menuManager, OverlayManager overlayManager)
	{
		this.client = client;
		this.eventBus = eventBus;
		this.menuManager = menuManager;
		this.overlayManager = overlayManager;
	}

	public <T extends Event> void subscribe(Class<T> eventClass, @NonNull Consumer<T> action)
	{
		eventBus.subscribe(eventClass, this, action);
	}

	public void unregister()
	{
		eventBus.unregister(this);
	}
}
