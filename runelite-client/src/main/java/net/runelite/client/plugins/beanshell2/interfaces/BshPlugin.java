package net.runelite.client.plugins.beanshell2.interfaces;


public interface BshPlugin
{
	BshContext getContext();
	void startup();
	void shutdown();
}
