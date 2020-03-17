package net.runelite.client.plugins.stash;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;

public class StashItem
{
	@Getter(AccessLevel.PUBLIC)
	private final int mainId; //the id that we will use for
	@Getter(AccessLevel.PUBLIC)
	private final int[] valid_ids;

	private StashItem(Integer... items)
	{
		this.mainId = items[0];
		this.valid_ids = ArrayUtils.toPrimitive(items);
	}

	public static StashItem item(int id)
	{
		return new StashItem(id);
	}

	public static StashItem item(int id, Integer... alternatives)
	{
		return new StashItem(ArrayUtils.insert(0, alternatives, id));
	}
}
