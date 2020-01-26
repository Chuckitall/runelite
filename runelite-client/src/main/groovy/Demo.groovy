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
import net.runelite.client.cs2.InterfaceChoice
import net.runelite.client.cs2.events.ChatboxMultiInit
import net.runelite.client.cs2.events.KeyInputListener
import net.runelite.client.cs2.events.SkillMultiInit
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

	private final String[] options = new String[] { "Duel", "Castle", "Clan"};
	String targetWord = "";
//	void onChatboxMultiInit(ChatboxMultiInit e)
//	{
//		log(LogLevel.DEBUG, e.toString() + " " + targetWord);
//		for (int i = 0; i < e.getOptionsNum(); i++)
//		{
//			log(LogLevel.INFO, "Option[${i}] = ${e.getOptions()[i]}");
//			if (targetWord.length() > 0 && e.getOptions()[i].containsIgnoreCase(targetWord))
//			{
//				e.setRequestedOp(i+1);
//				targetWord = "";
//				break;
//			}
//		}
//		log(LogLevel.WARN, e.toString());
//	}

	private final String[] skillingTargets = new String[] { "Water battlestaff", "Maple longbow", "Water orb"};
//	void onSkillMultiInit(SkillMultiInit e)
//	{
//		for (int i = 0; i < e.getOptionsNum(); i++)
//		{
//			log(LogLevel.INFO, "Option[${i}] = ${e.getOptions()[i]}");
//			if (skillingTargets.any {it -> e.getOptions()[i].containsIgnoreCase(it) } )
//			{
//				e.setRequestedOp(i+1);
//				break;
//			}
//		}
//		log(LogLevel.WARN, e.toString());
//	}

	void onInterfaceChoice(InterfaceChoice event)
	{
		if(event.getOptionCount() == 1)
		{
			event.requestOption(1);
		}
		else if(targetWord.length() > 0)
		{
			for(int i = 1; i <= event.getOptionCount() && event.free(); i++)
			{
				String op = event.getOption(i).toLowerCase();
				if (skillingTargets.any(it -> it.equalsIgnoreCase(op)))
				{
					event.requestOption(i);
				}
				else if(options.any {it -> op.containsIgnoreCase(it)})
				{
					event.requestOption(i);
					targetWord = "";
				}
			}
		}
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



	void startup()
	{
		_eventBus.subscribe(InterfaceChoice.class, this, this.&onInterfaceChoice as Consumer<InterfaceChoice>)

		_eventBus.subscribe(MenuOptionClicked.class, this, this::onMenuOptionClicked as Consumer<MenuOptionClicked>);
		_eventBus.subscribe(MenuEntryAdded.class, this, this::onMenuEntryAdded as Consumer<MenuEntryAdded>);
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
