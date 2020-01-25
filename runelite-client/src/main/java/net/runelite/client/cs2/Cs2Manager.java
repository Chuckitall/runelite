package net.runelite.client.cs2;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ScriptCallbackEvent;
import net.runelite.client.cs2.events.ChatboxMultiInit;
import net.runelite.client.cs2.events.KeyInputListener;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.menus.AbstractComparableEntry;
import net.runelite.client.menus.WidgetMenuOption;
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

	private void pushStringToStack(String string)
	{
		int stringStackSize = client.getStringStackSize();
		client.getStringStack()[++stringStackSize] = string;
		client.setStringStackSize(stringStackSize);
	}

	private int popIntOffStack()
	{
		int intStackSize = client.getIntStackSize();
		int i = client.getIntStack()[--intStackSize];
		client.setIntStackSize(intStackSize);
		return i;
	}

	private void pushIntToStack(int i)
	{
		int intStackSize = client.getIntStackSize();
		int[] intStack = client.getIntStack();
		intStack[++intStackSize] = i;
		client.setIntStackSize(intStackSize);
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
		int integer = client.getIntStack()[intStackSize-1-i];
		return integer;
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

	private void onScriptCallbackEvent(ScriptCallbackEvent callback)
	{
		if (callback.getEventName().equals("ChatboxMultiBuilt"))
		{
			int numberOfOps = copyIntFromStack(0);
			String[] ops = slurpStringsFromStack(5);
			ChatboxMultiInit event = new ChatboxMultiInit(numberOfOps, ops);
			eventBus.post(ChatboxMultiInit.class, event);
			log.debug("Event {}", event);
			pasteIntToStack(0, event.getRequestedOp());
		}
	}

	private void onGameTick(GameTick event)
	{
	}
}
