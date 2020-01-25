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

	private String copyStringOffStack(int i)
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
			toRet[i] = copyStringOffStack(i);
		}
		return toRet;
	}
	private int copyIntOffStack(int i)
	{
		int intStackSize = client.getIntStackSize();
		int integer = client.getIntStack()[intStackSize-1-i];
		return integer;
	}

	private int[] copyIntsFromStack(int toSlurp)
	{
		int[] toRet = new int[toSlurp];
		for(int i = toSlurp-1; i >= 0; i--)
		{
			toRet[i] = copyIntOffStack(i);
		}
		return toRet;
	}

	int requestedOp = 0;
	private void onScriptCallbackEvent(ScriptCallbackEvent callback)
	{
		if (callback.getEventName().startsWith("ChatboxMulti"))
		{
			if (callback.getEventName().equals("ChatboxMultiInit"))
			{
				int numOfOps = popIntOffStack();
				ChatboxMultiInit event = new ChatboxMultiInit(numOfOps, slurpStringsFromStack(5));
				eventBus.post(ChatboxMultiInit.class, event);
				requestedOp = event.getRequestedOp();
			}
			else if (callback.getEventName().equals("ChatboxMultiBuilt"))
			{
				log.debug(callback.getEventName());
				client.getIntStack()[client.getIntStackSize() - 1] = requestedOp;
				log.debug("_istack: {}", ArrayUtils.toString(client.getIntStack()));
				requestedOp = 0;
			}
			else if (callback.getEventName().equals("ChatboxMultiChanging"))
			{

			}
			return;
		}
		if (callback.getEventName().startsWith("ChatboxKeyInputListener"))
		{
			if (callback.getEventName().equals("ChatboxKeyInputListener_Spy"))
			{
				String[] copyS = copyStringsFromStack(2);
				int[] copyI = copyIntsFromStack(6);
				KeyInputListener event = new KeyInputListener(copyI[5], copyI[4], copyI[3], copyI[2], copyS[1], copyS[0], copyI[1], copyI[0]);
				eventBus.post(KeyInputListener.class, event);
			}
			return;
		}
//		else if (callback.getEventName().startsWith("SkillMulti"))
//		{
//			if (callback.getEventName().equals("SkillMultiGenerated"))
//			{
//				int numOfOps = popIntOfStack();
//				String s1 = popStringOfStack();
//				String s2 = popStringOfStack();
//				String s3 = popStringOfStack();
//				String s4 = popStringOfStack();
//				String s5 = popStringOfStack();
//				ChatboxMultiInit event = new ChatboxMultiInit(numOfOps, new String[] {s1, s2, s3, s4, s5});
//				eventBus.post(ChatboxMultiInit.class, event);
//			}
//		}
	}

	private void onGameTick(GameTick event)
	{
	}
}
