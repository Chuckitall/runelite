package net.runelite.client.plugins.groovy;

import com.google.common.base.Strings;
import com.google.inject.Binder;
import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.groovy.script.ScriptContext;
import net.runelite.client.plugins.groovy.script.ScriptWrapper;
import net.runelite.client.plugins.groovy.script.ScriptWrapper.ScriptCommand;
import net.runelite.client.plugins.groovy.script.ScriptWrapper.ScriptPanel;
import net.runelite.client.plugins.groovy.script.ScriptWrapper.ScriptState;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

import static net.runelite.client.plugins.groovy.GroovyScriptsParse.NEWLINE_SPLITTER;

/**
 * Authors fred
 */
@PluginDescriptor(
	name = "Groovy Core",
	description = "Allows for Groovy scripting at runtime",
	tags = {"fred", "shell", "scripting", "groovy"},
	type = PluginType.FRED
)
@Singleton
@Slf4j
public class GroovyCore extends Plugin
{
	@Inject
	private ScheduledExecutorService executorService;

	private NavigationButton navButton;

	@Setter(AccessLevel.PACKAGE)
	private GroovyPanel panel;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private Client client;

	@Inject
	private EventBus eventBus;

	@Inject
	private MenuManager menuManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private GroovyConfig config;

	@Getter(AccessLevel.PUBLIC)
	private static String groovyRoot;

	@Getter(AccessLevel.PUBLIC)
	private static String groovyScripts;

	private final List<ScriptWrapper> scripts = new ArrayList<>();

	@Provides
	GroovyConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(GroovyConfig.class);
	}

	@Override
	public void configure(Binder binder)
	{
		log.debug("configure callback");
	}

	private void purgeAllScripts()
	{
		scripts.stream().filter(f->f.getState() == ScriptState.ENABLED).forEach(f -> f.doCommand(ScriptCommand.DISABLE));
		scripts.clear();
	}

	private void loadScriptsFromConfig()
	{
		purgeAllScripts();

		groovyRoot = config.groovyRoot();
		groovyScripts = config.groovyScripts();
		if (!GroovyScriptsParse.parse(groovyScripts))
		{
			return;
		}


		if (!Strings.isNullOrEmpty(groovyScripts))
		{
			final StringBuilder sb = new StringBuilder();

			for (String str : groovyScripts.split("\n"))
			{
				if (!str.startsWith("//"))
				{
					sb.append(str).append("\n");
				}
			}

			@SuppressWarnings("UnstableApiUsage") final Map<String, String> split = NEWLINE_SPLITTER.withKeyValueSeparator(" | ").split(sb);
			for (Map.Entry<String, String> entry : split.entrySet())
			{
				final String fileName =  entry.getKey().trim();
				final boolean enabled = Boolean.parseBoolean(entry.getValue().trim());
				ScriptWrapper temp = new ScriptWrapper(fileName, new ScriptContext(client, eventBus, menuManager, overlayManager));
				temp.doCommand(ScriptCommand.LOAD);
				if (enabled)
				{
					temp.doCommand(ScriptCommand.ENABLE);
				}
				scripts.add(temp);
			}
		}
	}

	private final Supplier<List<ScriptPanel>> scriptEntrySupplier = () -> scripts.stream().map(ScriptWrapper::getPanel).collect(Collectors.toList());

	@Override
	protected void startUp()
	{
		panel = new GroovyPanel(this, client, eventBus, scriptEntrySupplier);
		final BufferedImage icon = loadImage("panel_icon");
		navButton = NavigationButton.builder()
			.tooltip("Groovy")
			.icon(icon)
			.priority(2)
			.panel(panel)
			.build();
		clientToolbar.addNavigation(navButton);

		loadScriptsFromConfig();
		addSubscriptions();
		this.updateList();
	}

	BufferedImage loadImage(String path)
	{
		return ImageUtil.getResourceStreamFromClass(getClass(), "/net/runelite/client/plugins/groovy/" + path + ".png");
	}

	@Override
	protected void shutDown()
	{
		purgeAllScripts();
		eventBus.unregister(this);
		clientToolbar.removeNavigation(navButton);
	}

	private void addSubscriptions()
	{
		eventBus.subscribe(ConfigChanged.class, this, this::onConfigChanged);
	}

	private void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals("groovy"))
		{
			return;
		}
		if (event.getKey().equalsIgnoreCase("groovyRoot") || event.getKey().equalsIgnoreCase("groovyScripts"))
		{
			loadScriptsFromConfig();
		}
	}

	void updateList()
	{
		executorService.submit(panel::rebuildScriptPanels);
	}
}
