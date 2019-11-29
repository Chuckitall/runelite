package net.runelite.client.plugins.fred.api.wrappers;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Slf4j
public class _ItemContainer
{
	int rows;
	int columns;
	List<_Item> items;

	public static _ItemContainer create(Client client, InventoryID invId)
	{
		if (client == null || !(invId == InventoryID.INVENTORY || invId == InventoryID.FISHING_TRAWLER_REWARD))
		{
			return null;
		}

		ItemContainer container = client.getItemContainer(invId);
		if (container == null)
		{
			return null;
		}

		int r_ = 0;
		int c_ = 0;
		if (invId == InventoryID.INVENTORY)
		{
			r_ = 7;
			c_ = 4;
		}

		List<_Item> fullSpots = new ArrayList<>();
		for (int i = 0; i < container.getItems().length; i++)
		{
			Item item = container.getItems()[i];
//			if (item.getQuantity() <= 0 || item.getId() <= 0)
//			{
//				log.debug("wtf {}", item);
//				continue;
//			}
			if (i < (r_ * c_))
			{
				fullSpots.add(new _Item(item.getId(), item.getQuantity(), i));
			}
			else
			{
				log.debug("fuckme");
			}
		}

		return new _ItemContainer(r_, c_, fullSpots);
	}

	public int getSize()
	{
		return rows * columns;
	}

	public int getSlotX(int slot)
	{
		return slot % columns;
	}

	public int getSlotY(int slot)
	{
		return slot / columns;
	}

	public int getSlotIdx(int x, int y)
	{
		if (x >= 0 && y >= 0 && x < columns && y < rows)
		{
			return x + (y * columns);
		}
		else
		{
			return -1;
		}
	}

	public int getDistBetweenSlots(int slotA, int slotB)
	{
		int aX = getSlotX(slotA);
		int aY = getSlotY(slotA);
		int bX = getSlotX(slotB);
		int bY = getSlotY(slotB);
		int dX = Math.abs(aX - bX);
		int dY = Math.abs(aY - bY);
		int delta = dX + dY;
		log.debug("[{}=({}, {}) | {}=({}, {})] => {}", slotA, aX, aY, slotB, bX, bY, delta);
		return delta;
	}

	public Optional<_Item> getItemAtIdx(int slot)
	{
		return items.stream().filter(f -> f.getIdx() == slot).findFirst();
	}

	public Optional<_Item> getItem(int[] itemIds)
	{
		List<_Item> temp = items.stream().filter(f -> Arrays.stream(itemIds).anyMatch(j -> j == f.getId())).collect(Collectors.toList());
		Collections.shuffle(temp);
		return temp.stream().findFirst();
	}

	public Optional<_Item> getItem(int itemId)
	{
		List<_Item> temp = items.stream().filter(f -> itemId == f.getId()).collect(Collectors.toList());
//		Collections.shuffle(temp);
		return temp.stream().findFirst();
	}

	public List<_Item> getItems(int[] itemIds)
	{
		return items.stream().filter(f -> Arrays.stream(itemIds).anyMatch(j -> j == f.getId())).collect(Collectors.toList());
	}

	public List<_Item> getItems(int itemId)
	{
		return getItems(new int[] {itemId});
	}

	public Optional<_Item> getItemNearSlot(int fromSlot, int[] itemIds)
	{
		List<_Item> matching = items.stream().filter(f -> Arrays.stream(itemIds).anyMatch(j -> j == f.getId())).collect(Collectors.toList());
		int min = (columns - 1) + (rows - 1);
		for (_Item i : matching)
		{
			min = Math.min(min, getDistBetweenSlots(fromSlot, i.getIdx()));
		}
		final int min_ = min;
		List<_Item> closest = matching.stream().filter(f -> getDistBetweenSlots(fromSlot, f.getIdx()) == min_).collect(Collectors.toList());
		Collections.shuffle(closest);
		return closest.stream().findFirst();
	}
}
