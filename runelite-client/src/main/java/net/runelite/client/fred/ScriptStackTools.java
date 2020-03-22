package net.runelite.client.fred;

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

	public String popStringOffStack()
	{
		int stringStackSize = client.getStringStackSize();
		String string = client.getStringStack()[--stringStackSize];
		client.setStringStackSize(stringStackSize);
		return string;
	}

	public int popIntOffStack()
	{
		int intStackSize = client.getIntStackSize();
		int i = client.getIntStack()[--intStackSize];
		client.setIntStackSize(intStackSize);
		return i;
	}

	public String[] slurpStringsFromStack(int toSlurp)
	{
		String[] toRet = new String[toSlurp];
		for(int i = toSlurp-1; i >= 0; i--)
		{
			toRet[i] = popStringOffStack();
		}
		return toRet;
	}

	public int[] slurpIntsFromStack(int toSlurp)
	{
		int[] toRet = new int[toSlurp];
		for(int i = toSlurp-1; i >= 0; i--)
		{
			toRet[i] = popIntOffStack();
		}
		return toRet;
	}

	public String copyStringFromStack(int i)
	{
		int stringStackSize = client.getStringStackSize();
		return client.getStringStack()[stringStackSize-1-i];
	}

	public String[] copyStringsFromStack(int toSlurp)
	{
		String[] toRet = new String[toSlurp];
		for(int i = toSlurp-1; i >= 0; i--)
		{
			toRet[i] = copyStringFromStack(i);
		}
		return toRet;
	}

	public int copyIntFromStack(int i)
	{
		int intStackSize = client.getIntStackSize();
		return client.getIntStack()[intStackSize-1-i];
	}

	public int[] copyIntsFromStack(int toSlurp)
	{
		int[] toRet = new int[toSlurp];
		for(int i = toSlurp-1; i >= 0; i--)
		{
			toRet[i] = copyIntFromStack(i);
		}
		return toRet;
	}

	public void pasteStringToStack(int i, String value)
	{
		int strStackSize = client.getStringStackSize();
		client.getStringStack()[strStackSize-1-i] = value;
	}

	public void pasteIntToStack(int i, int value)
	{
		int intStackSize = client.getIntStackSize();
		client.getIntStack()[intStackSize-1-i] = value;
	}
}
