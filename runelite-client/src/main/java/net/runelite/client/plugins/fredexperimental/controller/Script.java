package net.runelite.client.plugins.fredexperimental.controller;

import java.util.Optional;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.GameObject;
import net.runelite.client.plugins.fred.api.other.Pair;
import net.runelite.client.plugins.fred.api.wrappers._Area;
import net.runelite.client.plugins.fred.api.wrappers._TilePath;
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity;

public abstract class Script
{

	@Getter(AccessLevel.PUBLIC)
	private boolean enabled = false;

	public void init(ControllerPlugin context)
	{
		enabled = true;
	}
	public void cleanup()
	{
		enabled = false;
	}
	public abstract Optional<Pair<String, Runnable>> getNextAction(ControllerPlugin ctx);

	public abstract LayoutableRenderableEntity getWindowInfo();

	public abstract Set<_Area> debugGetAreas(ControllerPlugin ctx);
	public abstract Set<_TilePath> debugGetTilePaths(ControllerPlugin ctx);
	public abstract Set<GameObject> debugGetGameObject(ControllerPlugin ctx);
}
