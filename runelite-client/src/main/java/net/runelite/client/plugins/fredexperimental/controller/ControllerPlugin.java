package net.runelite.client.plugins.fredexperimental.controller;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.inject.Provides;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ObjectDefinition;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.fred.api.controllers._Banking;
import net.runelite.client.plugins.fred.api.controllers._Inventory;
import net.runelite.client.plugins.fred.api.controllers._Vision;
import net.runelite.client.plugins.fred.api.other.Pair;
import net.runelite.client.plugins.fred.api.wrappers._Item;
import net.runelite.client.plugins.fred.api.wrappers._Tile;
import net.runelite.client.plugins.fredexperimental.controller.listeners.InventoryItemsListener;
import net.runelite.client.plugins.fredexperimental.controller.scripts.FletchLongbowsScript;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;

@PluginDescriptor(
	name = "Freds Controller Plugin",
	description = "Runs scripts in a sandbox",
	tags = {"fred", "striker", "bot", "controller"},
	type = PluginType.FRED
)
@Singleton
@Slf4j
public class ControllerPlugin extends Plugin
{

	@Inject
	private ControllerConfig config;
	@Inject
	private EventBus eventBus;
	@Inject
	private KeyManager keyManager;

	//Context
	@Inject
	@Getter(AccessLevel.PUBLIC)
	private Client client;

	@Getter(AccessLevel.PUBLIC)
	@Inject
	private _Banking bankController;

	@Getter(AccessLevel.PUBLIC)
	@Inject
	private _Inventory inventoryController;

	@Getter(AccessLevel.PUBLIC)
	@Inject
	private _Vision visionController;

	@Inject
	private WindowOverlay windowOverlay;
	@Inject
	private SceneOverlay sceneOverlay;
	@Inject
	private InterfaceOverlay interfaceOverlay;
	@Inject
	private OverlayManager overlayManager;

//	private boolean scriptEnabled = false;
	private String currentTask = "";

	private Future<?> task;

	private _Tile playerLocation = null;

	private HotkeyListener toggleEnabled;

	@Getter(AccessLevel.PUBLIC)
	private List<_Item> inventoryItems = null;
	@Getter(AccessLevel.PUBLIC)
	private int freeInvSpaces = -1;

	ExecutorService threadExec;

	@Getter(AccessLevel.PACKAGE)
	Script script = new FletchLongbowsScript();

	@Provides
	ControllerConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ControllerConfig.class);
	}

	public ControllerPlugin getContext()
	{
		return this;
	}

	@Override
	protected void startUp() throws Exception
	{
		addSubscriptions();
		updateConfig();
		threadExec = Executors.newSingleThreadScheduledExecutor();
		toggleEnabled = new HotkeyListener(config::getHotkey)
		{
			@Override
			public void hotkeyPressed()
			{
				boolean enabled = script.isEnabled();
				log.debug("Is enabled: {}", enabled);
				if (enabled)
				{
					log.debug("Shut down script");
					script.cleanup();
				}
				else
				{
					log.debug("Staring up script");
					script.init(getContext());
				}
			}
		};
		keyManager.registerKeyListener(toggleEnabled);
		overlayManager.add(sceneOverlay);
		overlayManager.add(windowOverlay);
		overlayManager.add(interfaceOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		eventBus.unregister(this);
		keyManager.unregisterKeyListener(toggleEnabled);

		if (threadExec != null && !threadExec.isShutdown())
		{
			threadExec.shutdown();
			if (!threadExec.awaitTermination(2000, TimeUnit.MILLISECONDS))
			{
				threadExec.shutdownNow();
			}
		}

		if (script.isEnabled())
		{
			script.cleanup();
		}

		overlayManager.remove(sceneOverlay);
		overlayManager.remove(windowOverlay);
		overlayManager.remove(interfaceOverlay);
	}

	private void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals("fredsController"))
		{
			updateConfig();
		}
	}

	private void addSubscriptions()
	{
		eventBus.subscribe(ConfigChanged.class, this, this::onConfigChanged);
		eventBus.subscribe(GameTick.class, this, this::onGameTick);
		eventBus.subscribe(ItemContainerChanged.class, this, this::onItemContainerChanged);
		eventBus.subscribe(AnimationChanged.class, this, this::onAnimationChanged);
		eventBus.subscribe(GameObjectSpawned.class, this, this::onGameObjectSpawned);
		eventBus.subscribe(GameObjectDespawned.class, this, this::onGameObjectDespawned);
	}

	private void onGameTick(GameTick gameTick)
	{
		if (!this.client.getGameState().equals(GameState.LOGGED_IN) || client.getLocalPlayer() == null)
		{
			return;
		}

		_Tile temp = _Tile.ofPlayer(this.client);
		if (temp != this.playerLocation)
		{
			this.playerLocation = temp;
		}

		if (script != null && script.isEnabled() && (task == null || task.isDone()))
		{
			Optional<Pair<String, Runnable>> action = script.getNextAction(getContext());
			if (action.isPresent())
			{
				currentTask = action.get().get_1();
				log.debug("Starting task - {}", currentTask);
				task = threadExec.submit(action.get().get_2());
			}
			else
			{
				currentTask = "null";
				task = null;
			}
		}
//		else if (script != null && !script.isEnabled() && (task != null && !task.isDone()))
//		{
//			//task.cancel(true);
//			//task = null;
//		}
	}

	private void onItemContainerChanged(ItemContainerChanged event)
	{
		final ItemContainer itemContainer = event.getItemContainer();
		final List<Item> items = Arrays.asList(itemContainer.getItems());

		if (itemContainer == client.getItemContainer(InventoryID.INVENTORY))
		{
			List<_Item> tempList = Lists.newArrayList();
			this.freeInvSpaces = 28;
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
			this.freeInvSpaces -= tempList.size();
			this.inventoryItems = ImmutableList.copyOf(tempList);
			if (script != null && script instanceof InventoryItemsListener && script.isEnabled())
			{
				((InventoryItemsListener) script).onInventoryItemsChanged(this.inventoryItems);
			}
		}
	}

	private void onAnimationChanged(AnimationChanged event)
	{
//		if (client.getLocalPlayer() != null && event.getActor() == client.getLocalPlayer())
//		{
//			//log.debug("animationChanged {}", event.getActor().getAnimation());
//			//TODO: pass animation changes to script
//		}
	}

	private void updateConfig()
	{
		this.sceneOverlay.setDebugAreas(config.debugTileAreas());
		this.sceneOverlay.setDebugPaths(config.debugTilePaths());
		this.sceneOverlay.setDebugGameObjects(config.debugGameObjects());
		this.sceneOverlay.setDebugBank(config.debugBank());
		this.interfaceOverlay.setDebugInterfaces(config.debugInterfaces());
	}

	private int getSimpleId(GameObject obj)
	{
		if (obj == null)
		{
			return -1;
		}
		ObjectDefinition def = client.getObjectDefinition(obj.getId());
		if (def.getImpostorIds() != null)
		{
			return def.getImpostor().getId();
		}
		else
		{
			return def.getId();
		}
	}

	private void onGameObjectSpawned(GameObjectSpawned event)
	{
		GameObject spawn = event.getGameObject();
		if (spawn != null)
		{
			log.debug("spawned: {}", client.getObjectDefinition(getSimpleId(spawn)).getName());
		}
	}

	private void onGameObjectDespawned(GameObjectDespawned event)
	{
		GameObject despawn = event.getGameObject();
		if (despawn != null)
		{
			log.debug("despawned: {}",  client.getObjectDefinition(getSimpleId(despawn)).getName());
		}
	}

	public Set<GameObject> getNearbyBanks()
	{
		return ImmutableSet.copyOf(
				bankController.getNearbyBanks().stream()
				.filter(f -> f.getConvexHull() != null)
				.filter(visionController::isGameObjectOnScreen)
				.collect(Collectors.toSet()));
	}

	public String getCurrentTask()
	{
		return currentTask;
	}
}
