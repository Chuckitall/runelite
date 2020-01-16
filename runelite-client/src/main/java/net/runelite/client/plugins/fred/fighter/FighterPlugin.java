package net.runelite.client.plugins.fred.fighter;

import com.google.inject.Inject;
import com.google.inject.Provides;
import java.awt.AWTException;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import java.util.concurrent.TimeUnit;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.events.ConfigChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.NpcDefinitionChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.util.Text;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.flexo.Flexo;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.fred.util.ExtUtils;
import net.runelite.client.plugins.fred.util.FredCamController;
import net.runelite.client.plugins.fred.util.Tab;
import net.runelite.client.plugins.fred.util.TabUtils;
import net.runelite.client.plugins.fred.util.WidgetUtils;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;

import static net.runelite.api.ItemID.ARDOUGNE_CLOAK_1;
import static net.runelite.api.ItemID.ARDOUGNE_CLOAK_2;
import static net.runelite.api.ItemID.ARDOUGNE_CLOAK_3;
import static net.runelite.api.ItemID.ARDOUGNE_CLOAK_4;
import static net.runelite.api.ItemID.CAKE;
import static net.runelite.api.ItemID.SLICE_OF_CAKE;
import static net.runelite.api.ItemID.TROUT;
import static net.runelite.api.ItemID._23_CAKE;
import static net.runelite.api.Skill.HITPOINTS;
import static net.runelite.client.plugins.fred.fighter.TaskEnum.ATTACK_TARGET;
import static net.runelite.client.plugins.fred.fighter.TaskEnum.DELAY_LONG;
import static net.runelite.client.plugins.fred.fighter.TaskEnum.DELAY_SHORT;
import static net.runelite.client.plugins.fred.fighter.TaskEnum.EAT_FOOD;
import static net.runelite.client.plugins.fred.fighter.TaskEnum.GET_TARGET;
import static net.runelite.client.plugins.fred.fighter.TaskEnum.INVALID;
import static net.runelite.client.plugins.fred.fighter.TaskEnum.NONE;
import static net.runelite.client.plugins.fred.fighter.TaskEnum.OPEN_EQUIP;
import static net.runelite.client.plugins.fred.fighter.TaskEnum.OPEN_INV;
import static net.runelite.client.plugins.fred.fighter.TaskEnum.TELEPORT_EQUIP;
import static net.runelite.client.plugins.fred.fighter.TaskEnum.TELEPORT_INV;
import static net.runelite.client.plugins.fred.fighter.TaskEnum.TURN_CAMERA_TO_TARGET;
import static net.runelite.client.plugins.fred.fighter.TaskEnum.WALK_CLOSER_TO_TARGET;


/**
 * Created by npruff on 8/24/2019.
 */


@PluginDescriptor(
	name = "Fred's New Fighter",
	description = "AIO Fighter",
	tags = {"flexo", "combat", "aio", "bot", "fred"},
	type = PluginType.FRED
)
@Slf4j
public class FighterPlugin extends Plugin
{
	private ScheduledExecutorService threadExec = null;
	private ScheduledFuture scriptFuture = null;

	@Inject
	@Getter(AccessLevel.PUBLIC)
	private OverlayManager overlayManager;

	@Inject
	FighterOverlay fighterOverlay;

	@Inject
	FighterOverlayPanel fighterOverlayPanel;

	@Inject
	KeyManager keyManager;

	@Inject
	private EventBus eventBus;

	@Inject
	private FighterConfig config;

	@Inject
	Client client;

	private final static int[] ARDY_CLOAK_IDS = new int[] {ARDOUGNE_CLOAK_1, ARDOUGNE_CLOAK_2, ARDOUGNE_CLOAK_3, ARDOUGNE_CLOAK_4};
	private static final int[] FOOD_IDS = new int[]{TROUT, SLICE_OF_CAKE, _23_CAKE, CAKE};
	@Getter(AccessLevel.PUBLIC)
	private String npcName;
	@Getter(AccessLevel.PUBLIC)
	private int npcLevel;
	@Getter(AccessLevel.PUBLIC)
	private boolean highlightTarget;
	@Getter(AccessLevel.PUBLIC)
	private boolean highlightTargets;
	@Getter(AccessLevel.PUBLIC)
	private Color targetColor;
	@Getter(AccessLevel.PUBLIC)
	private Color targetsColor;

	@Getter(AccessLevel.PUBLIC)
	private List<NPC> targets;

	@Getter(AccessLevel.PUBLIC)
	private NPC target;

	@Getter(AccessLevel.PUBLIC)
	private TaskEnum task = NONE;

	@Getter(AccessLevel.PUBLIC)
	private int tickFightingWith;
	@Getter(AccessLevel.PUBLIC)
	private NPC fightingWith;

	private boolean delay = false;
	private boolean delayLong = false;

	@Getter(AccessLevel.PUBLIC)
	private Flexo flexo;

	@Getter(AccessLevel.PUBLIC)
	private FredCamController camera;

	@Getter(AccessLevel.PUBLIC)
	private boolean scriptEnabled;
	@Getter(AccessLevel.PUBLIC)
	private boolean scriptPaused;

	private ExtUtils utils;


	private HotkeyListener toggleScriptKeybind;
	private HotkeyListener togglePauseKeybind;
	private HotkeyListener triggerDebug1Keybind;
	private HotkeyListener triggerDebug2Keybind;
	private HotkeyListener triggerDebug3Keybind;
	private HotkeyListener triggerDebug4Keybind;

	private boolean hardReset = false;
	private boolean debug1 = false;
	private boolean debug2 = false;
	private boolean debug3 = false;
	private boolean debug4 = false;



	@Provides
	FighterConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(FighterConfig.class);
	}

	private boolean isTarget(NPC npc)
	{
		if (client.getLocalPlayer() == null)
		{
			return false;
		}
		if (npc == null || npc.isDead())
		{
			return false;
		}

		return Text.standardize(npcName).equals(Text.standardize(npc.getName())) && npcLevel == npc.getCombatLevel();
	}

	private void forceNPCRefresh()
	{
		for (NPC npc : client.getNpcs())
		{
			if (isTarget(npc))
			{
				targets.add(npc);
			}
		}
	}

	private void onHitsplatApplied(HitsplatApplied event)
	{
		if (!scriptEnabled)
		{
			return;
		}
		if (client.getLocalPlayer() == null || !(client.getLocalPlayer().getInteracting() instanceof NPC))
		{
			return;
		}
		if (fightingWith == null && event.getActor().equals(client.getLocalPlayer().getInteracting()))
		{
			log.debug("Started fighting {}", event.getActor().getName());
			tickFightingWith = 0;
			this.fightingWith = (NPC) event.getActor();
		}
		else if (event.getActor().equals(fightingWith))
		{
			log.debug("Still fighting {}", fightingWith);
			tickFightingWith = 0;
		}

	}

	private void onNpcSpawned(NpcSpawned npcSpawned)
	{
		if (!scriptEnabled)
		{
			return;
		}
		NPC npc = npcSpawned.getNpc();
		if (isTarget(npc) && !targets.contains(npc))
		{
			log.debug("Spawned -> (name: {}, level: {}, id: {})", npc.getName(), npc.getCombatLevel(), npc.getId());
			targets.add(npc);
		}
	}

	private void onNpcDefinitionChanged(NpcDefinitionChanged event)
	{
		if (!scriptEnabled)
		{
			return;
		}
		NPC npc = event.getNpc();
		if (isTarget(npc))
		{
			log.debug("Def changed -> (name: {}, level: {}, id: {})", npc.getName(), npc.getCombatLevel(), npc.getId());
			if (!targets.contains(npc))
			{
				targets.add(npc);
			}
		}
	}

	private void onNpcDespawned(NpcDespawned npcDespawned)
	{
		if (!scriptEnabled)
		{
			return;
		}
		NPC npc = npcDespawned.getNpc();
		if (targets.contains(npc))
		{
			targets.remove(npc);
			log.debug("Despawned -> (name: {}, level: {}, id: {})", npc.getName(), npc.getCombatLevel(), npc.getId());
		}
	}

	private void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (Text.standardize(event.getTarget()).toLowerCase().contains(this.npcName.toLowerCase()) && Text.standardize(event.getTarget()).toLowerCase().contains(this.npcLevel + ""))
		{
			this.delayLong = true;
			log.debug("Delay {}", event);
		}
	}

	private NPC getNextTarget()
	{
		if (client.getLocalPlayer() == null || client.getLocalPlayer().getLocalLocation() == null)
		{
			return null;
		}

		Optional<NPC> first = targets.stream().filter((NPC npc) -> (!npc.isDead() && (target == null || !target.equals(npc)))).min(Comparator.comparing(
			// Negate to have the furthest first
			(NPC npc) -> npc.getLocalLocation().distanceTo(client.getLocalPlayer().getLocalLocation()))
			// Order by position
			.thenComparing(NPC::getLocalLocation, Comparator.comparing(LocalPoint::getX)
				.thenComparing(LocalPoint::getY)));

		first.ifPresent((NPC npc) -> log.debug("Next target \"{}{\" found at ({}, {})", npc.getName() + " - " + npc.getCombatLevel(), npc.getWorldLocation().getX() - client.getBaseX(), npc.getWorldLocation().getY() - client.getBaseY()));
		return first.orElse(null);
	}

	@SuppressWarnings("unused")
	private void onGameTick(GameTick tick)
	{
		//log.debug("Tick | scriptEnabled: {} | scriptFuture: {}", scriptEnabled, scriptFuture != null ? scriptFuture.isDone() : "null");
		if (this.target != null && this.target.isDead())
		{
			this.target = null;
		}
		if (this.fightingWith != null && (this.fightingWith.isDead() || this.tickFightingWith++ > 12))
		{
			this.fightingWith = null;
			this.tickFightingWith = 0;
			this.target = null;
		}

		if (scriptEnabled && (scriptFuture == null || scriptFuture.isDone()))
		{
			if (this.hardReset)
			{
				this.hardReset = false;
				this.npcName = config.npcName();
				this.npcLevel = config.npcLevel();
				this.targets.clear();
				this.forceNPCRefresh();
			}
			this.task = getNextTask();
			if (!task.equals(NONE))
			{
				log.debug("Next Task -> {}", this.task.name());
			}
			switch (this.task)
			{
				case DELAY_SHORT:
					this.delay = false;
					scriptFuture = threadExec.schedule(() -> log.debug("Short delay"), 1000, TimeUnit.MILLISECONDS);
					break;
				case DELAY_LONG:
					this.delayLong = false;
					scriptFuture = threadExec.schedule(() -> log.debug("Long delay"), 3000, TimeUnit.MILLISECONDS);
					break;
				case OPEN_INV:
					scriptFuture = threadExec.schedule(() -> flexo.keyPress(TabUtils.getTabHotkey(Tab.INVENTORY, client)), 0, TimeUnit.MILLISECONDS);
					break;
				case OPEN_EQUIP:
					scriptFuture = threadExec.schedule(() -> flexo.keyPress(TabUtils.getTabHotkey(Tab.EQUIPMENT, client)), 0, TimeUnit.MILLISECONDS);
					break;
				case EAT_FOOD:
					scriptFuture = threadExec.schedule(
						() -> utils.handleInventoryClick(Objects.requireNonNull(WidgetUtils.getItemWidget(FOOD_IDS, client, true)).getCanvasBounds(), true), 0, TimeUnit.MILLISECONDS);
					break;
				case TELEPORT_EQUIP:
					scriptFuture = threadExec.schedule(
						() -> utils.handleInventoryClick(Objects.requireNonNull(WidgetUtils.getEquippedWidget(ARDY_CLOAK_IDS, client, true)).getBounds(), true), 0, TimeUnit.MILLISECONDS);
					break;
				case TELEPORT_INV:
					scriptFuture = threadExec.schedule(
						() -> utils.handleInventoryClick(Objects.requireNonNull(WidgetUtils.getItemWidget(ARDY_CLOAK_IDS, client, true)).getCanvasBounds(), true), 0, TimeUnit.MILLISECONDS);
					break;
				case WALK_CLOSER_TO_TARGET:
					scriptFuture = threadExec.schedule(() -> utils.handlePointClick(target.getMinimapLocation(), 7, true), 0, TimeUnit.MILLISECONDS);
					this.delay = true;
					break;
				case TURN_CAMERA_TO_TARGET:
					scriptFuture = threadExec.schedule(() -> camera.turnTo(target, 45), 0, TimeUnit.MILLISECONDS);
					break;
				case ATTACK_TARGET:
					scriptFuture = threadExec.schedule(() -> utils.handleInventoryClick(target.getConvexHull().getBounds(), true), 0, TimeUnit.MILLISECONDS);
					break;
				case GET_TARGET:
					this.target = getNextTarget();
					break;
				default:
					break;
			}
		}
	}

	private TaskEnum getNextTask()
	{
		if (scriptPaused)
		{
			return DELAY_SHORT;
		}
		else if (debug1)
		{
			debug1 = false;
			return NONE;
		}
		else if (debug2)
		{
			debug2 = false;
			return NONE;
		}
		else if (debug3)
		{
			debug3 = false;
			return NONE;
		}
		else if (debug4)
		{
			debug4 = false;
			return NONE;
		}
		else if (client.getBoostedSkillLevel(HITPOINTS) < (client.getRealSkillLevel(HITPOINTS) - 16))
		{
			if (WidgetUtils.hasItemInInventory(client, FOOD_IDS))
			{
				if (client.getWidget(WidgetInfo.INVENTORY).isHidden())
				{
					return OPEN_INV;
				}
				else
				{
					return EAT_FOOD;
				}
			}
			else if (WidgetUtils.hasItemInInventory(client, ARDY_CLOAK_IDS))
			{
				if (client.getWidget(WidgetInfo.INVENTORY).isHidden())
				{
					return OPEN_INV;
				}
				else
				{
					return TELEPORT_INV;
				}
			}
			else if (WidgetUtils.hasItemEquipped(client, ARDY_CLOAK_IDS))
			{
				if (client.getWidget(WidgetInfo.EQUIPMENT).isHidden())
				{
					return OPEN_EQUIP;
				}
				else
				{
					return TELEPORT_EQUIP;
				}
			}
			else
			{
				return INVALID;
			}
		}
		else if (this.delay)
		{
			return DELAY_SHORT;
		}
		else if (this.delayLong)
		{
			return DELAY_LONG;
		}
		else if (this.fightingWith == null)
		{
			if (this.target != null)
			{
				if (Math.abs(camera.getAngleToTarget(target)) > 90)
				{
					return TURN_CAMERA_TO_TARGET;
				}
				else if (isOnScreen(this.target))
				{
					return ATTACK_TARGET;
				}
				else
				{
					return WALK_CLOSER_TO_TARGET;
				}
			}
			else
			{
				return GET_TARGET;
			}
		}
		return NONE;
	}

	private boolean isOnScreen(NPC target)
	{
		if (target.getCanvasTilePoly() != null)
		{
			Canvas c = client.getCanvas();
			Rectangle2D r = target.getCanvasTilePoly().getBounds2D();
			boolean result = c.getBounds().contains(r);
			log.debug("Canvas: ({}, {}), ({}, {}) -> Object: ({}, {}), ({}, {}) = {}", c.getX(), c.getY(), c.getWidth(), c.getHeight(), r.getX(), r.getY(), r.getWidth(), r.getHeight(), result);
			return result;
		}
		return false;
	}

	private void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equalsIgnoreCase("FredFighter"))
		{
			return;
		}
		this.highlightTarget = config.highlightTarget();
		this.highlightTargets = config.highlightTargets();
		this.targetColor = config.targetColor();
		this.targetsColor = config.targetsColor();

		if (!this.npcName.equals(config.npcName()) || this.npcLevel != config.npcLevel())
		{
			this.hardReset = true;
		}
	}

	@Override
	protected void startUp()
	{
		threadExec = Executors.newSingleThreadScheduledExecutor();
		try
		{
			flexo = new Flexo();
		}
		catch (AWTException e)
		{
			e.printStackTrace();
		}
//		this.utils = new ExtUtils(client, flexo, configManager.getConfig(StretchedModeConfig.class));
		this.utils = new ExtUtils(client, flexo);

		this.highlightTarget = config.highlightTarget();
		this.highlightTargets = config.highlightTargets();
		this.targetColor = config.targetColor();
		this.targetsColor = config.targetsColor();

		this.npcName = config.npcName();
		this.npcLevel = config.npcLevel();

		this.targets = new ArrayList<>();
		this.camera = new FredCamController(client, flexo, 5000, 8);

		this.scriptEnabled = false;


		toggleScriptKeybind = new HotkeyListener(config::toggleScript)
		{
			@Override
			public void hotkeyPressed()
			{
				scriptEnabled = !scriptEnabled;
				if (!scriptEnabled)
				{
					target = null;
					fightingWith = null;
					targets.clear();
					scriptPaused = false;
				}
				else
				{
					hardReset = true;
				}
			}
		};

		togglePauseKeybind = new HotkeyListener(config::togglePaused)
		{
			@Override
			public void hotkeyPressed()
			{
				if (!scriptEnabled)
				{
					return;
				}
				scriptPaused = !scriptPaused;
//				if (scriptPaused)
//				{
//
//				}
//				else
//				{
//
//				}
			}
		};

		triggerDebug1Keybind = new HotkeyListener(config::debugKey1)
		{
			@Override
			public void hotkeyPressed()
			{
				debug1 = true;
			}
		};
		triggerDebug2Keybind = new HotkeyListener(config::debugKey2)
		{
			@Override
			public void hotkeyPressed()
			{
				debug2 = true;
			}
		};
		triggerDebug3Keybind = new HotkeyListener(config::debugKey3)
		{
			@Override
			public void hotkeyPressed()
			{
				debug3 = true;
			}
		};
		triggerDebug4Keybind = new HotkeyListener(config::debugKey4)
		{
			@Override
			public void hotkeyPressed()
			{
				debug4 = true;
			}
		};

		keyManager.registerKeyListener(toggleScriptKeybind);
		keyManager.registerKeyListener(togglePauseKeybind);
		keyManager.registerKeyListener(triggerDebug1Keybind);
		keyManager.registerKeyListener(triggerDebug2Keybind);
		keyManager.registerKeyListener(triggerDebug3Keybind);
		keyManager.registerKeyListener(triggerDebug4Keybind);

		eventBus.subscribe(HitsplatApplied.class, this, this::onHitsplatApplied);
		eventBus.subscribe(NpcSpawned.class, this, this::onNpcSpawned);
		eventBus.subscribe(NpcDefinitionChanged.class, this, this::onNpcDefinitionChanged);
		eventBus.subscribe(NpcDespawned.class, this, this::onNpcDespawned);
		eventBus.subscribe(GameTick.class, this, this::onGameTick);
		eventBus.subscribe(MenuOptionClicked.class, this, this::onMenuOptionClicked);
		eventBus.subscribe(ConfigChanged.class, this, this::onConfigChanged);

		overlayManager.add(fighterOverlay);
		overlayManager.add(fighterOverlayPanel);
	}

	@Override
	protected void shutDown() throws Exception
	{
		this.threadExec.shutdown();
		this.threadExec.awaitTermination(1000, TimeUnit.MILLISECONDS);
		this.scriptFuture = null;
		this.scriptEnabled = false;
		this.scriptPaused = false;
		this.hardReset = false;
		this.targets.clear();
		this.targets = null;
		this.debug1 = false;
		this.debug2 = false;
		this.debug3 = false;
		this.debug4 = false;

		overlayManager.remove(fighterOverlay);
		overlayManager.remove(fighterOverlayPanel);
		keyManager.unregisterKeyListener(toggleScriptKeybind);
		keyManager.unregisterKeyListener(togglePauseKeybind);
		keyManager.unregisterKeyListener(triggerDebug1Keybind);
		keyManager.unregisterKeyListener(triggerDebug2Keybind);
		keyManager.unregisterKeyListener(triggerDebug3Keybind);
		keyManager.unregisterKeyListener(triggerDebug4Keybind);
		eventBus.unregister(this);
	}
}
