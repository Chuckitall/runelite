package net.runelite.client.plugins.stash;

import com.google.common.base.Strings;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.util.Text;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;


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

//	private T2<String, String> getCacheGroupAndKey(STASHUnit unit)
//	{
//		assert isValidState();
//		assert unit != null;
//		return Tuples.of(StashPlugin.CONFIG_GROUP + ".CACHE." + username, unit.name());
//	}

	public void setCache(String username)
	{
		if (this.username != null && !this.username.equalsIgnoreCase(username))
		{
			eventBus.unregister(this.username);
			this.username = null;
			isValidState();
		}
		if(username != null && this.username == null)
		{
			this.username = username.toLowerCase();
			eventBus.subscribe(MenuOptionClicked .class, this.username, this::onMenuClicked);
			eventBus.subscribe(ChatMessage .class, this.username, this::onChatMessage);
			isValidState();
		}
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
		if(isValidState())
		{
			String value = configManager.getConfiguration(cacheGroup, unit.name());
			return (!Strings.isNullOrEmpty(value));
		}
		return false;
	}

	public boolean updateRecord(STASHUnit unit, RecordState updated)
	{
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
			if(old != RecordState.INVALID)
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

//	public STASHUnit[] getCached(RecordState filter)
//	{
//
//	}

	public STASHUnit[] getCached()
	{
		if (isValidState())
		{
			String temp = cacheGroup+".";
			List<String> keys = configManager.getConfigurationKeys(cacheGroup);
			keys.forEach(k -> log.debug("{}|{}|{}",cacheGroup, k, k.replace(temp, "")));
		}
		return new STASHUnit[] {};
	}

	private void onMenuClicked(MenuOptionClicked event)
	{
		if ((event.getOption().equalsIgnoreCase("Search") && event.getTarget().contains("STASH")) || (event.getOption().equalsIgnoreCase("Build") && event.getTarget().contains("Inconspicuous")))
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

			if (!newState.equals(RecordState.INVALID))
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
}
