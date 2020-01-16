package net.runelite.client.plugins.beanshell2.interfaces.config;

import net.runelite.client.plugins.fred.api.other.Either;

public class IntConfig extends ConfigTypeBase<Integer>
{
	public IntConfig(Integer defaultValue)
	{
		super(defaultValue);
	}

	@Override
	Either<Integer, String> parse(String string)
	{
		Either<Integer, String> e;
		try
		{
			int v = Integer.parseInt(string);
			e = Either.left(v);
		}
		catch (NumberFormatException ignored)
		{
			e = Either.right("could not parse \"" + string + "\" to an int");
		}
		return e;
	}
}
