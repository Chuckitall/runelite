package net.runelite.client.plugins.groovy.loader;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
public abstract class BaseScript
{
	public Client game;
	public EventBus eventBus;
	public MenuManager menuManager;
	public OverlayManager overlayManager;

	public BaseScript(ScriptContext ctx)
	{
		this.game = ctx.getClient();
		this.eventBus = ctx.getEventBus();
		this.menuManager = ctx.getMenuManager();
		this.overlayManager = ctx.getOverlayManager();
	}

	public abstract void startup();
	public abstract void shutdown();
}
