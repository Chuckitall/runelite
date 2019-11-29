package net.runelite.client.plugins.fred.oneclick.hijacks;

import com.google.common.collect.ImmutableSet;
import io.reactivex.annotations.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.inject.Singleton;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuOpcode;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.fred.oneclick.util.ItemSelector;
import net.runelite.client.plugins.fred.oneclick.util.MenuEntryHijack;

import static net.runelite.api.ItemID.ADAMANTITE_ORE;
import static net.runelite.api.ItemID.AIR_RUNE;
import static net.runelite.api.ItemID.COPPER_ORE;
import static net.runelite.api.ItemID.COSMIC_RUNE;
import static net.runelite.api.ItemID.EMERALD_AMULET;
import static net.runelite.api.ItemID.EMERALD_BRACELET;
import static net.runelite.api.ItemID.EMERALD_NECKLACE;
import static net.runelite.api.ItemID.EMERALD_RING;
import static net.runelite.api.ItemID.FIRE_RUNE;
import static net.runelite.api.ItemID.GOLD_ORE;
import static net.runelite.api.ItemID.IRON_ORE;
import static net.runelite.api.ItemID.MITHRIL_ORE;
import static net.runelite.api.ItemID.NATURE_RUNE;
import static net.runelite.api.ItemID.RUBY_AMULET;
import static net.runelite.api.ItemID.RUBY_BRACELET;
import static net.runelite.api.ItemID.RUBY_NECKLACE;
import static net.runelite.api.ItemID.RUBY_RING;
import static net.runelite.api.ItemID.RUNITE_ORE;
import static net.runelite.api.ItemID.SAPPHIRE_AMULET;
import static net.runelite.api.ItemID.SAPPHIRE_BRACELET;
import static net.runelite.api.ItemID.SAPPHIRE_NECKLACE;
import static net.runelite.api.ItemID.SAPPHIRE_RING;
import static net.runelite.api.ItemID.SILVER_ORE;
import static net.runelite.api.ItemID.TIN_ORE;
import static net.runelite.api.ItemID.WATER_RUNE;
import static net.runelite.client.plugins.fred.oneclick.util.ItemSelector.rune;

@Singleton
@Slf4j
public class MagicHijack extends MenuEntryHijack
{
	@Builder
	@Data
	private static class SpellOnItem
	{
		@Singular()
		List<ItemSelector> reqRunes;
		ItemSelector targetItems;
		String spellName;
		int widgetId;

		boolean isEnabled()
		{
			return reqRunes.stream().allMatch(ItemSelector::found) && targetItems.found();
		}
	}

	private final SpellOnItem superheat = SpellOnItem.builder()
		.reqRune(rune(NATURE_RUNE, 1))
		.reqRune(rune(FIRE_RUNE, 5))
		.targetItems(new ItemSelector(ImmutableSet.of(COPPER_ORE, TIN_ORE, IRON_ORE, SILVER_ORE, GOLD_ORE, MITHRIL_ORE, ADAMANTITE_ORE, RUNITE_ORE)))
		.spellName("<col=00ff00>Superheat</col>")
		.widgetId(WidgetInfo.PACK(WidgetID.SPELLBOOK_GROUP_ID, 29))
		.build();

	private final SpellOnItem lvl1Enchant = SpellOnItem.builder()
		.reqRune(rune(COSMIC_RUNE, 1))
		.reqRune(rune(WATER_RUNE, 1))
		.targetItems(new ItemSelector(ImmutableSet.of(SAPPHIRE_AMULET, SAPPHIRE_RING, SAPPHIRE_BRACELET, SAPPHIRE_NECKLACE)))
		.spellName("<col=00ff00>Lvl-1 Enchant</col>")
		.widgetId(WidgetInfo.PACK(WidgetID.SPELLBOOK_GROUP_ID, 9))
		.build();

	private final SpellOnItem lvl2Enchant = SpellOnItem.builder()
		.reqRune(rune(COSMIC_RUNE, 1))
		.reqRune(rune(AIR_RUNE, 3))
		.targetItems(new ItemSelector(ImmutableSet.of(EMERALD_AMULET, EMERALD_RING, EMERALD_BRACELET, EMERALD_NECKLACE)))
		.spellName("<col=00ff00>Lvl-2 Enchant</col>")
		.widgetId(WidgetInfo.PACK(WidgetID.SPELLBOOK_GROUP_ID, 20))
		.build();

	private final SpellOnItem lvl3Enchant = SpellOnItem.builder()
		.reqRune(rune(COSMIC_RUNE, 1))
		.reqRune(rune(FIRE_RUNE, 5))
		.targetItems(new ItemSelector(ImmutableSet.of(RUBY_AMULET, RUBY_RING, RUBY_BRACELET, RUBY_NECKLACE)))
		.spellName("<col=00ff00>Lvl-3 Enchant</col>")
		.widgetId(WidgetInfo.PACK(WidgetID.SPELLBOOK_GROUP_ID, 32))
		.build();

	private Set<SpellOnItem> spells = ImmutableSet.of(superheat/*, lvl1Enchant, lvl2Enchant, lvl3Enchant*/);

	private void clearSelectors()
	{
		spells.forEach(f -> f.reqRunes.forEach(ItemSelector::clear));
		spells.forEach(f -> f.targetItems.clear());
	}

	@Override
	protected void onEnabled()
	{
		clearSelectors();
		eventBus.subscribe(ItemContainerChanged.class, this, this::onItemContainerChanged);
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			this.onItemContainerChanged(new ItemContainerChanged(InventoryID.INVENTORY.getId(), client.getItemContainer(InventoryID.INVENTORY)));
		}
	}

	@Override
	protected void onDisabled()
	{
		clearSelectors();
		eventBus.unregister(this);
	}

	@Override
	protected void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (!this.isEnabled())// should be unnecessary
		{
			return;
		}
		if (event.getMenuOpcode() != MenuOpcode.WIDGET_TYPE_2)
		{
			return;
		}
		@Nullable SpellOnItem spell = spells.stream().filter(f -> f.getWidgetId() == event.getParam1()).findFirst().orElse(null);
		if (spell == null || !spell.isEnabled())
		{
			return;
		}
		client.insertMenuItem(spell.getSpellName(), "<col=ffffff> -> <col=ff9040>" + client.getItemDefinition(spell.getTargetItems().getSelectedId()).getName(), MenuOpcode.ITEM_USE_ON_WIDGET.getId(), spell.getTargetItems().getSelectedId(), spell.getTargetItems().getSelectedIdx(), event.getParam1(), true);
	}

	@Override
	protected void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (!this.isEnabled())// should be unnecessary
		{
			return;
		}
		if (spells.stream().noneMatch(f -> f.getSpellName().equals(event.getOption())))
		{
			return;
		}
		@Nullable SpellOnItem spell = spells.stream().filter(f -> f.getWidgetId() == event.getParam1()).findFirst().orElse(null);
		if (spell == null)
		{
			log.error("event: '{}' -> no spell matches.", event);
			return;
		}
		if (!spell.isEnabled())
		{
			log.error("event: '{}' -> spell: '{}' not enabled", event, spell.getSpellName());
			event.consume();
		}
		else
		{
			client.setSelectedSpellName(spell.getSpellName());
			client.setSelectedSpellWidget(spell.getWidgetId());
			event.setTarget(spell.getSpellName() + event.getTarget());
			event.setParam1(WidgetInfo.INVENTORY.getId());
			event.setOption("Cast");
			log.debug("menu hijacked -> {}", event);
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

		clearSelectors();

		for (int idx = 0; idx < items.size(); idx++)
		{
			final int id = items.get(idx).getId();
			final int qty = items.get(idx).getQuantity();
			for (SpellOnItem spell : spells)
			{
				for (ItemSelector rune : spell.getReqRunes())
				{
					rune.testAndAdd(id, qty, idx);
				}
				spell.getTargetItems().testAndAdd(id, qty, idx);
			}
		}
	}
}
