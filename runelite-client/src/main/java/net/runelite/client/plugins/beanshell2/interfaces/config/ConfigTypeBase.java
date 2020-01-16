package net.runelite.client.plugins.beanshell2.interfaces.config;

import io.reactivex.functions.Function;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.fred.api.other.Either;

@Slf4j
public abstract class ConfigTypeBase<E>
{
	private final E defaultValue;

	public ConfigTypeBase(E defaultValue)
	{
		this.defaultValue = defaultValue;
	}

	abstract Either<E, String> parse(String string);

	public final E getValue(String string)
	{
		return parse(string).map(
			(e) -> {
				log.debug("e -> {}", e);
				return e;
			},
			(s) -> {
				log.debug(s);
				return defaultValue;
			}
		);
	}
}
