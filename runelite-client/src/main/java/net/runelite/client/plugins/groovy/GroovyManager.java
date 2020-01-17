package net.runelite.client.plugins.groovy;

import com.google.common.collect.ImmutableList;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

	private GroovyCore plugin;

	@Inject
	public GroovyManager(GroovyCore core)
	{
		this.plugin = core;
	}

	private final Map<Integer, T3<Boolean, GroovyPlugin, GroovyScriptPanel>> allPlugins = new HashMap<>();

	private GroovyPlugin loadPlugin(GroovyContext context)
	{
		GroovyPlugin plugin = null;

//			Interpreter i = new Interpreter();
//			i.set("context", context);
//			i.eval(__DEFAULT_IMPORTS__);
//			i.source(context.getResolvedName());
//			i.eval("GroovyContext getContext() { return context; }");
//			plugin = (GroovyPlugin) null;
		return plugin;
	}

	public int registerScript(GroovyContext context)
	{
		Optional<Entry<Integer, T3<Boolean, GroovyPlugin, GroovyScriptPanel>>> clash = allPlugins.entrySet().stream().filter(f -> f.getValue().get_2().getContext().getNamespace().equalsIgnoreCase(context.getNamespace())).findFirst();
		if (clash.isPresent())
		{
			if (clash.get().getValue().get_1())
			{
				clash.get().getValue().get_2().shutdown();
			}
			allPlugins.remove(clash.get().getKey());
		}
		GroovyPlugin plugin = loadPlugin(context);
		if (plugin == null)
		{
			return -1;
		}
		int uuid = getUUID();

		allPlugins.put(uuid, Tuples.of(false, plugin, buildJPanel(uuid, plugin.getContext().getNamespace(), false)));
		return uuid;
	}

	public List<GroovyScriptPanel> getScriptPanels()
	{
		return ImmutableList.copyOf(allPlugins.values().stream().map(T3::get_3).collect(Collectors.toList()));
	}

	public boolean isUuid(int uuid)
	{
		Optional<T3<Boolean, GroovyPlugin, GroovyScriptPanel>> entry = Optional.ofNullable(allPlugins.get(uuid));
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
		T3<Boolean, GroovyPlugin, GroovyScriptPanel> value = allPlugins.get(uuid);
		if (!isUuid(uuid) || value.get_1() == enable)
		{
			return;
		}
		log.info("uuid: {}, enable: {}, current: {}", uuid, enable, value.get_1());
		if (!value.get_1() && enable)
		{
			value.get_2().startup();
			allPlugins.replace(uuid, Tuples.of(true, value.get_2(), value.get_3()));
			allPlugins.get(uuid).get_3().setEnabled(true);
		}
		else
		{
			value.get_2().shutdown();
			allPlugins.replace(uuid, Tuples.of(false, value.get_2(), value.get_3()));
			allPlugins.get(uuid).get_3().setEnabled(false);
		}
		plugin.updateList();
	}

	public void clear()
	{
		allPlugins.forEach((key, value) -> enablePlugin(key, false));
		allPlugins.clear();
		GroovyManager.__OFFSET__ = 0;
	}

	public int reloadScript(int uuid)
	{
		if (!isUuid(uuid))
		{
			return -1;
		}
		boolean wasEnabled = isPluginEnabled(uuid);
		enablePlugin(uuid, false);
		Optional<Entry<Integer, T3<Boolean, GroovyPlugin, GroovyScriptPanel>>> value = allPlugins.entrySet().stream().filter(f -> f.getKey() == uuid).findFirst();
		if (value.isPresent())
		{
			T3<Boolean, GroovyPlugin, GroovyScriptPanel> v = value.get().getValue();
			allPlugins.replace(uuid, Tuples.of(false, loadPlugin(v.get_2().getContext()), v.get_3()));
			enablePlugin(uuid, wasEnabled);
			return uuid;
		}
		else
		{
			log.error("No script w/ uuid {} to reload", uuid);
			return -1;
		}
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
