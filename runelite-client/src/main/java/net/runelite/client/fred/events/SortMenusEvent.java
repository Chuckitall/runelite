package net.runelite.client.fred.events;

import lombok.Data;
import lombok.NonNull;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.Event;

@Data
public class SortMenusEvent implements Event
{
	private final MenuEntry[] entries;
	@NonNull
	private MenuEntry leftClickEntry;

	private boolean touched = false;
}
