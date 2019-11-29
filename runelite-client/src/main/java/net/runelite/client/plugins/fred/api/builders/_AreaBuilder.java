package net.runelite.client.plugins.fred.api.builders;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Data;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.fred.api.wrappers._Area;
import net.runelite.client.plugins.fred.api.wrappers._Tile;


@Slf4j
public class _AreaBuilder
{
	@Data
	public static class _ComplexAreaBuilder
	{
		private _Tile startTile = null;
		private int ptr = 0;
		private final List<_Line> lines = Lists.newArrayList();

		public _ComplexAreaBuilder startingAt(_Tile tile)
		{
			if (startTile == null)
			{
				this.startTile = tile;
			}
			return this;
		}

		public _ComplexAreaBuilder column(int startOffset, int length)
		{
			if (startTile != null)
			{
				_Line line = _Line.of(startTile.dx(ptr).dy(startOffset), length);
				lines.add(line);
				ptr++;
			}
			return this;
		}

		public _ComplexAreaBuilder disjointed(int startOffset, int length)
		{
			if (startTile != null)
			{
				_Line line = _Line.of(startTile.dx(ptr).dy(startOffset), length);
				lines.add(line);
			}
			return this;
		}

		public _Area build()
		{
			if (startTile != null)
			{
				Set<_Tile> tiles = new HashSet<>();
				lines.forEach(f -> tiles.addAll(Arrays.asList(f.getTiles())));
				return new _Area(tiles.toArray(new _Tile[0]));
			}
			else
			{
				return new _Area(new _Tile[0]);
			}
		}

		//start tile projected y+ by length
		@Value(staticConstructor = "of")
		private static class _Line
		{
			private _Tile startTile;
			private int length;
			_Tile[] getTiles()
			{
				_Tile[] toRet = new _Tile[length];
				int ptr = 0;
				do
				{
					toRet[ptr] = _Tile.at(startTile.getX(), startTile.getY() + ptr, startTile.getZ());
				}
				while (++ptr < length);
				return toRet;
			}
		}
	}

	@Data
	public static class _RectAreaBuilder
	{
		private _Tile t1 = null;
		private _Tile t2 = null;

		public _RectAreaBuilder from(_Tile tile)
		{
			if (t1 == null)
			{
				this.t1 = tile;
			}
			return this;
		}

		public _RectAreaBuilder to(_Tile tile)
		{
			if (t1 != null && t2 == null && t1.getZ() == tile.getZ())
			{
				this.t2 = tile;
			}
			return this;
		}

		public _Area build()
		{
			if (t1 == null || t2 == null)
			{
				return new _Area(new _Tile[0]);
			}
			int zed = t1.getZ();
			int maxX = Math.max(t1.getX(), t2.getX());
			int maxY = Math.max(t1.getY(), t2.getY());

			int minX = Math.min(t1.getX(), t2.getX());
			int minY = Math.min(t1.getY(), t2.getY());
			int width  = 1 + (maxX - minX);
			int height = 1 + (maxY - minY);
			_Tile[] tiles = new _Tile[width * height];
			for (int i = 0; i < width; i++)
			{
				for (int j = 0; j < height; j++)
				{
					tiles[(i * height) + j] = _Tile.at(minX + i, minY + j, zed);
				}
			}
			return new _Area(tiles);
		}

//		public _Area build()
//		{
//			if (startTile != null)
//			{
//				Set<_Tile> tiles = new HashSet<>();
//				lines.forEach(f -> tiles.addAll(Arrays.asList(f.getTiles())));
//				return new _Area(tiles.toArray(new _Tile[0]));
//			}
//			else
//			{
//				return new _Area(new _Tile[0]);
//			}
//		}

		//start tile projected y+ by length
	}

}
