package net.runelite.client.plugins.beanshell2;

import bsh.EvalError;
import bsh.Interpreter;
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
import net.runelite.client.plugins.beanshell2.interfaces.BshContext;
import net.runelite.client.plugins.beanshell2.interfaces.BshPlugin;
import net.runelite.client.plugins.beanshell2.ui.BshPluginPanel;
import net.runelite.client.plugins.fred.api.other.Tuples;
import net.runelite.client.plugins.fred.api.other.Tuples.T3;

import static net.runelite.client.plugins.beanshell2.ui.BshCorePanel.GEAR_ICON;

@Singleton
@Slf4j
public class BshManager
{
	private static int __OFFSET__ = 0;
	private static String __DEFAULT_IMPORTS__ = "\n" +
		"import net.runelite.client.events.*;\n" +
		"import net.runelite.api.util.*;\n" +
		"import net.runelite.api.events.*;\n" +
		"\n" +
		"import io.reactivex.functions.*;\n" +
		"\n" +
		"import net.runelite.client.plugins.fred4106.api.other.*;\n" +
		"import net.runelite.client.plugins.beanshell2.interfaces.*;\n" +
		"\n" +
		"import net.runelite.api.Client;\n" +
		"import net.runelite.client.eventbus.EventBus;\n" +
		"import net.runelite.client.ui.overlay.OverlayManager;\n" +
		"import net.runelite.client.menus.MenuManager;\n" +
		"\n" +
		"import net.runelite.client.plugins.fred.api.other.Tuples.T1;\n" +
		"import net.runelite.client.plugins.fred.api.other.Tuples.T2;\n" +
		"import net.runelite.client.plugins.fred.api.other.Tuples.T3;\n" +
		"import net.runelite.client.plugins.fred.api.other.Tuples.T4;\n" +
		"import net.runelite.client.plugins.fred.api.other.Tuples.T5;\n" +
		"import net.runelite.client.plugins.fred.api.other.Tuples.T6;\n" +
		"\n" +
		"";

	private static String __DEFAULT_FUNCTIONS__ = "\n" +
		"BshContext getContext() { return context; }\n" +
		"";

	private static int getUUID()
	{
		return __OFFSET__++;
	}

	private BshCore plugin;

	@Inject
	public BshManager(BshCore core)
	{
		this.plugin = core;
	}

	private final Map<Integer, T3<Boolean, BshPlugin, BshPluginPanel>> allPlugins = new HashMap<>();

	private BshPlugin loadPlugin(BshContext context)
	{
		BshPlugin plugin = null;

		try
		{
			Interpreter i = new Interpreter();
			i.set("context", context);
			i.eval(__DEFAULT_IMPORTS__);
			i.eval(__DEFAULT_FUNCTIONS__);
			i.source(context.getResolvedName());
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
		return plugin;
	}

	public int registerScript(BshContext context)
	{
		Optional<Entry<Integer, T3<Boolean, BshPlugin, BshPluginPanel>>> clash = allPlugins.entrySet().stream().filter(f -> f.getValue().get_2().getContext().getNamespace().equalsIgnoreCase(context.getNamespace())).findFirst();
		if (clash.isPresent())
		{
			if (clash.get().getValue().get_1())
			{
				clash.get().getValue().get_2().shutdown();
			}
			allPlugins.remove(clash.get().getKey());
		}
		BshPlugin plugin = loadPlugin(context);
		if (plugin == null)
		{
			return -1;
		}
		int uuid = getUUID();

		allPlugins.put(uuid, Tuples.of(false, plugin, buildJPanel(uuid, plugin.getContext().getNamespace(), false)));
		return uuid;
	}

	public List<BshPluginPanel> getScriptPanels()
	{
		return ImmutableList.copyOf(allPlugins.values().stream().map(T3::get_3).collect(Collectors.toList()));
	}

	public boolean isUuid(int uuid)
	{
		Optional<T3<Boolean, BshPlugin, BshPluginPanel>> entry = Optional.ofNullable(allPlugins.get(uuid));
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
		T3<Boolean, BshPlugin, BshPluginPanel> value = allPlugins.get(uuid);
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
		BshManager.__OFFSET__ = 0;
	}

	public int reloadScript(int uuid)
	{
		if (!isUuid(uuid))
		{
			return -1;
		}
		boolean wasEnabled = isPluginEnabled(uuid);
		enablePlugin(uuid, false);
		Optional<Entry<Integer, T3<Boolean, BshPlugin, BshPluginPanel>>> value = allPlugins.entrySet().stream().filter(f -> f.getKey() == uuid).findFirst();
		if (value.isPresent())
		{
			T3<Boolean, BshPlugin, BshPluginPanel> v = value.get().getValue();
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

	public BshPluginPanel buildJPanel(int uuid, String name, boolean enabled)
	{
		BshPluginPanel toRet = new BshPluginPanel(uuid, name);
		toRet.setEnabled(enabled);
		toRet.gear.setIcon(GEAR_ICON);
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
