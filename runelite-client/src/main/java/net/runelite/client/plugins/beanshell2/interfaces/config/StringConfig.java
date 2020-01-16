package net.runelite.client.plugins.beanshell2.interfaces.config;

import net.runelite.client.plugins.fred.api.other.Either;

public class StringConfig extends ConfigTypeBase<String>
{
	public StringConfig(String defaultValue)
	{
		super(defaultValue);
	}

	@Override
	Either<String, String> parse(String string)
	{
		return (string != null ? Either.left(string) : Either.right("String was null"));
	}
}
