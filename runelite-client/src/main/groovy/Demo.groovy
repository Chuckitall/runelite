import groovy.transform.CompileStatic;
import groovy.transform.InheritConstructors
import io.reactivex.functions.Consumer
import net.runelite.api.events.GameTick
import net.runelite.api.events.RunScriptEvent
import net.runelite.api.events.ScriptCallbackEvent
import net.runelite.api.widgets.WidgetInfo
import net.runelite.client.plugins.groovy.debugger.DebuggerWindow.LogLevel
import net.runelite.client.plugins.groovy.script.ScriptedPlugin
import net.runelite.client.util.ColorUtil
import org.apache.commons.lang3.builder.ReflectionToStringBuilder
import org.apache.commons.lang3.builder.ToStringStyle

@CompileStatic
@InheritConstructors
class Demo extends ScriptedPlugin {
	int[] ignored = [
			1004, 3174, 39//, 2053, 839, 3277
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

	int min (int a, int b)
	{
		if(a > b) {
			return b;
		}
		return a;
	}

	int max (int a, int b)
	{
		if(a > b) {
			return a;
		}
		return b;
	}

	void onTick(GameTick)
	{
		log(LogLevel.TRACE, "tick: ${_client.getTickCount()}");
	}

	int amount;
	void onScriptCallbackEvent(ScriptCallbackEvent e)
	{
//		if ()
//		{
//			log((e.getScriptId() == -1) ? LogLevel.ERROR : LogLevel.INFO, "Script called with id: " + e.scriptId + " and arguments " + e.arguments.toString());
//			if(e.getScriptId() == -1)
//			{
//				log(LogLevel.ERROR, (ReflectionToStringBuilder.toString(e.getArguments()[0], ToStringStyle.SHORT_PREFIX_STYLE)));
//			}
//		}
		if(!e.getEventName().startsWith("SkillMulti"))
		{
			return;
		}
		//log(LogLevel.WARN, e.getEventName() + " has int stack {" + _client.getIntStack()[0..max(12, _client.getIntStackSize()-1)] + "}");
		//log(LogLevel.INFO, e.getEventName() + " has string stack {" + _client.getStringStack()[0..min(4, _client.getStringStackSize()-1)] + "}")
		if (e.getEventName().equals("SkillMultiFinished"))
		{
			log(LogLevel.INFO, e.getEventName() + " has int stack {" + _client.getIntStack()[_client.getIntStackSize()] + "}");
			int[] is = _client.getIntStack();
			e.getScript()
			is[_client.getIntStackSize() - 1] = 69
		}
		else if (e.getEventName().equals("SkillMultiChanged"))
		{
			log(LogLevel.INFO, e.getEventName() + " has int stack {" + _client.getIntStack()[_client.getIntStackSize()] + "}");
			int[] is = _client.getIntStack();
			is[_client.getIntStackSize() - 1] = WidgetInfo.PACK(270, 13 + amount);
		}
		else if (e.getEventName().equals("SkillMultiGenerated"))
		{
			log(LogLevel.INFO, e.getEventName() + " has int stack {" + _client.getIntStack()[_client.getIntStackSize()] + "}");
			final String[] sstack = _client.getStringStack();//[_client.getStringStackSize()-1];

			def tokens = sstack[_client.getStringStackSize()-1].tokenize("|").indexed(1)
			tokens.each {token -> log(LogLevel.DEBUG, "$token")}
			def idex = tokens.find {f -> f.value.equals("Maple longbow")}.getKey()
			if (idex > 0 && idex <= 10)
			{
				amount = idex;
			}
			log(LogLevel.DEBUG, "idex target: ${idex}")
		}
	}

	void startup() {
		//_eventBus.subscribe(RunScriptEvent.class, this, this.&onScriptRunEvent as Consumer<RunScriptEvent>);
		_eventBus.subscribe(GameTick.class, this, this.&onTick as Consumer<GameTick>)
		_eventBus.subscribe(ScriptCallbackEvent.class, this, this.&onScriptCallbackEvent as Consumer<ScriptCallbackEvent>);
		log(LogLevel.DEBUG, "Starting Up + " + TO_GROUP(17694744));
	}

	void shutdown() {
		_eventBus.unregister(this);
		log(LogLevel.DEBUG,"Shutting Down");
	}

	static int TO_GROUP(int id)
	{
		return id >>> 16;
	}

	/**
	 * Utility method that converts an ID returned by child back
	 * to its child ID.
	 *
	 * @param id passed group-child ID
	 * @return the child ID
	 */
	static int TO_CHILD(int id)
	{
		return id & 0xFFFF;
	}

	/**
	 * Packs the group and child IDs into a single integer.
	 *
	 * @param groupId the group ID
	 * @param childId the child ID
	 * @return the packed ID
	 */
	static int PACK(int groupId, int childId)
	{
		return groupId << 16 | childId;
	}
}
