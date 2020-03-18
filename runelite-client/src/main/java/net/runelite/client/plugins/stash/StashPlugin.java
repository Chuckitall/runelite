package net.runelite.client.plugins.stash;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.google.inject.Inject;
import com.google.inject.Provides;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Experience;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.ScriptID;
import net.runelite.api.Skill;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.kit.KitType;
import net.runelite.api.util.Text;
import net.runelite.client.chat.ChatCommandManager;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.fred.api.other.Tuples;
import net.runelite.client.plugins.stash.StashCache.RecordState;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.MiscUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.LoggerFactory;

import static java.lang.Math.min;

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
	@Getter(AccessLevel.PUBLIC)
	private StashCache cache;

	@Provides
	StashConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(StashConfig.class);
	}

	private void onGameStateChanged(GameStateChanged event)
	{
		if(event.getGameState().equals(GameState.LOGGED_IN))
		{
			cache.setCache(client.getLocalPlayer().getName());
		}
		else if(event.getGameState().equals(GameState.LOGIN_SCREEN))
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
			Arrays.stream(cache.getCached()).forEach(j -> client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", j.name(), null));
		}
		else if (command.equals("isBuilt") && args.length > 0)
		{
			Arrays.stream(STASHUnit.values()).filter(f -> Arrays.stream(args).anyMatch(a -> a.equals(f.getObjectId()+""))).forEach(
				unit ->
				{
					RecordState isBuilt = cache.getRecord(unit);
					if (isBuilt.equals(RecordState.INVALID))
					{
						client.runScript(ScriptID.WATSON_STASH_UNIT_CHECK, unit.getObjectId(), 0, 0, 0);
						int[] intStack = client.getIntStack();
						isBuilt = (intStack[0] == 1) ? RecordState.BUILT_MAYBE_FILLED : RecordState.NOT_BUILT;
					}
					log.debug("unit {} -> state {}", unit.name(), isBuilt.name());
					cache.updateRecord(unit, isBuilt);
				}
			);

		}
	}

	@Override
	protected void startUp()
	{
		if(client.getGameState().equals(GameState.LOGGED_IN))
		{
			cache.setCache(client.getLocalPlayer().getName());
		}
		else
		{
			cache.setCache(null);
		}
		eventBus.subscribe(GameStateChanged.class, this, this::onGameStateChanged);
		eventBus.subscribe(CommandExecuted.class, this, this::onCommandExecuted);
	}

	@Override
	protected void shutDown()
	{
		cache.setCache(null);
		eventBus.unregister(this);
	}
}
