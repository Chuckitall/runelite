package net.runelite.client.plugins.groovy.loader;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseScript
{
	@Getter(AccessLevel.PROTECTED)
	private final ScriptContext ctx;

	public BaseScript(ScriptContext ctx)
	{
		this.ctx = ctx;
	}

	@Getter(AccessLevel.PUBLIC)
	private boolean running = false;

	abstract boolean startup();
	abstract boolean shutdown();
	void _startup()
	{
		if (startup())
		{
			running = true;
		}
	}
	void _shutdown()
	{
		if (shutdown())
		{
			running = false;
		}
	}
}
