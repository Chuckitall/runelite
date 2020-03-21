package net.runelite.client.plugins.stash;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.fred.api.other.Tuples;
import net.runelite.client.fred.api.other.Tuples.T2;

@SuppressWarnings("FieldCanBeLocal")
@Singleton
@Slf4j
class StashCache
{
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

		static RecordState decode(int v)
		{
			RecordState toRet = INVALID;
			for (RecordState r : RecordState.values())
			{
				if (r.getValue() != v) continue;
				else
				{
					toRet = r;
					break;
				}
			}
			return toRet;
		}
	}

	private final StashPlugin plugin;
	private final Client client;
	private final ConfigManager configManager;
	private final EventBus eventBus;

	@Inject
	private StashCache(StashPlugin plugin, Client client, ConfigManager configManager, EventBus eventBus)
	{
		this.plugin = plugin;
		this.client = client;
		this.configManager = configManager;
		this.eventBus = eventBus;
	}

	//used as the lifecycle of subscribed events
	private String username = null;
	private String cacheGroup = null;
	private STASHUnit lastClickedStashUnit = null;
	private int lastClickedCountDown = 0;

	public boolean isSet()
	{
		return username != null;
	}

	private boolean isValidState()
	{
		if (this.username != null && this.cacheGroup == null)
		{
			this.cacheGroup = (StashPlugin.CONFIG_GROUP + ".CACHE." + this.username);
		}
		if (this.username == null && this.cacheGroup != null)
		{
			this.cacheGroup = null;
		}
		return this.username != null && this.cacheGroup != null;
	}

	public void setCache(String username)
	{
		log.debug("log1 -> {}, {}", this.username, this.cacheGroup);
		if (this.username != null && !this.username.equalsIgnoreCase(username))
		{
			eventBus.unregister(this.username);
			this.username = null;
			log.debug("log2 -> {}, {}", this.username, this.cacheGroup);
			isValidState();
		}
		log.debug("log3 -> {}, {}", this.username, this.cacheGroup);
		if (username != null && this.username == null)
		{
			this.username = username.toLowerCase();
			eventBus.subscribe(MenuOptionClicked.class, this.username, this::onMenuClicked);
			eventBus.subscribe(GameTick.class, this.username, this::onGameTick);
			eventBus.subscribe(ChatMessage.class, this.username, this::onChatMessage);
			log.debug("log4 -> {}, {}", this.username, this.cacheGroup);
			isValidState();
		}
		log.debug("log5 -> {}, {}", this.username, this.cacheGroup);
	}

	public RecordState getRecord(STASHUnit unit)
	{
		RecordState toRet = RecordState.INVALID;
		if (isValidState() && recordExists(unit))
		{
			try
			{
				String value = configManager.getConfiguration(cacheGroup, unit.name());
				Integer i = (Integer.valueOf(value));
				if (i != null)
				{
					toRet = RecordState.decode(i);
				}
			}
			catch (Exception e)
			{
				log.error("Exception -> {}", e.getLocalizedMessage(), e);
			}
		}
		return toRet;
	}

	public boolean recordExists(STASHUnit unit)
	{
		if (isValidState())
		{
			String value = configManager.getConfiguration(cacheGroup, unit.name());
			return (!Strings.isNullOrEmpty(value));
		}
		return false;
	}

	public boolean updateRecord(STASHUnit unit, RecordState updated)
	{
		log.debug("unit {} == updated {}", unit.name(), updated.name());
		if (isValidState() && updated != null && !updated.equals(RecordState.INVALID))
		{
			RecordState old = RecordState.INVALID;
			if (recordExists(unit))
			{
				old = getRecord(unit);
			}
			if (old.equals(updated))
			{
				log.debug("old {} == updated {}, so no change is needed.", old, updated);
				return true;//record is up to date
			}
			configManager.setConfiguration(cacheGroup, unit.name(), updated.getValue() + "");
			if (old != RecordState.INVALID)
			{
				log.debug("updated key {}.{} from {} to {}", cacheGroup, unit.name(), old.name(), updated.name());
			}
			else
			{
				log.debug("created key {}.{} with value {}", cacheGroup, unit.name(), updated.name());
			}
			return true;
		}
		return false;
	}

	public List<T2<STASHUnit, RecordState>> getCached()
	{
		List<T2<STASHUnit, RecordState>> toReturn;
		if (isValidState())
		{
			String temp = cacheGroup + ".";
			toReturn = (configManager.getConfigurationKeys(cacheGroup).stream()
				.map(k -> k.replace(temp, ""))
				.filter(k -> Arrays.stream(STASHUnit.values()).anyMatch(j -> j.name().equals(k)))
				.map(STASHUnit::valueOf)
				.map(k -> Tuples.of(k, getRecord(k)))
				.collect(Collectors.toList())
			);
		}
		else
		{
			toReturn = new ArrayList<>();
		}
		return toReturn;
	}

	private void setLastClickedStash(STASHUnit unit)
	{
		if (unit != null)
		{
			lastClickedCountDown = 100;
		}
		else
		{
			lastClickedCountDown = 0;
		}
		lastClickedStashUnit = unit;
	}

	private Optional<STASHUnit> getLastClickedStash()
	{
		if (lastClickedCountDown > 0)
		{
			return Optional.ofNullable(lastClickedStashUnit);
		}
		return Optional.empty();
	}

	private void onGameTick(GameTick event)
	{
		if (lastClickedCountDown > 0)
		{
			lastClickedCountDown--;
		}
	}

	private void onMenuClicked(MenuOptionClicked event)
	{
		if ((event.getOption().equalsIgnoreCase("Search") && event.getTarget().contains("STASH")) || (event.getOption().equalsIgnoreCase("Build") && event.getTarget().contains("Inconspicuous")))
		{
			Optional<STASHUnit> clicked = Arrays.stream(STASHUnit.values()).filter(f -> f.getObjectId() == event.getIdentifier()).findFirst();
			if (clicked.isPresent())
			{
				setLastClickedStash(clicked.get());
				log.debug("Clicked stash w/ id: {} and named: {}", getLastClickedStash().map(STASHUnit::getObjectId).orElse(-1),  getLastClickedStash().map(STASHUnit::name).orElse("NULL"));
			}
			else
			{
				setLastClickedStash(null);
				log.error("No stash w/ id: {} was found from click {}", event.getIdentifier(), event);
			}
		}
	}

	private final ImmutableList<T2<RecordState, String>> mappings = ImmutableList.of(
		Tuples.of(RecordState.NOT_BUILT, "You do not have the required materials to build a STASH unit here."),
		Tuples.of(RecordState.NOT_BUILT, "You do not have the required construction level in order to build a STASH unit here."),
		Tuples.of(RecordState.BUILT_NOT_FILLED, "You build a STASH unit."),
		Tuples.of(RecordState.BUILT_NOT_FILLED, "You need all of the required items in order to store them here.<br>Keep an eye out for messages you may receive around the world, they may indicate what you need."),
		Tuples.of(RecordState.BUILT_NOT_FILLED, "You withdraw your items from the STASH unit."),
		Tuples.of(RecordState.BUILT_FILLED, "You deposit your items into the STASH unit.")
	);

	private void onChatMessage(ChatMessage event)
	{
		if (getLastClickedStash().isPresent() && event.getType().equals(ChatMessageType.GAMEMESSAGE))
		{
			String msg = event.getMessage();
			STASHUnit unit = getLastClickedStash().get();
			RecordState newState = RecordState.INVALID;
			for (T2<RecordState, String> map : mappings)
			{
				if (map.get_2().equalsIgnoreCase(msg))
				{
					newState = map.get_1();
					break;
				}
			}

			if (!newState.equals(RecordState.INVALID))
			{
				boolean worked = updateRecord(unit, newState);
				log.debug("update {} on chat message {}", worked ? "worked" : "failed", event);
				setLastClickedStash(null);
			}
		}
	}
}
