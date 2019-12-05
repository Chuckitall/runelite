package net.runelite.client.plugins.fredexperimental.oneclick2;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Binder;
import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.LocatableQueryResults;
import net.runelite.api.MenuEntry;
import net.runelite.api.MenuOpcode;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.util.Text;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.menus.AbstractComparableEntry;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.fred.api.wrappers._GameObject;
import net.runelite.client.plugins.fred.api.wrappers._Item;
import net.runelite.client.plugins.fredexperimental.oneclick2.lua.LuaMatcher2;
import net.runelite.client.plugins.fredexperimental.oneclick2.lua.StockEntry;
import net.runelite.client.plugins.fredexperimental.oneclick2.panel.Oneclick2Panel;
import net.runelite.client.plugins.fredexperimental.oneclick2.panel.ScriptPanel;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import static net.runelite.api.MenuOpcode.LUA_MENU;

@PluginDescriptor(
	name = "Oneclick 2.0",
	description = "Enable the Oneclick 2.0 panel",
	tags = {"oneclick", "oneclick2", "panel", "menu"}
)
@Slf4j
public class Oneclick2 extends Plugin
{
	static final Splitter NEWLINE_SPLITTER = Splitter
		.on("\n")
		.omitEmptyStrings()
		.trimResults();

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	@Getter(AccessLevel.PUBLIC)
	private Client client;

	@Inject
	private MenuManager menuManager;

	@Inject
	private Oneclick2Config config;

	@Inject
	private EventBus eventBus;

	@Inject
	@Getter(AccessLevel.PUBLIC)
	private LuaMatchManager luaMatchManager;

	@Inject
	private ScheduledExecutorService executorService;

	private NavigationButton navButton;
	@Setter(AccessLevel.PACKAGE)
	private Oneclick2Panel panel;

	//settings
	@Getter(AccessLevel.PUBLIC)
	private boolean placeholder;

	//settings
	@Getter(AccessLevel.PUBLIC)
	private String rootPath;

	@Getter(AccessLevel.PUBLIC)
	private String scripts;

	@Provides
	Oneclick2Config provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(Oneclick2Config.class);
	}

	@Override
	public void configure(Binder binder)
	{
		log.debug("configure callback");
	}

	private AbstractComparableEntry prioritiseCheck = new AbstractComparableEntry()
	{
		@Override
		public boolean matches(MenuEntry entry)
		{
			return MenuOpcode.of(entry.getOpcode()).equals(LUA_MENU);
		}
	};

	private final Supplier<List<ScriptPanel>> scriptEntrySupplier = () -> luaMatchManager.getScriptPanels();

	@Override
	protected void startUp() throws Exception
	{
		panel = new Oneclick2Panel(this, scriptEntrySupplier);
		final BufferedImage icon = loadImage("panel_icon");
		navButton = NavigationButton.builder()
			.tooltip("Oneclick 2.0")
			.icon(icon)
			.priority(2)
			.panel(panel)
			.build();
		clientToolbar.addNavigation(navButton);
		initConfigValues();

		menuManager.addPriorityEntry(prioritiseCheck);
		addSubscriptions();
		updateList();
	}

	@Override
	protected void shutDown() throws Exception
	{
		eventBus.unregister(this);
		clientToolbar.removeNavigation(navButton);
		menuManager.removePriorityEntry(prioritiseCheck);
	}

	private void addSubscriptions()
	{
		eventBus.subscribe(ConfigChanged.class, this, this::onConfigChanged);
		eventBus.subscribe(MenuEntryAdded.class, this, this::onMenuEntryAdded);
		eventBus.subscribe(MenuOptionClicked.class, this, this::onMenuOptionClicked);
		eventBus.subscribe(GameTick.class, this, this::onGameTick);

		eventBus.subscribe(ItemContainerChanged.class, this, this::onItemContainerChanged);
	}

	private void onGameTick(GameTick tick)
	{
		luaMatchManager.callOnAllEnabled(LuaMatcher2::tick);
	}

	private Optional<StockEntry> onMenuAdded(LuaMatcher2 matcher, StockEntry e)
	{
		if (!matcher.peak(e.op, e.id))
		{
			return Optional.empty();
		}

		matcher.refresh(e);
		if (!matcher.isMatch(e))
		{
			return Optional.empty();
		}
		Optional<StockEntry> toAdd = Optional.ofNullable(matcher.doAdd(e));
		toAdd.ifPresent(f -> {
			f.op = LUA_MENU.getId();
			f.id = matcher.getUuid();
		});
		return toAdd;
	}

	private void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (event.getOpcode() == LUA_MENU.getId())
		{
			return;
		}
		List<MenuEntry> toAdd = luaMatchManager.getAllEnabled().stream().map(f -> onMenuAdded(f, new StockEntry(event))).map(f -> f.map(StockEntry::build)).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
		if (!toAdd.isEmpty())
		{
			toAdd.forEach(f -> client.insertMenuItem(
				f.getOption(), f.getTarget(),
				f.getOpcode(), f.getIdentifier(),
				f.getParam0(), f.getParam1(),
				f.isForceLeftClick())
			);
		}
	}

	private void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (event.getOpcode() != LUA_MENU.getId())
		{
			return;
		}
		LuaMatcher2 m;
		if (luaMatchManager.isUUID(event.getIdentifier()) && luaMatchManager.isScriptEnabled(event.getIdentifier()) && (m = luaMatchManager.getScriptMatcher(event.getIdentifier())) != null)
		{
			Optional<MenuEntry> t = Optional.ofNullable(m.onClick(new StockEntry(event))).map(StockEntry::build);
			if (t.isPresent())
			{
				event.setMenuEntry(t.get());
			}
			else
			{
				event.consume();
			}
		}
		else
		{
			event.consume();
		}
	}

	private void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals("oneclick2"))
		{
			return;
		}
		if (event.getKey().equalsIgnoreCase("scripts"))
		{
			loadScriptsFromConfig();
		}
		if (event.getKey().equalsIgnoreCase("placeholder"))
		{
			boolean enabledO = Boolean.parseBoolean(event.getOldValue());
			boolean enabledN = Boolean.parseBoolean(event.getNewValue());
		}
	}

	private void initConfigValues()
	{
		this.placeholder = config.placeholder();
		this.rootPath = config.scriptsPath();
		loadScriptsFromConfig();
	}

	private void loadScriptsFromConfig()
	{
		this.scripts = config.scripts();

		if (!Strings.isNullOrEmpty(scripts))
		{
			final StringBuilder sb = new StringBuilder();

			for (String str : scripts.split("\n"))
			{
				if (!str.startsWith("//"))
				{
					sb.append(str).append("\n");
				}
			}

			@SuppressWarnings("UnstableApiUsage") final Map<String, String> split = NEWLINE_SPLITTER.withKeyValueSeparator(" | ").split(sb);
			for (Map.Entry<String, String> entry : split.entrySet())
			{
				final String name = entry.getKey().trim();
				final boolean enabled = Boolean.parseBoolean(entry.getValue().trim());
				int uuid = luaMatchManager.registerScript(rootPath, name);
				if (enabled && !luaMatchManager.isScriptEnabled(uuid))
				{
					luaMatchManager.enableScript(uuid);
				}
				else if (!enabled && luaMatchManager.isScriptEnabled(uuid))
				{
					luaMatchManager.disableScript(uuid);
				}
			}
		}
	}

	BufferedImage loadImage(String path)
	{
		return ImageUtil.getResourceStreamFromClass(getClass(), "/net/runelite/client/plugins/fredexperimental/oneclick2/" + path + ".png");
	}

	//methods used to keep track of state. Lua matchers query these w/ my api libraries.
	private Map<InventoryID, List<_Item>> itemContainerMap = new HashMap<>();
	public List<_Item> getItemContainer(InventoryID container)
	{
		if (itemContainerMap.containsKey(container))
		{
			return ImmutableList.copyOf(itemContainerMap.get(container));
		}
		return ImmutableList.of();
	}

	public List<_Item> getItemContainer(int container)
	{
		InventoryID c = InventoryID.getValue(container);
		return this.getItemContainer(c);
	}

	public List<_GameObject> getGameObjects(List<Integer> ids)
	{
		LocatableQueryResults<GameObject> objs = new GameObjectQuery().idEquals(ids).result(client);
		List<_GameObject> arr = objs.stream().map(_GameObject::from).collect(Collectors.toList());
		return ImmutableList.copyOf(arr);
	}

	public _GameObject getNearestGameObject(List<Integer> ids)
	{
		return _GameObject.from(new GameObjectQuery().idEquals(ids).result(client).nearestTo(client.getLocalPlayer()));
	}

	private void onItemContainerChanged(ItemContainerChanged event)
	{
		final InventoryID containerId = InventoryID.getValue(event.getContainerId());
		final ItemContainer itemContainer = event.getItemContainer();
		final List<Item> items = Arrays.asList(itemContainer.getItems());

		List<_Item> tempList = Lists.newArrayList();
		for (int idx = 0; idx < items.size(); idx++)
		{
			final Item item = items.get(idx);
			final int id = item.getId();
			final int qty = item.getQuantity();
			if (qty == 0 && id == -1)
			{
				continue;
			}
			_Item temp = new _Item(id, qty, idx);
			if (qty == 0 || id == -1)
			{
				log.error("temp: '{}' -> was invalid", temp);
			}
			else
			{
				tempList.add(temp);
			}
		}

		if (itemContainerMap.containsKey(containerId))
		{
			List<_Item> existing = itemContainerMap.get(containerId);
			existing.clear();
			existing.addAll(tempList);
		}
		else
		{
			itemContainerMap.put(containerId, tempList);
		}
	}

	void updateList()
	{
		executorService.submit(panel::rebuildScriptPanels);
	}
}

