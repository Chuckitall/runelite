package net.runelite.client.plugins.groovy.script;

import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.fred.ScriptStackTools;
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

	@Getter(AccessLevel.PUBLIC)
	private ClientThread clientThread;

	@Getter(AccessLevel.PUBLIC)
	private ScriptStackTools scriptStackTools;

	public ScriptContext(Client client, EventBus eventBus, MenuManager menuManager, OverlayManager overlayManager, ClientThread clientThread, ScriptStackTools scriptStackTools)
	{
		this.client = client;
		this.eventBus = eventBus;
		this.menuManager = menuManager;
		this.overlayManager = overlayManager;
		this.clientThread = clientThread;
		this.scriptStackTools = scriptStackTools;
	}
}
