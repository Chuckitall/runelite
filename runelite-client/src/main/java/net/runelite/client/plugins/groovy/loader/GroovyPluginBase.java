package net.runelite.client.plugins.groovy.loader;

import net.runelite.api.Client;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.ui.overlay.OverlayManager;

public abstract class GroovyPluginBase
{
	public Client _client;
	public EventBus _eventBus;
	public MenuManager _menuManager;
	public OverlayManager _overlayManager;
	public GroovyPluginBase(ScriptContext ctx)
	{
		_client = ctx.getClient();
		_eventBus = ctx.getEventBus();
		_menuManager = ctx.getMenuManager();
		_overlayManager = ctx.getOverlayManager();
	}

	public abstract void startup();
	public abstract void shutdown();
}
