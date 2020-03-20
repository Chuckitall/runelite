package net.runelite.client.plugins.stash;

import com.google.inject.Inject;
import com.google.inject.Provides;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ScriptID;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ScriptCallbackEvent;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.fred.FredManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.fred.api.other.Tuples.T2;
import net.runelite.client.plugins.stash.StashCache.RecordState;
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

	@Inject
	private ClientThread clientThread;

	@Inject
	private FredManager fredManager;

	@Inject
	@Getter(AccessLevel.PUBLIC)
	private StashCache cache;

	@Provides
	StashConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(StashConfig.class);
	}

	Object GAME_TICK_LOCK = new Object();
	private void onGameTick(GameTick tick)
	{
		if(cache.isSet())
		{
			return;
		}
		String name = Optional.of(client.getLocalPlayer()).map(Actor::getName).orElse(null);
		if (name != null)
		{
			log.debug("Gametick {}", name);
			cache.setCache(name);
		}
	}

	private void onGameStateChanged(GameStateChanged event)
	{
		if(event.getGameState().equals(GameState.LOGIN_SCREEN))
		{
			cache.setCache(null);
		}
	}

	private void onCommandExecuted(CommandExecuted commandExecuted)
	{
		if (!commandExecuted.getCommand().equals("stash"))
		{
			return;
		}
		String[] args_ = commandExecuted.getArguments();
		String command = "";
		String[] args;
		if (args_.length > 0)
		{
			command = args_[0];
			args = ArrayUtils.remove(args_, 0);
		}
		else
		{
			args = new String[] {};
		}

		if (command.equals(""))
		{
			cache.getCached().forEach(
				j -> client.addChatMessage(ChatMessageType.GAMEMESSAGE, "Stash", j.get_1().name() + " -> " + j.get_2().name(), null)
			);
		}
		else if (command.equals("isBuilt") && args.length > 0)
		{
			clientThread.invokeLater(() ->
			{
				Arrays.stream(STASHUnit.values()).filter(f -> Arrays.stream(args).anyMatch(a -> a.equals(f.getObjectId()+""))).forEach(
					unit ->
					{
						RecordState isBuilt = cache.getRecord(unit);
						client.runScript(ScriptID.WATSON_STASH_UNIT_CHECK, unit.getObjectId(), 0, 0, 0);
						int[] intStack = client.getIntStack();
						isBuilt = (intStack[0] == 1) ? RecordState.BUILT_MAYBE_FILLED : RecordState.NOT_BUILT;
						log.debug("unit {} -> state {}", unit.name(), isBuilt.name());
						cache.updateRecord(unit, isBuilt);
					});
			});
		}
		else if (command.equals("clear") && args.length > 0)
		{
			Arrays.stream(STASHUnit.values()).filter(f -> Arrays.stream(args).anyMatch(a -> a.equals(f.getObjectId()+""))).forEach(
			unit ->
			{
				cache.updateRecord(unit, RecordState.NOT_BUILT);
				log.debug("cleared state of unit {}", unit.name());
			});
		}
		else if (command.equals("set"))
		{
			STASHUnit unit = null;
			RecordState state = RecordState.INVALID;
			try
			{
				unit = STASHUnit.valueOf(args[0].toUpperCase());
				state = RecordState.valueOf(args[1].toUpperCase());
			}
			catch (Exception ignored)
			{

			}

			if (args.length != 2 || unit == null || state == null)
			{
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "Stash", "syntax \"::stash set <STASHUnit name> <StashRecord name>\"", null);
			}
			else
			{
				boolean result = cache.updateRecord(unit, state);
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "Stash", unit.name() + " = " + state.name(), null);
			}
		}
		else if (command.equals("list"))
		{
			if (args.length == 0)
			{
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "Stash", "syntax \"::stash list <beginner/easy/medium/hard/elite/master> <not_built/built_maybe_filled/built_not_filled/built_filled>\"", null);
				return;
			}
			List<T2<STASHUnit, RecordState>> units = cache.getCached();
			STASHUnit[] param1 = Arrays.stream(args).filter(a -> Arrays.stream(StashLevel.values())
				.map(Enum::name).anyMatch(f->f.equalsIgnoreCase(a)))
				.map(a -> StashLevel.valueOf(a.toUpperCase()).getUnits())
				.flatMap(Arrays::stream).distinct().toArray(STASHUnit[]::new);

			RecordState[] param2 = Arrays.stream(args).filter(a -> Arrays.stream(RecordState.values())
				.map(Enum::name).anyMatch(f->f.equalsIgnoreCase(a)))
				.map(a -> RecordState.valueOf(a.toUpperCase()))
				.distinct().toArray(RecordState[]::new);
			STASHUnit[] filter1 = (param1.length == 0) ? STASHUnit.values() : param1;
			RecordState[] filter2 = (param2.length == 0) ? (new RecordState[] {RecordState.NOT_BUILT, RecordState.BUILT_MAYBE_FILLED, RecordState.BUILT_NOT_FILLED, RecordState.BUILT_FILLED}) : param2;
			units.stream().filter(f -> Arrays.stream(filter1).anyMatch(j -> j.equals(f.get_1())) && Arrays.stream(filter2).anyMatch(j -> j.equals(f.get_2())))
				.sorted((a, b) -> (a.get_2().equals(b.get_2())) ? a.get_1().name().compareTo(b.get_1().name()) : a.get_2().ordinal() - b.get_2().ordinal())
				.forEach(j ->
					client.addChatMessage(ChatMessageType.GAMEMESSAGE, "Stash", j.get_1().name() + " -> " + j.get_2().name(), null)
				);
		}
	}

	private void onScriptCallback(ScriptCallbackEvent event)
	{
		if (!event.getEventName().contains("1478_callback"))
		{
			return;
		}
		int[] stack = fredManager.getStackTools().copyIntsFromStack(3);
		STASHUnit unit = Arrays.stream(STASHUnit.values()).filter(f -> f.getObjectId() == stack[0]).findFirst().orElse(null);
		boolean filled = stack[1] == 1;
		boolean built = stack[2] == 1;
		if (unit != null)
		{
			cache.updateRecord(unit, built ? (filled ? RecordState.BUILT_FILLED : RecordState.BUILT_NOT_FILLED) : RecordState.NOT_BUILT);
		}
		else
		{
			log.error("name: {}, (0: {}, 1: {}, 2: {})", event.getEventName(), stack[0], stack[1], stack[2]);
		}
	}

	@Override
	protected void startUp()
	{
		eventBus.subscribe(GameTick.class, GAME_TICK_LOCK, this::onGameTick);
		eventBus.subscribe(GameStateChanged.class, this, this::onGameStateChanged);
		eventBus.subscribe(CommandExecuted.class, this, this::onCommandExecuted);
		eventBus.subscribe(ScriptCallbackEvent.class, this, this::onScriptCallback);
//		if(client.getLocalPlayer() != null)
//		{
//			cache.setCache(client.getLocalPlayer().getName());
//		}
//		else
//		{
//			cache.setCache(null);
//		}
	}

	@Override
	protected void shutDown()
	{
		eventBus.unregister(this);
		eventBus.unregister(GAME_TICK_LOCK);
		cache.setCache(null);
	}
}
