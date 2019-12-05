package net.runelite.client.plugins.fred.api.wrappers;

import lombok.Value;
import net.runelite.client.plugins.fred.api.builders._AreaBuilder._ComplexAreaBuilder;
import net.runelite.client.plugins.fred.api.builders._AreaBuilder._RectAreaBuilder;
import net.runelite.client.plugins.fred.api.interfaces._TileContainer;
import net.runelite.client.plugins.fred.util.Random;
import org.apache.commons.lang3.ArrayUtils;

@Value
public class _Area implements _TileContainer
{
	private _Tile[] tiles;

	public boolean contains(_Tile t)
	{
		return t != null && ArrayUtils.contains(tiles, t);
	}

	public _Tile getTile()
	{
		return tiles[Random.nextInt(0, tiles.length)];
	}

	public static _ComplexAreaBuilder buildComplex()
	{
		return new _ComplexAreaBuilder();
	}

	public static _RectAreaBuilder buildRect()
	{
		return new _RectAreaBuilder();
	}
}
