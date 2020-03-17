package net.runelite.client.plugins.stash;

import java.util.Arrays;
import lombok.AccessLevel;
import lombok.Getter;

public class StashSet
{
	@Getter(AccessLevel.PUBLIC)
	private final StashItem[] items;

	private StashSet(StashItem... items)
	{
		this.items = items;
	}

	public static StashSet set(Integer... items)
	{
		return new StashSet(Arrays.stream(items).map(StashItem::item).toArray(StashItem[]::new));
	}

	public static StashSet set(StashItem... items)
	{
		return new StashSet(items);
	}

	public static StashSet empty()
	{
		return new StashSet();
	}
}
