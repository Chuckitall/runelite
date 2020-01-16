package net.runelite.client.plugins.fred.npctalker;

import com.google.inject.Inject;
import com.google.inject.Provides;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;

import static java.awt.event.KeyEvent.VK_1;
import static java.awt.event.KeyEvent.VK_2;
import static java.awt.event.KeyEvent.VK_3;
import static java.awt.event.KeyEvent.VK_4;
import static java.awt.event.KeyEvent.VK_5;
import static java.awt.event.KeyEvent.VK_SPACE;
import static net.runelite.api.NpcID.ALI_MORRISANE;
import static net.runelite.api.NpcID.CAPTAIN_KHALED_6972;
import static net.runelite.api.widgets.WidgetID.*;
import static net.runelite.api.widgets.WidgetInfo.*;

import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.flexo.Flexo;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.ui.overlay.OverlayManager;
import org.jetbrains.annotations.Contract;

/**
 * Created by npruff on 9/2/2019.
 */

@PluginDescriptor(
	name = "Fred's Talker",
	description = "Talker/Interactor bot",
	tags = {"flexo", "utility", "bot", "fred"},
	type = PluginType.FRED
)
@Slf4j
public class TalkerPlugin extends Plugin
{
	static final String CONFIG_GROUP = "FredTalker";
	private static final Map<Integer, WidgetInfo> widgetIdMap = new HashMap<>();

	@Getter(AccessLevel.PACKAGE)
	@Inject
	private Client client;

	@Getter(AccessLevel.PUBLIC)
	@Inject
	private TalkerConfig config;

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
	@Getter(AccessLevel.PUBLIC)
	private ConfigManager configManager;

	@Inject
	@Getter(AccessLevel.PACKAGE)
	private TalkerOverlay talkerOverlay;

	@Getter(AccessLevel.PUBLIC)
	private Flexo flexo;
	private BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(1);

	@Getter(AccessLevel.PUBLIC)
	private ThreadPoolExecutor executorService = new ThreadPoolExecutor(1, 1, 25, TimeUnit.SECONDS, queue,
		new ThreadPoolExecutor.DiscardPolicy());

	private final static Random r = new Random();

	@Getter(AccessLevel.PACKAGE)
	private boolean loggedIn;

	@Getter(AccessLevel.PACKAGE)
	private boolean dirty_DIALOG_NPC = false;

	@Getter(AccessLevel.PACKAGE)
	private boolean dirty_DIALOG_PLAYER = false;

	@Getter(AccessLevel.PACKAGE)
	private boolean dirty_DIALOG_OPTION = false;

	@Getter(AccessLevel.PACKAGE)
	private NPC interactingWith = null;

	@Getter
	@AllArgsConstructor
	enum FLEXO_CHAT_KEY
	{
		NULL(-1, -1),
		SPACE(0, VK_SPACE),
		ONE(1, VK_1),
		TWO(2, VK_2),
		THREE(3, VK_3),
		FOUR(4, VK_4),
		FIVE(5, VK_5);

		int value;
		int keycode;
	}

	@Contract(pure = true)
	private boolean stateInvalid()
	{
		return (flexo == null || !loggedIn || !config.speedThroughDialog());
	}

	@Contract(pure = true)
	private boolean isDirty()
	{
		return dirty_DIALOG_NPC || dirty_DIALOG_PLAYER || dirty_DIALOG_OPTION;
	}

	private void setClean()
	{
		dirty_DIALOG_PLAYER = false;
		dirty_DIALOG_NPC = false;
		dirty_DIALOG_OPTION = false;
	}

	private FLEXO_CHAT_KEY getOptionToChoose(Widget parent)
	{
		int group = parent != null ? TO_GROUP(parent.getId()) : -1;
		log.debug("Get option w/ widget group {}", group);
		if (parent == null || group != DIALOG_OPTION_GROUP_ID || interactingWith == null) return FLEXO_CHAT_KEY.NULL;
		Widget[] dChild = parent.getDynamicChildren();
		if (interactingWith.getId() == ALI_MORRISANE)
		{
			if (dChild.length > 1 && dChild[1] != null && dChild[1].getText().equalsIgnoreCase("I would like to have a look at your selection of runes."))
				return FLEXO_CHAT_KEY.ONE;
			else if (dChild.length > 4 && dChild[4] != null && dChild[4].getText().equalsIgnoreCase("Buy other runes."))
				return FLEXO_CHAT_KEY.FOUR;
		}
		else if(interactingWith.getId() == CAPTAIN_KHALED_6972)
		{
			if (dChild.length > 1 && dChild[1] != null && dChild[1].getText().equalsIgnoreCase("I have what it takes."))
			{
				return FLEXO_CHAT_KEY.ONE;
			}
			else if (dChild.length > 2 && dChild[2] != null && dChild[2].getText().equalsIgnoreCase("Looking for any help?"))
			{
				return FLEXO_CHAT_KEY.TWO;
			}
		}

		//hooks for talking to beanshell plugin
		return FLEXO_CHAT_KEY.NULL;
	}

	@Provides
	TalkerConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TalkerConfig.class);
	}

	private void startFlexo()
	{
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
		});
	}

	private void stopFlexo()
	{
		this.flexo = null;
	}

	@Override
	protected void startUp()
	{
		//Flexo.client = client;
		startFlexo();
		eventBus.subscribe(GameStateChanged.class, this, this::onGameStateChanged);
		eventBus.subscribe(WidgetLoaded.class, this, this::onWidgetLoaded);
		eventBus.subscribe(GameTick.class, this, this::onGameTick);
		eventBus.subscribe(InteractingChanged.class, this, this::onInteractingChanged);
		overlayManager.add(talkerOverlay);

		//Initial state
		loggedIn = client.getGameState() == GameState.LOGGED_IN;
	}

	@Override
	protected void shutDown() throws Exception
	{
		setClean();
		stopFlexo();
		eventBus.unregister(this);
		overlayManager.remove(talkerOverlay);
	}

	private void onWidgetLoaded(WidgetLoaded widgetLoaded)
	{
		int g = widgetLoaded.getGroupId();
		if (stateInvalid()) return;

		//log.debug("WidgetLoaded -> {}, {}", widgetLoaded.toString(), widgetLoaded.getGroupId());
		dirty_DIALOG_NPC = dirty_DIALOG_NPC || (g == DIALOG_NPC_GROUP_ID);
		dirty_DIALOG_PLAYER = dirty_DIALOG_PLAYER || (g == DIALOG_PLAYER_GROUP_ID);
		dirty_DIALOG_OPTION = dirty_DIALOG_OPTION || (g == DIALOG_OPTION_GROUP_ID);
		//dirty quest action dialot
	}

	private void onGameTick(GameTick event)
	{
		if (stateInvalid()) return;
		if (!isDirty()) return;
		//log.debug("Game Tick!");
		//if (dirty_DIALOG_PLAYER) debugWidget(client.getWidget(DIALOG_PLAYER));
		//if (dirty_DIALOG_NPC)    debugWidget(client.getWidget(DIALOG_NPC));
		if (dirty_DIALOG_OPTION) debugWidget(client.getWidget(DIALOG_OPTION).getParent());

		final FLEXO_CHAT_KEY key = (dirty_DIALOG_PLAYER || dirty_DIALOG_NPC) ? FLEXO_CHAT_KEY.SPACE : (dirty_DIALOG_OPTION ? getOptionToChoose(client.getWidget(DIALOG_OPTION).getParent()) : FLEXO_CHAT_KEY.NULL);
		log.debug("Pressing {} in response to widget!", key.name());
		if (key != FLEXO_CHAT_KEY.NULL)
		{
			executorService.submit(() ->
			{
				flexo.delay((int) getMillis());
				flexo.holdKey(key.keycode, r.nextInt(35) + (r.nextInt(3) * r.nextInt(10)));
			});
		}

		setClean();
	}

	private void onGameStateChanged(GameStateChanged event)
	{
		loggedIn = event.getGameState() == GameState.LOGGED_IN;
	}

	private void onInteractingChanged(InteractingChanged event)
	{
		if (event.getSource() != client.getLocalPlayer())
		{
			return;
		}

		final Actor target = event.getTarget();

		if (!(target instanceof NPC))
		{
			return;
		}

		log.debug("InteractingChanged -> target: {}, source: {}", event.getTarget().toString(), event.getSource().toString());
		interactingWith = (NPC) target;
	}

	private static WidgetInfo getWidgetInfo(int packedId)
	{
		if (widgetIdMap.isEmpty())
		{
			//Initialize map here so it doesn't create the index
			//until it's actually needed.
			WidgetInfo[] widgets = WidgetInfo.values();
			for (WidgetInfo w : widgets)
			{
				widgetIdMap.put(w.getPackedId(), w);
			}
		}

		return widgetIdMap.get(packedId);
	}


	private void debugWidget(Widget w)
	{
		if (w == null) return;
		boolean foundChildren = false;
		StringBuilder result = new StringBuilder(String.format("WidgetDebug -> %s {", getWidgetName(w.getId())));
		for (Widget c : w.getStaticChildren())
		{
			if (c == null || c.getText().equals("")) continue;
			result.append(String.format("\n\t%s -> %s", getWidgetName(c.getId()), c.getText()));
			foundChildren = true;
		}
		Widget[] dChilds = w.getDynamicChildren();
		for (int i = 0; i < dChilds.length; i++)
		{
			if (dChilds[i] == null || dChilds[i].getText().equals("")) continue;
			result.append(String.format("\n\t%s[%d] -> %s", getWidgetName(w.getId()), i, dChilds[i].getText()));
			foundChildren = true;
		}
		result.append("\n}");
		if (foundChildren)
		{
			log.debug(result.toString());
		}
	}

	private String getWidgetName(int packed)
	{
		String str = Integer.toString(packed);
		WidgetInfo info = getWidgetInfo(packed);
		if (info != null)
		{
			str += " [" + info.name() + "]";
		}

		return str;
	}

	public long getMillis()
	{
		return (long) (Math.random() * config.randLow() + config.randHigh());
	}
}
