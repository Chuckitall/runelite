package net.runelite.client.plugins.beanshell2.interfaces;

import lombok.AccessLevel;
import lombok.Getter;

public interface BshPlugin
{
	BshContext getContext();
	void startup();
	void shutdown();
}
