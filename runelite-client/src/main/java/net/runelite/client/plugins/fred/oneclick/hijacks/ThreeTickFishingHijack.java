package net.runelite.client.plugins.fred.oneclick.hijacks;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.MenuOpcode;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.fred.oneclick.util.MenuEntryHijack;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.client.ui.overlay.components.table.TableAlignment;
import net.runelite.client.ui.overlay.components.table.TableComponent;

@Slf4j
@Singleton
public class ThreeTickFishingHijack extends MenuEntryHijack
{
	private static final String MAKE_VAMBRACE = "Make Vambrace";
	private static final String MAKE_HERB = "Make Tar";
	private static final Set<Integer> HERBS = ImmutableSet.of(
		ItemID.GUAM_LEAF, ItemID.MARRENTILL, ItemID.TARROMIN, ItemID.HARRALANDER
	);

	private Overlay fishOverlay;

	@Inject
	private OverlayManager overlayManager;

	private int tick = 0;
	private int vambraceIdx;
	private int vambraceId;
	private boolean vambrace;
	private int tarIdx;
	private int tarId;
	private boolean tar;
	private boolean pestle;

	private boolean fish;
	private int fishId;
	private int fishIdx;

	private boolean canDrop;

	@Override
	protected void onEnabled()
	{
		this.tarIdx = -1;
		this.tarId = -1;
		this.tar = false;
		this.pestle = false;

		this.canDrop = false;

		this.fish = false;
		this.fishId = -1;
		this.fishIdx = -1;

		this.vambraceIdx = -1;
		this.vambraceId = -1;
		this.vambrace = false;

		this.fishOverlay = new Overlay()
		{
			private final PanelComponent panelComponent = new PanelComponent();
			@Override
			public Dimension render(Graphics2D graphics)
			{
				panelComponent.getChildren().clear();
				panelComponent.setPreferredSize(new Dimension(55, 0));
				panelComponent.getChildren().add(TitleComponent.builder()
					.text("3T Fish")
					.color(Color.BLUE)
					.build());

				TableComponent tableComponent = new TableComponent();
				tableComponent.setColumnAlignments(TableAlignment.LEFT, TableAlignment.RIGHT);
				tableComponent.addRow("Tick:", tick + "");

				panelComponent.getChildren().add(tableComponent);
				return panelComponent.render(graphics);
			}
		};

		fishOverlay.setPosition(OverlayPosition.BOTTOM_LEFT);

		overlayManager.add(fishOverlay);

		eventBus.subscribe(ItemContainerChanged.class, this, this::onItemContainerChanged);
		eventBus.subscribe(GameTick.class, this, this::onGameTick);
		ItemContainer temp = client.getItemContainer(InventoryID.INVENTORY);
		this.onItemContainerChanged(new ItemContainerChanged(InventoryID.INVENTORY.getId(), temp));
	}

	@Override
	protected void onDisabled()
	{
		this.tarId = -1;
		this.tarIdx = -1;
		this.tar = false;
		this.canDrop = false;
		this.fish = false;
		this.fishId = -1;
		this.fishIdx = -1;
		this.vambraceId = -1;
		this.vambraceIdx = -1;
		this.vambrace = false;
		this.pestle = false;

		overlayManager.remove(fishOverlay);
		eventBus.unregister(this);
	}

	@Override
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		final int id = event.getIdentifier();
		if (this.tar && this.pestle && event.getOpcode() == MenuOpcode.ITEM_USE.getId() && HERBS.contains(id))
		{
			log.debug("added");
			event.setOption(MAKE_HERB);
			event.setModified();
		}
		else if (this.vambrace && event.getOpcode() == MenuOpcode.ITEM_USE.getId() && id == ItemID.KEBBIT_CLAWS)
		{
			log.debug("added");
			event.setOption(MAKE_VAMBRACE);
			event.setModified();
		}
	}

	@Override
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (!this.isEnabled())// should be unnecessary
		{
			return;
		}
//		final MenuEntry entry = event.getMenuEntry();

		int newId = -1;
		int newIdx = -1;
		boolean newSwap = false;

		if (tar && pestle && event.getOption().equals(MAKE_HERB))
		{
			newId = tarId;
			newIdx = tarIdx;
			newSwap = true;
		}
		else if (vambrace && event.getOption().equals(MAKE_VAMBRACE))
		{
			newId = vambraceId;
			newIdx = vambraceIdx;
			newSwap = true;
		}

		if (newSwap && event.getOpcode() == MenuOpcode.ITEM_USE.getId() && tick >= 3)
		{
			tick = 0;
			event.setOpcode(MenuOpcode.ITEM_USE_ON_WIDGET_ITEM.getId());
			event.setOption("Use");
			client.setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
			client.setSelectedItemSlot(newIdx);
			client.setSelectedItemID(newId);
			canDrop = fish;
			log.debug("3 tick action {}", event);
		}
//		else if (canDrop && event.getOpcode() == MenuOpcode.ITEM_USE.getId() && tick == 1)
//		{
//			entry.setOpcode(MenuOpcode.ITEM_DROP.getId());
//			entry.setParam0(fishIdx);
//			entry.setIdentifier(fishId);
//			entry.setOption("Drop");
//			canDrop = false;
//			log.debug("drop action -> {}", event);
//		}
	}

	private void onGameTick(GameTick event)
	{
		tick++;
	}

	private void onItemContainerChanged(ItemContainerChanged event)
	{
		if (!this.isEnabled()) //should be unnecessary
		{
			return;
		}

		final ItemContainer itemContainer = event.getItemContainer();
		final List<Item> items = Arrays.asList(itemContainer.getItems());

		if (itemContainer != client.getItemContainer(InventoryID.INVENTORY) || !Collections.disjoint(items, HERBS))
		{
			return;
		}
		tarIdx = -1;
		tarId = -1;
		tar = false;
		pestle = false;
		fish = false;
		fishId = -1;
		fishIdx = -1;

		vambrace = false;
		vambraceId = -1;
		vambraceIdx = -1;

		for (int i = 0; i < items.size(); i++)
		{
			final int itemId = items.get(i).getId();
			if (!tar && ItemID.SWAMP_TAR == itemId)
			{
				tarId = itemId;
				tarIdx = i;
				tar = true;
			}
			if (!pestle && ItemID.PESTLE_AND_MORTAR == itemId)
			{
				pestle = true;
			}
//			if (!fish && (ItemID.LEAPING_SALMON == itemId || ItemID.LEAPING_STURGEON == itemId || ItemID.LEAPING_TROUT == itemId))
//			{
//				this.fish = true;
//				this.fishIdx = i;
//				this.fishId = itemId;
//			}
			if (!vambrace && (ItemID.LEATHER_VAMBRACES == itemId))
			{
				this.vambrace = true;
				this.vambraceIdx = i;
				this.vambraceId = itemId;
			}
		}
	}
}
