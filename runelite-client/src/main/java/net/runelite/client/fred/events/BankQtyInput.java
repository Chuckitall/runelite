package net.runelite.client.fred.events;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.Event;

@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
@Slf4j
public class BankQtyInput implements Event
{
	@Getter(AccessLevel.PUBLIC)
	private final int itemID;
	@Getter(AccessLevel.PUBLIC)
	private final String itemName;
	@Getter(AccessLevel.PUBLIC)
	private final int itemQty;

	private final Set<Integer> qtyOpsToAdd = new HashSet<>();

	public void requestQtyOption(int qty)
	{
		if(qty <= itemQty)
		{
			qtyOpsToAdd.add(qty);
		}
	}

	public int[] getSortedQtyOps()
	{
		return qtyOpsToAdd.stream().mapToInt(f -> f).sorted().toArray();
	}
}
