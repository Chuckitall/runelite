package net.runelite.client.plugins.fred.api.wrappers;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import net.runelite.api.GameObject;
import net.runelite.api.ObjectDefinition;
import net.runelite.client.RuneLite;
import net.runelite.client.plugins.fred.api.interfaces.I_GameObject;
import net.runelite.client.plugins.fred.api.other.Tuples;
import net.runelite.client.plugins.fred.api.other.Tuples.T2;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@ToString
@EqualsAndHashCode
public class _GameObject implements I_GameObject
{
	int id;
	T2<Integer, Integer> position;
	int impostorId;
	String name;

	public _GameObject(int id, T2<Integer, Integer> pos)
	{
		this.id = id;
		this.position = pos;
		this.impostorId = RuneLite.getClient().map(f -> f.getObjectDefinition(id)).filter(f -> f.getImpostorIds() != null).map(ObjectDefinition::getImpostor).map(ObjectDefinition::getId).orElse(-1);
		this.name = RuneLite.getClient().map(f -> f.getObjectDefinition(id).getName()).orElse("ERROR");
	}

	public static _GameObject from(GameObject obj)
	{
		if (obj != null)
		{
			return new _GameObject(obj.getId(), Tuples.of(obj.getSceneMinLocation().getX(), obj.getSceneMinLocation().getY()));
		}
		return null;
	}

	@Override
	public int getId()
	{
		return id;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public int getImposterId()
	{
		return impostorId;
	}

	@Override
	public T2<Integer, Integer> getPos()
	{
		return position;
	}
}
