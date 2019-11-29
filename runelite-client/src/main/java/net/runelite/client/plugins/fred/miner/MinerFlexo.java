package net.runelite.client.plugins.fred.miner;

import com.google.inject.Inject;
import java.awt.Canvas;
import java.awt.geom.Rectangle2D;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.ObjectID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.flexo.Flexo;
import net.runelite.client.plugins.fred.util.ExtUtils;
import net.runelite.client.plugins.fred.util.Tab;
import net.runelite.client.plugins.fred.util.TabUtils;
import net.runelite.client.plugins.fred.util.WidgetUtils;
import net.runelite.client.plugins.fred.util.WorldUtils;
import net.runelite.client.util.HotkeyListener;

import javax.inject.Singleton;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * Created by npruff on 8/24/2019.
 */
@Singleton
@Slf4j
class MinerFlexo
{
	private final MinerPlugin plugin;

	private final MinerConfig config;

	private final ConfigManager configManager;

	private WorldUtils worldUtil;

	private Flexo flexo;
	private BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(1);
	private ThreadPoolExecutor executorService = new ThreadPoolExecutor(1, 1, 25, TimeUnit.SECONDS, queue,
			new ThreadPoolExecutor.DiscardPolicy());

	private final HotkeyListener triggerMineAction;
	private final HotkeyListener triggerTeleportAction;
	private final HotkeyListener triggerBankAction;
	private ExtUtils utils;
//	private final HotkeyListener triggerTest;
	//private final HotkeyListener triggerLight;
	//private final HotkeyListener triggerChop;
	@Inject
	private MinerFlexo(final MinerPlugin plugin, final MinerConfig config, final ConfigManager configManager)
	{
		this.plugin = plugin;
		this.config = config;
		this.configManager = configManager;
		this.worldUtil = new WorldUtils(plugin.getClient());

		triggerMineAction = new HotkeyListener(config::triggerMine)
		{
			@Override
			public void hotkeyPressed()
			{
				doMine();
			}
		};

		triggerTeleportAction = new HotkeyListener(config::triggerTeleport)
		{
			@Override
			public void hotkeyPressed()
			{
				doTeleport();
			}
		};
		triggerBankAction = new HotkeyListener(config::triggerBank)
		{
			@Override
			public void hotkeyPressed()
			{
				doBank();
			}
		};

		executorService.submit(() -> 
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
	}

	void startup()
	{
		plugin.getKeyManager().registerKeyListener(triggerMineAction);
		plugin.getKeyManager().registerKeyListener(triggerTeleportAction);
		plugin.getKeyManager().registerKeyListener(triggerBankAction);
//		plugin.getKeyManager().registerKeyListener(triggerTest);
	}

	void shutdown()
	{
		plugin.getKeyManager().unregisterKeyListener(triggerMineAction);
		plugin.getKeyManager().unregisterKeyListener(triggerTeleportAction);
		plugin.getKeyManager().unregisterKeyListener(triggerBankAction);
//		plugin.getKeyManager().unregisterKeyListener(triggerTest);
	}

	boolean doTeleport()
	{
		if (!plugin.getClient().getGameState().equals(GameState.LOGGED_IN))
		{
			return false;
		}
		if (!plugin.isHasTele() || !plugin.isHasCloak())
		{
			return false;
		}
		if (plugin.getRegionID() == MinerPlugin.ARDY_REGION && (WidgetUtils.getFreeInventorySpaces(plugin.getClient()) > 0 || !plugin.isHasTele()))
		{
			return false;
		}
		if ((plugin.getRegionID() == MinerPlugin.CASTLE_WARS_REGION || plugin.getRegionID() == MinerPlugin.WINTERTODT_REGION) && (plugin.getOresCount() > 0 || !plugin.isHasCloak()))
		{
			return false;
		}
		Widget equipment = plugin.getClient().getWidget(WidgetInfo.EQUIPMENT);

		if (equipment == null)
		{
			return false;
		}
		log.debug("doTeleport");
		executorService.submit(() -> 
		{
			if (equipment.isHidden())
			{
				flexo.keyPress(TabUtils.getTabHotkey(Tab.EQUIPMENT, plugin.getClient()));
			}
			Widget game = WidgetUtils.getEquippedWidget(MinerPlugin.GAMES_NECKLACE_IDS, plugin.getClient(), true);
			Widget duel = WidgetUtils.getEquippedWidget(MinerPlugin.DUEL_RING_IDS, plugin.getClient(), true);
			Widget cloak = WidgetUtils.getEquippedWidget(MinerPlugin.ARDY_CLOAK_IDS, plugin.getClient(), true);
			Widget target = (plugin.getRegionID() == MinerPlugin.ARDY_REGION) ? (game == null ? duel : game) : cloak;
			if (target != null && target.getBounds() != null)
			{
				utils.handleInventoryClick(target.getBounds(), true);
			}
		});
		return true;
	}

	boolean doMine()
	{
		if (!plugin.getClient().getGameState().equals(GameState.LOGGED_IN))
		{
			return false;
		}
		if (!plugin.isHasPickaxe())
		{
			return false;
		}
		if (WidgetUtils.getFreeInventorySpaces(plugin.getClient()) <= 0)
		{
			return false;
		}
		if (plugin.getCurrentActivity().equals(MinerActivity.MINING))
		{
			return false;
		}
		GameObject o = worldUtil.getClosestObject(plugin.getRockIDs().stream().mapToInt(f -> f).toArray(), 6);
		if (o == null)
		{
			return false;
		}
		if (o.getConvexHull() == null)
		{
			return false;
		}
		log.debug("doMine");
		plugin.setTargetRock(o);
		executorService.submit(() -> utils.handleInventoryClick(o.getConvexHull().getBounds(), true));
		return true;
	}

	private boolean isObjectOnScreen(GameObject o)
	{
		if (o == null)
		{
			return false;
		}
		boolean result;
		Canvas c = plugin.getClient().getCanvas();
		Rectangle2D r = o.getCanvasTilePoly().getBounds2D();
		result = c.getBounds().contains(r);
		log.debug("Canvas: ({}, {}), ({}, {}) -> Object: ({}, {}), ({}, {}) = {}", c.getX(), c.getY(), c.getWidth(), c.getHeight(), r.getX(), r.getY(), r.getWidth(), r.getHeight(), result);
		return result;
	}

	private boolean doBank()
	{
		if (!plugin.getClient().getGameState().equals(GameState.LOGGED_IN))
		{
			return false;
		}
		if (plugin.getRegionID() != MinerPlugin.CASTLE_WARS_REGION && plugin.getRegionID() != MinerPlugin.WINTERTODT_REGION)
		{
			return false;
		}
		if (plugin.getOresCount() == 0 && plugin.isHasTele())
		{
			return false;
		}
		GameObject o = worldUtil.getClosestObject(new int[] {ObjectID.BANK_CHEST_4483}, 16);
		if (o == null)
		{
			return false;
		}
		if (o.getCanvasTilePoly() == null)
		{
			return false;
		}
		if (!isObjectOnScreen(o))
		{
			return false;
		}


		log.debug("doBank");
		executorService.submit(() -> utils.handleInventoryClick(o.getCanvasTilePoly().getBounds(), true));
		return true;
	}

	boolean doBankAction()
	{
		return plugin.getClient().getGameState().equals(GameState.LOGGED_IN);
//		executorService.submit(() -> {
//			int targetx = 6726;
//			if(plugin.getClient().getCameraX() > targetx)
//			{
//				flexo.keyPress(VK_A);
//				int i = 0;
//				while (plugin.getClient().getCameraX() > targetx && i < 50)
//				{
//					i +  + ;
//					flexo.delay(50);
//				}
//				flexo.keyRelease(VK_A);
//			}
//		});
	}
}
