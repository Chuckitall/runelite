package net.runelite.client.fred;

import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;

@Slf4j
public class ScriptStackTools
{
	private final Client client;

	ScriptStackTools(Client client)
	{
		this.client = client;
	}

	String popStringOffStack()
	{
		int stringStackSize = client.getStringStackSize();
		String string = client.getStringStack()[--stringStackSize];
		client.setStringStackSize(stringStackSize);
		return string;
	}

	int popIntOffStack()
	{
		int intStackSize = client.getIntStackSize();
		int i = client.getIntStack()[--intStackSize];
		client.setIntStackSize(intStackSize);
		return i;
	}

	String[] slurpStringsFromStack(int toSlurp)
	{
		String[] toRet = new String[toSlurp];
		for(int i = toSlurp-1; i >= 0; i--)
		{
			toRet[i] = popStringOffStack();
		}
		return toRet;
	}

	int[] slurpIntsFromStack(int toSlurp)
	{
		int[] toRet = new int[toSlurp];
		for(int i = toSlurp-1; i >= 0; i--)
		{
			toRet[i] = popIntOffStack();
		}
		return toRet;
	}

	String copyStringFromStack(int i)
	{
		int stringStackSize = client.getStringStackSize();
		return client.getStringStack()[stringStackSize-1-i];
	}

	String[] copyStringsFromStack(int toSlurp)
	{
		String[] toRet = new String[toSlurp];
		for(int i = toSlurp-1; i >= 0; i--)
		{
			toRet[i] = copyStringFromStack(i);
		}
		return toRet;
	}

	int copyIntFromStack(int i)
	{
		int intStackSize = client.getIntStackSize();
		return client.getIntStack()[intStackSize-1-i];
	}

	int[] copyIntsFromStack(int toSlurp)
	{
		int[] toRet = new int[toSlurp];
		for(int i = toSlurp-1; i >= 0; i--)
		{
			toRet[i] = copyIntFromStack(i);
		}
		return toRet;
	}

	void pasteStringToStack(int i, String value)
	{
		int strStackSize = client.getStringStackSize();
		client.getStringStack()[strStackSize-1-i] = value;
	}

	void pasteIntToStack(int i, int value)
	{
		int intStackSize = client.getIntStackSize();
		client.getIntStack()[intStackSize-1-i] = value;
	}
}
