package net.runelite.client.plugins.fred.api.wrappers;

import java.awt.Polygon;
import java.awt.Rectangle;
import lombok.Value;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.plugins.fred.api.interfaces._TileContainer;

@Value
public class _TilePath implements _TileContainer
{
	private _Tile[] tiles;

	public _Tile getNextTile(_Tile location, int delta)
	{
		_Tile toRet = null;
		for (int i = tiles.length - 1; i >= 0; i--)
		{
			if (tiles[i].distanceTo(location) < delta)
			{
				toRet = tiles[i];
				break;
			}
		}
		return toRet;
	}

	public _Tile getNextTile(Client client)
	{
		Rectangle bounds = client.getWidget(164, 5) != null ? client.getWidget(164, 5).getBounds() : null;
		if (bounds == null)
		{
			return null;
		}
		_Tile toRet = null;
		for (int i = tiles.length - 1; i >= 0; i--)
		{
			LocalPoint point = tiles[i].toLocalPoint(client);
			Polygon poly = (point != null) ? Perspective.getCanvasTilePoly(client, point) : null;
			Rectangle b2 = (poly != null) ? poly.getBounds() : null;
			if (b2 != null && bounds.contains(b2))
			{
				toRet = tiles[i];
				break;
			}
		}
		return toRet;
	}

}
