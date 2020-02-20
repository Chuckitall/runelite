package net.runelite.client.fred.events;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.Event;

@Data
@AllArgsConstructor
public class SortMenusEvent implements Event
{
	private final List<MenuEntry> entries;
	private MenuEntry leftClickEntry;
}
