package net.runelite.client.plugins.fred.oneclick.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import net.runelite.client.RuneLite;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class _Item
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

	public int getId()
	{
		return id;
	}

	public int getQty()
	{
		return qty;
	}

		public int getIdx()
	{
		return idx;
	}

	public String getName()
	{
		return String.copyValueOf(name.toCharArray());
	}
}