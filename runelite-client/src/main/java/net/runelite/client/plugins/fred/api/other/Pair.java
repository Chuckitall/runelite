package net.runelite.client.plugins.fred.api.other;

import lombok.Value;

@Value(staticConstructor = "of")
public class Pair<T, U>
{
	T _1;
	U _2;
}