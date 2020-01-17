package net.runelite.client.plugins.groovy.interfaces;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.events.Event;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.ui.overlay.OverlayManager;

public class GroovyContext
{
	@Getter(AccessLevel.PUBLIC)
	private final String name;

	@Getter(AccessLevel.PUBLIC)
	private final String mainFile;

	@Getter(AccessLevel.PUBLIC)
	private Client client;

	@Getter(AccessLevel.PUBLIC)
	private MenuManager menuManager;

	@Getter(AccessLevel.PUBLIC)
	private OverlayManager overlayManager;

	@Getter(AccessLevel.PUBLIC)
	private EventBus eventBus;

	@Getter(AccessLevel.PUBLIC)
	@Setter(AccessLevel.PUBLIC)
	private int uuid = -1;

	public GroovyContext(String name, String mainFile, Client client, EventBus eventBus, MenuManager menuManager, OverlayManager overlayManager)
	{
		this.name = name;
		this.mainFile = mainFile;

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
