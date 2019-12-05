package net.runelite.client.plugins.fredexperimental.smelter;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Value;
import net.runelite.api.ItemID;

@Getter
public enum SmelterItem
{
	MOLTEN_GLASS(
		Lists.newArrayList(_SmelterItem.one(ItemID.MOLTEN_GLASS), _SmelterItem.one(ItemID.BUCKET)),
		Lists.newArrayList(_SmelterItem.of(ItemID.BUCKET_OF_SAND, 14), _SmelterItem.of(ItemID.SODA_ASH, 14))
	),
	STEEL_BAR(
		_SmelterItem.one(ItemID.STEEL_BAR),
		Lists.newArrayList(_SmelterItem.of(ItemID.IRON_ORE, 9), _SmelterItem.of(ItemID.COAL, 18))
	),
	GOLD_BAR(
		_SmelterItem.one(ItemID.GOLD_BAR),
		_SmelterItem.of(ItemID.GOLD_ORE, 28)
	),
	EMERALD_RING(
		_SmelterItem.one(ItemID.EMERALD_RING),
		Lists.newArrayList(_SmelterItem.of(ItemID.EMERALD, 13), _SmelterItem.of(ItemID.GOLD_BAR, 13)),
		_SmelterItem.one(ItemID.RING_MOULD)
	);

	@Value(staticConstructor = "of")
	public static class _SmelterItem
	{
		final int id;
		final int qty;
		public static _SmelterItem one(Integer id)
		{
			return new _SmelterItem(id, 1);
		}
	}

	private final Set<_SmelterItem> products;
	private final Set<_SmelterItem> ingredients;
	private final Set<_SmelterItem> catalysts;

	SmelterItem(_SmelterItem product, List<_SmelterItem> ingredients)
	{
		this.products = ImmutableSet.of(product);
		this.ingredients = ImmutableSet.copyOf(ingredients);
		this.catalysts = ImmutableSet.of();
	}

	SmelterItem(_SmelterItem product, _SmelterItem ingredient)
	{
		this.products = ImmutableSet.of(product);
		this.ingredients = ImmutableSet.of(ingredient);
		this.catalysts = ImmutableSet.of();
	}

	SmelterItem(_SmelterItem product, _SmelterItem ingredient, _SmelterItem catalyst)
	{
		this.products = ImmutableSet.of(product);
		this.ingredients = ImmutableSet.of(ingredient);
		this.catalysts = ImmutableSet.of(catalyst);
	}

	SmelterItem(_SmelterItem product, List<_SmelterItem> ingredients, _SmelterItem catalyst)
	{
		this.products = ImmutableSet.of(product);
		this.ingredients = ImmutableSet.copyOf(ingredients);
		this.catalysts = ImmutableSet.of(catalyst);
	}

	SmelterItem(List<_SmelterItem> products, List<_SmelterItem> ingredients)
	{
		this.products = ImmutableSet.copyOf(products);
		this.ingredients = ImmutableSet.copyOf(ingredients);
		this.catalysts = ImmutableSet.of();
	}
}