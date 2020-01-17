package net.runelite.client.plugins.groovy.loader;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public abstract class BaseScript
{
	@Getter(AccessLevel.PROTECTED)
	private final ScriptContext ctx;

	abstract boolean startup();
	abstract boolean shutdown();
}
