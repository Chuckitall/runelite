package net.runelite.client.fred.events;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.Event;
import net.runelite.client.fred.FredMenu._Request;

@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
@Slf4j
public class MushroomTeleEvent implements Event
{
	@Getter(AccessLevel.PUBLIC)
	final String[] options = {"House", "Valley", "Swamp", "Meadow"};
	private final int[] keys;
	private int selectedOption = -1;
	public void requestOption(int i)
	{
		if(selectedOption == -1 && i >= 0 && i < options.length)
		{
			selectedOption = i;
		}
	}

	public int getSelectedOption()
	{
		if(selectedOption == -1)
		{
			return 0;
		}
		return keys[selectedOption];
	}
}
