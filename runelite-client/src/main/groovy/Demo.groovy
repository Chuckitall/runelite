import groovy.transform.CompileStatic;
import groovy.transform.InheritConstructors
import io.reactivex.functions.Consumer
import net.runelite.api.MenuOpcode
import net.runelite.api.events.GameTick
import net.runelite.api.events.MenuEntryAdded
import net.runelite.api.events.MenuOptionClicked
import net.runelite.api.events.RunScriptEvent
import net.runelite.api.events.ScriptCallbackEvent
import net.runelite.api.widgets.WidgetID
import net.runelite.api.widgets.WidgetInfo
import net.runelite.client.cs2.events.ChatboxMultiInit
import net.runelite.client.cs2.events.KeyInputListener
import net.runelite.client.plugins.groovy.debugger.DebuggerWindow.LogLevel
import net.runelite.client.plugins.groovy.script.ScriptedPlugin
import org.apache.commons.lang3.ArrayUtils
import org.apache.commons.lang3.builder.ReflectionToStringBuilder
import org.apache.commons.lang3.builder.ToStringStyle

import static net.runelite.api.ItemID.RING_OF_DUELING1
import static net.runelite.api.ItemID.RING_OF_DUELING2
import static net.runelite.api.ItemID.RING_OF_DUELING3
import static net.runelite.api.ItemID.RING_OF_DUELING4
import static net.runelite.api.ItemID.RING_OF_DUELING5
import static net.runelite.api.ItemID.RING_OF_DUELING6
import static net.runelite.api.ItemID.RING_OF_DUELING7
import static net.runelite.api.ItemID.RING_OF_DUELING8

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
			//log((e.getScriptId() == -1) ? LogLevel.ERROR : LogLevel.INFO, "Script called with id: " + e.scriptId + " and arguments " + e.arguments.toString());
			//log(LogLevel.WARN, _client.getIntStack().toString())
			//log(LogLevel.WARN, _client.getStringStack().toString())
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
	boolean foundChatbox = false;

	void onTick(GameTick t)
	{
//		log(LogLevel.TRACE, "tick: ${_client.getTickCount()}");
//		if(foundChatbox)
//		{
//			if(_client.getWidget(219, 1) != null) {
//				def childs = _client.getWidget(219, 1).getDynamicChildren();
//				if(childs.length > 0)
//				{
//					for(int i = 0; i < childs.length; i++)
//					{
//						log(LogLevel.DEBUG, "onKey[i]: " + childs[i].getOnKeyListener());
//					}
//				}
//			}
//			foundChatbox = false;
//		}
	}
	int skillMultiCallback_

	void skillMultiCallback(ScriptCallbackEvent e)
	{
		assert e.getEventName().startsWith("SkillMulti")
		if (e.getEventName().equals("SkillMultiFinished"))
		{
			log(LogLevel.INFO, e.getEventName() + " has int stack {" + _client.getIntStack()[_client.getIntStackSize()] + "}");
			int[] is = _client.getIntStack();
			e.getScript()
			if (skillMultiCallback_ == -1)
			{
				log(LogLevel.INFO, "Not making any changes to the stack");
			}
			else
			{
				log(LogLevel.INFO, "Planning to set the requested button to ${skillMultiCallback_}");
				is[_client.getIntStackSize() - 1] = 69
			}
		}
		else if (e.getEventName().equals("SkillMultiChanged"))
		{
			log(LogLevel.INFO, e.getEventName() + " has int stack {" + _client.getIntStack()[_client.getIntStackSize()] + "}");
			int[] is = _client.getIntStack();
			is[_client.getIntStackSize() - 1] = WidgetInfo.PACK(270, 13 + skillMultiCallback_);
			skillMultiCallback_ = -1;
		}
		else if (e.getEventName().equals("SkillMultiGenerated"))
		{
			log(LogLevel.INFO, e.getEventName() + " has int stack {" + _client.getIntStack()[_client.getIntStackSize()] + "}");
			final String[] sstack = _client.getStringStack();

			def tokens = sstack[_client.getStringStackSize()-1].tokenize("|").indexed(1)
			tokens.each {token -> log(LogLevel.DEBUG, "$token")}
			def idex = tokens.find {f -> f.value.equals("Maple longbow") || f.value.equals("Water battlestaff")}.getKey()
			if (idex > 0 && idex <= 10)
			{
				skillMultiCallback_ = idex;
			}
			log(LogLevel.DEBUG, "idex target: ${idex}")
		}
	}

//	void chatboxMultiCallback(ScriptCallbackEvent e)
//	{
//		assert e.getEventName().startsWith("ChatboxMulti")
//		if (e.getEventName().equals("ChatboxMultiInit"))
//		{
//			log(LogLevel.INFO, e.getEventName() + " has int stack {" + _client.getIntStack()[_client.getIntStackSize()-1] + "}");
//			def sstack = _client.getStringStack();
//			for (int i = _client.getStringStackSize() - 1; i >= 0; i--)
//			{
//				log(LogLevel.INFO, e.getEventName() + ": sstack[${i}] -> ${sstack[i]}");
//			}
//		}
//	}

	void onKeyInputListenerCallback(KeyInputListener callback)
	{
		log(LogLevel.WARN, callback.toString());
	}

	void onScriptCallbackEvent(ScriptCallbackEvent e)
	{
		if(e.getEventName().startsWith("SkillMulti"))
		{
			skillMultiCallback(e);
		}
	}

	private final String[] options = new String[] { "Duel", "Castle", "Clan"};
	String targetWord = "";
	void onChatboxMultiInit(ChatboxMultiInit e)
	{
		log(LogLevel.DEBUG, e.toString() + " " + targetWord);
		for (int i = 0; i < e.getOptionsNum(); i++)
		{
			log(LogLevel.INFO, "Option[${i}] = ${e.getOptions()[i]}");
			if (targetWord.length() > 0 && e.getOptions()[i].containsIgnoreCase(targetWord))
			{
				e.setRequestedOp(i+1);
				targetWord = "";
				break;
			}
		}
		log(LogLevel.WARN, e.toString());
	}

	int[] ringsOfDueling = [RING_OF_DUELING1,RING_OF_DUELING2,RING_OF_DUELING3,RING_OF_DUELING4,RING_OF_DUELING5,RING_OF_DUELING6,RING_OF_DUELING7,RING_OF_DUELING8];

	void onMenuEntryAdded(MenuEntryAdded e)
	{
		if (WidgetID.INVENTORY_GROUP_ID == WidgetInfo.TO_GROUP(e.getParam1()) && e.getOpcode() == MenuOpcode.ITEM_FOURTH_OPTION.getId() && e.getOption().contains("Rub") && ringsOfDueling.any {int it -> e.getIdentifier() == it} ) //check item is in inventory
		{
			for(int a = 0; a < options.length; a++)
			{
				_client.insertMenuItem(options[a], e.getTarget(), e.getOpcode(), e.getIdentifier(), e.getParam0(), e.getParam1(), false);
			}
			//return//Option[2] = Castle Wars Arena.Option[3] = Clan Wars Arena.
		}
//		log(LogLevel.DEBUG, e.toString());
	}

	void onMenuOptionClicked(MenuOptionClicked e)
	{
		if (WidgetID.INVENTORY_GROUP_ID == WidgetInfo.TO_GROUP(e.getParam1()) && e.getOpcode() == MenuOpcode.ITEM_FOURTH_OPTION.getId() && ringsOfDueling.any {int it -> e.getIdentifier() == it} ) //check item is in inventory
		{
			if(!e.getOption().equalsIgnoreCase("Rub"))
			{
				targetWord = e.getOption();
			}
		}
		log(LogLevel.DEBUG, e.toString());
	}

	void startup() {
		_eventBus.subscribe(GameTick.class, this, this.&onTick as Consumer<GameTick>)
		_eventBus.subscribe(ScriptCallbackEvent.class, this, this.&onScriptCallbackEvent as Consumer<ScriptCallbackEvent>);
		_eventBus.subscribe(ChatboxMultiInit.class, this, this.&onChatboxMultiInit as Consumer<ChatboxMultiInit>);
		_eventBus.subscribe(RunScriptEvent.class, this, this.&onScriptRunEvent as Consumer<RunScriptEvent>);
		_eventBus.subscribe(KeyInputListener.class, this, this.&onKeyInputListenerCallback as Consumer<KeyInputListener>)

		_eventBus.subscribe(MenuOptionClicked.class, this, this::onMenuOptionClicked as Consumer<MenuOptionClicked>);
		_eventBus.subscribe(MenuEntryAdded.class, this, this::onMenuEntryAdded as Consumer<MenuEntryAdded>);
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
