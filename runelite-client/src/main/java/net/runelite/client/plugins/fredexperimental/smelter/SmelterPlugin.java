package net.runelite.client.plugins.fredexperimental.smelter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.inject.Provides;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.AnimationID;
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
import net.runelite.api.queries.BankItemQuery;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.queries.InventoryWidgetItemQuery;
import net.runelite.api.queries.WidgetItemQuery;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.fred.api.wrappers._Item;
import net.runelite.client.plugins.fred.api.wrappers._Tile;
import net.runelite.client.plugins.fredexperimental.smelter.SmelterItem._SmelterItem;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;

@PluginDescriptor(
	name = "Freds Smelter",
	description = "Smelts things at the furnace automatically.",
	tags = {"fred", "striker", "bot", "smithing", "crafting"},
	type = PluginType.FRED
)
@Singleton
@Slf4j
public class SmelterPlugin extends Plugin
{
	@Inject
	@Getter(AccessLevel.PACKAGE)
	private Client client;
	@Inject
	private SmelterConfig config;
	@Inject
	private EventBus eventBus;
	@Inject
	private SmelterOverlay windowOverlay;
	@Inject
	private SmelterSceneOverlay sceneOverlay;
	@Inject
	private SmelterInterfaceOverlay interfaceOverlay;
	@Inject
	private OverlayManager overlayManager;

	@Inject
	private KeyManager keyManager;

	@Getter
	private SmelterLocation location = null;

	@Getter
	private SmelterItem producing = null;

	@Getter
	private boolean enabled = false;

	private int lastSmithTick = -1;

	public boolean isSmelting()
	{
		if (lastSmithTick == -1)
		{
			return false;
		}
		else
		{
			if (lastSmithTick == client.getTickCount())
			{
				return true;
			}
			else
			{
				int dif = client.getTickCount() - lastSmithTick;
//				log.debug("Smithing tick difference. ({} - {}) = {}", client.getTickCount(), lastSmithTick, dif);
				return dif < 8;
			}
		}
	}

	@Getter(AccessLevel.PACKAGE)
	private GameObject bankBooth = null;

	@Getter(AccessLevel.PACKAGE)
	private GameObject furnace = null;

	private _Tile playerLocation = null;

	private HotkeyListener toggleEnabled;

	@Getter(AccessLevel.PUBLIC)
	private ImmutableList<_Item> inventoryItems = null;
	@Getter(AccessLevel.PUBLIC)
	private int freeInvSpaces = -1;

//	private _Tile lastFrameLocation = null;
//	@Getter(AccessLevel.PUBLIC)
//	private boolean moving = false;

	@Inject
	private SmeltingController controller;

	private Set<WidgetItem> getItemWidgets(List<Integer> ids)
	{
		if (ids != null && ids.size() > 0)
		{
			WidgetItemQuery q1 = (new InventoryWidgetItemQuery()).idEquals(ids);
			WidgetItemQuery q2 = (new BankItemQuery()).idEquals(ids);
			List<WidgetItem> temp = Lists.newArrayList();
			temp.addAll(q1.result(client).list);
			temp.addAll(q2.result(client).list);
			return ImmutableSet.copyOf(temp);
		}
		return ImmutableSet.of();
	}

	public Set<WidgetItem> getConsumedItemWidgets()
	{
		if (producing != null)
		{
			return getItemWidgets(producing.getIngredients().stream().map(_SmelterItem::getId).collect(Collectors.toList()));
		}
		return ImmutableSet.of();
	}

	public Set<WidgetItem> getProducedItemWidgets()
	{
		if (producing != null)
		{
			return getItemWidgets(producing.getProducts().stream().map(_SmelterItem::getId).collect(Collectors.toList()));
		}
		return ImmutableSet.of();
	}

	public Set<WidgetItem> getCatalystItemWidgets()
	{
		if (producing != null)
		{
			return getItemWidgets(producing.getCatalysts().stream().map(_SmelterItem::getId).collect(Collectors.toList()));
		}
		return ImmutableSet.of();
	}

	@Provides
	SmelterConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SmelterConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		addSubscriptions();
		updateConfig();
		toggleEnabled = new HotkeyListener(config::getHotkey)
		{
			@Override
			public void hotkeyPressed()
			{
				boolean isRunning = controller.isRunning();
				log.debug("Is running: {}", isRunning);
				if (isRunning)
				{
					log.debug("Disabling");
					controller.disable();
				}
				else
				{
					log.debug("Enabling");
					controller.enable();
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
		if (controller.isRunning())
		{
			boolean disResult = controller.disable();
			log.debug("Controller disabled -> {}", disResult);
		}
		overlayManager.remove(sceneOverlay);
		overlayManager.remove(windowOverlay);
		overlayManager.remove(interfaceOverlay);
	}

	private void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals("fredsSmelter"))
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

		this.enabled = controller.isRunning();
		_Tile temp = _Tile.ofPlayer(this.client);
		if (temp != this.playerLocation)
		{
			this.playerLocation = temp;
			updateSmelter();
			updateBankObj();
		}
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
		}
	}

	private void onAnimationChanged(AnimationChanged event)
	{
		if (client.getLocalPlayer() == null || event.getActor() != client.getLocalPlayer())
		{
			return;
		}
		int a = client.getLocalPlayer().getAnimation();
		if (a == AnimationID.SMITHING_SMELTING || a == AnimationID.SMITHING_CANNONBALL)
		{
			this.lastSmithTick = client.getTickCount();
		}
	}

	private void updateConfig()
	{
		this.location = config.getLocation();
		this.producing = config.getProducing();
		this.sceneOverlay.setDebugArea(config.debugArea());
		this.sceneOverlay.setDebugPath(config.debugPath());
		this.sceneOverlay.setDebugFurnace(config.debugFurnace());
		this.sceneOverlay.setDebugBank(config.debugBank());
		this.interfaceOverlay.setDebugInterfaces(config.debugInterfaces());

		this.updateBankObj();
		this.updateSmelter();
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

	private void updateBankObj()
	{
		this.bankBooth = (new GameObjectQuery()).idEquals(location.getBankBoothId()).result(client).nearestTo(client.getLocalPlayer());
	}

	private void updateSmelter()
	{
		this.furnace = (new GameObjectQuery()).idEquals(location.getSmelterId()).result(client).nearestTo(client.getLocalPlayer());
	}

	private void onGameObjectSpawned(GameObjectSpawned event)
	{
		GameObject spawn = event.getGameObject();
		if (spawn == null || location == null || !location.getBoundsArea().contains(_Tile.fromWorld(spawn.getWorldLocation())))
		{
			return;
		}
		if (getSimpleId(spawn) == location.getBankBoothId())
		{
			//update nearest bank booth
			updateBankObj();
		}
		else if (getSimpleId(spawn) == location.getSmelterId())
		{
			//update nearest smelter booth
			updateSmelter();
		}
	}

	private void onGameObjectDespawned(GameObjectDespawned event)
	{
		GameObject despawn = event.getGameObject();
		if (despawn == null || location == null || !location.getBoundsArea().contains(_Tile.fromWorld(despawn.getWorldLocation())))
		{
			return;
		}
		if (getSimpleId(despawn) == location.getBankBoothId())
		{
			//update nearest bank booth
			updateBankObj();
		}
		else if (getSimpleId(despawn) == location.getSmelterId())
		{
			updateSmelter();
			//update nearest smelter booth
		}
	}
}
