import groovy.transform.InheritConstructors
import io.reactivex.rxjava3.functions.Consumer
import net.runelite.api.InventoryID
import net.runelite.api.ScriptID
import net.runelite.api.events.Event
import net.runelite.api.events.ItemContainerChanged
import net.runelite.api.events.MenuOptionClicked
import net.runelite.client.plugins.groovy.debugger.DebuggerWindow.LogLevel
import net.runelite.client.plugins.groovy.script.ScriptedPlugin
import net.runelite.client.plugins.stash.STASHUnit
import org.apache.commons.lang3.ArrayUtils

@InheritConstructors
class Cat extends ScriptedPlugin
{
	void onInventoryContainerChanged(ItemContainerChanged e)
	{
		if (InventoryID.values().any {f -> e.getContainerId() == f.getId()})
		{
			return;
		}
		log(LogLevel.WARN, "ContainerID ${e.getContainerId()} has ${e.getItemContainer().getItems().length} items in it.");
		e.getItemContainer().getItems().collect {i -> "${i.getId()}x${i.getQuantity()}"}.withIndex().collect(i -> "items[${i.v2}] = ${i.v1}").forEach(i -> log(LogLevel.TRACE, i));
	}

	void startup()
	{
//		_clientThread.invokeLater(() -> {
//			log(LogLevel.DEBUG, "stackSize: ${_client.getIntStackSize()}, ${ArrayUtils.subarray(_client.getIntStack(), 0, _client.getIntStackSize()+5)}")
//			_client.runScript(ScriptID.WATSON_STASH_UNIT_CHECK, STASHUnit.MUBARIZS_ROOM_AT_THE_DUEL_ARENA.getObjectId(), 0, 0, 0)
//			int[] intStack = _client.getIntStack();
//			boolean stashUnitBuilt = intStack[0] == 1;
//			log(LogLevel.TRACE, "stashUnitBuilt: ${stashUnitBuilt}, stackSize: ${_client.getIntStackSize()}, stack: ${ArrayUtils.subarray(_client.getIntStack(), 0, _client.getIntStackSize())}")
//		});
		_eventBus.subscribe(ItemContainerChanged.class, this, this::onInventoryContainerChanged as Consumer<ItemContainerChanged>);

	}

	void shutdown() {
		_eventBus.unregister(this);
//		_eventBus.unregister(lifecycle);
//		log(LogLevel.DEBUG,"Shutting Down");
	}
}
