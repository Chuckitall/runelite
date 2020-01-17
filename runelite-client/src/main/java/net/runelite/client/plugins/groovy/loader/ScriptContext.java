package net.runelite.client.plugins.groovy.loader;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.ui.overlay.OverlayManager;

@Value
@Slf4j
public class ScriptContext
{
	Client client;
	MenuManager menuManager;
	OverlayManager overlayManager;
	EventBus eventBus;
}
