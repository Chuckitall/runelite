package net.runelite.client.cs2.events;

import lombok.Data;
import net.runelite.api.events.Event;

@Data
public class ChatboxMultiInit implements Event
{
	private final int optionsNum;
	private final String[] options;
	private int requestedOp = 0;

	public boolean modified()
	{
		return requestedOp > 0 && options.length >= requestedOp;
	}
}
