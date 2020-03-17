package net.runelite.client.plugins.stash;

import com.google.inject.Inject;
import com.google.inject.Provides;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ScriptID;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.util.Text;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.fred.api.other.Tuples;
import net.runelite.client.plugins.fred.api.other.Tuples.T2;
import net.runelite.client.ui.overlay.OverlayManager;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Created by npruff on 9/2/2019.
 */

@PluginDescriptor(
	name = "Stash",
	description = "Tracks which stash units you have build, and which have been filled.",
	tags = {"uim", "fred", "stash"},
	type = PluginType.FRED
)
@Slf4j
public class StashPlugin extends Plugin
{
	static final String CONFIG_GROUP = "Stash";
	static final String CONFIG_CACHE_GROUP = "StashCache";

	@Getter(AccessLevel.PACKAGE)
	@Inject
	private Client client;

	@Getter(AccessLevel.PUBLIC)
	@Inject
	private StashConfig config;

	@Getter(AccessLevel.PACKAGE)
	@Inject
	private OverlayManager overlayManager;

	@Getter(AccessLevel.PACKAGE)
	@Inject
	private EventBus eventBus;

	@Inject
	@Getter(AccessLevel.PUBLIC)
	private ConfigManager configManager;

//	private final Map<STASHUnit, StashRecord> stashCache = new HashMap<STASHUnit, StashRecord>();

	@Provides
	StashConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(StashConfig.class);
	}

	//all the loading/saving/updating cache is done here
	//format for group -> CONFIG_CACHE_GROUP+"."+client.getLocalPlayer().getName();
	//format for key is STASHUnit.name();
	//format for value is {0 -> not built/not filled, 1 -> built/??? filled, 2 -> built/not filled, 4 -> built/filled}
	public enum RecordState
	{
		INVALID(-1),//not logged in or something?
		NOT_BUILT(0),//not built/not filled
		BUILT_MAYBE_FILLED(1),//built/??? filled
		BUILT_NOT_FILLED(2),//built/not filled
		BUILT_FILLED(3);//built/filled

		@Getter(AccessLevel.PUBLIC)
		private int value;

		RecordState(int value)
		{
			this.value = value;
		}

		public boolean isBuilt()
		{
			return this.value > 0;
		}

		public boolean isFilled()
		{
			return this.value == 3;
		}

		public boolean isAccurate()
		{
			return this.value == 1;
		}

		static RecordState decode(Integer value)
		{
			if (value == null || value <= INVALID.getValue() || value > BUILT_FILLED.getValue())
			{
				return INVALID;
			}
			for (RecordState r : RecordState.values())
			{
				if (r.getValue() == value)
				{
					return r;
				}
			}
			return INVALID;
		}
	}

	private RecordState getOrCreateRecord(STASHUnit unit)
	{
		RecordState toReturn = RecordState.INVALID;

		if (client.getGameState().getState() == GameState.LOGGED_IN.getState())
		{
			String group = CONFIG_CACHE_GROUP + "." + client.getLocalPlayer().getName();
			List<String> cacheKeys = configManager.getConfigurationKeys(group);
			for(int i = 0; i < cacheKeys.size(); i++)
			{
				log.debug("cacheKeys[{}] -> {}", i, cacheKeys.get(i));
			}

//			configManager.getConfigurationKeys(group).stream().map(item -> item.replace(group, "")))
//			return (cacheKeys.stream().map(f->f.substring(group.length())).anyMatch(f -> f.equalsIgnoreCase(unit.name())));
			if (cacheKeys.stream().map(f->f.replace(group, "")).anyMatch(f -> f.equalsIgnoreCase("." + unit.name())))
			{
				log.error("hey1");
				Integer loadedValue = configManager.getConfiguration(group, unit.name(), int.class);
				toReturn = RecordState.decode(loadedValue);
			}
			else
			{
				log.error("hey2");
				client.runScript(ScriptID.WATSON_STASH_UNIT_CHECK, unit.getObjectId(), 0, 0, 0);
				int[] intStack = client.getIntStack();
				toReturn = (intStack[0] == 1) ? RecordState.BUILT_MAYBE_FILLED : RecordState.NOT_BUILT;
				updateRecord(unit, toReturn);
			}
		}

		return toReturn;
	}

	private boolean recordExists(STASHUnit unit)
	{
		boolean toReturn = false;

		if (client.getGameState().getState() == GameState.LOGGED_IN.getState())
		{
			String group = CONFIG_CACHE_GROUP + "." + client.getLocalPlayer().getName();
			List<String> cacheKeys = configManager.getConfigurationKeys(group);
			return (cacheKeys.stream().map(f->f.replace(group, "")).anyMatch(f -> f.equalsIgnoreCase("." + unit.name())));
		}
		return toReturn;
	}

	private boolean updateRecord(STASHUnit unit, RecordState record)
	{
		if (client.getGameState().getState() == GameState.LOGGED_IN.getState())
		{
			String group = CONFIG_CACHE_GROUP + "." + client.getLocalPlayer().getName();
			RecordState old = null;
			if (recordExists(unit))
			{
				old = getOrCreateRecord(unit);
			}
			if (old != null && old == record)
			{
				return true;//short out
			}
			configManager.setConfiguration(group, unit.name(), record.getValue());
			log.debug("unit: {} -> old: {} | new: {}", unit.name(), old, record);
			return true;
		}
		return false;
	}

	STASHUnit lastClickedStashUnit = null;

	boolean firstRun = true;
	boolean latched = false;
	private void onGameTick(GameTick tick)
	{
		if (client.getGameState().equals(GameState.LOGIN_SCREEN) && latched)
		{
			latched = false;
		}
		else if (!latched && client.getGameState().equals(GameState.LOGGED_IN))
		{
			if(firstRun)
			{
				Arrays.stream(STASHUnit.values()).filter(
					this::recordExists
				).map(f -> Tuples.of(f, this.getOrCreateRecord(f))).forEach(f -> log.warn("unit: {} -> {}", f.get_1().name(), f.get_2().name()));
				firstRun = false;
			}

			latched = true;
			Arrays.stream(STASHUnit.values()).forEach(
				this::getOrCreateRecord
			);
		}
	}

	private void onMenuClicked(MenuOptionClicked event)
	{
		if (event.getOption().equalsIgnoreCase("Search") && event.getTarget().contains("STASH"))
		{
			Optional<STASHUnit> clicked = Arrays.stream(STASHUnit.values()).filter(f -> f.getObjectId() == event.getIdentifier()).findFirst();
			if (clicked.isPresent())
			{
				lastClickedStashUnit = clicked.get();
				log.debug("Clicked stash w/ id: {} and named: {}", lastClickedStashUnit.getObjectId(), lastClickedStashUnit.name());
			}
			else
			{
				lastClickedStashUnit = null;
				log.error("No stash w/ id: {} was found from click {}", event.getIdentifier(), event);
			}
		}
	}

	private void onChatMessage(ChatMessage event)
	{
		if (lastClickedStashUnit != null && Text.standardize(event.getMessage()).contains("stash"))
		{
			RecordState newState = RecordState.INVALID;
			if(Text.standardize(event.getMessage()).contains("deposit"))
			{
				newState = RecordState.BUILT_FILLED;
			}
			else if(Text.standardize(event.getMessage()).contains("withdraw") || Text.standardize(event.getMessage()).contains("build"))
			{
				newState = RecordState.BUILT_NOT_FILLED;
			}

			if (newState != RecordState.INVALID)
			{
				boolean worked = updateRecord(lastClickedStashUnit, newState);
				log.debug("update {} on chat message {}", worked ? "worked" : "failed", event);
			}
			else
			{
				log.warn("chat message {} on {}", event, lastClickedStashUnit.name());
			}
			lastClickedStashUnit = null;
		}
	}

	@Override
	protected void startUp()
	{
		latched = false;
		firstRun = true;
		eventBus.subscribe(GameTick.class, this, this::onGameTick);
		eventBus.subscribe(MenuOptionClicked.class, this, this::onMenuClicked);
		eventBus.subscribe(ChatMessage.class, this, this::onChatMessage);
	}

	@Override
	protected void shutDown()
	{
		eventBus.unregister(this);
		latched = false;
		firstRun = true;
	}
}
