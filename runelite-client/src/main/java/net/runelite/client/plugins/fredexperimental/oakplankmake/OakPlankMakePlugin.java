package net.runelite.client.plugins.fredexperimental.oakplankmake;

import com.google.inject.Provides;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.fred.util.WidgetUtils;
import net.runelite.client.plugins.fredexperimental.striker.Striker;
import net.runelite.client.plugins.fredexperimental.striker.StrikerUtils;
import net.runelite.client.ui.overlay.OverlayManager;

import static net.runelite.api.ItemID.COINS_995;
import static net.runelite.api.ItemID.OAK_LOGS;
import static net.runelite.api.ItemID.OAK_PLANK;

@PluginDescriptor(
	name = "Fred's Plank Make",
	description = "Chops oak logs at wc guild and turns them into planks.",
	tags = {"fred", "striker", "oak", "plank", "woodcutting"},
	type = PluginType.FRED
)
@Singleton
@Slf4j
public class OakPlankMakePlugin extends Plugin
{
	@Inject
	@Getter(AccessLevel.PACKAGE)
	private Client client;
	@Inject
	private OakPlankMakeConfig config;
	@Inject
	private EventBus eventBus;
	@Inject
	private OakPlankMakeOverlay sceneOverlay;
	@Inject
	private OverlayManager overlayManager;

	private int delay;

	private LocalPoint lastTickLocation;

	@Getter
	private String clickTarget = "";
	@Getter
	private boolean click = false;
	@Getter
	private int animation = -1;

	@Provides
	OakPlankMakeConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(OakPlankMakeConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		this.delay = 50;

		addSubscriptions();
		updateConfig();
		overlayManager.add(sceneOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		this.delay = 50;

		eventBus.unregister(this);
		overlayManager.remove(sceneOverlay);
	}

	private void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals("oakplankmake"))
		{
			updateConfig();
		}
	}

	private void addSubscriptions()
	{
		eventBus.subscribe(ConfigChanged.class, this, this::onConfigChanged);
		eventBus.subscribe(GameTick.class, this, this::onGameTick);
		eventBus.subscribe(ItemContainerChanged.class, this, this::onItemContainerChanged);
		eventBus.subscribe(AnimationChanged.class, this, this::onAnimationChanged);
	}

	private void onGameTick(GameTick gameTick)
	{
		if (click)
		{
			switch (clickTarget)
			{
				case "coins":
					Striker.schedule(() ->
					{
						int[] ids = {COINS_995};
						Striker.clickMouse(StrikerUtils.getClickPoint(Optional.ofNullable(WidgetUtils.getItemWidget(ids, client, true)).map(WidgetItem::getCanvasBounds).orElse(new java.awt.Rectangle(0, 0, 0, 0))), 1);
					}, delay);
					log.debug("coins");
					click = false;
					break;
				case "logs":
					Striker.schedule(() ->
					{
						int[] ids =
							{
								OAK_LOGS
							};
						Striker.clickMouse(StrikerUtils.getClickPoint(Optional.ofNullable(WidgetUtils.getItemWidget(ids, client, true)).map(WidgetItem::getCanvasBounds).orElse(new java.awt.Rectangle(0, 0, 0, 0))), 1);
					}, delay);
					log.debug("logs");
					click = false;
					break;
				case "plank":
					Striker.schedule(() ->
					{
						int[] ids = {
							OAK_PLANK
						};
						java.awt.Rectangle orElse = new java.awt.Rectangle(0, 0, 0, 0);
						Striker.clickMouse(StrikerUtils.getClickPoint(Optional.ofNullable(WidgetUtils.getItemWidget(ids, client, true)).map(WidgetItem::getCanvasBounds).orElse(orElse)), 1);
					}, delay);
					log.debug("plank");
					click = false;
					break;
			}
		}
		else
		{
			Widget w = client.getWidget(WidgetInfo.DIALOG_OPTION) != null ? client.getWidget(WidgetInfo.DIALOG_OPTION).getParent() : null;
			Widget q = client.getWidget(403, 88);

			if (w != null && !w.isHidden())
			{
				Widget[] dChild = w.getDynamicChildren();
				if (dChild.length == 7 && dChild[0] != null && dChild[0].getText().equalsIgnoreCase("How many would you like to deposit?"))
				{
					Widget all = dChild[4];
					if (all != null && all.getText().equalsIgnoreCase("All") && !all.isHidden())
					{
						this.clickTarget = "plank";
						this.click = true;
					}
				}
			}
			else if (q != null && !q.isHidden())
			{
				clickTarget = "logs";
				click = true;
			}

		}
		if (client.getLocalPlayer() != null)
		{
			lastTickLocation = client.getLocalPlayer().getLocalLocation();
		}
	}

	private void onItemContainerChanged(ItemContainerChanged event)
	{
		final ItemContainer itemContainer = event.getItemContainer();
		final List<Item> items = Arrays.asList(itemContainer.getItems());

		if (itemContainer != client.getItemContainer(InventoryID.INVENTORY))
		{
			return;
		}
		log.debug("itemCount: {}", Arrays.stream(itemContainer.getItems()).filter(f -> f.getId() != -1 && f.getQuantity() > 0).count());
		if (Arrays.stream(itemContainer.getItems()).filter(f -> f.getId() != -1 && f.getQuantity() > 0).count() == 28)
		{
			if (Arrays.stream(itemContainer.getItems()).noneMatch(f -> f.getId() == ItemID.OAK_PLANK))
			{
				clickTarget = "logs";
				click = true;
			}
			else
			{
				clickTarget = "plank";
				click = true;
			}
		}
		else if (animation == -1 || animation == 834)
		{
			clickTarget = "coins";
			click = true;
		}
	}

	private void onAnimationChanged(AnimationChanged event)
	{
		if (client.getLocalPlayer() != null && event.getActor() == client.getLocalPlayer())
		{
			log.debug("animation {}", client.getLocalPlayer().getAnimation());
			animation = client.getLocalPlayer().getAnimation();
		}
	}

	private void updateConfig()
	{
		this.delay = config.delay();
	}
}
