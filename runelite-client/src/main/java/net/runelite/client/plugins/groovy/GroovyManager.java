package net.runelite.client.plugins.groovy;

import com.google.common.collect.ImmutableList;
import groovy.lang.GroovyClassLoader;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.fred.api.other.Tuples;
import net.runelite.client.plugins.fred.api.other.Tuples.T3;
import net.runelite.client.plugins.groovy.interfaces.GroovyContext;
import net.runelite.client.plugins.groovy.interfaces.GroovyPlugin;
import net.runelite.client.plugins.groovy.ui.GroovyPanel;
import net.runelite.client.plugins.groovy.ui.GroovyScriptPanel;

@Singleton
@Slf4j
public class GroovyManager
{
	private static int __OFFSET__ = 0;

	private static String __DEFAULT_FUNCTIONS__ = "\n" +
		"\n" +
		"";

	private static int getUUID()
	{
		return __OFFSET__++;
	}

	private GroovyCore core;

	@Inject
	public GroovyManager(GroovyCore core)
	{
		this.core = core;
	}

	private final List<T3<Boolean, GroovyPlugin, GroovyScriptPanel>> allPlugins = new ArrayList<>();

	private GroovyPlugin loadPlugin(GroovyContext context)
	{
		log.debug("loading plugin {} w/ main file {} @ root {}", context.getName(), context.getMainFile(), core.getGroovyRoot());
		String fileName = context.getMainFile() + ".groovy";
		String path = core.getGroovyRoot() + context.getName();
//		String resolvedName = path + fileName;
		GroovyPlugin plugin = null;
		GroovyClassLoader gcl = new GroovyClassLoader();
		Arrays.stream(gcl.getLoadedClasses()).filter(f -> f.getPackageName().contains("plugins")).forEach(f -> log.debug("Class -> {}\t\t{}", f.getName(), f.getCanonicalName()));
		//		gcl.addClasspath(path);

		try
		{
			Class clazz = gcl.parseClass(new File(path, fileName)) ;
			Object inst = clazz.newInstance();
			if (inst instanceof  GroovyPlugin)
			{
				plugin = (GroovyPlugin) inst;
				plugin.setContext(context);
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
		return plugin;
	}

	public int registerScript(GroovyContext context)
	{
		Optional<T3<Boolean, GroovyPlugin, GroovyScriptPanel>> clash_o = allPlugins.stream().filter(f -> f.get_2().getContext().getName().equalsIgnoreCase(context.getName())).findFirst();
		int newUUID = clash_o.map(f -> f.get_2().getContext().getUuid()).orElse(-1);
		if (clash_o.isPresent())
		{
			T3<Boolean, GroovyPlugin, GroovyScriptPanel> conflict = clash_o.get();
			if (conflict.get_1())
			{
				conflict.get_2().shutdown();
			}
			allPlugins.remove(conflict);
		}
		context.setUuid(-1);
		GroovyPlugin plugin = loadPlugin(context);
		if (plugin == null)
		{
			return -1;
		}
		plugin.getContext().setUuid(getUUID());
		allPlugins.add(Tuples.of(false, plugin, buildJPanel(plugin.getContext().getUuid(), plugin.getContext().getName(), false)));
		return plugin.getContext().getUuid();
	}

	public List<GroovyScriptPanel> getScriptPanels()
	{
		return ImmutableList.copyOf(allPlugins.stream().map(T3::get_3).collect(Collectors.toList()));
	}

	public boolean isUuid(int uuid)
	{
		Optional<T3<Boolean, GroovyPlugin, GroovyScriptPanel>> entry =
			allPlugins.stream().filter(f -> f.get_2().getContext().getUuid() == uuid).findFirst();
		return entry.isPresent();
	}

	public boolean isPluginEnabled(int uuid)
	{
		if (!isUuid(uuid))
		{
			return false;
		}
		return allPlugins.stream().filter(f -> f.get_2().getContext().getUuid() == uuid).findFirst().get().get_1();
	}

	private T3<Boolean, GroovyPlugin, GroovyScriptPanel> getPlugin(int uuid)
	{
		return allPlugins.stream().filter(f -> f.get_2().getContext().getUuid() == uuid).findFirst().orElse(null);
	}

	public void enablePlugin(int uuid, boolean enable)
	{
		T3<Boolean, GroovyPlugin, GroovyScriptPanel> state = getPlugin(uuid);
		if (!isUuid(uuid) || state == null || state.get_1() == enable)
		{
			return;
		}
		log.info("uuid: {}, enable: {}, current: {}", uuid, enable, state.get_1());
		allPlugins.remove(state);
		boolean wasEnabled = state.get_1();
		if (!wasEnabled && enable)
		{
			state.get_2().startup();
//			allPlugins.add(Tuples.of(true, state.get_2(), state.get_3()));
//			allPlugins.replace(uuid, Tuples.of(true, value.get_2(), value.get_3()));
//			state.get_3().setEnabled(true);
		}
		else
		{
			state.get_2().shutdown();
//			allPlugins.replace(uuid, Tuples.of(false, value.get_2(), value.get_3()));
//			state.get_3().setEnabled(false);
		}
		state.get_3().setEnabled(enable);
		allPlugins.add(Tuples.of(enable, state.get_2(), state.get_3()));
		core.updateList();
	}

	public void clear()
	{
		allPlugins.stream().filter(data -> data.get_2().getContext() != null).forEach(data -> enablePlugin(data.get_2().getContext().getUuid(), false));
		allPlugins.clear();
		GroovyManager.__OFFSET__ = 0;
	}

	public int reloadScript(int uuid)
	{
		if (!isUuid(uuid))
		{
			return -1;
		}
		T3<Boolean, GroovyPlugin, GroovyScriptPanel> state = getPlugin(uuid);
		boolean wasEnabled = state.get_1();
		enablePlugin(uuid, false);
		state = getPlugin(uuid);
		allPlugins.remove(state);
		allPlugins.add(Tuples.of(false, loadPlugin(state.get_2().getContext()), state.get_3()));
		enablePlugin(uuid, wasEnabled);
		return uuid;
	}

	public GroovyScriptPanel buildJPanel(int uuid, String name, boolean enabled)
	{
		GroovyScriptPanel toRet = new GroovyScriptPanel(uuid, name);
		toRet.setEnabled(enabled);
		toRet.gear.setIcon(GroovyPanel.GEAR_ICON);
		toRet.avatar.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				Arrays.stream(toRet.getMouseListeners()).forEach(f -> f.mousePressed(e));
				super.mousePressed(e);
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				Arrays.stream(toRet.getMouseListeners()).forEach(f -> f.mouseReleased(e));
				super.mouseReleased(e);
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				Arrays.stream(toRet.getMouseListeners()).forEach(f -> f.mouseEntered(e));
				super.mouseEntered(e);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				Arrays.stream(toRet.getMouseListeners()).forEach(f -> f.mouseExited(e));
				super.mouseExited(e);
			}
			@Override
			public void mouseClicked(MouseEvent e)
			{
				enablePlugin(toRet.getUuid(), !isPluginEnabled(toRet.getUuid()));
				super.mouseClicked(e);
			}
		});
		toRet.titleLabel.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				Arrays.stream(toRet.getMouseListeners()).forEach(f -> f.mousePressed(e));
				super.mousePressed(e);
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				Arrays.stream(toRet.getMouseListeners()).forEach(f -> f.mouseReleased(e));
				super.mouseReleased(e);
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				Arrays.stream(toRet.getMouseListeners()).forEach(f -> f.mouseEntered(e));
				super.mouseEntered(e);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				Arrays.stream(toRet.getMouseListeners()).forEach(f -> f.mouseExited(e));
				super.mouseExited(e);
			}

			@Override
			public void mouseClicked(MouseEvent e)
			{
				reloadScript(toRet.getUuid());
			}
		});
		toRet.gear.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				Arrays.stream(toRet.getMouseListeners()).forEach(f -> f.mousePressed(e));
				super.mousePressed(e);
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				Arrays.stream(toRet.getMouseListeners()).forEach(f -> f.mouseReleased(e));
				super.mouseReleased(e);
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				Arrays.stream(toRet.getMouseListeners()).forEach(f -> f.mouseEntered(e));
				super.mouseEntered(e);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				Arrays.stream(toRet.getMouseListeners()).forEach(f -> f.mouseExited(e));
				super.mouseExited(e);
			}

			@Override
			public void mouseClicked(MouseEvent e)
			{
				log.debug("Open Config window for script: {}", toRet.getUuid());
				//openConfigWindow();
			}
		});
		return toRet;
	}
}
