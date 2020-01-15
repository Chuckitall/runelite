package net.runelite.client.plugins.fred.api.scripting;

import java.util.List;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.client.plugins.fred.api.wrappers._GameObject;
import net.runelite.client.plugins.fred.api.wrappers._Item;

public interface ScriptPlugin
{
	Client getClient();

	List<_GameObject> getGameObjects(List<Integer> ids);
	_GameObject getNearestGameObject(List<Integer> ids);

	List<_Item> getItemContainer(InventoryID container);
	List<_Item> getItemContainer(int container);
}
