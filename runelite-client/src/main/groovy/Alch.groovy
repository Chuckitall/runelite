import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import io.reactivex.functions.Consumer
import net.runelite.api.events.ScriptRunEvent
import net.runelite.client.plugins.groovy.data.ScriptData
import net.runelite.client.plugins.groovy.data.ScriptData.ScriptEntry
import net.runelite.client.plugins.groovy.debugger.DebuggerWindow.LogLevel
import net.runelite.client.plugins.groovy.script.ScriptedPlugin

@CompileStatic
@InheritConstructors
class Alch extends ScriptedPlugin {
	//key is script number/argn, value is arg[]
	void onScriptRunEvent(ScriptRunEvent e) {
		if (e.getArgs() != null && e.getArgs().length > 0)
		{
			ScriptEntry entry = ScriptData.lookup(e.args[0] as Integer);
			log(entry.category.equals("clientscript") ? LogLevel.WARN : (entry.category.equals("proc") ? LogLevel.DEBUG : LogLevel.INFO), "${entry.name}: ${e.args.drop(1).toArrayString()}");
		}
	}

	void startup() {
		_eventBus.subscribe(ScriptRunEvent.class, this, this.&onScriptRunEvent as Consumer<ScriptRunEvent>);
		log(LogLevel.DEBUG, "Starting Up");
	}

	void shutdown() {
		_eventBus.unregister(this);
		log(LogLevel.DEBUG,"Shutting Down");
	}
}
