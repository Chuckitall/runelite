package net.runelite.client.plugins.fred.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.ObjectDefinition;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import org.apache.commons.lang3.ArrayUtils;

import java.util.stream.Collectors;

/**
 * Created by npruff on 8/25/2019.
 */
@Slf4j
public class WorldUtils
{
	private Client client;
	public WorldUtils(Client client)
	{
		this.client = client;
	}

	private boolean tileValidForMovement(WorldArea l1, int dx, int dy)
	{
		if (l1 == null)
		{
			return false;
		}
		return l1.canTravelInDirection(client, dx, dy);
	}

	/**
	 * Calculates the next area that will be occupied if this area attempts
	 * to move toward it by using the normal Player travelling pattern.
	 *
	 * @param target the target area
	 * @return the next occupied area
	 */
	public WorldArea calculateNextTravellingPointPlayer(WorldArea src, WorldArea target)
	{
		if (src.getPlane() != target.getPlane())
		{
			return null;
		}

		if (src.intersectsWith(target))
		{
			return src;
		}

		int dx = target.getX() - src.getX();
		int dy = target.getY() - src.getY();
		Point axisDistances = src.getAxisDistances(target);
		if (axisDistances.getX() + axisDistances.getY() == 1)
		{
			// NPC is in melee distance of target, so no movement is done
			return src;
		}

		LocalPoint lp = LocalPoint.fromWorld(client, src.getX(), src.getY());
		if (lp == null ||
				lp.getSceneX() + dx < 0 || lp.getSceneX() + dy >= Constants.SCENE_SIZE ||
				lp.getSceneY() + dx < 0 || lp.getSceneY() + dy >= Constants.SCENE_SIZE)
		{
			// Player is travelling out of the scene, so collision data isn't available
			return null;
		}

		int dxSig = Integer.signum(dx);
		int dySig = Integer.signum(dy);
		if (dxSig != 0 && dySig != 0 && src.canTravelInDirection(client, dxSig, dySig, x -> true))
		{
			return new WorldArea(src.getX() + dxSig, src.getY() + dySig, src.getWidth(), src.getHeight(), src.getPlane());
		}
		if (axisDistances.getY() > axisDistances.getX() && src.canTravelInDirection(client, 0, dySig, x -> true))
		{
			return new WorldArea(src.getX(), src.getY() + dySig, src.getWidth(), src.getHeight(), src.getPlane());
		}
		if (axisDistances.getX() > axisDistances.getY() && src.canTravelInDirection(client, dxSig, 0, x -> true))
		{
			return new WorldArea(src.getX() + dxSig, src.getY(), src.getWidth(), src.getHeight(), src.getPlane());
		}
		else if (dxSig != 0 && src.canTravelInDirection(client, dxSig, 0, x -> true))
		{
			return new WorldArea(src.getX() + dxSig, src.getY(), src.getWidth(), src.getHeight(), src.getPlane());
		}
		else if (dySig != 0 && src.canTravelInDirection(client, 0, dySig, x -> true))
		{
			return new WorldArea(src.getX(), src.getY() + dySig, src.getWidth(), src.getHeight(), src.getPlane());
		}
		// The NPC is stuck

		return null;
	}

	public ArrayList<WorldArea> getPathToObject(final GameObject o)
	{
		ArrayList<WorldArea> toReturn = new ArrayList<>();
		if (o == null || client.getGameState() != GameState.LOGGED_IN)
		{
			return toReturn;
		}
		Player p = client.getLocalPlayer();
		if (p == null || p.getWorldArea() == null || o.getWorldLocation() == null)
		{
			return toReturn;
		}

		final WorldArea target = new WorldArea(o.getWorldLocation(), 1, 1);
		WorldArea previous = p.getWorldArea();
		WorldArea incremental = calculateNextTravellingPointPlayer(previous, target);
		int steps = 0;
		while (incremental != previous)
		{
			toReturn.add(incremental);
			steps++;
			if (incremental == null)
			{
				steps = -1;
				toReturn.clear();
				break;
			}
			previous = incremental;
			if (steps > 10)
			{
				break;
			}
			incremental = calculateNextTravellingPointPlayer(previous, target);
		}
		if (steps == -1) return null;
		return toReturn;
		//log.debug("");
	}

	public int getPathDistanceToObject(final GameObject o)
	{
		if (o == null || client.getGameState() != GameState.LOGGED_IN)
		{
			return -1;
		}
		Player p = client.getLocalPlayer();
		if (p == null || p.getWorldArea() == null || o.getWorldLocation() == null)
		{
			return -1;
		}

		ArrayList<WorldArea> path = getPathToObject(o);
		if (path == null) return 99;
		return path.size();
	}

	private int bounded(int i, int min, int max)
	{
		return Math.min(Math.max(i, min), max);
	}

	public ArrayList<GameObject> getObjects(final int[] objectIDs, final int maxDist)
	{
		if (objectIDs == null || objectIDs.length == 0 || client.getGameState() != GameState.LOGGED_IN)
		{
			return null;
		}
		Player p = client.getLocalPlayer();
		if (p == null || p.getWorldArea() == null)
		{
			return null;
		}
		final Scene scene = client.getScene();
		final WorldPoint playerLocation = p.getWorldLocation();

		final Tile[][] tiles = scene.getTiles()[client.getPlane()];
		final ArrayList<GameObject> found = new ArrayList<>();
		WorldPoint pLocal = tiles[0][0].getWorldLocation();
		int xLoc = playerLocation.getX() - pLocal.getX();
		int yLoc = playerLocation.getY() - pLocal.getY();
		int xLower = bounded(xLoc - maxDist, 0, tiles.length);
		int xUpper = bounded(xLoc + maxDist, 0, tiles.length);
		int yLower = bounded(yLoc - maxDist, 0, tiles[0].length);
		int yUpper = bounded(yLoc + maxDist, 0, tiles[0].length);
//		log.debug("Player(x, y, z) -> ({},{},{})", playerLocation.getX(), playerLocation.getY(), playerLocation.getPlane());
		for (int x = xLower; x < xUpper; x++)
		{
			for (int y = yLower; y < yUpper; y++)
			{
				Tile tile = tiles[x][y];
//				log.debug("Tile(x, y, z) -> ({},{},{})", tile.getWorldLocation().getX(), tile.getWorldLocation().getY(), tile.getWorldLocation().getPlane());
				for (GameObject object : tile.getGameObjects())
				{
					if (object == null)
					{
						continue;
					}

					if (ArrayUtils.contains(objectIDs, object.getId()))
					{
						found.add(object);
						continue;
					}

					// Check impostors
					final ObjectDefinition comp = client.getObjectDefinition(object.getId());
					final ObjectDefinition impostor = comp.getImpostorIds() != null ? comp.getImpostor() : comp;

					if (impostor != null && ArrayUtils.contains(objectIDs, impostor.getId()))
					{
						found.add(object);
					}
				}
			}
		}
		if (found.size() == 0) return null;
		return found;
	}

	public <E> E getRandomElement(List<E> options)
	{
		return (options != null && options.size() > 0) ? options.get(Random.nextInt(0, options.size())) : null;
	}

	public GameObject getClosestObject(final int[] objectIDs, final int maxDist)
	{
		final ArrayList<GameObject> found = getObjects(objectIDs, maxDist);
		if (found == null) return null;

		Map<GameObject, Integer> distances = new HashMap<>();
		found.forEach((GameObject object) -> distances.put(object, this.getPathDistanceToObject(object)));
		if (distances.size() == 0)
		{
			return null;
		}
		int min = Collections.min(distances.values());
		List<GameObject> targets = distances.entrySet().stream().filter(f -> f.getValue() == min).map(Map.Entry::getKey).collect(Collectors.toList());
		return getRandomElement(targets);
	}
}
