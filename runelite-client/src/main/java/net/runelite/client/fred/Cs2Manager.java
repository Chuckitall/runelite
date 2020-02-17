package net.runelite.client.fred;

import java.util.ArrayList;
import java.util.Arrays;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ScriptCallbackEvent;
import net.runelite.api.util.Text;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.fred.events.MushroomTeleEvent;
import net.runelite.client.plugins.fred.api.other.Tuples;
import net.runelite.client.plugins.fred.api.other.Tuples.T2;
import org.apache.commons.lang3.ArrayUtils;
import org.codehaus.groovy.runtime.ArrayUtil;

@Singleton
@Slf4j
public class Cs2Manager
{
	private final Client client;
	private final EventBus eventBus;

	//Used to manage custom non-player menu options

	@Inject
	private Cs2Manager(Client client, EventBus eventBus)
	{
		this.client = client;
		this.eventBus = eventBus;

		eventBus.subscribe(ScriptCallbackEvent.class, this, this::onScriptCallbackEvent);
		eventBus.subscribe(GameTick.class, this, this::onGameTick);
	}

	private String popStringOffStack()
	{
		int stringStackSize = client.getStringStackSize();
		String string = client.getStringStack()[--stringStackSize];
		client.setStringStackSize(stringStackSize);
		StringBuilder toRet = new StringBuilder();
		return toRet.append(string).toString();
	}

	private int popIntOffStack()
	{
		int intStackSize = client.getIntStackSize();
		int i = client.getIntStack()[--intStackSize];
		client.setIntStackSize(intStackSize);
		return i;
	}

	private String[] slurpStringsFromStack(int toSlurp)
	{
		String[] toRet = new String[toSlurp];
		for(int i = toSlurp-1; i >= 0; i--)
		{
			toRet[i] = popStringOffStack();
		}
		return toRet;
	}

	private String copyStringFromStack(int i)
	{
		int stringStackSize = client.getStringStackSize();
		String string = client.getStringStack()[stringStackSize-1-i];
		StringBuilder toRet = new StringBuilder();
		return toRet.append(string).toString();
	}

	private String[] copyStringsFromStack(int toSlurp)
	{
		String[] toRet = new String[toSlurp];
		for(int i = toSlurp-1; i >= 0; i--)
		{
			toRet[i] = copyStringFromStack(i);
		}
		return toRet;
	}

	private int copyIntFromStack(int i)
	{
		int intStackSize = client.getIntStackSize();
		return client.getIntStack()[intStackSize-1-i];
	}

	private void pasteIntToStack(int i, int value)
	{
		int intStackSize = client.getIntStackSize();
		client.getIntStack()[intStackSize-1-i] = value;
	}

	private int[] copyIntsFromStack(int toSlurp)
	{
		int[] toRet = new int[toSlurp];
		for(int i = toSlurp-1; i >= 0; i--)
		{
			toRet[i] = copyIntFromStack(i);
		}
		return toRet;
	}

	private String skill_options_string = null;
	private void onScriptCallbackEvent(ScriptCallbackEvent callback)
	{
		switch (callback.getEventName())
		{
			case "ChatboxMultiBuilt":
			{
				int numberOfOps = copyIntFromStack(0);
				String[] ops = slurpStringsFromStack(5);
				String header = popStringOffStack();
				ops = ArrayUtils.subarray(ops, 0, numberOfOps);
				InterfaceChoice event = new InterfaceChoice(WidgetInfo.DIALOG_OPTION, header, ops);
				eventBus.post(InterfaceChoice.class, event);
				log.debug("Event {}", event);
				pasteIntToStack(0, event.getRequestedOp());
				break;
			}
			case "SkillMulti_Start":
			{
				skill_options_string = copyStringFromStack(0);
				break;
			}
			case "SkillMulti_End":
			{
				int numberOfOps = copyIntFromStack(0);
				pasteIntToStack(0, 0);
				String[] ops = skill_options_string.split("\\|");
				for (int i = 0; i < ops.length; i++)
				{
					log.info("Option[{}] = \"{}\"", i, ops[i]);
				}
				InterfaceChoice event = new InterfaceChoice(WidgetInfo.MULTI_SKILL_MENU, "SkillMulti", ops);
				eventBus.post(InterfaceChoice.class, event);
				log.debug("Skill Event {}", event);
				if (!event.free())
				{
					pasteIntToStack(0, WidgetInfo.PACK(270, 13 + event.getRequestedOp()));
				}
				skill_options_string = null;
				break;
			}
			case "OnMushroomTeleportWidgetBuilt":
			{
				int[] options = copyIntsFromStack(4);
				ArrayUtils.reverse(options);
				MushroomTeleEvent event = new MushroomTeleEvent(options);
				log.debug("Event {}", event);
				eventBus.post(MushroomTeleEvent.class, event);
				log.debug("Event {}", event);
				pasteIntToStack(4, event.getSelectedOption());
				break;
			}
		}
	}

	private void onGameTick(GameTick event)
	{
	}
}
