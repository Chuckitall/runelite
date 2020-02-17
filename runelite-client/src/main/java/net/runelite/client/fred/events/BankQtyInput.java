package net.runelite.client.fred.events;

import com.google.common.collect.Comparators;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.Event;
import net.runelite.client.fred.FredMenu;
import net.runelite.client.fred.FredMenu._Request;

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

	private final Set<_Request> qtyOpsToAdd = new HashSet<>();

	public void requestQtyOption(int qty)
	{
		requestQtyOption(qty, false);
	}

	public void requestQtyOption(int qty, boolean noted)
	{
		if(qty <= itemQty)
		{
			qtyOpsToAdd.add(new _Request(qty, noted));
		}
	}

	public _Request[] getSortedQtyOps()
	{
		return qtyOpsToAdd.stream().sorted(new Comparator<_Request>()
		{
			@Override
			public int compare(_Request o1, _Request o2)
			{
				if(o1.noted == o2.noted)
				{
					return o1.qty - o2.qty;
				}
				else
				{
					return o1.noted ? -1 : 1;
				}
			}
		}).toArray(_Request[]::new);
	}
}
