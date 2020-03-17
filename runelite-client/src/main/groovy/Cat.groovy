import groovy.transform.InheritConstructors
import net.runelite.api.ScriptID
import net.runelite.client.plugins.groovy.debugger.DebuggerWindow.LogLevel
import net.runelite.client.plugins.groovy.script.ScriptedPlugin
import net.runelite.client.plugins.stash.StashUnit
import org.apache.commons.lang3.ArrayUtils

@InheritConstructors
class Cat extends ScriptedPlugin
{
	void startup()
	{

		log(LogLevel.DEBUG, "stackSize: ${_client.getIntStackSize()}, ${ArrayUtils.subarray(_client.getIntStack(), 0, _client.getIntStackSize()+5)}")
		_client.runScript(ScriptID.WATSON_STASH_UNIT_CHECK, StashUnit.MUBARIZS_ROOM_AT_THE_DUEL_ARENA.getObjectId(), 0, 0, 0);
		int[] intStack = _client.getIntStack();
		boolean stashUnitBuilt = intStack[0] == 1;
		log(LogLevel.TRACE, "stashUnitBuilt: ${stashUnitBuilt}, stackSize: ${_client.getIntStackSize()}, stack: ${ArrayUtils.subarray(_client.getIntStack(), 0, _client.getIntStackSize())}")
	}

	void shutdown() {
//		_eventBus.unregister(this);
//		_eventBus.unregister(lifecycle);
//		log(LogLevel.DEBUG,"Shutting Down");
	}
}
