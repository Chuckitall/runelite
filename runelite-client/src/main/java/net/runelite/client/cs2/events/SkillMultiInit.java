package net.runelite.client.cs2.events;

import lombok.Data;
import net.runelite.api.events.Event;

@Data
public class SkillMultiInit implements Event
{
	private final int optionsNum;
	private String[] options;
	private int requestedOp = -1;

	public boolean modified()
	{
		return requestedOp > -1 && options.length > requestedOp;
	}
}
