import groovy.transform.CompileStatic;
import groovy.transform.InheritConstructors
import io.reactivex.functions.Consumer
import net.runelite.api.VarClientInt
import net.runelite.api.VarClientStr
import net.runelite.api.events.GameTick
import net.runelite.api.events.RunScriptEvent
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.groovy.debugger.DebuggerWindow.LogLevel
import net.runelite.client.plugins.groovy.script.ScriptedPlugin

@CompileStatic
@InheritConstructors
class Demo extends ScriptedPlugin {
	void onScriptRunEvent(RunScriptEvent e)
	{
		log(LogLevel.INFO, "Script called with id: " + e.scriptId + " and arguments " + e.arguments.toString());
		//log(LogLevel.INFO, e.getTarget().toLowerCase().contains("battlestaff"));
	}

	void startup() {
		_eventBus.subscribe(RunScriptEvent.class, this, this.&onScriptRunEvent as Consumer<RunScriptEvent>);
		log(LogLevel.DEBUG, "Starting Up");
	}

	void shutdown() {
		_eventBus.unregister(this);
		log(LogLevel.DEBUG,"Shutting Down");
	}
}
