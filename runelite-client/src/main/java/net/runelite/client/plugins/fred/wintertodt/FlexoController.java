package net.runelite.client.plugins.fred.wintertodt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ObjectDefinition;
import net.runelite.api.Scene;
import net.runelite.api.Skill;
import net.runelite.api.Tile;
import net.runelite.client.events.ConfigChanged;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.flexo.Flexo;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.fred.util.ExtUtils;
import net.runelite.client.plugins.fred.util.WidgetUtils;
import net.runelite.client.util.HotkeyListener;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.runelite.api.ItemID.*;
import static net.runelite.api.ObjectID.*;
import static net.runelite.client.plugins.fred.wintertodt.FredWinterPlugin.WINTERTODT_KINDLING_MULTIPLIER;
import static net.runelite.client.plugins.fred.wintertodt.FredWinterPlugin.WINTERTODT_ROOTS_MULTIPLIER;

/**
 * Created by npruff on 8/7/2019.
 * RSBOT
 */
@Singleton
@Slf4j
public class FlexoController
{
	private static final int[] food_ids = new int[]{TROUT, SLICE_OF_CAKE, _23_CAKE, CAKE};
	private static final Set<Integer> BRAZIER_IDS = new HashSet<>(Arrays.asList(BURNING_BRAZIER_29314, BRAZIER_29312, BRAZIER_29313));
	private static final Set<Integer> ROOTS_IDS = new HashSet<>(Collections.singletonList(BRUMA_ROOTS));

	private final FredWinterPlugin plugin;
	private final Client client;
	private final EventBus eventBus;
	private final KeyManager keyManager;
	private final ConfigManager configManager;
	private final FredWinterConfig config;
	private ExtUtils utils;

	//@Getter
	//private final List<GameObject> objects = new ArrayList<>();

//	private boolean doActions = false;

	@Inject
	private FlexoOverlay flexoOverlay;

//	@Getter
//	@Setter
//	private boolean shouldEat = false;

	@Getter
	@Setter
	private GameObject target_brazier;

	@Getter
	@Setter
	private GameObject target_root;

	private Future futureFlexo = null;

	private Flexo flexo;
	private BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(1);
	private ThreadPoolExecutor executorService = new ThreadPoolExecutor(1, 1, 25, TimeUnit.SECONDS, queue,
			new ThreadPoolExecutor.DiscardPolicy());
	private final HotkeyListener triggerFletch;
	private final HotkeyListener triggerEat;
	private final HotkeyListener triggerFeed;
	private final HotkeyListener triggerFix;
	private final HotkeyListener triggerLight;
	private final HotkeyListener triggerChop;

//	private void assignTarget()
//	{
//		AtomicReference<GameObject> b_target = new AtomicReference<>();
//		AtomicReference<GameObject> r_target = new AtomicReference<>();
//		List<Integer> tmp = new ArrayList<>();
//		objects.stream().filter(f -> f.getId() == BURNING_BRAZIER_29314 || f.getId() == BRAZIER_29313 || f.getId() == BRAZIER_29312).forEach(object ->
//		{
//			final int distance = object.getWorldLocation().distanceTo(client.getLocalPlayer().getWorldArea());
//			tmp.add(distance);
//			int lowest = Collections.min(tmp);
//			if (distance == lowest)
//			{
//				b_target.set(object);
//			}
//		});
//		tmp.clear();
//		objects.stream().filter(f -> f.getId() == BRUMA_ROOTS).forEach(object ->
//		{
//			final int distance = object.getWorldLocation().distanceTo(client.getLocalPlayer().getWorldArea());
//			tmp.add(distance);
//			int lowest = Collections.min(tmp);
//			if (distance == lowest)
//			{
//				r_target.set(object);
//			}
//		});
//		tmp.clear();
//		setBrazierTarget(b_target.get());
//		setRootsTarget(r_target.get());
//	}

	private GameObject getClosestObject(Set<Integer> objectIDs)
	{
		if (client.getLocalPlayer() == null)
		{
			return null;
		}
		final Scene scene = client.getScene();
		final Tile[][] tiles = scene.getTiles()[client.getPlane()];
		final ArrayList<GameObject> found = new ArrayList<>();

		for (Tile[] tiles2 : tiles)
		{
			for (Tile tile : tiles2)
			{
				for (GameObject object : tile.getGameObjects())
				{
					if (object == null)
					{
						continue;
					}

					if (objectIDs.contains(object.getId()))
					{
						found.add(object);
						continue;
					}

					// Check impostors
					final ObjectDefinition comp = client.getObjectDefinition(object.getId());
					final ObjectDefinition impostor = comp.getImpostorIds() != null ? comp.getImpostor() : comp;

					if (impostor != null && objectIDs.contains(impostor.getId()))
					{
						found.add(object);
					}
				}
			}
		}
		List<Integer> tmp = new ArrayList<>();
		AtomicReference<GameObject> target = new AtomicReference<>(null);
		found.forEach(object ->
		{
			final int distance = object.getWorldLocation().distanceTo(client.getLocalPlayer().getWorldArea());
			tmp.add(distance);
			int lowest = Collections.min(tmp);
			if (distance == lowest)
			{
				target.set(object);
			}
		});
		return target.get();
	}

	@Inject
	private FlexoController(final FredWinterPlugin plugin, final EventBus eventBus, final KeyManager keyManager, final FredWinterConfig config, final Client client, final ConfigManager configManager)
	{
		this.plugin = plugin;
		this.eventBus = eventBus;
		this.keyManager = keyManager;
		this.config = config;
		this.client = client;
		this.configManager = configManager;

		triggerFletch = new HotkeyListener(config::triggerFletch)
		{
			@Override
			public void hotkeyPressed()
			{
//				if (canFletch())
//				{
					doFletchLogs();
//				} else {
//					log.warn("Cant Fletch!");
//				}
			}
		};
		triggerEat = new HotkeyListener(config::triggerEat)
		{
			@Override
			public void hotkeyPressed()
			{
//				if (canEat())
//				{
					doEat();
//				} else {
//					log.warn("Cant Eat!");
//				}
			}
		};
		triggerFeed = new HotkeyListener(config::triggerFeed)
		{
			@Override
			public void hotkeyPressed()
			{
//				if (canFeed())
//				{
					doFeed(getClosestObject(BRAZIER_IDS));
//				} else {
//					log.warn("Cant Feed!");
//				}
			}
		};
		triggerFix = new HotkeyListener(config::triggerFix)
		{
			@Override
			public void hotkeyPressed()
			{
//				if (canFix())
//				{
					doFix(getClosestObject(BRAZIER_IDS));
//				} else {
//					log.warn("Cant Fix!");
//				}
			}
		};
		triggerLight = new HotkeyListener(config::triggerLight)
		{
			@Override
			public void hotkeyPressed()
			{
//				if (canLight())
//				{
				doLight(getClosestObject(BRAZIER_IDS));
//				} else {
//					log.warn("Cant Light!");
//				}
			}
		};
		triggerChop = new HotkeyListener(config::triggerChop)
		{
			@Override
			public void hotkeyPressed()
			{
				doChop(getClosestObject(ROOTS_IDS));
			}
		};
	}

	protected void startUp()
	{
		//Flexo.client = client;
		keyManager.registerKeyListener(triggerFletch);
		keyManager.registerKeyListener(triggerEat);
		keyManager.registerKeyListener(triggerFeed);
		keyManager.registerKeyListener(triggerLight);
		keyManager.registerKeyListener(triggerFix);
		keyManager.registerKeyListener(triggerChop);
//		doActions = config.autoTrigger();
		futureFlexo = executorService.submit(() ->
		{
			flexo = null;
			try
			{
				flexo = new Flexo();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
//			this.utils = new ExtUtils(plugin.getClient(), flexo, configManager.getConfig(StretchedModeConfig.class));
			this.utils = new ExtUtils(plugin.getClient(), flexo);
		});
		plugin.getOverlayManager().add(flexoOverlay);
	}

	protected void shutDown()
	{
		keyManager.unregisterKeyListener(triggerFletch);
		keyManager.unregisterKeyListener(triggerEat);
		keyManager.unregisterKeyListener(triggerFeed);
		keyManager.unregisterKeyListener(triggerLight);
		keyManager.unregisterKeyListener(triggerFix);
		keyManager.unregisterKeyListener(triggerChop);
		eventBus.unregister(this);
		eventBus.unregister("fred-inside-wintertodt-flexo");
		plugin.getOverlayManager().remove(flexoOverlay);
	}

	protected void reset()
	{
//		shouldEat = false;
//		doActions = config.autoTrigger();
	}

	protected void addSubscriptions()
	{
		eventBus.subscribe(ConfigChanged.class, this, this::onConfigChanged);
		//eventBus.subscribe(GameStateChanged.class, this, this::onGameStateChanged);
	}

	void wintertodtSubscriptions(boolean subscribe)
	{
		if (subscribe)
		{
//			eventBus.subscribe(GameTick.class, "fred-inside-wintertodt-flexo", this::onGameTick);
//			eventBus.subscribe(GameObjectChanged.class, "fred-inside-wintertodt-flexo", this::onGameObjectChanged);
//			eventBus.subscribe(GameObjectSpawned.class, "fred-inside-wintertodt-flexo", this::onGameObjectSpawned);
//			eventBus.subscribe(GameObjectDespawned.class, "fred-inside-wintertodt-flexo", this::onGameObjectDespawned);
		}
		else
		{
			eventBus.unregister("fred-inside-wintertodt-flexo");
		}
	}

	private void onConfigChanged(ConfigChanged event)
	{
//		doActions = plugin.getConfig().autoTrigger();
	}

//	private boolean addObjectToList(GameObject obj)
//	{
//		if (obj.getId() == BURNING_BRAZIER_29314 || obj.getId() == BRAZIER_29313 || obj.getId() == BRAZIER_29312 || obj.getId() == BRUMA_ROOTS)
//		{
//			objects.add(obj);
//			return true;
//		}
//		return false;
//	}
//
//	private boolean removeObjectFromList(GameObject obj)
//	{
//		if (obj.getId() == BURNING_BRAZIER_29314 || obj.getId() == BRAZIER_29313 || obj.getId() == BRAZIER_29312 || obj.getId() == BRUMA_ROOTS)
//		{
//			objects.remove(obj);
//			return true;
//		}
//		return false;
//	}

//	public void onGameObjectSpawned(GameObjectSpawned event)
//	{
//		if (addObjectToList(event.getGameObject()))
//		{
//			this.assignTarget();
//		}
//	}
//
//	public void onGameObjectDespawned(GameObjectDespawned event)
//	{
//		if (removeObjectFromList(event.getGameObject()))
//		{
//			this.assignTarget();
//		}
//	}
//
//	public void onGameObjectChanged(GameObjectChanged event)
//	{
//		GameObject previous = event.getPrevious();
//		GameObject obj = event.getGameObject();
//		System.out.println(event.getPrevious().getId() + "|" + event.getGameObject().getId());
//		boolean t1 = this.addObjectToList(obj);
//		boolean t2 = this.removeObjectFromList(previous);
//		if (t1 || t2) {
//			assignTarget();
//		}
//	}

//	public void onGameStateChanged(GameStateChanged event)
//	{
//		if (event.getGameState() == GameState.LOGGED_IN)
//		{
//			return;
//		}
//
//		objects.clear();
//	}

//	private void onGameTick(GameTick event)
//	{
//		if (!futureFlexo.isDone()) {
//			return;
//		}
//
//		if (!doActions) {
//			return;
//		}
//
//		if (shouldEat)
//		{
//			if (client.getBoostedSkillLevel(Skill.HITPOINTS) < 28)
//			{
//				log.debug("Eat!");
//				doEat(false);
//			} else {
//				shouldEat = false;
//			}
//		}
//		else
//		{
//			if (plugin.getCurrentActivity().equals(FredWinterActivity.IDLE))
//			{
//				if (canFletch())
//				{
//					log.debug("Fletch!");
//					//TODO: Check how many points we have
//					doFletchLogs(false);
//				} else if (canFeed())
//				{
//					log.debug("Feed!");
//					doFeed(false);
//				} else if (canFix())
//				{
//					log.debug("Fix!");
//					doFix(false);
//				} else if (canLight())
//				{
//					log.debug("Light!");
//					doLight(false);
//				} else {
//					log.error("Panic!!!!!!!");
//				}
//			}
//		}
//	}

	private static <E> E getRandom(List<E> options)
	{
		return (options != null && options.size() > 0) ? (options.get((int) (Math.random() * (double)options.size()))) : null;
	}

	private static WidgetItem getFoodToEat(List<WidgetItem> options)
	{
		List<WidgetItem> foodToEat = new ArrayList<WidgetItem>();
		for (int i = 0; i < food_ids.length; i++)
		{
			final int id = food_ids[i];
			foodToEat = options.stream().filter(f -> f.getId() == id).collect(Collectors.toList());
			if (foodToEat.size() > 0)
			{
				break;
			}
		}
		return getRandom(foodToEat);
	}

	private boolean canLight(final GameObject o)
	{
		if (!client.getGameState().equals(GameState.LOGGED_IN))
		{
			log.warn("failed!");
			return false;
		}
		try
		{
			return plugin.isHasTinderbox() && o != null && o.getId() == BRAZIER_29312 && !plugin.getCurrentActivity().equals(FredWinterActivity.LIGHTING_BRAZIER);
		}
		catch (Exception e)
		{
			return false;
		}
	}

	private void doLight(final GameObject o)
	{
//		if (!futureFlexo.isDone()) {
//			return;
//		}
		setTarget_brazier(o);
		if (canLight(o))
		{
			futureFlexo = executorService.submit(() ->
			{
				utils.handleInventoryClick(o.getConvexHull().getBounds(), true);
//				flexo.delay(250+rand.nextInt(250));
			});
		}
	}

	private boolean canFix(final GameObject o)
	{
		if (!client.getGameState().equals(GameState.LOGGED_IN))
		{
			log.warn("failed!");
			return false;
		}
		try
		{
			return plugin.isHasHammer() && o != null && o.getId() == BRAZIER_29313 && !plugin.getCurrentActivity().equals(FredWinterActivity.FIXING_BRAZIER);
		}
		catch (Exception e)
		{
			return false;
		}
	}

	private void doFix(final GameObject o)
	{
//		if (!futureFlexo.isDone()) {
//			return;
//		}
		setTarget_brazier(o);
		if (canFix(o))
		{
			futureFlexo = executorService.submit(() ->
			{
				utils.handleInventoryClick(o.getConvexHull().getBounds(), true);
//				flexo.delay(250+rand.nextInt(250));
			});
		}
	}

	private boolean canFeed(final GameObject o)
	{
		if (!client.getGameState().equals(GameState.LOGGED_IN))
		{
			log.warn("failed!");
			return false;
		}
		try
		{
			return (plugin.getNumRoots() > 0 || plugin.getNumKindling() > 0) && !plugin.getCurrentActivity().equals(FredWinterActivity.FEEDING_BRAZIER) && o != null && o.getId() == BURNING_BRAZIER_29314;
		}
		catch (Exception e)
		{
			return false;
		}
	}

	private void doFeed(final GameObject o)
	{
//		if (!futureFlexo.isDone()) {
//			return;
//		}
		setTarget_brazier(o);
		if (canFeed(o))
		{
			futureFlexo = executorService.submit(() ->
			{
				utils.handleInventoryClick(o.getConvexHull().getBounds(), true);
//				flexo.delay(250+rand.nextInt(250));
			});
		}
	}

	private boolean canChop(final GameObject o)
	{
		if (!client.getGameState().equals(GameState.LOGGED_IN))
		{
			log.warn("failed!");
			return false;
		}
		try
		{
			int i = client.getWidget(WidgetInfo.INVENTORY).getWidgetItems().size();
			log.debug("Chop - Items - " + i);
			return !plugin.getCurrentActivity().equals(FredWinterActivity.WOODCUTTING) && o != null && o.getId() == BRUMA_ROOTS && i < 28;
		}
		catch (Exception e)
		{
			log.error("CanChop", e.getCause());
			return false;
		}
	}

	private void doChop(final GameObject o)
	{
//		if (!futureFlexo.isDone()) {
//			return;
//		}
		setTarget_root(o);
		if (canChop(o))
		{
			futureFlexo = executorService.submit(() ->
			{
				utils.handleInventoryClick(o.getConvexHull().getBounds(), true);
			});
		}
	}

	private boolean canEat()
	{
		ItemContainer i = client.getItemContainer(InventoryID.INVENTORY);
		Stream<Item> items = Arrays.stream((i != null) ? i.getItems() : new Item[0]);
		return (client.getBoostedSkillLevel(Skill.HITPOINTS) < 24) && items.anyMatch(x ->
		{
			boolean t = false;
			for (int f : food_ids)
			{
				if (x.getId() == f)
				{
					t = true;
					break;
				}
			}
			return t;
		});
	}

	private void doEat()
	{
//		if (!futureFlexo.isDone()) {
//			return;
//		}
		if (canEat())
		{
			WidgetItem food = WidgetUtils.getItemWidget(food_ids, client, false);
			if (food == null)
			{
//				log.warn("noFood1");
				return;
			}
			futureFlexo = executorService.submit(() ->
			{
				utils.handleInventoryClick(food.getCanvasBounds(), true);
			});
		}
	}

	private boolean canFletch()
	{
		return (plugin.getNumRoots() > 0 && plugin.isHasKnife() && plugin.getCurrentActivity().equals(FredWinterActivity.IDLE) && (plugin.getPoints() + (plugin.getNumRoots() * WINTERTODT_ROOTS_MULTIPLIER) + (plugin.getNumKindling() * WINTERTODT_KINDLING_MULTIPLIER)) < 500);
	}

	private void doFletchLogs()
	{
//		if (!futureFlexo.isDone()) {
//			return;
//		}
		if (canFletch())
		{
			List<WidgetItem> logs = WidgetUtils.getItemWidgets(new int[]{BRUMA_ROOT}, client);
			List<WidgetItem> knifes = WidgetUtils.getItemWidgets(new int[]{KNIFE}, client);
			if (logs.size() == 0 || knifes.size() == 0 || plugin.getCurrentActivity().equals(FredWinterActivity.FLETCHING))
			{
				return;
			}


			WidgetItem log = logs.stream().filter(f -> f.getWidget().getBorderType() == 2).findFirst().orElse(getRandom(logs));
			WidgetItem knife = knifes.stream().filter(f -> f.getWidget().getBorderType() == 2).findFirst().orElse(getRandom(knifes));
			if (log == null || knife == null)
			{
				return;
			}
			futureFlexo = executorService.submit(() ->
			{
				if (log.getWidget().getBorderType() == 2)
				{
					utils.handleInventoryClick(knife.getCanvasBounds(), true);
				}
				else if (knife.getWidget().getBorderType() == 2)
				{
					utils.handleInventoryClick(log.getCanvasBounds(), true);
				}
				else
				{
					utils.handleInventoryClick(knife.getCanvasBounds(), true);
					flexo.delay();
					utils.handleInventoryClick(log.getCanvasBounds(), true);
				}
//				flexo.delay(250+rand.nextInt(250));
			});
		}
	}
}
