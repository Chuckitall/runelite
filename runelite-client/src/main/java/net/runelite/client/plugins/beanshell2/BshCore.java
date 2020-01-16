package net.runelite.client.plugins.beanshell2;

import com.google.common.base.Strings;
import com.google.inject.Binder;
import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;
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
import net.runelite.client.plugins.beanshell2.interfaces.BshContext;
import net.runelite.client.plugins.beanshell2.ui.BshCorePanel;
import net.runelite.client.plugins.beanshell2.ui.BshPluginPanel;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

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
	private ScheduledExecutorService executorService;

	private NavigationButton navButton;

	@Setter(AccessLevel.PACKAGE)
	private BshCorePanel panel;

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

				BshContext context = new BshContext(name, resolvedName, client, eventBus, menuManager, overlayManager);

				int uuid = bshManager.registerScript(context);
				bshManager.enablePlugin(uuid, enabled);
			}
		}
	}

	private final Supplier<List<BshPluginPanel>> scriptEntrySupplier = () -> bshManager.getScriptPanels();

	@Override
	protected void startUp()
	{
		panel = new BshCorePanel(this, scriptEntrySupplier);
		final BufferedImage icon = loadImage("panel_icon");
		navButton = NavigationButton.builder()
			.tooltip("BshCore")
			.icon(icon)
			.priority(2)
			.panel(panel)
			.build();
		clientToolbar.addNavigation(navButton);

		this.bshRoot = config.bshRoot();

		loadScriptsFromConfig();
		addSubscriptions();
		this.updateList();
	}

	BufferedImage loadImage(String path)
	{
		return ImageUtil.getResourceStreamFromClass(getClass(), "/net/runelite/client/plugins/beanshell2/" + path + ".png");
	}

	@Override
	protected void shutDown()
	{
		bshManager.clear();
		eventBus.unregister(this);
		clientToolbar.removeNavigation(navButton);
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

	void updateList()
	{
		executorService.submit(panel::rebuildScriptPanels);
	}
}
