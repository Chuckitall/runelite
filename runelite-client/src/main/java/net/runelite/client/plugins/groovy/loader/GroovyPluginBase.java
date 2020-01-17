package net.runelite.client.plugins.groovy.loader;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
public abstract class GroovyPluginBase
{
	public Client client;
	public EventBus eventBus;
	public MenuManager menuManager;
	public OverlayManager overlayManager;
	public GroovyPluginBase(ScriptContext ctx)
	{
		client = ctx.getClient();
		eventBus = ctx.getEventBus();
		menuManager = ctx.getMenuManager();
		overlayManager = ctx.getOverlayManager();
	}

	public abstract void startup();
	public abstract void shutdown();
}
