package net.runelite.client.plugins.hotkeys;

import com.google.common.base.Strings;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import java.applet.Applet;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuOpcode;
import net.runelite.api.Player;
import net.runelite.api.VarPlayer;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.SoundEffectPlayed;
import net.runelite.api.kit.KitType;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.Keybind;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.PlayerManager;
import net.runelite.client.game.XpDropEvent;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.hotkeys.script.AutoScriptTypes;
import net.runelite.client.plugins.hotkeys.script.HotKeysAutoScript;
import net.runelite.client.plugins.hotkeys.script.HotKeysScript;
import net.runelite.client.plugins.hotkeys.script.SourceTypes;
import net.runelite.client.plugins.hotkeys.ui.HotKeysAutoOverlay;
import net.runelite.client.plugins.hotkeys.ui.HotKeysAutoScriptEditor;
import net.runelite.client.plugins.hotkeys.ui.HotKeysOverlay;
import net.runelite.client.plugins.hotkeys.ui.HotKeysPluginPanel;
import net.runelite.client.plugins.hotkeys.ui.HotKeysScriptEditor;
import net.runelite.client.plugins.hotkeys.utils.ExtUtils;
import net.runelite.client.plugins.hotkeys.utils.MenuEntrySwapHandler;
import net.runelite.client.plugins.hotkeys.utils.data.WeaponMap;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.HotkeyListener;
import net.runelite.client.util.ImageUtil;
import org.apache.commons.lang3.ObjectUtils;
import static org.apache.commons.lang3.math.NumberUtils.createInteger;
import org.apache.commons.lang3.tuple.Pair;

@PluginDescriptor(
	name = "HotKeys",
	description = "Keys of the hot variety",
	tags = {""},
	type = PluginType.EXTERNAL
)
@Slf4j
@Singleton
public class HotKeysPlugin extends Plugin
{
	private static final String PLUGIN_NAME = "HotKeys";

	private static final String CONFIG_GROUP = "hotkeys";
	private static final String CONFIG_PRESET_GROUP = "hotkeyspreset";

	private static final String CONFIG_KEY = "scripts";
	private static final String AUTO_CONFIG_KEY = "autoscripts";
	private static final String TOGGLE_CONFIG_KEY = "togglekeybind";
	private static final String PRAYFLICK_CONFIG_KEY = "prayflick";
	private static final String TOGGLE_PRAYFLICK_CONFIG_KEY = "toggleprayflick";
	private static final String KEEP_SALVE_ON_CONFIG_KEY = "keepsalveon";
	private static final String MAIN_OVERLAY_CONFIG_KEY = "mainoverlay";
	private static final String AUTO_OVERLAY_CONFIG_KEY = "autooverlay";
	private static final String ICON_NAME = "logo.png";
	private static final File BASE = RuneLite.PLUGIN_DIR;

	@Inject
	@Getter
	private Client client;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private EventBus eventBus;

	@Inject
	private ConfigManager configManager;

	@Inject
	@Getter
	private PlayerManager playerManager;

	private NavigationButton navigationButton;

	@Setter
	@Getter
	private List<HotKeysScript> scripts = new ArrayList<>();

	@Setter
	@Getter
	private List<HotKeysAutoScript> autoScripts = new ArrayList<>();

	@Inject
	private KeyManager keyManager;

	@Getter
	@Inject
	private ItemManager itemManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private HotKeysOverlay overlay;

	@Inject
	private HotKeysAutoOverlay autoOverlay;

	private List<HotKeysScriptEditor> scriptEditors = new CopyOnWriteArrayList<>();

	private List<HotKeysAutoScriptEditor> autoScriptEditors = new CopyOnWriteArrayList<>();

	private List<HotkeyListener> listeners = new ArrayList<>();

	@Getter
	private List<Pair<HotKeysScript, Integer>> scheduledScripts = new ArrayList<>();
	private BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(50);
	private ThreadPoolExecutor executorService = new ThreadPoolExecutor(1, 1, 2, TimeUnit.SECONDS, queue,
		new ThreadPoolExecutor.DiscardPolicy());

	@Getter
	@Setter
	private Keybind toggleKeybind;

	@Getter
	@Setter
	private Keybind prayFlickKeybind;

	@Getter
	@Setter
	private Keybind togglePrayFlickKeybind;

	private HotkeyListener toggleHotkeyListener;

	@Getter
	@Inject
	private HotKeysPluginPanel pluginPanel;

	@Getter
	@Setter
	private int minDelay;
	@Getter
	@Setter
	private int maxDelay;

	@Getter
	private boolean hotkeysOn = false;

	@Getter
	@Inject
	private MenuEntrySwapHandler menuEntrySwapHandler;

	@Getter
	@Setter
	private WidgetInfo lastSpellSelected;
	@Setter
	private boolean castWithoutWeapon = false;
	private boolean leftClickCast = false;
	@Getter
	private boolean prayFlicking;
	private boolean swappedToMage = false;
	private boolean skipTickCheck = false;
	@Getter
	private Actor lastTarget = null;
	@Getter
	private Player lastAttackingPlayer = null;
	@Getter
	private int lastAttackedOpcode = -1;
	@Getter
	@Setter
	private boolean keepSalveOn;
	@Getter
	@Setter
	private boolean mainOverlayOn;
	@Getter
	@Setter
	private boolean autoOverlayOn;

	private Integer lastSpecEnergy = null;

	@Override
	protected void startUp() throws Exception
	{
		loadConfig();
		updateConfig();
		//pluginPanel = injector.getInstance(HotKeysPluginPanel.class);
		pluginPanel.rebuildPanel();
		final BufferedImage icon = ImageUtil.getResourceStreamFromClass(getClass(), ICON_NAME);

		navigationButton = NavigationButton.builder()
			.tooltip(PLUGIN_NAME)
			.icon(icon)
			.priority(10)
			.panel(pluginPanel)
			.build();

		clientToolbar.addNavigation(navigationButton);

		hotkeysOn = false;


		overlayManager.add(overlay);
		overlayManager.add(autoOverlay);
		addSubscriptions();
		leftClickCast = ExtUtils.shouldLeftClickCast(this);
	}

	@Override
	protected void shutDown() throws Exception
	{
		updateConfig();
		clientToolbar.removeNavigation(navigationButton);

		for (HotKeysScriptEditor scriptEditor : scriptEditors)
		{
			scriptEditor.dispose();
		}
		scriptEditors.clear();

		for (HotKeysAutoScriptEditor autoScriptEditor : autoScriptEditors)
		{
			autoScriptEditor.dispose();
		}
		autoScriptEditors.clear();
		for (HotkeyListener listener : listeners)
		{
			keyManager.unregisterKeyListener(listener);
		}
		listeners.clear();
		keyManager.unregisterKeyListener(toggleHotkeyListener);
		overlayManager.remove(overlay);
		overlayManager.remove(autoOverlay);
		eventBus.unregister(this);
	}

	private void addSubscriptions()
	{
		eventBus.subscribe(GameTick.class, this, this::onGameTick);
		eventBus.subscribe(MenuOptionClicked.class, this, this::onMenuOptionClicked);
		eventBus.subscribe(MenuEntryAdded.class, this, this::onMenuEntryAdded);
		eventBus.subscribe(CommandExecuted.class, this, this::onCommandExecuted);
		eventBus.subscribe(InteractingChanged.class, this, this::onInteractingChanged);
		eventBus.subscribe(XpDropEvent.class, this, this::onXpDropEvent);
		eventBus.subscribe(ItemContainerChanged.class, this, this::onItemContainerChanged);
		eventBus.subscribe(AnimationChanged.class, this, this::onAnimationChanged);
		eventBus.subscribe(SoundEffectPlayed.class, this, this::onSoundEffectPlayed);
		eventBus.subscribe(GameStateChanged.class, this, this::onGameStateChanged);
	}

	private void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (leftClickCast)
		{
			if (event.getOpcode() == MenuOpcode.PLAYER_SECOND_OPTION.getId()) //TODO - null check the client.getSelectedSpellName(), for some reason every way I tried to do that crashed the client
			{
				if (swappedToMage || castWithoutWeapon)
				{
					client.insertMenuItem("Cast " + ColorUtil.prependColorTag(client.getSelectedSpellName(), Color.GREEN) + " -> ", event.getTarget(), MenuOpcode.SPELL_CAST_ON_PLAYER.getId(), event.getIdentifier(), 0, 0, true);
				}
			}
			if (event.getOpcode() == MenuOpcode.NPC_SECOND_OPTION.getId())
			{
				if (swappedToMage || castWithoutWeapon)
				{
					client.insertMenuItem("Cast " + ColorUtil.prependColorTag(client.getSelectedSpellName(), Color.GREEN) + " -> ", event.getTarget(), MenuOpcode.SPELL_CAST_ON_NPC.getId(), event.getIdentifier(), 0, 0, true);
				}
			}
		}
	}

	private void onMenuOptionClicked(MenuOptionClicked event)
	{
		int opcode = event.getOpcode();
		if (opcode == MenuOpcode.ITEM_SECOND_OPTION.getId())
		{
			if (WeaponMap.StyleMap.get(event.getIdentifier()) == WeaponMap.WeaponStyle.MAGIC)
			{
				skipTickCheck = true;
				swappedToMage = true;
			}
			else if (WeaponMap.StyleMap.get(event.getIdentifier()) != null)
			{
				skipTickCheck = true;
				swappedToMage = false;
			}
		}
/*		else if (opcode == MenuOpcode.PLAYER_SECOND_OPTION.getId() || opcode == MenuOpcode.SPELL_CAST_ON_PLAYER.getId())
		{
			int pid = event.getIdentifier();

			for (Player p : client.getPlayers())
			{
				if (p.getPlayerId() == pid)
				{
					lastTarget = p;
					log.info("Set last hit target as player: " + p.getName());
				}
			}

		}
		else if (opcode == MenuOpcode.NPC_SECOND_OPTION.getId() || opcode == MenuOpcode.SPELL_CAST_ON_NPC.getId())
		{
			int index = event.getIdentifier();

			for (NPC npc : client.getNpcs())
			{
				if (npc.getIndex() == index)
				{
					lastTarget = npc;
					log.info("Set last hit target as NPC: " + npc.getName());
				}
			}
		}*/
	}

	private void onInteractingChanged(InteractingChanged event)
	{
		if (event.getSource() instanceof Player)
		{
			Player localPlayer = client.getLocalPlayer();
			Player sourcePlayer = (Player) event.getSource();
			Actor targetActor = event.getTarget();
			if (localPlayer == sourcePlayer && targetActor != null)
			{
				if (lastTarget != targetActor)
				{
					lastTarget = targetActor;
				}/*
				if (targetActor instanceof Player)
				{
					client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Set last target as: " + targetActor.getName() + " (Player), PID " + ((Player) targetActor).getPlayerId(), null);
				}
				else if (targetActor instanceof NPC)
				{
					client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Set last target as: " + targetActor.getName() + " (NPC), index " + ((NPC) targetActor).getIndex(), null);
				}*/
			}
			else if (targetActor == localPlayer)
			{
				lastAttackingPlayer = (Player) event.getSource();
			}
		}
	}

	private void onGameTick(GameTick event)
	{
		if (!scheduledScripts.isEmpty())
		{
			log.info(scheduledScripts.toString());
		}
		int currentSpec = client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT);
		if (lastSpecEnergy == null)
		{
			lastSpecEnergy = currentSpec;
		}
		else
		{
			if (lastSpecEnergy > currentSpec)
			{
				for (HotKeysAutoScript autoScript : autoScripts)
				{
					if (autoScript.getAutoScriptType() == AutoScriptTypes.SPEC_CHANGED)
					{
						executorService.submit(() -> ExtUtils.runAutoScript(autoScript, this));
					}
				}
			}
			lastSpecEnergy = currentSpec;
		}
		if (prayFlicking)
		{
			log.info("should flick here");
			executorService.submit(() -> ExtUtils.flick(this));
		}
		if (skipTickCheck)
		{
			skipTickCheck = false;
		}
		else
		{
			final int weaponID = ObjectUtils.defaultIfNull(client.getLocalPlayer().getPlayerAppearance().getEquipmentId(KitType.WEAPON), 0);

			if (WeaponMap.StyleMap.get(weaponID) == WeaponMap.WeaponStyle.MAGIC)
			{
				swappedToMage = true;
			}
			else
			{
				swappedToMage = false;
			}
		}

		for (HotKeysAutoScript autoScript : autoScripts)
		{
			if (autoScript.getAutoScriptType() == AutoScriptTypes.GAME_TICK)
			{
				executorService.submit(() -> ExtUtils.runAutoScript(autoScript, this));
			}
		}

		List<Pair<HotKeysScript, Integer>> nextTickList = new ArrayList<>();
		for (Pair<HotKeysScript, Integer> p : scheduledScripts)
		{
			if (p.getValue() > 0)
			{
				Pair temp = Pair.of(p.getKey(), p.getValue() - 1);
				nextTickList.add(temp);
			}
			else if (p.getValue() == 0)
			{
				Pair<HotKeysScript, Integer> finalP = p;
				executorService.submit(() -> ExtUtils.runScript(finalP.getKey(), this));
			}
		}
		scheduledScripts = nextTickList;
	}

	private void onXpDropEvent(XpDropEvent event)
	{
		for (HotKeysAutoScript autoScript : autoScripts)
		{
			if (autoScript.getAutoScriptType() == AutoScriptTypes.XP_DROP)
			{
				if (autoScript.getSkill() == event.getSkill())
				{
					if (event.getExp() >= autoScript.getArgs()[0] && event.getExp() <= autoScript.getArgs()[1])
					{
						executorService.submit(() -> ExtUtils.runAutoScript(autoScript, this));
					}
				}
			}
		}
	}

	private void onCommandExecuted(CommandExecuted event)
	{
		String[] args = event.getArguments();

		switch (event.getCommand())
		{
			case "loadPreset":
				loadPreset(args[0]);
				break;
			case "savePreset":
				savePreset(args[0]);
				break;
		}
	}

	private void onItemContainerChanged(ItemContainerChanged event)
	{
		ItemContainer container = event.getItemContainer();
		if (container == client.getItemContainer(InventoryID.INVENTORY))
		{
			for (HotKeysAutoScript autoScript : autoScripts)
			{
				if (autoScript.getAutoScriptType() == AutoScriptTypes.INVENTORY_CHANGED)
				{
					executorService.submit(() -> ExtUtils.runAutoScript(autoScript, this));
				}
			}
		}
	}

	private void onAnimationChanged(AnimationChanged event)
	{
		for (HotKeysAutoScript autoScript : autoScripts)
		{
			if (autoScript.getAutoScriptType() == AutoScriptTypes.ANIMATION_PLAYED)
			{
				int animationId = event.getActor().getAnimation();
				switch (autoScript.getSourceType())
				{
					case ANY:
						if (animationId == autoScript.getArgs()[0])
						{
							executorService.submit(() -> ExtUtils.runAutoScript(autoScript, this));
						}
						break;
					case SELF:
						if (client.getLocalPlayer() == event.getActor() && animationId == autoScript.getArgs()[0])
						{
							executorService.submit(() -> ExtUtils.runAutoScript(autoScript, this));
						}
						break;
					case OPPONENT:
						if (lastTarget == event.getActor() && animationId == autoScript.getArgs()[0])
						{
							executorService.submit(() -> ExtUtils.runAutoScript(autoScript, this));
						}
						break;
				}
			}
		}
	}

	private void onSoundEffectPlayed(SoundEffectPlayed event)
	{
		for (HotKeysAutoScript autoScript : autoScripts)
		{
			if (autoScript.getAutoScriptType() == AutoScriptTypes.SOUND_EFFECT_PLAYED)
			{
				int soundId = event.getSoundId();
				switch (autoScript.getSourceType())
				{
					case ANY:
						if (soundId == autoScript.getArgs()[0])
						{
							executorService.submit(() -> ExtUtils.runAutoScript(autoScript, this));
						}
						break;
					case SELF:
						if (client.getLocalPlayer() == event.getSource() && soundId == autoScript.getArgs()[0])
						{
							executorService.submit(() -> ExtUtils.runAutoScript(autoScript, this));
						}
						break;
					case OPPONENT:
						if (lastTarget == event.getSource() && soundId == autoScript.getArgs()[0])
						{
							executorService.submit(() -> ExtUtils.runAutoScript(autoScript, this));
						}
						break;
				}
			}
		}
	}

	private void onGameStateChanged(GameStateChanged event)
	{
		lastSpecEnergy = null;
	}

	private HotKeysScriptEditor getScriptEditor(HotKeysScript script)
	{
		for (HotKeysScriptEditor scriptEditor : scriptEditors)
		{
			if (scriptEditor.getScript().equals(script))
			{
				return scriptEditor;
			}
		}
		HotKeysScriptEditor scriptEditor = new HotKeysScriptEditor(this, script);
		scriptEditor.setLocationRelativeTo((Applet) client);
		scriptEditors.add(scriptEditor);
		return scriptEditor;
	}

	public void openScriptEditor(HotKeysScript script)
	{
		getScriptEditor(script).open();
	}

	private boolean hasScript(String name)
	{
		for (HotKeysScript script : scripts)
		{
			if (script.getName().equals(name))
			{
				return true;
			}
		}
		return false;
	}

	public boolean createNewScript(String name, Keybind hotkey)
	{
		if (hasScript(name))
		{
			return false;
		}
		scripts.add(new HotKeysScript(name, hotkey));
		updateConfig();
		return true;
	}

	public boolean renameScript(String newName, HotKeysScript script)
	{
		if (hasScript(newName))
		{
			return false;
		}
		script.setName(newName);
		updateConfig();
		return true;
	}

	public void deleteScript(HotKeysScript script)
	{
		scripts.remove(script);
		HotKeysScriptEditor scriptEditor = getScriptEditor(script);
		scriptEditor.dispose();
		scriptEditors.remove(scriptEditor);
		pluginPanel.rebuild();
		updateConfig();
	}

	private HotKeysAutoScriptEditor getAutoScriptEditor(HotKeysAutoScript autoScript)
	{
		for (HotKeysAutoScriptEditor autoScriptEditor : autoScriptEditors)
		{
			if (autoScriptEditor.getAutoScript().equals(autoScript))
			{
				return autoScriptEditor;
			}
		}
		HotKeysAutoScriptEditor autoScriptEditor = new HotKeysAutoScriptEditor(this, autoScript);
		autoScriptEditor.setLocationRelativeTo((Applet) client);
		autoScriptEditors.add(autoScriptEditor);
		return autoScriptEditor;
	}

	public void openAutoScriptEditor(HotKeysAutoScript autoScript)
	{
		getAutoScriptEditor(autoScript).open();
	}

	private boolean hasAutoScript(String name)
	{
		for (HotKeysAutoScript autoScript : autoScripts)
		{
			if (autoScript.getName().equals(name))
			{
				return true;
			}
		}
		return false;
	}

	public boolean createNewAutoScript(String name)
	{
		if (hasAutoScript(name))
		{
			return false;
		}
		autoScripts.add(new HotKeysAutoScript(name, new ArrayList<>(), AutoScriptTypes.NOT_SET, null, SourceTypes.ANY, new int[]{0, 0}, Keybind.NOT_SET, false));
		updateConfig();
		return true;
	}

	public boolean renameAutoScript(String newName, HotKeysAutoScript autoScript)
	{
		if (hasAutoScript(newName))
		{
			return false;
		}
		autoScript.setName(newName);
		updateConfig();
		return true;
	}

	public void deleteScript(HotKeysAutoScript autoScript)
	{
		autoScripts.remove(autoScript);
		HotKeysAutoScriptEditor autoScriptEditor = getAutoScriptEditor(autoScript);
		autoScriptEditor.dispose();
		autoScriptEditors.remove(autoScriptEditor);
		pluginPanel.rebuild();
		updateConfig();
	}

	public void updateConfig()
	{
		configManager.setConfiguration(CONFIG_GROUP, MenuEntrySwapHandler.MIN_DELAY_CONFIG_KEY, minDelay);
		configManager.setConfiguration(CONFIG_GROUP, MenuEntrySwapHandler.MAX_DELAY_CONFIG_KEY, maxDelay);
		configManager.setConfiguration(CONFIG_GROUP, TOGGLE_CONFIG_KEY, toggleKeybind);
		configManager.setConfiguration(CONFIG_GROUP, PRAYFLICK_CONFIG_KEY, prayFlickKeybind);
		configManager.setConfiguration(CONFIG_GROUP, TOGGLE_PRAYFLICK_CONFIG_KEY, togglePrayFlickKeybind);
		configManager.setConfiguration(CONFIG_GROUP, KEEP_SALVE_ON_CONFIG_KEY, keepSalveOn);
		configManager.setConfiguration(CONFIG_GROUP, MAIN_OVERLAY_CONFIG_KEY, mainOverlayOn);
		configManager.setConfiguration(CONFIG_GROUP, AUTO_OVERLAY_CONFIG_KEY, autoOverlayOn);
		keyManager.unregisterKeyListener(toggleHotkeyListener);
		toggleHotkeyListener = new HotkeyListener(() -> toggleKeybind)
		{
			@Override
			public void hotkeyPressed()
			{
				hotkeysOn = !hotkeysOn;
				registerHotkeyListeners(hotkeysOn);
			}
		};
		keyManager.registerKeyListener(toggleHotkeyListener);
		if (scripts.isEmpty())
		{
			configManager.unsetConfiguration(CONFIG_GROUP, CONFIG_KEY);
		}
		if (autoScripts.isEmpty())
		{
			configManager.unsetConfiguration(CONFIG_GROUP, AUTO_CONFIG_KEY);
		}

		final Gson gson = new Gson();
		final String json = gson.toJson(scripts);
		configManager.setConfiguration(CONFIG_GROUP, CONFIG_KEY, json);
		final Gson autoGson = new Gson();
		final String autoJson = autoGson.toJson(autoScripts);
		configManager.setConfiguration(CONFIG_GROUP, AUTO_CONFIG_KEY, autoJson);//

		setupHotkeyListeners();
		leftClickCast = ExtUtils.shouldLeftClickCast(this);
	}

	private void loadConfig()
	{
		final Gson gson = new Gson();
		final String scriptsJson = configManager.getConfiguration(CONFIG_GROUP, CONFIG_KEY);
		if (Strings.isNullOrEmpty(scriptsJson))
		{
			scripts = new ArrayList<>();
		}
		try
		{
			Type type = new TypeToken<List<HotKeysScript>>()
			{
			}.getType();
			scripts = ObjectUtils.defaultIfNull(gson.fromJson(scriptsJson, type), new ArrayList<HotKeysScript>());
		}
		catch (Exception ex)
		{
			scripts = new ArrayList<HotKeysScript>();
		}

		final String autoScriptsJson = configManager.getConfiguration(CONFIG_GROUP, AUTO_CONFIG_KEY);
		if (Strings.isNullOrEmpty(autoScriptsJson))
		{
			autoScripts = new ArrayList<>();
		}
		try
		{
			Type type = new TypeToken<List<HotKeysAutoScript>>()
			{
			}.getType();
			autoScripts = ObjectUtils.defaultIfNull(gson.fromJson(autoScriptsJson, type), new ArrayList<HotKeysAutoScript>());
		}
		catch (Exception ex)
		{
			autoScripts = new ArrayList<HotKeysAutoScript>();
		}

		minDelay = ObjectUtils.defaultIfNull(createInteger(configManager.getConfiguration(MenuEntrySwapHandler.CONFIG_GROUP, MenuEntrySwapHandler.MIN_DELAY_CONFIG_KEY)), MenuEntrySwapHandler.DEFAULT_MIN_DELAY);
		maxDelay = ObjectUtils.defaultIfNull(createInteger(configManager.getConfiguration(MenuEntrySwapHandler.CONFIG_GROUP, MenuEntrySwapHandler.MAX_DELAY_CONFIG_KEY)), MenuEntrySwapHandler.DEFAULT_MAX_DELAY);
		toggleKeybind = ObjectUtils.defaultIfNull(configManager.getConfiguration(CONFIG_GROUP, TOGGLE_CONFIG_KEY, Keybind.class), Keybind.NOT_SET);
		prayFlickKeybind = ObjectUtils.defaultIfNull(configManager.getConfiguration(CONFIG_GROUP, PRAYFLICK_CONFIG_KEY, Keybind.class), Keybind.NOT_SET);
		togglePrayFlickKeybind = ObjectUtils.defaultIfNull(configManager.getConfiguration(CONFIG_GROUP, TOGGLE_PRAYFLICK_CONFIG_KEY, Keybind.class), Keybind.NOT_SET);
		keepSalveOn = ObjectUtils.defaultIfNull(configManager.getConfiguration(CONFIG_GROUP, KEEP_SALVE_ON_CONFIG_KEY, boolean.class), false);
		mainOverlayOn = ObjectUtils.defaultIfNull(configManager.getConfiguration(CONFIG_GROUP, MAIN_OVERLAY_CONFIG_KEY, boolean.class), false);
		autoOverlayOn = ObjectUtils.defaultIfNull(configManager.getConfiguration(CONFIG_GROUP, AUTO_OVERLAY_CONFIG_KEY, boolean.class), false);
	}

	private void setupHotkeyListeners()
	{
		for (HotkeyListener listener : listeners)
		{
			keyManager.unregisterKeyListener(listener);
		}
		listeners.clear();
		HotKeysPlugin plugin = this;
		for (HotKeysScript script : scripts)
		{
			HotkeyListener listener = new HotkeyListener(script::getHotkey)
			{
				@Override
				public void hotkeyPressed()
				{
					WidgetInfo spellWidget = ExtUtils.checkleftClickCastPress(script);
					if (spellWidget != null)
					{
						castWithoutWeapon = true;
						//log.info("Casting without weapon");
						ExtUtils.setSelectedSpell(spellWidget, plugin);
					}
					executorService.submit(() -> ExtUtils.runScript(script, plugin));
				}

				@Override
				public void hotkeyReleased()
				{
					castWithoutWeapon = false;
					//log.info("No longer casting without weapon");
				}
			};
			listeners.add(listener);
			if (hotkeysOn)
			{
				for (HotkeyListener hotList : listeners)
				{
					keyManager.registerKeyListener(hotList);
				}
			}
		}

		for (HotKeysAutoScript autoScript : autoScripts)
		{
			HotkeyListener listener = new HotkeyListener(autoScript::getHotkey)
			{
				@Override
				public void hotkeyPressed()
				{
					autoScript.toggleEnabled();
				}
			};
			listeners.add(listener);
			if (hotkeysOn)
			{
				for (HotkeyListener hotList : listeners)
				{
					keyManager.registerKeyListener(hotList);
				}
			}
		}

		HotkeyListener prayFlick = new HotkeyListener(() -> prayFlickKeybind)
		{
			@Override
			public void hotkeyPressed()
			{
				prayFlicking = true;
			}

			@Override
			public void hotkeyReleased()
			{
				prayFlicking = false;
			}
		};
		listeners.add(prayFlick);
		HotkeyListener togglePrayFlick = new HotkeyListener(() -> togglePrayFlickKeybind)
		{
			@Override
			public void hotkeyPressed()
			{
				prayFlicking = !prayFlicking;
			}
		};
		listeners.add(togglePrayFlick);
	}

	private void registerHotkeyListeners(boolean shouldRegister)
	{
		//log.info("registerHotkeyListeners(" + shouldRegister + ")");
		for (HotkeyListener listener : listeners)
		{
			keyManager.unregisterKeyListener(listener);
		}
		if (shouldRegister)
		{
			for (HotkeyListener listener : listeners)
			{
				keyManager.registerKeyListener(listener);
			}
		}
	}

	public void savePreset(String presetName)
	{
		if (presetName.isEmpty())
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Invalid preset name", null);
			return;
		}
		final HotKeysPreset preset = new HotKeysPreset(scripts, autoScripts, toggleKeybind, prayFlickKeybind, togglePrayFlickKeybind, keepSalveOn, mainOverlayOn, autoOverlayOn);
		final Gson gson = new Gson();
		final String json = gson.toJson(preset);
		configManager.unsetConfiguration(CONFIG_PRESET_GROUP, presetName);
		configManager.setConfiguration(CONFIG_PRESET_GROUP, presetName, json);
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Saved preset " + presetName, null);
	}

	public void deletePreset(String presetName)
	{
		final String json = configManager.getConfiguration(CONFIG_PRESET_GROUP, presetName);
		if (json == null || json.isEmpty())
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "The preset (" + ObjectUtils.defaultIfNull(presetName, "null") + ") doesn't even exist my dude", null);
			return;
		}
		configManager.unsetConfiguration(CONFIG_PRESET_GROUP, presetName);
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Deleted preset " + presetName, null);
	}

	public void loadPreset(String presetName)
	{
		final String json = configManager.getConfiguration(CONFIG_PRESET_GROUP, presetName);
		log.info(json);
		if (json == null || json.isEmpty())
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "The preset (" + ObjectUtils.defaultIfNull(presetName, "null") + ") doesn't even exist my dude", null);
			return;
		}
		final Gson gson = new Gson();
		Type type = new TypeToken<HotKeysPreset>()
		{
		}.getType();
		HotKeysPreset preset = gson.fromJson(json, type);
		autoScripts = preset.getAutoScripts();
		scripts = preset.getScripts();
		toggleKeybind = preset.getToggleKeybind();
		prayFlickKeybind = preset.getPrayFlickKeybind();
		togglePrayFlickKeybind = preset.getTogglePrayFlickKeybind();
		keepSalveOn = preset.isKeepSalveOn();
		mainOverlayOn = preset.isMainOverlayOn();
		autoOverlayOn = preset.isAutoOverlayOn();
		updateConfig();
		pluginPanel.rebuildPanel();
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Loaded preset " + presetName, null);
	}

	public List<String> getPresetNames()
	{
		List<String> returnNames = new ArrayList<String>();
		List<String> fullNames = configManager.getConfigurationKeys(CONFIG_PRESET_GROUP);
		for (String s : fullNames)
		{
			String replaced = s.replaceFirst(CONFIG_PRESET_GROUP + ".", "");
			returnNames.add(replaced);
		}
		return returnNames;
	}
}

