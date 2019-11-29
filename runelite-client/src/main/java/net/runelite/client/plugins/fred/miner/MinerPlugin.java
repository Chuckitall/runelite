package net.runelite.client.plugins.fred.miner;

import com.google.inject.Inject;
import com.google.inject.Provides;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuEntry;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.client.events.ConfigChanged;
import net.runelite.api.events.GameObjectChanged;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.input.KeyManager;
import net.runelite.client.menus.AbstractComparableEntry;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.fred.util.WidgetUtils;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.api.util.Text;

import static net.runelite.api.AnimationID.IDLE;
import static net.runelite.api.AnimationID.MINING_BRONZE_PICKAXE;
import static net.runelite.api.AnimationID.MINING_IRON_PICKAXE;
import static net.runelite.api.AnimationID.MINING_STEEL_PICKAXE;
import static net.runelite.api.AnimationID.MINING_BLACK_PICKAXE;
import static net.runelite.api.AnimationID.MINING_MITHRIL_PICKAXE;
import static net.runelite.api.AnimationID.MINING_ADAMANT_PICKAXE;
import static net.runelite.api.AnimationID.MINING_RUNE_PICKAXE;
import static net.runelite.api.AnimationID.MINING_DRAGON_PICKAXE;
import static net.runelite.api.AnimationID.MINING_DRAGON_PICKAXE_UPGRADED;
import static net.runelite.api.AnimationID.MINING_DRAGON_PICKAXE_OR;
import static net.runelite.api.AnimationID.MINING_INFERNAL_PICKAXE;
import static net.runelite.api.AnimationID.MINING_3A_PICKAXE;
import static net.runelite.api.AnimationID.MINING_CRYSTAL_PICKAXE;
import static net.runelite.api.ItemID.ADAMANT_PICKAXE;
import static net.runelite.api.ItemID.ARDOUGNE_CLOAK_1;
import static net.runelite.api.ItemID.ARDOUGNE_CLOAK_2;
import static net.runelite.api.ItemID.ARDOUGNE_CLOAK_3;
import static net.runelite.api.ItemID.ARDOUGNE_CLOAK_4;
import static net.runelite.api.ItemID.BLACK_PICKAXE;
import static net.runelite.api.ItemID.BRONZE_PICKAXE;
import static net.runelite.api.ItemID.GAMES_NECKLACE1;
import static net.runelite.api.ItemID.GAMES_NECKLACE2;
import static net.runelite.api.ItemID.GAMES_NECKLACE3;
import static net.runelite.api.ItemID.GAMES_NECKLACE4;
import static net.runelite.api.ItemID.GAMES_NECKLACE5;
import static net.runelite.api.ItemID.GAMES_NECKLACE6;
import static net.runelite.api.ItemID.GAMES_NECKLACE7;
import static net.runelite.api.ItemID.GAMES_NECKLACE8;
import static net.runelite.api.ItemID.IRON_PICKAXE;
import static net.runelite.api.ItemID.MITHRIL_PICKAXE;
import static net.runelite.api.ItemID.RING_OF_DUELING1;
import static net.runelite.api.ItemID.RING_OF_DUELING2;
import static net.runelite.api.ItemID.RING_OF_DUELING3;
import static net.runelite.api.ItemID.RING_OF_DUELING4;
import static net.runelite.api.ItemID.RING_OF_DUELING5;
import static net.runelite.api.ItemID.RING_OF_DUELING6;
import static net.runelite.api.ItemID.RING_OF_DUELING7;
import static net.runelite.api.ItemID.RING_OF_DUELING8;
import static net.runelite.api.ItemID.RUNE_PICKAXE;
import static net.runelite.api.ItemID.STEEL_PICKAXE;

/**
 * Created by npruff on 8/24/2019.
 */


@PluginDescriptor(
	name = "Fred's Miner",
	description = "Miner bot",
	tags = {"flexo", "mining", "bot", "fredminer"},
	type = PluginType.EXTERNAL
)
@Slf4j
public class MinerPlugin extends Plugin
{
	@Getter(AccessLevel.PACKAGE)
	@Inject
	private Client client;

	@Getter(AccessLevel.PACKAGE)
	@Inject
	private MinerConfig config;

	@Getter(AccessLevel.PACKAGE)
	@Inject
	private KeyManager keyManager;

	@Getter(AccessLevel.PACKAGE)
	@Inject
	private OverlayManager overlayManager;

	@Getter(AccessLevel.PACKAGE)
	@Inject
	private EventBus eventBus;

	@Inject
	@Getter(AccessLevel.PACKAGE)
	private ConfigManager configManager;

	@Inject
	@Getter(AccessLevel.PACKAGE)
	private MenuManager menuManager;

	@Inject
	@Getter(AccessLevel.PACKAGE)
	private MinerOverlay minerOverlay;

	@Inject
	@Getter(AccessLevel.PACKAGE)
	private MinerOverlayPanel minerOverlayPanel;

	//plugin state values
	@Getter(AccessLevel.PACKAGE)
	private MinerActivity currentActivity = MinerActivity.IDLE;

	@Getter(AccessLevel.PACKAGE)
	private GameState currentGameState = GameState.UNKNOWN;

	private final static int[] PICK_IDS = new int[] {BRONZE_PICKAXE, IRON_PICKAXE, STEEL_PICKAXE, BLACK_PICKAXE, MITHRIL_PICKAXE, ADAMANT_PICKAXE, RUNE_PICKAXE};
	final static int[] GAMES_NECKLACE_IDS = new int[] {GAMES_NECKLACE1, GAMES_NECKLACE2, GAMES_NECKLACE3, GAMES_NECKLACE4, GAMES_NECKLACE5, GAMES_NECKLACE6, GAMES_NECKLACE7, GAMES_NECKLACE8};
	final static int[] DUEL_RING_IDS = new int[] {RING_OF_DUELING1, RING_OF_DUELING2, RING_OF_DUELING3, RING_OF_DUELING4, RING_OF_DUELING5, RING_OF_DUELING6, RING_OF_DUELING7, RING_OF_DUELING8};
	final static int[] ARDY_CLOAK_IDS = new int[] {ARDOUGNE_CLOAK_1, ARDOUGNE_CLOAK_2, ARDOUGNE_CLOAK_3, ARDOUGNE_CLOAK_4};
	final static int WINTERTODT_REGION = 6461;
	final static int CASTLE_WARS_REGION = 9776;
	final static int ARDY_REGION = 10290;

	@Getter(AccessLevel.PACKAGE)
	@Setter(AccessLevel.PRIVATE)
	private Set<Integer> oreIDs = new HashSet<>();

	@Getter(AccessLevel.PACKAGE)
	@Setter(AccessLevel.PRIVATE)
	private Set<Integer> rockIDs = new HashSet<>();

	@Getter(AccessLevel.PACKAGE)
	private GameObject targetRock;

	@Getter(AccessLevel.PACKAGE)
	private WorldPoint lastRockClicked = null;

	@Getter(AccessLevel.PACKAGE)
	private AbstractComparableEntry mineRockCompare = new AbstractComparableEntry()
	{
		@Override
		public boolean matches(MenuEntry entry)
		{
			if (Text.standardize(entry.getTarget()).contains("rocks") && Text.standardize(entry.getOption()).contains("mine"))
			{
				final GameObject tRock = getTargetRock();
				if (tRock != null && tRock.getWorldLocation().isInScene(client))
				{
					LocalPoint p = LocalPoint.fromWorld(client, tRock.getWorldLocation());
					return p != null && p.getSceneX() == entry.getParam0() && p.getSceneY() == entry.getParam1();
				}
			}
			return false;
		}
	};

	@Inject
	private MinerFlexo minerFlexo;

	@Provides
	MinerConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(MinerConfig.class);
	}

	boolean isHasCloak()
	{
		return WidgetUtils.hasItemEquipped(client, ARDY_CLOAK_IDS);
	}

	boolean isHasTele()
	{
		return WidgetUtils.hasItemEquipped(client, GAMES_NECKLACE_IDS) || WidgetUtils.hasItemEquipped(client, DUEL_RING_IDS);
	}

	boolean isHasPickaxe()
	{
		return WidgetUtils.hasItem(client, PICK_IDS);
	}

	void setTargetRock(GameObject o)
	{
		targetRock = o;
	}
//
//	GameObject findTargetRock()
//	{
//		//WorldUtils.getClosestObject(client, getRockIDs().stream().mapToInt(f -> f).toArray(), 6)
//		return WorldUtils.getClosestObject(client, getRockIDs().stream().mapToInt(f -> f).toArray(), 6);
//	}

	int getOresCount()
	{
		return WidgetUtils.getInventoryItemCount(client, oreIDs);
	}

	private void reset()
	{
		mineRockCompare.setPriority(10);
		setTargetRock(null);
	}

	private void readConfig()
	{
		this.oreIDs.clear();
		this.rockIDs.clear();
		Text.fromCSV(config.oreItemIDs()).forEach(f ->
		{
			try
			{
				int d = Integer.parseInt(f);
				this.oreIDs.add(d);
			}
			catch (NumberFormatException ignored)
			{

			}
		});
		Text.fromCSV(config.oreRockIDs()).forEach(f ->
		{
			try
			{
				int d = Integer.parseInt(f);
				this.rockIDs.add(d);
			}
			catch (NumberFormatException ignored)
			{

			}
		});
		reset();
	}

	@Override
	protected void startUp()
	{
		readConfig();

		eventBus.subscribe(ConfigChanged.class, this, this::onConfigChanged);
		eventBus.subscribe(GameStateChanged.class, this, this::onGameStateChanged);
//		eventBus.subscribe(ChatMessage.class, this, this::onChatMessage);
		eventBus.subscribe(AnimationChanged.class, this, this::onAnimationChanged);
		eventBus.subscribe(ItemContainerChanged.class, this, this::onItemContainerChanged);
		eventBus.subscribe(GameTick.class, this, this::onGameTick);
		eventBus.subscribe(GameObjectChanged.class, this, this::onGameObjectChanged);
		eventBus.subscribe(GameObjectDespawned.class, this, this::onGameObjectDespawned);
		eventBus.subscribe(MenuOptionClicked.class, this, this::onMenuOptionClicked);

		overlayManager.add(minerOverlay);
		overlayManager.add(minerOverlayPanel);
		menuManager.addPriorityEntry(mineRockCompare);
		minerFlexo.startup();
		reset();
	}

	private void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() != getCurrentGameState())
		{
			reset();
			currentGameState = event.getGameState();
		}
	}

	private void onMenuOptionClicked(MenuOptionClicked menuOpt)
	{
		//log.debug("CLicked '{}' '{}' -> x:{} y:{}", menuOpt.getTarget(), menuOpt.getIdentifier(),  menuOpt.getActionParam0(), menuOpt.getActionParam1());
		if (mineRockCompare.matches((menuOpt)))//MenuOpcode.GAME_OBJECT_FIRST_OPTION == menuOpt.getMenuOpcode() && menuOpt.getTarget().contains("Rocks"))
		{
			lastRockClicked = WorldPoint.fromScene(client, menuOpt.getParam0(), menuOpt.getParam1(), client.getPlane());
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		eventBus.unregister(this);
		overlayManager.remove(minerOverlay);
		overlayManager.remove(minerOverlayPanel);
		menuManager.removePriorityEntry(mineRockCompare);
		minerFlexo.shutdown();
	}

	private void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals("FredMiner"))
		{
			return;
		}
		readConfig();
		reset();
	}


	public int getRegionID()
	{
		try
		{
			WorldPoint localWorld = Objects.requireNonNull(client.getLocalPlayer()).getWorldLocation();
			return localWorld.getRegionID();
		}
		catch (NullPointerException e)
		{
			return -1;
		}
	}

	private void onGameTick(GameTick event)
	{
		if (getRegionID() != ARDY_REGION && getTargetRock() != null)
		{
			setTargetRock(null);
		}
	}

	private void onGameObjectChanged(GameObjectChanged event)
	{
		if (getTargetRock() != null && event.getPrevious().equals(getTargetRock()))
		{
			log.debug("changed rock");
//			setTargetRock(WorldUtils.getClosestObject(client, getRockIDs().stream().mapToInt(f->f).toArray(),6));
			setTargetRock(null);
		}
	}

	private void onGameObjectDespawned(GameObjectDespawned event)
	{
		if (getTargetRock() != null && event.getGameObject().equals(getTargetRock()))
		{
			log.debug("despawned rock");
//			setTargetRock(WorldUtils.getClosestObject(client, getRockIDs().stream().mapToInt(f->f).toArray(),6));
			setTargetRock(null);
		}
	}

//	private void onGameObjectSpawned(GameObjectSpawned event)
//	{
//		if (getCurrentActivity() == MinerActivity.IDLE)
//		{
//			setTargetRock(WorldUtils.getClosestObject(client, getRockIDs().stream().mapToInt(f->f).toArray(),6));
//		}
//	}

	private void onAnimationChanged(final AnimationChanged event)
	{
		final Player local = client.getLocalPlayer();

		if (local == null || event.getActor() != local)
		{
			return;
		}

		final int animId = local.getAnimation();
		switch (animId)
		{
			case MINING_BRONZE_PICKAXE:
			case MINING_IRON_PICKAXE:
			case MINING_STEEL_PICKAXE:
			case MINING_BLACK_PICKAXE:
			case MINING_MITHRIL_PICKAXE:
			case MINING_ADAMANT_PICKAXE:
			case MINING_RUNE_PICKAXE:
			case MINING_DRAGON_PICKAXE:
			case MINING_DRAGON_PICKAXE_UPGRADED:
			case MINING_DRAGON_PICKAXE_OR:
			case MINING_3A_PICKAXE:
			case MINING_CRYSTAL_PICKAXE:
			case MINING_INFERNAL_PICKAXE:
				setActivity(MinerActivity.MINING);
				break;
			case IDLE:
				setActivity(MinerActivity.IDLE);
		}
	}

	private void onItemContainerChanged(ItemContainerChanged event)
	{
		final ItemContainer container = event.getItemContainer();

		if (container == client.getItemContainer(InventoryID.INVENTORY))
		{
			log.debug("Items count: " + container.getItems().length);
			setActivity(MinerActivity.IDLE);
			if (config.autoAction())
			{
				if (!minerFlexo.doMine()) minerFlexo.doTeleport();
			}
		}
		else if (container == client.getItemContainer(InventoryID.EQUIPMENT))
		{
			log.debug("Equipt count: " + container.getItems().length);
		}
	}

	private void setActivity(MinerActivity action)
	{
		currentActivity = action;
	}
}
