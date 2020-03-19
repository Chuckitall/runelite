package net.runelite.client.plugins.fred.npctalker;

import com.google.inject.Inject;
import com.google.inject.Provides;
import java.awt.Color;
import java.time.Instant;
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
import net.runelite.api.ChatMessageType;
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
import static net.runelite.api.widgets.WidgetID.*;
import static net.runelite.api.widgets.WidgetInfo.*;

import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
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
	private ChatMessageManager chatMessageManager;

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
	private boolean dirty_DIALOG_NOTIFICATION = false;

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
		return (flexo == null || !loggedIn);
	}

	@Contract(pure = true)
	private boolean isDirty()
	{
		return dirty_DIALOG_NPC || dirty_DIALOG_PLAYER || dirty_DIALOG_NOTIFICATION;
	}

	private void setClean()
	{
		dirty_DIALOG_PLAYER = false;
		dirty_DIALOG_NPC = false;
		dirty_DIALOG_NOTIFICATION = false;
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
//		overlayManager.add(talkerOverlay);



		//Initial state
		loggedIn = client.getGameState() == GameState.LOGGED_IN;
	}

	@Override
	protected void shutDown()
	{
		setClean();
		stopFlexo();
		eventBus.unregister(this);
//		overlayManager.remove(talkerOverlay);
	}

	private void onWidgetLoaded(WidgetLoaded widgetLoaded)
	{
		int g = widgetLoaded.getGroupId();
		if (stateInvalid()) return;

		//log.debug("WidgetLoaded -> {}, {}", widgetLoaded.toString(), widgetLoaded.getGroupId());
		dirty_DIALOG_NPC = dirty_DIALOG_NPC || (g == DIALOG_NPC_GROUP_ID);
		dirty_DIALOG_PLAYER = dirty_DIALOG_PLAYER || (g == DIALOG_PLAYER_GROUP_ID);
		dirty_DIALOG_NOTIFICATION = dirty_DIALOG_NOTIFICATION || (g == DIALOG_NOTIFICATION_GROUP_ID);
		//dirty quest action dialog
	}

	private void onGameTick(GameTick event)
	{
		if (stateInvalid()) return;
		if (!isDirty()) return;
		//log.debug("Game Tick!");
		//if (dirty_DIALOG_PLAYER) debugWidget(client.getWidget(DIALOG_PLAYER));
		//if (dirty_DIALOG_NPC)    debugWidget(client.getWidget(DIALOG_NPC));

		if (config.logDialogs())
		{

			String name = "NULL";
			String body = "NULL";
			Color color = Color.BLACK;
			if (dirty_DIALOG_NPC)
			{
				Widget npc_text = client.getWidget(DIALOG_NPC_TEXT);
				Widget npc_name = client.getWidget(DIALOG_NPC_NAME);
				color = config.npcColor();
				if (npc_text != null)
				{
					body = npc_text.getText();
				}
				if (npc_name != null)
				{
					name = npc_name.getText();
				}
			}
			else if (dirty_DIALOG_PLAYER)
			{
				Widget player_text = client.getWidget(DIALOG_PLAYER_TEXT);
				Widget player_name = client.getWidget(DIALOG_PLAYER_NAME);
				color = config.playerColor();
				if (player_text != null)
				{
					body = player_text.getText();
				}
				if (player_name != null)
				{
					name = player_name.getText();
				}
			}
			else if (dirty_DIALOG_NOTIFICATION)
			{
				Widget notification_text = client.getWidget(DIALOG_NOTIFICATION_TEXT);
				color = config.notificationColor();
				if (notification_text != null)
				{
					body = notification_text.getText();
				}
				name = "";
			}

			final String formattedMessage = new ChatMessageBuilder()
				.append(ChatColorType.NORMAL)
				.append(color, body)
				.build();

			chatMessageManager.queue(QueuedMessage.builder()
				.type(ChatMessageType.PUBLICCHAT)
				.sender("talker")
				.name(name)
				.runeLiteFormattedMessage(formattedMessage)
				.build());
		}

		if (config.speedThroughDialog())
		{
			executorService.submit(() ->
			{
				flexo.delay((int) getMillis());
				flexo.holdKey(FLEXO_CHAT_KEY.SPACE.keycode, r.nextInt(35) + (r.nextInt(3) * r.nextInt(10)));
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
