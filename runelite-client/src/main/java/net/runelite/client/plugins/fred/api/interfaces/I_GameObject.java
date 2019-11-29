package net.runelite.client.plugins.fred.api.interfaces;

import net.runelite.client.plugins.fred.api.other.Tuples.T2;

public interface I_GameObject
{
	int getId();
	String getName();
	int getImposterId();
	T2<Integer, Integer> getPos();
}
