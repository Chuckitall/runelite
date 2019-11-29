package net.runelite.client.plugins.fred.api.wrappers;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

@Value(staticConstructor = "at")
@Slf4j
public class _Tile
{
	final int x;
	final int y;
	final int z;

	public _Tile dx(int o)
	{
		return _Tile.at(x + o, y, z);
	}

	public _Tile dy(int o)
	{
		return _Tile.at(x, y + o, z);
	}

	public _Tile delta(int x, int y)
	{
		return _Tile.at(this.x + x, this.y + y, this.z);
	}

	public int distanceTo(_Tile other)
	{
		if (other.getZ() != this.getZ())
		{
			return -1;
		}
		int xDif = Math.abs(other.getX() - this.getX());
		int yDif = Math.abs(other.getY() - this.getY());
		int minorDif = Math.min(xDif, yDif);
		int majorDif = Math.max(xDif, yDif);
		//		log.debug("{} <- ({}, {}), ({}, {})", dif, xDif, yDif, minorDif, majorDif);
		return minorDif + (majorDif - minorDif);
	}

	public static _Tile at(int x, int y)
	{
		return new _Tile(x, y, 0);
	}

	public static _Tile ofPlayer(Client client)
	{
		if (client.getLocalPlayer() == null)
		{
			return null;
		}
		return fromWorld(client.getLocalPlayer().getWorldLocation());
	}

	public static _Tile fromGameObject(GameObject o)
	{
		if (o == null)
		{
			return null;
		}
		return _Tile.fromWorld(o.getWorldLocation());
	}

	public static _Tile fromWorld(WorldPoint p)
	{
		if (p == null)
		{
			return null;
		}
		return _Tile.at(p.getX(), p.getY(), p.getPlane());
	}

	public WorldPoint toWorldPoint()
	{
		return new WorldPoint(x, y, z);
	}

	public LocalPoint toLocalPoint(Client client)
	{
		if (client == null)
		{
			return null;
		}
		WorldPoint p = this.toWorldPoint();
		if (p.isInScene(client))
		{
			return LocalPoint.fromWorld(client, p);
		}
		return null;
	}
}
