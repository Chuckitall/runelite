package net.runelite.client.plugins.hotkeys.utils;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.events.ConfigChanged;
import org.apache.commons.lang3.ObjectUtils;
import static org.apache.commons.lang3.math.NumberUtils.createInteger;

@Singleton
@Slf4j
public class MenuEntrySwapHandler
{
	public static final String CONFIG_GROUP = "hotkeys";
	public static final String MIN_DELAY_CONFIG_KEY = "mindelay";
	public static final String MAX_DELAY_CONFIG_KEY = "maxdelay";
	public static final int DEFAULT_MIN_DELAY = 25;
	public static final int DEFAULT_MAX_DELAY = 50;
	private ArrayBlockingQueue<MenuEntry> autoEntriesQueue = new ArrayBlockingQueue<>(50, true);
	private final Client client;
	private int minDelay = DEFAULT_MIN_DELAY;
	private int maxDelay = DEFAULT_MAX_DELAY;
	private ScheduledThreadPoolExecutor executorService = new ScheduledThreadPoolExecutor(50, new ThreadPoolExecutor.DiscardPolicy());
	private MenuEntrySwapConsumer menuEntrySwapConsumerAutomatic;

	private ConfigManager configManager;

	private static Set<Integer> exceptionList = ImmutableSet.of(11927560);

	@Inject
	public MenuEntrySwapHandler(Client client, EventBus eventBus, ConfigManager configManager)
	{
		this.client = client;
		this.configManager = configManager;
		eventBus.subscribe(CommandExecuted.class, this, this::onCommandExecuted);
		eventBus.subscribe(ConfigChanged.class, this, this::onConfigChanged);
		updateDelay();
		menuEntrySwapConsumerAutomatic = new MenuEntrySwapConsumer(autoEntriesQueue, minDelay, maxDelay, client, eventBus, configManager);
		executorService.submit(menuEntrySwapConsumerAutomatic);
	}

	public void addEntry(@NonNull MenuEntry entry, int delay)
	{
		final int parent = entry.getParam1() >> 16;
		if (entry.getParam1() > 0 && parent > 0 && !exceptionList.contains(entry.getParam1()))
		{
			final int child = entry.getParam1() & Short.MAX_VALUE;
			final Widget widget = client.getWidget(parent, child);
			log.debug("|{}| Parent: {}, Child: {}", entry.getParam1(), parent, child);
			if (widget == null)
			{
				log.error("|{}| Parent: {}, Child: {}, was null.", entry.getParam1(), parent, child);
				return;
			}
		}

		executorService.schedule(() ->
		{
			try
			{
				log.info("Submitting autoEntriesQueue.put(entry) with the executorService");
				autoEntriesQueue.put(entry);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}, delay, TimeUnit.MILLISECONDS);
	}

	public void addEntry(@NonNull MenuEntry entry)
	{
		final int parent = entry.getParam1() >> 16;
		if (entry.getParam1() > 0 && parent > 0 && !exceptionList.contains(entry.getParam1()))
		{
			final int child = entry.getParam1() & Short.MAX_VALUE;
			final Widget widget = client.getWidget(parent, child);
			log.debug("|{}| Parent: {}, Child: {}", entry.getParam1(), parent, child);
			if (widget == null)
			{
				log.error("|{}| Parent: {}, Child: {}, was null.", entry.getParam1(), parent, child);
				return;
			}
		}
		executorService.submit(() ->
		{
			try
			{
				log.info("Submitting autoEntriesQueue.put(entry) with the executorService");
				autoEntriesQueue.put(entry);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		});
	}

	public void addEntries(List<MenuEntry> entries)
	{
		if (entries.isEmpty())
		{
			return;
		}
		for (MenuEntry entry : entries)
		{
			final int parent = entry.getParam1() >> 16;
			if (entry.getParam1() > 0 && parent > 0 && !exceptionList.contains(entry.getParam1()))
			{
				final int child = entry.getParam1() & Short.MAX_VALUE;
				final Widget widget = client.getWidget(parent, child);
				log.debug("|{}| Parent: {}, Child: {}", entry.getParam1(), parent, child);
				if (widget == null)
				{
					log.error("|{}| Parent: {}, Child: {}, was null.", entry.getParam1(), parent, child);
					return;
				}
			}
		}
		executorService.submit(() ->
		{
			try
			{
				log.info("putting in a bunch of shit");
				autoEntriesQueue.addAll(entries);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		});
	}

	public void flushQueue()
	{
		menuEntrySwapConsumerAutomatic.setMenuEntryInProgress(null);
		autoEntriesQueue.clear();
	}

	private void onCommandExecuted(CommandExecuted commandExecuted)
	{
		final String[] args = commandExecuted.getArguments();

		switch (commandExecuted.getCommand())
		{
			case "setMinDelay":
			{
				String message;
				if (args.length < 1)
				{
					message = "Min Delay: " + minDelay;
				}
				else
				{
					int newDelay = Integer.parseInt(args[0]);
					configManager.setConfiguration(CONFIG_GROUP, MIN_DELAY_CONFIG_KEY, newDelay);
					message = "Current Delay: " + minDelay;
				}
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", message, null);
				break;
			}
			case "setMaxDelay":
			{
				String message;
				if (args.length < 1)
				{
					message = "Max Delay: " + maxDelay;
				}
				else
				{
					int newDev = Integer.parseInt(args[0]);
					configManager.setConfiguration(CONFIG_GROUP, MAX_DELAY_CONFIG_KEY, newDev);
					message = "Max Delay: " + maxDelay;
				}
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", message, null);
				break;
			}
		}
	}

	private void updateDelay()
	{
		Thread updateDelayThread = new Thread()
		{
			public void run()
			{
				while (configManager == null)
				{
				}
				minDelay = ObjectUtils.defaultIfNull(createInteger(configManager.getConfiguration(CONFIG_GROUP, MIN_DELAY_CONFIG_KEY)), DEFAULT_MIN_DELAY);
				maxDelay = ObjectUtils.defaultIfNull(createInteger(configManager.getConfiguration(CONFIG_GROUP, MAX_DELAY_CONFIG_KEY)), DEFAULT_MAX_DELAY);
				if (menuEntrySwapConsumerAutomatic != null)
				{
					menuEntrySwapConsumerAutomatic.setMinDelay(minDelay);
					menuEntrySwapConsumerAutomatic.setMaxDelay(maxDelay);
				}
			}
		};
		updateDelayThread.start();
	}

	private void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals("hotkeys"))
		{
			return;
		}
		updateDelay();
	}
}
