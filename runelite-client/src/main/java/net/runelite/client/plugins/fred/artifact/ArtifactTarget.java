package net.runelite.client.plugins.fred.artifact;

import com.google.common.collect.Comparators;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.Locatable;
import net.runelite.api.NPC;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.queries.NPCQuery;

@Getter(AccessLevel.PACKAGE)
public enum ArtifactTarget
{
	NO_TASK(0, 1844, 3748 ,0, 6972, "Get task from Captain Khaled."),
	NORTH(1, 1767, 3750, 0, 27771, "Steal from the northern house."),
	SOUTH_EAST(2, 1773, 3730, 1, 27772, "Steal from the south-eastern house."),
	SOUTH(3, 1764, 3735, 1, 27773, "Steal from the southern house."),
	SOUTH_WEST(4, 1749, 3735, 1, 27774, "Steal from the south-western house."),
	WEST(5, 1747, 3749, 1, 27775, "Steal from the western house."),
	NORTH_WEST(6, 1750, 3763, 1, 27776, "Steal from the north-western house."),
	FAILED(7, 1844, 3748 ,0, 6972, "Tell Captain Khaled you failed."),
	SUCCESS(8, 1844, 3748 ,0, 6972, "Return artifact to Captain Khaled.");

	private static final Map<Integer, ArtifactTarget> byId = buildIdMap();

	static ArtifactTarget getByState(int id)
	{
		return byId.get(id);
	}

	private static Map<Integer, ArtifactTarget> buildIdMap()
	{
		Map<Integer, ArtifactTarget> byId = new HashMap<>();
		for (ArtifactTarget c : values())
		{
			byId.put(c.id, c);
		}
		return byId;
	}

	private final int id;
	private final int targetX;
	private final int targetY;
	private final int targetZ;
	private final int targetId;
	private final String text;
	private final boolean isTheftState;

	ArtifactTarget(int state, int x, int y, int z, int id, String text)
	{
		this.id = state;
		this.targetX = x;
		this.targetY =  y;
		this.targetZ = z;
		this.targetId = id;
		this.text = text;
		this.isTheftState = state > 0 && state < 7;
	}

	Optional<GameObject> getTargetObject(Client client)
	{
		GameObject toRet = null;
		if (isTheftState)
		{
			if (targetZ == client.getPlane())
			{
				toRet = new GameObjectQuery().idEquals(targetId).result(client).nearestTo(client.getLocalPlayer());
			}
			else
			{
				toRet = new GameObjectQuery().idEquals(27634).filter(f -> Math.abs(f.getWorldLocation().getX() - targetX) < 10 && Math.abs(f.getWorldLocation().getY() - targetY) < 10).result(client).list.stream().min(Comparator.comparingInt(j -> j.getWorldLocation().distanceTo(new WorldPoint(targetX, targetY, client.getPlane())))).orElse(null);
			}
		}
		else if (client.getPlane() != targetZ)
		{
			toRet = new GameObjectQuery().idEquals(27635).result(client).nearestTo(client.getLocalPlayer());
		}
		return Optional.ofNullable(toRet);
	}

	Optional<NPC> getTargetNPC(Client client)
	{
		NPC toRet = null;
		if (!isTheftState && client.getPlane() == targetZ)
		{
			toRet = new NPCQuery().idEquals(targetId).result(client).nearestTo(client.getLocalPlayer());
		}
		return Optional.ofNullable(toRet);
	}

	WorldPoint getMarkerPoint(Client client)
	{
		return getTargetObject(client).map(GameObject::getWorldLocation).orElse(getTargetNPC(client).map(NPC::getWorldLocation).orElse(new WorldPoint(targetX, targetY, targetZ)));
	}
}
