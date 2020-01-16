package net.runelite.client.plugins.beanshell2.interfaces;

import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.ui.overlay.OverlayManager;

public class BshContext
{
	@Getter(AccessLevel.PUBLIC)
	private final String namespace;

	@Getter(AccessLevel.PUBLIC)
	private Client client;

	@Getter(AccessLevel.PUBLIC)
	private MenuManager menuManager;

	@Getter(AccessLevel.PUBLIC)
	private OverlayManager overlayManager;

	@Getter(AccessLevel.PUBLIC)
	private EventBus eventBus;

	public BshContext(String namespace, Client client, EventBus eventBus, MenuManager menuManager, OverlayManager overlayManager)
	{
		this.namespace = namespace;
		this.client = client;
		this.eventBus = eventBus;
		this.menuManager = menuManager;
		this.overlayManager = overlayManager;
	}
}
