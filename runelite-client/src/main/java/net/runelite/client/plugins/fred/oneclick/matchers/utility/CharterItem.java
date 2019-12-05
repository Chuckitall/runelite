package net.runelite.client.plugins.fred.oneclick.matchers.utility;

import lombok.Getter;
import net.runelite.api.ItemID;

@Getter
public enum CharterItem
{
	PINEAPPLE(16, ItemID.PINEAPPLE),
	BUCKET_OF_SLIME(19, ItemID.BUCKET_OF_SLIME),
	BUCKET_OF_SAND(21, ItemID.BUCKET_OF_SAND),
	SEAWEED(22, ItemID.SEAWEED),
	SODA_ASH(23, ItemID.SODA_ASH);

	final private int idx;
	final private int id;

	CharterItem(int idx, int id)
	{
		this.idx = idx;
		this.id = id;
	}
}