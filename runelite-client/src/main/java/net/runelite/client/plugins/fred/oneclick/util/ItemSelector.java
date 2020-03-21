package net.runelite.client.plugins.fred.oneclick.util;

import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import net.runelite.client.plugins.fred.oneclick.api._Item;

public class ItemSelector
{

	private final Set<Integer> matchingIds;
	@Getter
	private int minQty;

	private List<_Item> foundMatches = new ArrayList<>();

	@Getter
	private int selectedIdx = -1;

	public boolean found()
	{
		return foundMatches.size() > 0;
	}

	public boolean testAndAdd(int id, int idx)
	{
		return testAndAdd(id, 1, idx);
	}

	public boolean testAndAdd(int id, int qty, int idx)
	{
		if (matchingIds.contains(id) && 28 > idx && idx >= 0 && foundMatches.stream().noneMatch(f -> f.getIdx() == idx) && qty >= minQty)
		{
			foundMatches.add(new _Item(id, qty, idx));
			if (foundMatches.size() == 1)
			{
				setSelectedIdx(idx);
			}
			return true;
		}
		return false;
	}

	public void setSelectedIdx(int idx)
	{
		if (idxMatches(idx))
		{
			this.selectedIdx = idx;
		}
	}

	public boolean idxMatches(int idx)
	{
		return foundMatches.stream().anyMatch(f -> (f.getIdx() == idx));
	}

	public int getSelectedId()
	{
		Optional<_Item> entry = foundMatches.stream().filter(f -> f.getIdx() == getSelectedIdx()).findFirst();
		return entry.map(_Item::getId).orElse(-1);
	}

	public int getSelectedQty()
	{
		Optional<_Item> entry = foundMatches.stream().filter(f -> f.getIdx() == getSelectedIdx()).findFirst();
		return entry.map(_Item::getQty).orElse(-1);
	}

	public void clear()
	{
		foundMatches.clear();
		selectedIdx = -1;
	}

	public ItemSelector(Set<Integer> matchingIds)
	{
		this.matchingIds = matchingIds;
		this.minQty = 1;
	}

	public ItemSelector(Set<Integer> matchingIds, int minQty)
	{
		this.matchingIds = matchingIds;
		this.minQty = minQty;
	}

	public static ItemSelector rune(int id, int qty)
	{
		return new ItemSelector(ImmutableSet.of(id), qty);
	}
}
