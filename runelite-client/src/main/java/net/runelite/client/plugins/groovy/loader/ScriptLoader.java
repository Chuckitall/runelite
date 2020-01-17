package net.runelite.client.plugins.groovy.loader;

import com.google.inject.Singleton;
import groovy.lang.GroovyClassLoader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.plugins.groovy.GroovyCore;
import net.runelite.client.ui.overlay.OverlayManager;

@Singleton
@Slf4j
public class ScriptLoader
{
	@Inject
	private Client client;

	@Inject
	private EventBus eventBus;

	@Inject
	private MenuManager menuManager;

	@Inject
	private OverlayManager overlayManager;

	private final HashMap<ScriptIdent, BaseScript> scriptCache = new HashMap<>();

	private BaseScript loadPlugin(ScriptIdent ident)
	{
		log.debug("loading plugin {} w/ main file {} @ root {}", ident.getScriptPackage(), ident.getMainFile(), GroovyCore.getGroovyRoot());
		String path = GroovyCore.getGroovyRoot() + ident.getScriptPackage();
		String fileName = ident.getMainFile() + ".groovy";
		BaseScript plugin = null;
		GroovyClassLoader gcl = new GroovyClassLoader();

		try
		{
			Class clazz = gcl.parseClass(new File(path, fileName));
			Object inst = clazz.getDeclaredConstructor(ScriptContext.class).newInstance(new ScriptContext(client, menuManager, overlayManager, eventBus));
			if (inst instanceof  BaseScript)
			{
				plugin = (BaseScript) inst;
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
		catch (InstantiationException e)
		{
			e.printStackTrace();
		}
		catch (NoSuchMethodException e)
		{
			e.printStackTrace();
		}
		catch (InvocationTargetException e)
		{
			e.printStackTrace();
		}
		return plugin;
	}

	public ScriptIdent registerScript(String folder, String file)
	{
		ScriptIdent ident = new ScriptIdent(folder, file);
		if (scriptCache.containsKey(ident))
		{
			log.error("Attempting too register duplicate [{}, {}]", folder, file);
			return null;
		}
		BaseScript plugin = loadPlugin(ident);
		if (plugin != null)
		{
			scriptCache.put(ident, plugin);
			return ident;
		}
		return null;
	}

	public boolean reloadScript(ScriptIdent ident)
	{
		if (!scriptCache.containsKey(ident))
		{
			return false;
		}
		BaseScript plugin = loadPlugin(ident);
		if (plugin != null)
		{
			if (scriptCache.get(ident).isRunning())
			{
				scriptCache.get(ident)._shutdown();
				plugin._startup();
			}
			scriptCache.replace(ident, plugin);
			return true;
		}
		return false;
	}
}
