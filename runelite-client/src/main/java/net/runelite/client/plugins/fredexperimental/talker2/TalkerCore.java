package net.runelite.client.plugins.fredexperimental.talker2;

import com.google.inject.Inject;
import com.google.inject.Provides;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.ui.overlay.OverlayManager;

import static net.runelite.api.widgets.WidgetID.DIALOG_NPC_GROUP_ID;
import static net.runelite.api.widgets.WidgetID.DIALOG_OPTION_GROUP_ID;
import static net.runelite.api.widgets.WidgetID.DIALOG_PLAYER_GROUP_ID;
import static net.runelite.api.widgets.WidgetID.MULTISKILL_MENU_GROUP_ID;

/**
 * Created by npruff on 9/2/2019.
 */

@PluginDescriptor(
	name = "Fred Talker 2",
	description = "Talker/Interactor bot",
	tags = {"flexo", "utility", "fred"},
	type = PluginType.FRED
)
@Slf4j
public class TalkerCore extends Plugin
{
	public static final String TALKER_GROUP = "talker";

	/**
	 * Context items injected from the client go here.
	 */
	@Inject
	@Getter(AccessLevel.PACKAGE)
	private Client client;


	@Inject
	@Getter(AccessLevel.PACKAGE)
	private KeyManager keyManager;

	@Inject
	@Getter(AccessLevel.PACKAGE)
	private OverlayManager overlayManager;

	@Inject
	@Getter(AccessLevel.PACKAGE)
	private EventBus eventBus;

	@Inject
	@Getter(AccessLevel.PUBLIC)
	private ConfigManager configManager;


	/**
	 * Context items injected from local package go here.
	 */
	@Inject
	@Getter(AccessLevel.PUBLIC)
	private TalkerConfig config;

	@Inject
	@Getter(AccessLevel.PACKAGE)
	private TalkerOverlay talkerOverlay;

	/**
	 * CONFIG file here
	 */
	@ConfigGroup(TalkerCore.TALKER_GROUP)
	public interface TalkerConfig extends Config
	{

		@ConfigItem(
			position = 0,
			keyName = "autoSpace",
			name = "Hit space automatically",
			description = "Clicks through dialogs where the only option is space."
		)
		default boolean autoSpace()
		{
			return false;
		}
	}

	@Provides
	TalkerConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TalkerConfig.class);
	}


	@Override
	protected void startUp()
	{
		eventBus.subscribe(WidgetLoaded.class, this, this::onWidgetLoaded);
		eventBus.subscribe(MenuOptionClicked.class, this, this::onMenuOptionClicked);
		eventBus.subscribe(MenuEntryAdded.class, this, this::onMenuEntryAdded);
		overlayManager.add(talkerOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		eventBus.unregister(this);
		overlayManager.remove(talkerOverlay);
	}

	private void onMenuOptionClicked(MenuOptionClicked clicked)
	{

	}

	private void onMenuEntryAdded(MenuEntryAdded added)
	{

	}

	private void onWidgetLoaded(WidgetLoaded widgetLoaded)
	{
		int g = widgetLoaded.getGroupId();
		if (g == DIALOG_NPC_GROUP_ID)
		{

		}
		else if (g == DIALOG_OPTION_GROUP_ID)
		{
			debugWidget(client.getWidget(WidgetInfo.DIALOG_OPTION));
		}
		else if (g == DIALOG_PLAYER_GROUP_ID)
		{

		}
	}

	//Debugging
	private static final Map<Integer, WidgetInfo> widget_id_map = new HashMap<>();
	private static WidgetInfo getWidgetInfo(int packedId)
	{
		if (widget_id_map.isEmpty())
		{
			//Initialize map here so it doesn't create the index
			//until it's actually needed.
			WidgetInfo[] widgets = WidgetInfo.values();
			for (WidgetInfo w : widgets)
			{
				widget_id_map.put(w.getPackedId(), w);
			}
		}

		return widget_id_map.get(packedId);
	}

	private void debugWidget(Widget w)
	{
		if (w == null) return;
		boolean foundChildren = false;
		StringBuilder result = new StringBuilder(String.format("WidgetDebug -> %s {", getWidgetName(w.getId())));
		for (Widget c : w.getStaticChildren())
		{
			if (c == null || c.getText().equals("")) continue;
			result.append(String.format("\n\t%s -> %s", getWidgetName(c.getId()), c.getText()));
			foundChildren = true;
		}
		Widget[] dChilds = w.getDynamicChildren();
		for (int i = 0; i < dChilds.length; i++)
		{
			if (dChilds[i] == null || dChilds[i].getText().equals("")) continue;
			result.append(String.format("\n\t%s[%d] -> %s", getWidgetName(w.getId()), i, dChilds[i].getText()));
			foundChildren = true;
		}
		result.append("\n}");
		if (foundChildren)
		{
			log.debug(result.toString());
		}
	}

	private String getWidgetName(int packed)
	{
		String str = Integer.toString(packed);
		WidgetInfo info = getWidgetInfo(packed);
		if (info != null)
		{
			str += " [" + info.name() + "]";
		}

		return str;
	}
}
