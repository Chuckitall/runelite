package net.runelite.client.cs2;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.Event;
import net.runelite.api.widgets.WidgetInfo;

@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
@Slf4j
public class InterfaceChoice implements Event
{
	public final WidgetInfo source;
	private final String headerText;
	private final String[] options;

	private int requestedOp = 0;
	public int getRequestedOp()
	{
		return requestedOp;
	}

	private boolean indexInRange(int i)
	{
		return i > 0 && i <= options.length;
	}

	public int getOptionCount()
	{
		return options.length;
	}

	public boolean free()
	{
		return requestedOp == 0;
	}

	public void requestOption(int i)
	{
		if (free() && indexInRange(i))
		{
			requestedOp = i;
		}
	}

	public String getOption(int i)
	{
		if (indexInRange(i))
		{
			return options[i - 1];
		}
		log.error("Query about option[{}] from event {} received", i, this);
		return "";
	}
}
