package net.runelite.client.plugins.fredexperimental.beanshell;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.fred.api.other.Tuples;
import net.runelite.client.plugins.fred.api.other.Tuples.T3;
import net.runelite.client.plugins.fredexperimental.beanshell.panel.ScriptPanel;

@Singleton
@Slf4j
public class BeanshellManager
{
	private static int __OFFSET__ = 0;

	private static int getUUID()
	{
		return __OFFSET__++;
	}

	private BeanshellPlugin plugin;

	@Inject
	public BeanshellManager(BeanshellPlugin plugin)
	{
		this.plugin = plugin;
	}

	private final Map<Integer, T3<Boolean, BeanshellScript, ScriptPanel>> allBeanshellScripts = new HashMap<>();

	public String getScriptName(int uuid)
	{
		return allBeanshellScripts.keySet().stream().filter(f -> f == uuid).findFirst().map(f -> allBeanshellScripts.get(f).get_2().getName()).orElse("INVALID");
	}

	public String getScriptPath(int uuid)
	{
		return allBeanshellScripts.keySet().stream().filter(f -> f == uuid).findFirst().map(f -> allBeanshellScripts.get(f).get_2().getPath()).orElse("INVALID");
	}

	public int registerScript(String path, String name)
	{
		String file = path.concat(name);
		Optional<Entry<Integer, T3<Boolean, BeanshellScript, ScriptPanel>>> clash = allBeanshellScripts.entrySet().stream().filter(f -> f.getValue().get_2().getResolvedName().equalsIgnoreCase(file)).findFirst();
		if (clash.isPresent())
		{
			int uuid = clash.get().getKey();
			log.debug("New script \"{}\" clashes w/ existing script -> uuid: {}, path: {}", file, uuid, clash.get().getValue().get_2().getResolvedName());
			allBeanshellScripts.replace(uuid, Tuples.of(false, new BeanshellScript(this.plugin, uuid, clash.get().getValue().get_2().getPath(), clash.get().getValue().get_2().getName()), buildJPanel(uuid, name, false)));
			allBeanshellScripts.get(uuid).get_2().load();
			return uuid;
		}
		else
		{
			int uuid = getUUID();
			allBeanshellScripts.put(uuid, Tuples.of(false, new BeanshellScript(this.plugin, uuid, path, name), buildJPanel(uuid, name, false)));
			allBeanshellScripts.get(uuid).get_2().load();
			return uuid;
		}
	}

	public int reloadScript(int uuid)
	{
		Optional<Entry<Integer, T3<Boolean, BeanshellScript, ScriptPanel>>> value = allBeanshellScripts.entrySet().stream().filter(f -> f.getKey() == uuid).findFirst();
		if (value.isPresent())
		{
			allBeanshellScripts.replace(value.get().getKey(), Tuples.of(value.get().getValue().get_1(), new BeanshellScript(this.plugin, value.get().getKey(), value.get().getValue().get_2().getPath(), value.get().getValue().get_2().getName()), value.get().getValue().get_3()));
			return value.get().getKey();
		}
		else
		{
			log.error("No script w/ uuid {} to reload", uuid);
			return -1;
		}
	}

	public void enableScript(int uuid)
	{
		if (!allBeanshellScripts.containsKey(uuid))
		{
			log.error("No such uuid {}", uuid);
			return;
		}
		T3<Boolean, BeanshellScript, ScriptPanel> value = allBeanshellScripts.get(uuid);
		if (value != null && !value.get_1())
		{
			allBeanshellScripts.replace(uuid, Tuples.of(true, value.get_2(), value.get_3()));
			allBeanshellScripts.get(uuid).get_3().setEnabled(true);
			plugin.updateList();
		}
		else
		{
			log.debug("value ({}, {})", value != null ? (value.get_1()) : "NULL", value != null ? value.get_2() : "NULL");
		}
	}

	public void disableScript(int uuid)
	{
		if (!allBeanshellScripts.containsKey(uuid))
		{
			log.error("No such uuid {}", uuid);
			return;
		}
		T3<Boolean, BeanshellScript, ScriptPanel> value = allBeanshellScripts.get(uuid);
		if (value != null && value.get_1())
		{
			allBeanshellScripts.replace(uuid, Tuples.of(false, value.get_2(), value.get_3()));
			allBeanshellScripts.get(uuid).get_3().setEnabled(false);
			plugin.updateList();
		}
		else
		{
			log.debug("value ({}, {})", value != null ? (value.get_1()) : "NULL", value != null ? value.get_2() : "NULL");
		}
	}

	public boolean isScriptEnabled(int uuid)
	{
		if (!allBeanshellScripts.containsKey(uuid))
		{
			log.error("No such uuid {}", uuid);
			return false;
		}
		T3<Boolean, BeanshellScript, ScriptPanel> value = allBeanshellScripts.get(uuid);
		return value != null && value.get_1();
	}

	public BeanshellScript getScript(int uuid)
	{
		if (!allBeanshellScripts.containsKey(uuid))
		{
			log.error("No such uuid {}", uuid);
			return null;
		}
		T3<Boolean, BeanshellScript, ScriptPanel> value = allBeanshellScripts.get(uuid);
		if (value != null)
		{
			return value.get_2();
		}
		log.error("Uuid {} does not exist", uuid);
		return null;
	}

	public boolean isUUID(int uuid)
	{
		return allBeanshellScripts.containsKey(uuid);
	}

	public List<Integer> getScriptUUIDs(boolean enabled)
	{
		return ImmutableList.copyOf(
			allBeanshellScripts.entrySet().stream().filter(
				f -> (enabled == f.getValue().get_1())
			).map(Entry::getKey).collect(Collectors.toList())
		);
	}

	public List<ScriptPanel> getScriptPanels()
	{
		return ImmutableList.copyOf(allBeanshellScripts.values().stream().map(T3::get_3).collect(Collectors.toList()));
	}

	public List<Integer> getScriptUUIDs()
	{
		log.debug("allScriptUUUIDs {}", allBeanshellScripts.keySet().size());
		return ImmutableList.copyOf(allBeanshellScripts.keySet());
	}

	public List<BeanshellScript> getAllEnabled()
	{
		return allBeanshellScripts.values().stream().filter(T3::get_1).map(T3::get_2).collect(Collectors.toList());
	}

	public void callOnAllEnabled(Consumer<? super BeanshellScript> action)
	{
		allBeanshellScripts.values().stream().filter(T3::get_1).map(T3::get_2).collect(Collectors.toList()).forEach(action);
	}

	public <F> void callOnAllEnabled(BiConsumer<? super BeanshellScript, F> action, F param)
	{
		allBeanshellScripts.values().stream().filter(T3::get_1).map(T3::get_2).collect(Collectors.toList()).forEach(j -> action.accept(j, param));
	}

	public ScriptPanel buildJPanel(int uuid, String name, boolean enabled)
	{
		ScriptPanel toRet = new ScriptPanel(uuid, name);
		toRet.setEnabled(enabled);
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
				if (isScriptEnabled(toRet.getUuid()))
				{
					disableScript(toRet.getUuid());
				}
				else
				{
					enableScript(toRet.getUuid());
				}
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
		return toRet;
	}
}
