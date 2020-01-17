package net.runelite.client.plugins.groovy.interfaces;


import java.util.List;
import net.runelite.client.plugins.fred.api.other.Tuples.T2;

public interface GroovyPlugin
{
	GroovyContext getContext();
	List<T2<String, String>> getConfigDefinition();
	void startup();
	void shutdown();
}
