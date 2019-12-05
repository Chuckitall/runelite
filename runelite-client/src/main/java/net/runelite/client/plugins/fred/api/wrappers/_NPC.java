package net.runelite.client.plugins.fred.api.wrappers;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.NPCDefinition;
import net.runelite.api.ObjectDefinition;
import net.runelite.client.RuneLite;
import net.runelite.client.plugins.fred.api.other.Tuples;
import net.runelite.client.plugins.fred.api.other.Tuples.T2;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@ToString
@EqualsAndHashCode
public class _NPC
{
	int idx;
	public _NPC(int idx)
	{
		this.idx = idx;
	}

	public static _NPC from(NPC npc)
	{
		if (npc != null)
		{
			return new _NPC(npc.getIndex());
		}
		return null;
	}

	public int getIdx()
	{
		return idx;
	}

	public int getId()
	{
		return RuneLite.getClient().map(Client::getCachedNPCs).map(f -> f[idx]).map(NPC::getId).orElse(-1);
	}

//	public T2<Integer, Integer> getPos()
//	{
//		return RuneLite.getClient().map(Client::getCachedNPCs).map(f -> f[idx]).map(NPC::getLocalLocation).map(f -> Tuples.of(f.getSceneX(), f.getSceneY())).orElse(Tuples.of(-1, -1));
//	}
	public _Tile getPos()
	{
		return RuneLite.getClient().map(Client::getCachedNPCs).map(f -> f[idx]).map(NPC::getWorldLocation).map(_Tile::fromWorld).orElse(null);
	}

	public String getName()
	{
		return RuneLite.getClient().map(Client::getCachedNPCs).map(f -> f[idx]).map(NPC::getName).orElse("ERROR");
	}

	public int getLevel()
	{
		return RuneLite.getClient().map(Client::getCachedNPCs).map(f -> f[idx]).map(NPC::getCombatLevel).orElse(-1);
	}

	public boolean isDead()
	{
		return RuneLite.getClient().map(Client::getCachedNPCs).map(f -> f[idx]).map(NPC::isDead).orElse(true);
	}
}