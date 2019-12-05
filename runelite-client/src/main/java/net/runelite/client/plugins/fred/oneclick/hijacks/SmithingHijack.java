package net.runelite.client.plugins.fred.oneclick.hijacks;


import com.google.common.collect.ImmutableSet;
import com.google.inject.Singleton;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.MenuOpcode;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.fred.oneclick.util.MenuEntryHijack;

import static net.runelite.api.MenuOpcode.ITEM_USE_ON_GAME_OBJECT;
import static net.runelite.api.MenuOpcode.WIDGET_DEFAULT;
import static net.runelite.api.MenuOpcode.WIDGET_TYPE_6;
import static net.runelite.api.ObjectID.ANVIL_2097;

@Slf4j
@Singleton
public class SmithingHijack extends MenuEntryHijack
{
	private static final String UNNOTE = "Un-note";
	private static final String YES = "Yes";
	private static final String MAKE = "Make";
	private static final String MAKEDART = "Dart";
	private static final Set<Integer> BARS = ImmutableSet.of(
		ItemID.BRONZE_BAR, ItemID.IRON_BAR, ItemID.STEEL_BAR
	);

	private static final Set<Integer> NOTED_BARS = ImmutableSet.of(
		ItemID.BRONZE_BAR + 1, ItemID.IRON_BAR + 1, ItemID.STEEL_BAR + 1
	);

	private int noteIdx;
	private int noteId;
	private boolean note;
	private int barIdx;
	private int barId;
	private boolean bar;
	private boolean hammer;

	private GameObjectQuery anvilQuery = new GameObjectQuery().idEquals(ANVIL_2097);
	private GameObjectQuery bankQuery = new GameObjectQuery().idEquals(34810);

	@Override
	protected void onEnabled()
	{
		this.barIdx = -1;
		this.barId = -1;
		this.bar = false;
		this.noteIdx = -1;
		this.noteId = -1;
		this.note = false;
		this.hammer = false;
		eventBus.subscribe(ItemContainerChanged.class, this, this::onItemContainerChanged);
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			this.onItemContainerChanged(new ItemContainerChanged(InventoryID.INVENTORY.getId(), client.getItemContainer(InventoryID.INVENTORY)));
		}
	}

	@Override
	protected void onDisabled()
	{
		this.barIdx = -1;
		this.barId = -1;
		this.bar = false;
		this.noteIdx = -1;
		this.noteId = -1;
		this.note = false;
		this.hammer = false;
		eventBus.unregister(this);
	}

	@Override
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (hammer && event.getMenuOpcode() == MenuOpcode.ITEM_USE && (NOTED_BARS.contains(event.getIdentifier()) || BARS.contains(event.getIdentifier())))
		{
			GameObject bank = bankQuery.result(client).nearestTo(client.getLocalPlayer());
			GameObject anvil = anvilQuery.result(client).nearestTo(client.getLocalPlayer());
			if (NOTED_BARS.contains(event.getIdentifier()) && bank != null && !bar)
			{
				this.noteIdx = event.getParam0();
				this.noteId = event.getIdentifier();
				Widget w = client.getWidget(WidgetInfo.DIALOG_OPTION) != null ? client.getWidget(WidgetInfo.DIALOG_OPTION).getParent() : null;
				if (w == null || w.isHidden())
				{
					client.insertMenuItem(UNNOTE, "<col=ff9040>" + client.getItemDefinition(noteId).getName() + "<col=ffffff> -> <col=ffff>Bank booth", ITEM_USE_ON_GAME_OBJECT.getId(), bank.getId(), bank.getLocalLocation().getSceneX(), bank.getLocalLocation().getSceneY(), true);
				}
				else if (!w.isHidden())
				{
					Widget[] dChild = w.getDynamicChildren();
					if (dChild.length == 5 && dChild[0] != null && dChild[0].getText().equalsIgnoreCase("Un-note the banknotes?"))
					{
						Widget yes = dChild[1];
						if (yes != null && yes.getText().equalsIgnoreCase("Yes") && !yes.isHidden())
						{
							client.insertMenuItem(YES, "", MenuOpcode.WIDGET_TYPE_6.getId(), 0, 1, yes.getId(), true);
						}
					}
				}
			}
			else if (BARS.contains(event.getIdentifier()) && anvil != null && bar)
			{
				this.barIdx = event.getParam0();
				this.barId = event.getIdentifier();
				Widget w = client.getWidget(WidgetInfo.SMITHING_ANVIL_DART_TIPS);
				if (w == null || w.isHidden())
				{
					client.insertMenuItem(MAKE, "<col=ff9040>" + client.getItemDefinition(barId).getName() + "<col=ffffff> -> <col=ffff>" + client.getObjectDefinition(anvil.getId()).getName(), ITEM_USE_ON_GAME_OBJECT.getId(), anvil.getId(), anvil.getLocalLocation().getSceneX(), anvil.getLocalLocation().getSceneY(), true);
				}
				else
				{
					client.insertMenuItem(MAKEDART, "<col=ff9040>" + client.getItemDefinition(barId).getName().split(" ")[0] + " dart tip</col>", WIDGET_DEFAULT.getId(), 1, -1, w.getId(), true);
				}
			}
		}
	}

	@Override
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (!this.isEnabled())// should be unnecessary
		{
			return;
		}
		if (hammer && note && event.getMenuOpcode() == WIDGET_TYPE_6 && event.getOption().equals(YES))
		{
			event.setOption("Continue");
		}
		else if (hammer && note && event.getMenuOpcode() == ITEM_USE_ON_GAME_OBJECT && event.getOption().equals(UNNOTE))
		{
			event.setOption("Use");
			client.setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
			client.setSelectedItemSlot(noteIdx);
			client.setSelectedItemID(noteId);
		}
		else if (hammer && bar && event.getMenuOpcode() == ITEM_USE_ON_GAME_OBJECT && event.getOption().equals(MAKE))
		{
			event.setOption("Use");
			client.setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
			client.setSelectedItemSlot(barIdx);
			client.setSelectedItemID(barId);
		}
		else if (hammer && bar && event.getMenuOpcode() == WIDGET_DEFAULT && event.getOption().equals(MAKEDART))
		{
			event.setOption("Smith set");
		}
	}

	private void onItemContainerChanged(ItemContainerChanged event)
	{
		if (!this.isEnabled()) //should be unnecessary
		{
			return;
		}

		final ItemContainer itemContainer = event.getItemContainer();
		final List<Item> items = Arrays.asList(itemContainer.getItems());

		if (itemContainer != client.getItemContainer(InventoryID.INVENTORY))
		{
			return;
		}
		noteIdx = -1;
		noteId = -1;
		note = false;
		barIdx = -1;
		barId = -1;
		bar = false;
		hammer = false;

		for (int i = 0; i < items.size(); i++)
		{
			final int itemId = items.get(i).getId();
			if (!note && NOTED_BARS.contains(itemId))
			{
				noteId = itemId;
				noteIdx = i;
				note = true;
			}
			if (!bar && BARS.contains(itemId))
			{
				barId = itemId;
				barIdx = i;
				bar = true;
			}
			if (ItemID.HAMMER == itemId)
			{
				hammer = true;
			}
		}
	}
}
