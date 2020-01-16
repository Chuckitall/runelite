package net.runelite.client.plugins.beanshell2;

import com.google.common.base.Strings;
import com.google.inject.Binder;
import com.google.inject.Provides;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.beanshell2.interfaces.BshContext;
import net.runelite.client.ui.overlay.OverlayManager;
import static net.runelite.client.plugins.beanshell2.BshLocationsParse.NEWLINE_SPLITTER;

/**
 * Authors fred
 */
@PluginDescriptor(
	name = "Beanshell Core",
	description = "Allows for Beanshell scripting at runtime",
	tags = {"fred", "shell", "scripting"},
	type = PluginType.FRED
)
@Singleton
@Slf4j
public class BshCore extends Plugin
{
	@Inject
	private ScheduledExecutorService executorService; //Used for rebuilding the gui

	@Inject
	private Client client;

	@Inject
	private EventBus eventBus;

	@Inject
	private MenuManager menuManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private BshConfig config;

	@Inject
	private BshManager bshManager;

	//settings
	@Getter(AccessLevel.PUBLIC)
	private String bshRoot;

	@Getter(AccessLevel.PUBLIC)
	private String bshScripts;

	@Provides
	BshConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BshConfig.class);
	}

	@Override
	public void configure(Binder binder)
	{
		log.debug("configure callback");
	}

	private void loadScriptsFromConfig()
	{
		this.bshScripts = config.bshScripts();
		bshManager.clear();
		if (!Strings.isNullOrEmpty(bshScripts))
		{
			final StringBuilder sb = new StringBuilder();

			for (String str : bshScripts.split("\n"))
			{
				if (!str.startsWith("//"))
				{
					sb.append(str).append("\n");
				}
			}

			@SuppressWarnings("UnstableApiUsage") final Map<String, String> split = NEWLINE_SPLITTER.withKeyValueSeparator(" | ").split(sb);
			for (Map.Entry<String, String> entry : split.entrySet())
			{
				final String name = entry.getKey().trim();
				final boolean enabled = Boolean.parseBoolean(entry.getValue().trim());
				final String resolvedName = bshRoot + name;

				BshContext context = new BshContext(name, client, eventBus, menuManager, overlayManager);

				int uuid = bshManager.registerScript(resolvedName, context);
				bshManager.enablePlugin(uuid, enabled);
			}
		}
	}

	@Override
	protected void startUp()
	{
		this.bshRoot = config.bshRoot();
		loadScriptsFromConfig();
		addSubscriptions();
	}

	@Override
	protected void shutDown()
	{
		bshManager.clear();
		eventBus.unregister(this);
	}

	private void addSubscriptions()
	{
		eventBus.subscribe(ConfigChanged.class, this, this::onConfigChanged);
	}

	private void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals("bsh"))
		{
			return;
		}
		if (event.getKey().equalsIgnoreCase("bshRoot") || event.getKey().equalsIgnoreCase("bshScripts"))
		{
			loadScriptsFromConfig();
		}
	}
}
