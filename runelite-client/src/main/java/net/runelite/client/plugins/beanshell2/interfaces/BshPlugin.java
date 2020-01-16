package net.runelite.client.plugins.beanshell2.interfaces;


import java.util.List;
import net.runelite.client.plugins.beanshell2.interfaces.config.ConfigTypeBase;
import net.runelite.client.plugins.fred.api.other.Tuples.T2;

public interface BshPlugin
{
	BshContext getContext();
	List<T2<String, ConfigTypeBase<?>>> getConfigDefinition();
	void startup();
	void shutdown();
}
