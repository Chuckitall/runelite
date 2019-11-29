package net.runelite.client.plugins.fredexperimental.oneclick2;

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
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.fred.api.other.Tuples;
import net.runelite.client.plugins.fred.api.other.Tuples.T3;
import net.runelite.client.plugins.fredexperimental.oneclick2.lua.LuaMatcher2;
import net.runelite.client.plugins.fredexperimental.oneclick2.panel.ScriptPanel;

@Singleton
@Slf4j
public class LuaMatchManager
{
	private static int __OFFSET__ = 0;

	private static int getUUID()
	{
		return __OFFSET__++;
	}

	private Oneclick2 plugin;

	@Inject
	public LuaMatchManager(Oneclick2 plugin)
	{
		this.plugin = plugin;
	}

	private final Map<Integer, T3<Boolean, LuaMatcher2, ScriptPanel>> allLuaMatchers = new HashMap<>();

	public String getScriptName(int uuid)
	{
		return allLuaMatchers.keySet().stream().filter(f -> f == uuid).findFirst().map(f -> allLuaMatchers.get(f).get_2().getName()).orElse("INVALID");
	}

	public String getScriptPath(int uuid)
	{
		return allLuaMatchers.keySet().stream().filter(f -> f == uuid).findFirst().map(f -> allLuaMatchers.get(f).get_2().getPath()).orElse("INVALID");
	}

	public int registerScript(String path, String name)
	{
		String file = path.concat(name);
		Optional<Entry<Integer, T3<Boolean, LuaMatcher2, ScriptPanel>>> clash = allLuaMatchers.entrySet().stream().filter(f -> f.getValue().get_2().getResolvedName().equalsIgnoreCase(file)).findFirst();
		if (clash.isPresent())
		{
			log.debug("New script \"{}\" clashes w/ existing script -> uuid: {}, path: {}", file, clash.get().getKey(), clash.get().getValue().get_2().getResolvedName());
			allLuaMatchers.replace(clash.get().getKey(), Tuples.of(false, new LuaMatcher2(this.plugin, clash.get().getKey(), clash.get().getValue().get_2().getPath(), clash.get().getValue().get_2().getName()), buildJPanel(clash.get().getKey(), name, false)));
			return clash.get().getKey();
		}
		else
		{
			int uuid = getUUID();
			allLuaMatchers.put(uuid, Tuples.of(false, new LuaMatcher2(this.plugin, uuid, path, name), buildJPanel(uuid, name, false)));
			return uuid;
		}
	}

	public int reloadScript(int uuid)
	{
		Optional<Entry<Integer, T3<Boolean, LuaMatcher2, ScriptPanel>>> value = allLuaMatchers.entrySet().stream().filter(f -> f.getKey() == uuid).findFirst();
		if (value.isPresent())
		{
			allLuaMatchers.replace(value.get().getKey(), Tuples.of(value.get().getValue().get_1(), new LuaMatcher2(this.plugin, value.get().getKey(), value.get().getValue().get_2().getPath(), value.get().getValue().get_2().getName()), value.get().getValue().get_3()));
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
		if (!allLuaMatchers.containsKey(uuid))
		{
			log.error("No such uuid {}", uuid);
			return;
		}
		T3<Boolean, LuaMatcher2, ScriptPanel> value = allLuaMatchers.get(uuid);
		if (value != null && !value.get_1())
		{
			allLuaMatchers.replace(uuid, Tuples.of(true, value.get_2(), value.get_3()));
			allLuaMatchers.get(uuid).get_3().setEnabled(true);
			plugin.updateList();
		}
		else
		{
			log.debug("value ({}, {})", value != null ? (value.get_1()) : "NULL", value != null ? value.get_2() : "NULL");
		}
	}

	public void disableScript(int uuid)
	{
		if (!allLuaMatchers.containsKey(uuid))
		{
			log.error("No such uuid {}", uuid);
			return;
		}
		T3<Boolean, LuaMatcher2, ScriptPanel> value = allLuaMatchers.get(uuid);
		if (value != null && value.get_1())
		{
			allLuaMatchers.replace(uuid, Tuples.of(false, value.get_2(), value.get_3()));
			allLuaMatchers.get(uuid).get_3().setEnabled(false);
			plugin.updateList();
		}
		else
		{
			log.debug("value ({}, {})", value != null ? (value.get_1()) : "NULL", value != null ? value.get_2() : "NULL");
		}
	}

	public boolean isScriptEnabled(int uuid)
	{
		if (!allLuaMatchers.containsKey(uuid))
		{
			log.error("No such uuid {}", uuid);
			return false;
		}
		T3<Boolean, LuaMatcher2, ScriptPanel> value = allLuaMatchers.get(uuid);
		return value != null && value.get_1();
	}

	public LuaMatcher2 getScriptMatcher(int uuid)
	{
		if (!allLuaMatchers.containsKey(uuid))
		{
			log.error("No such uuid {}", uuid);
			return null;
		}
		T3<Boolean, LuaMatcher2, ScriptPanel> value = allLuaMatchers.get(uuid);
		if (value != null)
		{
			return value.get_2();
		}
		log.error("Uuid {} does not exist", uuid);
		return null;
	}

	public boolean isUUID(int uuid)
	{
		return allLuaMatchers.containsKey(uuid);
	}

	public List<Integer> getScriptUUIDs(boolean enabled)
	{
		return ImmutableList.copyOf(
			allLuaMatchers.entrySet().stream().filter(
				f -> (enabled == f.getValue().get_1())
			).map(Entry::getKey).collect(Collectors.toList())
		);
	}

	public List<ScriptPanel> getScriptPanels()
	{
		return ImmutableList.copyOf(allLuaMatchers.values().stream().map(T3::get_3).collect(Collectors.toList()));
	}

	public List<Integer> getScriptUUIDs()
	{
		log.debug("allScriptUUUIDs {}", allLuaMatchers.keySet().size());
		return ImmutableList.copyOf(allLuaMatchers.keySet());
	}

	public List<LuaMatcher2> getAllEnabled()
	{
		return allLuaMatchers.values().stream().filter(T3::get_1).map(T3::get_2).collect(Collectors.toList());
	}

	public void callOnAllEnabled(Consumer<? super LuaMatcher2> action)
	{
		allLuaMatchers.values().stream().filter(T3::get_1).map(T3::get_2).collect(Collectors.toList()).forEach(action);
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
