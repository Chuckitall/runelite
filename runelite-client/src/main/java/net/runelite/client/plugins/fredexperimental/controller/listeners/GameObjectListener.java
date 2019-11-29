package net.runelite.client.plugins.fredexperimental.controller.listeners;

import net.runelite.api.events.GameObjectChanged;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;

public interface GameObjectListener
{
	void onGameObjectSpawned(GameObjectSpawned event);
	void onGameObjectDespawned(GameObjectDespawned event);
	void onGameObjectChanged(GameObjectChanged event);
}
