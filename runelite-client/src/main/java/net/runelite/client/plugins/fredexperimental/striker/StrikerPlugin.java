package net.runelite.client.plugins.fredexperimental.striker;

import com.github.joonasvali.naturalmouse.api.MouseMotionFactory;
import com.github.joonasvali.naturalmouse.support.DefaultNoiseProvider;
import com.github.joonasvali.naturalmouse.support.DefaultOvershootManager;
import com.github.joonasvali.naturalmouse.support.DefaultSpeedManager;
import com.github.joonasvali.naturalmouse.support.Flow;
import com.github.joonasvali.naturalmouse.support.SinusoidalDeviationProvider;
import com.github.joonasvali.naturalmouse.util.FlowTemplates;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import java.awt.KeyboardFocusManager;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.FocusChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.ScriptCallbackEvent;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.stretchedmode.StretchedModeConfig;
import net.runelite.client.ui.ClientUI;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
	name = "Striker API",
	description = "Interface to interact with the game client.",
	tags = {"fred", "striker", "automation", "api"},
	type = PluginType.FRED
)
@Slf4j
@Singleton
public class StrikerPlugin extends Plugin
{
	@Inject
	@Getter(AccessLevel.PACKAGE)
	private Client client;

	@Inject
	@Getter(AccessLevel.PACKAGE)
	private ClientUI clientUI;

	@Inject
	@Getter(AccessLevel.PACKAGE)
	private ConfigManager configManager;

	@Inject
	private EventBus eventBus;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private StrikerOverlay overlay;

	@Inject
	@Getter(AccessLevel.PACKAGE)
	private StrikerConfig config;

	/**
	 * Settings Section
	 **/
	//striker
	private boolean lockClient;
	private StrikerMode strikerMode;

	//robot
	private MouseMotionFactory currentMouseMotionFactory;
	private int reactionTimeVariation;
	private int mouseDragSpeed;
	private int overshoots;
	private boolean variatingFlow;
	private boolean slowStartupFlow;
	private boolean slowStartup2Flow;
	private boolean jaggedFlow;
	private boolean interruptedFlow;
	private boolean interrupted2Flow;
	private boolean stoppingFlow;
	private int deviationSlopeDivider;
	private String noisinessDivider;

	//event

	//overlay
	private boolean overlayEnabled;
	private boolean debugNPCs;
	private boolean debugPlayers;
	private boolean debugGroundItems;

	@Getter(AccessLevel.PUBLIC)
	private KeyboardFocusManager focusManager = null;

	private void updateMouseMotionFactory()
	{
		MouseMotionFactory factory = new MouseMotionFactory();
		// TODO:Add Options for various flows to allow more personalization
		List<Flow> flows = new ArrayList<>();

		// Always add random
		flows.add(new Flow(FlowTemplates.random()));

		if (this.variatingFlow)
		{
			flows.add(new Flow(FlowTemplates.variatingFlow()));
		}

		if (this.slowStartupFlow)
		{
			flows.add(new Flow(FlowTemplates.slowStartupFlow()));
		}

		if (this.slowStartup2Flow)
		{
			flows.add(new Flow(FlowTemplates.slowStartup2Flow()));
		}

		if (this.jaggedFlow)
		{
			flows.add(new Flow(FlowTemplates.jaggedFlow()));
		}

		if (this.interruptedFlow)
		{
			flows.add(new Flow(FlowTemplates.interruptedFlow()));
		}

		if (this.interrupted2Flow)
		{
			flows.add(new Flow(FlowTemplates.interruptedFlow2()));
		}

		if (this.stoppingFlow)
		{
			flows.add(new Flow(FlowTemplates.stoppingFlow()));
		}

		DefaultSpeedManager manager = new DefaultSpeedManager(flows);
		//TODO:Add options for custom Deviation Provider and Noise Provider
		factory.setDeviationProvider(new SinusoidalDeviationProvider(this.deviationSlopeDivider));
		factory.setNoiseProvider(new DefaultNoiseProvider(Double.parseDouble(this.noisinessDivider)));
		factory.getNature().setReactionTimeVariationMs(this.reactionTimeVariation);
		manager.setMouseMovementBaseTimeMs(this.mouseDragSpeed);

		DefaultOvershootManager overshootManager = (DefaultOvershootManager) factory.getOvershootManager();
		overshootManager.setOvershoots(this.overshoots);

		factory.setSpeedManager(manager);
		this.currentMouseMotionFactory = factory;
	}

	private void configUpdate()
	{
		this.overlayEnabled = config.getOverlayEnabled();
		this.debugNPCs = config.getDebugNPCs();
		this.debugPlayers = config.getDebugPlayers();
		this.debugGroundItems = config.getDebugGroundItems();

//		this.minDelayAmt = config.getMinDelayAmt();
		this.reactionTimeVariation = config.getReactionTimeVariation();
		this.mouseDragSpeed = config.getMouseDragSpeed();
		this.overshoots = config.getOvershoots();
		this.variatingFlow = config.getVariatingFlow();
		this.slowStartupFlow = config.getSlowStartupFlow();
		this.slowStartup2Flow = config.getSlowStartup2Flow();
		this.jaggedFlow = config.getJaggedFlow();
		this.interruptedFlow = config.getInterruptedFlow();
		this.interrupted2Flow = config.getInterrupted2Flow();
		this.stoppingFlow = config.getStoppingFlow();
		this.deviationSlopeDivider = config.getDeviationSlopeDivider();
		this.noisinessDivider = config.getNoisinessDivider();
		updateMouseMotionFactory();

		strikerMode = config.getStrikerMode();
		Striker.setEnabled(this.strikerMode);

		this.lockClient = config.getLockWindow();
		this.setLockClient(config.getLockWindow());
	}

	public void setLockClient(boolean lock)
	{
		if (lock != this.lockClient)
		{
			if (lock)
			{
				lockClient();
			}
			else
			{
				unlockClient();
			}
		}
	}

	private void lockClient()
	{
		this.lockClient = true;
		this.focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		ClientUI.frame.setFocusable(false);
		client.getCanvas().setEnabled(false);
	}

	private void unlockClient()
	{
		this.lockClient = false;
		ClientUI.frame.setFocusable(true);
		clientUI.requestFocus();
		client.getCanvas().setEnabled(true);
	}

	@Provides
	StrikerConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(StrikerConfig.class);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
		setLockClient(false);
		StrikerRobot.setEnabled(false);
		StrikerEvent.setEnabled(false);
	}

	@Override
	protected void startUp() throws Exception
	{
		eventBus.subscribe(ScriptCallbackEvent.class, this, this::onScriptCallbackEvent);
		eventBus.subscribe(ConfigChanged.class, this, this::onConfigChanged);
		eventBus.subscribe(GameTick.class, this, this::onGameTick);
		eventBus.subscribe(ClientTick.class, this, this::onClientTick);
		eventBus.subscribe(ChatMessage.class, this, this::onChatMessage);
		eventBus.subscribe(ItemContainerChanged.class, this, this::onItemContainerChanged);
		eventBus.subscribe(MenuEntryAdded.class, this, this::onMenuEntryAdded);
		eventBus.subscribe(MenuOptionClicked.class, this, this::onMenuOptionClicked);
		eventBus.subscribe(FocusChanged.class, this, this::onFocusChanged);
		overlayManager.add(overlay);

		setLockClient(config.getLockWindow());
		StrikerUtils.init(this);
		StrikerRobot.init(this);
		StrikerEvent.init(this);
		Striker.init(this);
		this.configUpdate();
	}

	private void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals("striker"))
		{
			return;
		}
		this.configUpdate();
	}

	private void onMenuEntryAdded(MenuEntryAdded event)
	{
//		log.debug("MenuEntryAdded -> {}", event);
	}

	private void onMenuOptionClicked(MenuOptionClicked event)
	{
//		log.debug("MenuOptionClicked -> {}", event);
	}

	private void onScriptCallbackEvent(ScriptCallbackEvent event)
	{
//		log.debug("ScriptCallbackEvent -> {}", event);
	}

	private void onClientTick(ClientTick event)
	{
//		log.debug("ClientTick -> {}", event);
	}

	private void onGameTick(GameTick event)
	{
//		log.debug("GameTick -> {}", event);
	}

	private void onChatMessage(ChatMessage event)
	{
//		log.debug("ChatMessage -> {}", event);
	}

	private void onItemContainerChanged(ItemContainerChanged event)
	{
//		log.debug("ItemContainerChanged -> {}", event);
	}

	private void onFocusChanged(FocusChanged event)
	{
//		log.debug("event {}", event);
	}

	public MouseMotionFactory getCurrentMouseMotionFactory()
	{
		return currentMouseMotionFactory;
	}

	public double getScaleFactor()
	{
		return configManager.getConfig(StretchedModeConfig.class).scalingFactor();
	}
}
