package net.runelite.client.plugins.beanshell2;

import bsh.EvalError;
import bsh.Interpreter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.beanshell2.interfaces.BshContext;
import net.runelite.client.plugins.beanshell2.interfaces.BshPlugin;
import net.runelite.client.plugins.fred.api.other.Tuples;
import net.runelite.client.plugins.fred.api.other.Tuples.T2;

@Singleton
@Slf4j
public class BshManager
{
	private static int __OFFSET__ = 0;

	private static int getUUID()
	{
		return __OFFSET__++;
	}

	private BshCore plugin;

	private final Map<Integer, T2<Boolean, BshPlugin>> allPlugins = new HashMap<>();

	public int registerScript(String resolvedName, BshContext context)
	{
		Optional<Entry<Integer, T2<Boolean, BshPlugin>>> clash = allPlugins.entrySet().stream().filter(f -> f.getValue().get_2().getContext().getNamespace().equalsIgnoreCase(context.getNamespace())).findFirst();
		if (clash.isPresent())
		{
			if (clash.get().getValue().get_1())
			{
				clash.get().getValue().get_2().shutdown();
			}
			allPlugins.remove(clash.get().getKey());
		}
		BshPlugin plugin = null;
		try
		{
			Interpreter i = new Interpreter();
			i.set("context", context);
			i.eval("import net.runelite.api.Client;\n" +
				"import net.runelite.client.eventbus.EventBus;\n" +
				"import net.runelite.client.ui.overlay.OverlayManager;\n" +
				"import net.runelite.client.menus.MenuManager;\n" +
				"import net.runelite.client.plugins.beanshell2.interfaces.*;\n" +
				"import net.runelite.client.events.*;\n" +
				"import net.runelite.api.events.*;");
			i.source(resolvedName);
			//i.eval("BshContext getContext() { return context; }");
			plugin = (BshPlugin) i.getInterface(BshPlugin.class);
		}
		catch (EvalError evalError)
		{
			evalError.printStackTrace();
		}
		catch (FileNotFoundException fnf)
		{
			fnf.printStackTrace();
		}
		catch (IOException io)
		{
			io.printStackTrace();
		}

		if (plugin == null)
		{
			return -1;
		}
		int uuid = getUUID();

		allPlugins.put(uuid, Tuples.of(false, plugin));
		return uuid;
	}

	public boolean isUuid(int uuid)
	{
		Optional<T2<Boolean, BshPlugin>> entry = Optional.ofNullable(allPlugins.get(uuid));
		return entry.isPresent();
	}

	public boolean isPluginEnabled(int uuid)
	{
		if (!isUuid(uuid))
		{
			return false;
		}
		return allPlugins.get(uuid).get_1();
	}

	public void enablePlugin(int uuid, boolean enable)
	{
		T2<Boolean, BshPlugin> value = allPlugins.get(uuid);
		if (!isUuid(uuid) || value.get_1() == enable)
		{
			return;
		}
		log.debug("uuid: {}, enable: {}, current: {}", uuid, enable, value.get_1());
		if (!value.get_1() && enable)
		{
			value.get_2().startup();
			allPlugins.replace(uuid, Tuples.of(true, value.get_2()));
		}
		else
		{
			value.get_2().shutdown();
			allPlugins.replace(uuid, Tuples.of(false, value.get_2()));
		}
	}

	public void clear()
	{
		allPlugins.forEach((key, value) -> enablePlugin(key, false));
		allPlugins.clear();
		BshManager.__OFFSET__ = 0;
	}
}
