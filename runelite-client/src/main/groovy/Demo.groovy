import groovy.transform.CompileStatic;
import groovy.transform.InheritConstructors
import io.reactivex.functions.Consumer
import net.runelite.api.events.RunScriptEvent
import net.runelite.client.plugins.groovy.debugger.DebuggerWindow.LogLevel
import net.runelite.client.plugins.groovy.script.ScriptedPlugin
import org.apache.commons.lang3.builder.ReflectionToStringBuilder
import org.apache.commons.lang3.builder.ToStringStyle

@CompileStatic
@InheritConstructors
class Demo extends ScriptedPlugin {
	int[] ignored = [
			1004, 3174, 39, 2053, 839, 3277
	];
	void onScriptRunEvent(RunScriptEvent e)
	{
		if (!ignored.contains(e.getScriptId()))
		{
			log((e.getScriptId() == -1) ? LogLevel.ERROR : LogLevel.INFO, "Script called with id: " + e.scriptId + " and arguments " + e.arguments.toString());
			if(e.getScriptId() == -1)
			{
				log(LogLevel.ERROR, (ReflectionToStringBuilder.toString(e.getArguments()[0], ToStringStyle.SHORT_PREFIX_STYLE)));
			}
		}
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
