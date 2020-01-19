import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import io.reactivex.functions.Consumer
import net.runelite.api.events.ScriptRunEvent
import net.runelite.client.plugins.groovy.data.ScriptData
import net.runelite.client.plugins.groovy.debugger.DebuggerWindow.LogLevel
import net.runelite.client.plugins.groovy.script.ScriptedPlugin

@CompileStatic
@InheritConstructors
class Alch extends ScriptedPlugin {
	//key is script number/argn, value is arg[]
	List<Integer> ignored = List.of(1004, 3174, 39, 446, 82, 2069, 1699, 2164, 447, 1005, 664, 2622, 905, 73, 839, 3277);

	void onScriptRunEvent(ScriptRunEvent e) {
		if (e.getArgs() != null && e.getArgs().length > 0) {
			if (!ignored.contains(e.args[0])) {
				ScriptData.ScriptEntry entry = ScriptData.lookup(e.args[0] as Integer);
				if(entry.id == 914 || entry.id == 915)
				{

				}
				else
				{
					log(entry.category.equalsIgnoreCase("clientscript") ? LogLevel.WARN : (entry.category.equalsIgnoreCase("proc") ? LogLevel.DEBUG : LogLevel.INFO), "${entry.id} | ${entry.category} -> ${entry.name}: ${e.args.drop(1).toArrayString()}");
				}

			}
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
