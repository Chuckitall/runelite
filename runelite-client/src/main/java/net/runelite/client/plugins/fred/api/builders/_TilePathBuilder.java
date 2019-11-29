package net.runelite.client.plugins.fred.api.builders;

import com.google.common.collect.Lists;
import java.util.List;
import lombok.Data;
import net.runelite.client.plugins.fred.api.wrappers._Tile;
import net.runelite.client.plugins.fred.api.wrappers._TilePath;
import org.apache.commons.lang3.tuple.Pair;

@Data(staticConstructor = "startingAt")
public class _TilePathBuilder
{
	private final _Tile startTile;
	private final List<Pair<Integer, Integer>> delta = Lists.newArrayList();

	public _TilePathBuilder dx(int o)
	{
		Pair<Integer, Integer> offset = Pair.of(o, 0);
		delta.add(offset);
		return this;
	}

	public _TilePathBuilder dy(int o)
	{
		Pair<Integer, Integer> offset = Pair.of(0, o);
		delta.add(offset);
		return this;
	}

	public _TilePathBuilder delta(int x, int y)
	{
		Pair<Integer, Integer> offset = Pair.of(x, y);
		delta.add(offset);
		return this;
	}

	public _TilePath build()
	{
		List<_Tile> tiles = Lists.newArrayList(startTile);
		for (int i = 0; i < delta.size(); i++)
		{
			_Tile last = tiles.get(i);
			Pair<Integer, Integer> offset = delta.get(i);
			tiles.add(last.delta(offset.getLeft(), offset.getRight()));
		}
		return new _TilePath(tiles.toArray(new _Tile[0]));
	}
}
