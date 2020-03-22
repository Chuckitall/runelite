package net.runelite.client.fred.events;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
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
	@Getter(AccessLevel.PUBLIC)
	private final WidgetInfo source;
	@Getter(AccessLevel.PUBLIC)
	private final String headerText;
	/**
	 * Note that everything in the class is 1 indexed.
	 * This is because a 0 value is regarded as a
	 * no selection state in the rs2asm file.
	 **/
	//No getter because I have a 1 indexed getter available.
	private final String[] options;

	private int requestedOp = 0;

	public int getRequestedOp()
	{
		return requestedOp;
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

	private boolean indexInRange(int i)
	{
		return i > 0 && i <= options.length;
	}
}
