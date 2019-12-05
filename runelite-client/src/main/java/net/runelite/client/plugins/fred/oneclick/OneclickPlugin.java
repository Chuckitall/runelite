package net.runelite.client.plugins.fred.oneclick;

import com.google.common.collect.Lists;
import com.google.inject.Provides;
import java.awt.event.KeyEvent;
import java.util.Arrays;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.client.events.ConfigChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.fred.oneclick.hijacks.BirdhouseHijack;
import net.runelite.client.plugins.fred.oneclick.hijacks.GlassblowingHijack;
import net.runelite.client.plugins.fred.oneclick.hijacks.HerbPasteHijack;
import net.runelite.client.plugins.fred.oneclick.hijacks.MagicHijack;
import net.runelite.client.plugins.fred.oneclick.hijacks.SmithingHijack;
import net.runelite.client.plugins.fred.oneclick.hijacks.ThreeTickFishingHijack;
import net.runelite.client.plugins.fred.oneclick.matchers.ConstructionMatches;
import net.runelite.client.plugins.fred.oneclick.matchers.CraftingMatches;
import net.runelite.client.plugins.fred.oneclick.matchers.FishingMatches;
import net.runelite.client.plugins.fred.oneclick.matchers.FletchingMatches;
import net.runelite.client.plugins.fred.oneclick.matchers.HunterMatches;
import net.runelite.client.plugins.fred.oneclick.matchers.SweatyMatches;
import net.runelite.client.plugins.fred.oneclick.matchers.TwistedMatches;
import net.runelite.client.plugins.fred.oneclick.matchers.UtilityMatches;
import net.runelite.client.plugins.fred.oneclick.matchers.WoodcuttingMatches;
import net.runelite.client.plugins.fred.oneclick.util.MenuMatchSet;
import net.runelite.client.plugins.fred.api.wrappers._Item;
@PluginDescriptor(
	name = "Fred's Oneclick",
	description = "1 Click MES Hijacks -DO NOT ABUSE/AUTOCLICK THESE-",
	tags = {"fred", "MES", "hijack"},
	type = PluginType.EXTERNAL
)
@Slf4j
@Singleton
public class OneclickPlugin extends Plugin
{
	@Inject
	private Client client;
	@Inject
	private EventBus eventBus;
	@Inject
	private OneclickConfig config;
	@Inject
	private KeyManager keyManager;

	//Crafting
	@Inject
	private GlassblowingHijack glassblowingHijack;

	//Hunter
	@Inject
	private BirdhouseHijack birdhouseHijack;

	//Smithing
	@Inject
	private SmithingHijack smithingHijack;

	//Magic
	@Inject
	private MagicHijack magicHijack;

	//Other
	@Inject
	private HerbPasteHijack herbPasteHijack;
	@Inject
	private ThreeTickFishingHijack threeTickFishHijack;

	@Inject
	private WoodcuttingMatches woodcutting;

	@Inject
	private ConstructionMatches construction;

	@Inject
	private HunterMatches hunter;

	@Inject
	private FletchingMatches fletching;

	@Inject
	private FishingMatches fishing;

	@Inject
	private CraftingMatches crafting;

	@Inject
	private UtilityMatches utility;

	@Inject
	private SweatyMatches sweaty;

	@Inject
	private TwistedMatches twisted;

	@Getter(AccessLevel.PUBLIC)
	private int freeInvSpaces = -1;

	@Getter(AccessLevel.PUBLIC)
	@Setter(AccessLevel.PRIVATE)
	private boolean shiftPressed = false;

	private KeyListener shiftListener = new KeyListener()
	{
		private static final int HOTKEY = KeyEvent.VK_SHIFT;

		@Override
		public void keyTyped(KeyEvent e)
		{

		}

		@Override
		public void keyPressed(KeyEvent e)
		{
			if (!isShiftPressed() && e.getKeyCode() == HOTKEY)
			{
				setShiftPressed(true);
			}
		}

		@Override
		public void keyReleased(KeyEvent e)
		{
			if (isShiftPressed() && e.getKeyCode() == HOTKEY)
			{
				setShiftPressed(false);
			}
		}
	};

	private List<MenuMatchSet> getMatchSets()
	{
		return Lists.newArrayList(woodcutting, hunter, fletching, fishing, crafting, construction, utility, sweaty, twisted);
	}

	@Provides
	OneclickConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(OneclickConfig.class);
	}

	@Override
	public void startUp()
	{
		keyManager.registerKeyListener(shiftListener);
		eventBus.subscribe(ConfigChanged.class, this, this::onConfigChanged);
		eventBus.subscribe(GameTick.class, this, this::onGameTick);
		eventBus.subscribe(MenuOptionClicked.class, this, this::onMenuOptionClicked);
		eventBus.subscribe(MenuEntryAdded.class, this, this::onMenuEntryAdded);
		eventBus.subscribe(ItemContainerChanged.class, this, this::onItemContainerChanged);
		for (MenuMatchSet matchSet : getMatchSets())
		{
			matchSet.init(config);
		}

		birdhouseHijack.setEnabled(config.birdhouse());
		glassblowingHijack.setEnabled(config.glassblowing());
		smithingHijack.setEnabled(config.leftClickSmith());
		magicHijack.setEnabled(config.superheat());
		herbPasteHijack.setEnabled(config.leftClickHerbPaste());
		threeTickFishHijack.setEnabled(config.threeTickFishing());

		if (client.getGameState() == GameState.LOGGED_IN)
		{
			this.onItemContainerChanged(new ItemContainerChanged(InventoryID.INVENTORY.getId(), client.getItemContainer(InventoryID.INVENTORY)));
		}
	}

	@Override
	public void shutDown()
	{
		birdhouseHijack.setEnabled(false);
		smithingHijack.setEnabled(false);
		magicHijack.setEnabled(false);
		herbPasteHijack.setEnabled(false);
		threeTickFishHijack.setEnabled(false);
		glassblowingHijack.setEnabled(false);
		keyManager.unregisterKeyListener(shiftListener);
		eventBus.unregister(this);
	}

	private void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals("fredsOneclick"))
		{
			return;
		}
		for (MenuMatchSet matchSet : getMatchSets())
		{
			matchSet.onConfigChanged(event);
		}
		birdhouseHijack.setEnabled(config.birdhouse());
		smithingHijack.setEnabled(config.leftClickSmith());
		magicHijack.setEnabled(config.superheat());
		herbPasteHijack.setEnabled(config.leftClickHerbPaste());
		threeTickFishHijack.setEnabled(config.threeTickFishing());
		glassblowingHijack.setEnabled(config.glassblowing());
	}

	private void onMenuEntryAdded(MenuEntryAdded event)
	{
		for (MenuMatchSet matchSet : getMatchSets())
		{
			if (matchSet.onMenuAdded(event)) break;
		}
	}

	private void onMenuOptionClicked(MenuOptionClicked event)
	{
		for (MenuMatchSet matchSet : getMatchSets())
		{
			if (matchSet.onMenuClicked(event)) break;
		}
	}

	private void onItemContainerChanged(ItemContainerChanged event)
	{
		final ItemContainer itemContainer = event.getItemContainer();
		final List<Item> items = Arrays.asList(itemContainer.getItems());

		if (itemContainer == client.getItemContainer(InventoryID.INVENTORY))
		{
			List<_Item> tempList = Lists.newArrayList();
			this.freeInvSpaces = 28;
			for (int idx = 0; idx < items.size(); idx++)
			{
				final Item item = items.get(idx);
				final int id = item.getId();
				final int qty = item.getQuantity();
				if (qty == 0 && id == -1)
				{
					continue;
				}
				_Item temp = new _Item(id, qty, idx);
				if (qty == 0 || id == -1)
				{
					log.error("temp: '{}' -> was invalid", temp);
				}
				else
				{
					tempList.add(temp);
				}
			}
			this.freeInvSpaces -= tempList.size();
			for (MenuMatchSet matchSet : getMatchSets())
			{
				matchSet.onInventoryChanged(tempList);
			}
		}
	}

	private void onGameTick(GameTick tick)
	{
		for (MenuMatchSet matchSet : getMatchSets())
		{
			matchSet.onGameTick();
		}
	}
}