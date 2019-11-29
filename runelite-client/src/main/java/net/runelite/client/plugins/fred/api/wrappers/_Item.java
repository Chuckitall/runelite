package net.runelite.client.plugins.fred.api.wrappers;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import net.runelite.client.RuneLite;
import net.runelite.client.plugins.fred.api.interfaces.I_Item;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class _Item implements I_Item
{
	int id;
	int qty;
	int idx;
	String name;

	public _Item(int id, int qty, int idx)
	{
		this.id = id;
		this.qty = qty;
		this.idx = idx;
		this.name = RuneLite.getClient().map(f -> f.getItemDefinition(id).getName()).orElse("ERROR");
	}

	@Override
	public int getId()
	{
		return id;
	}

	@Override
	public int getQty()
	{
		return qty;
	}

	@Override
		public int getIdx()
	{
		return idx;
	}

	public String getName()
	{
		return String.copyValueOf(name.toCharArray());
	}
}