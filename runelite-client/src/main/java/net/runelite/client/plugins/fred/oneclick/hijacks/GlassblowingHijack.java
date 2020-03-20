package net.runelite.client.plugins.fred.oneclick.hijacks;


import com.google.common.collect.ImmutableSet;
import com.google.inject.Singleton;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.LocatableQueryResults;
import net.runelite.api.MenuOpcode;
import net.runelite.api.ObjectDefinition;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.fred.oneclick.util.ItemSelector;
import net.runelite.client.plugins.fred.oneclick.util.MenuEntryHijack;
import net.runelite.client.plugins.fred.oneclick.util.OneclickConstants;

import static net.runelite.api.ItemID.BEER_GLASS;
import static net.runelite.api.ItemID.EMPTY_CANDLE_LANTERN;
import static net.runelite.api.ItemID.EMPTY_OIL_LAMP;
import static net.runelite.api.ItemID.FISHBOWL;
import static net.runelite.api.ItemID.GLASSBLOWING_PIPE;
import static net.runelite.api.ItemID.LANTERN_LENS;
import static net.runelite.api.ItemID.LIGHT_ORB;
import static net.runelite.api.ItemID.MOLTEN_GLASS;
import static net.runelite.api.ItemID.UNPOWERED_ORB;
import static net.runelite.api.ItemID.VIAL;
import static net.runelite.api.MenuOpcode.GAME_OBJECT_SECOND_OPTION;
import static net.runelite.api.MenuOpcode.ITEM_USE;


@Slf4j
@Singleton
public class GlassblowingHijack extends MenuEntryHijack
{
	private ItemSelector glassblowingPipe = new ItemSelector(ImmutableSet.of(GLASSBLOWING_PIPE));
	private ItemSelector moltenGlass = new ItemSelector(ImmutableSet.of(MOLTEN_GLASS));
	private ItemSelector glassItems = new ItemSelector(ImmutableSet.of(BEER_GLASS, EMPTY_CANDLE_LANTERN, EMPTY_OIL_LAMP, VIAL, FISHBOWL, UNPOWERED_ORB, LANTERN_LENS, LIGHT_ORB));

	@Override
	protected void onEnabled()
	{
		glassblowingPipe.clear();
		moltenGlass.clear();
		glassItems.clear();
		eventBus.subscribe(ItemContainerChanged.class, this, this::onItemContainerChanged);
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			this.onItemContainerChanged(new ItemContainerChanged(InventoryID.INVENTORY.getId(), client.getItemContainer(InventoryID.INVENTORY)));
		}
	}

	@Override
	protected void onDisabled()
	{
		glassblowingPipe.clear();
		moltenGlass.clear();
		glassItems.clear();
		eventBus.unregister(this);
	}
	private boolean isBankOpen()
	{
		Widget bankContainer = client.getWidget(WidgetInfo.BANK_CONTENT_CONTAINER);
		Widget bankItemContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
		return bankContainer != null && !bankContainer.isHidden() && bankItemContainer != null && !bankItemContainer.isHidden();
	}
	@Override
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (!this.isEnabled())// should be unnecessary
		{
			return;
		}
		if (glassblowingPipe.found() && event.getMenuOpcode() == ITEM_USE && glassblowingPipe.idxMatches(event.getParam0())) //p0:7, p1:9764864, opCode:38, ident:1785, option:Use, target:Use, mouse:(827, 356), authentic:true
		{
			if (isBankOpen())
			{

			}
			else
			{
				if (!moltenGlass.found())
				{
					LocatableQueryResults banks = OneclickConstants.findBanks(client, 5);
//					log.debug("banks count {}", banks.size());
					if (banks.size() > 0)
					{
						//Menuaction -> p0:56, p1:51, opCode:4, ident:10356, option:Bank, target:<col=ffff>Bank booth, mouse:(453, 254), authentic:true
						GameObject bank = (GameObject) banks.nearestTo(client.getLocalPlayer());
						if (bank != null)
						{
//							log.debug("bank {}", bank);
							ObjectDefinition bankDef = client.getObjectDefinition(bank.getId());
							if (bankDef.getImpostorIds() != null)
							{
								bankDef = bankDef.getImpostor();
							}
							client.insertMenuItem("BANK", "<col=ffff>" + bankDef.getName(), MenuOpcode.GAME_OBJECT_SECOND_OPTION.getId(), bank.getId(), bank.getLocalLocation().getSceneX(), bank.getLocalLocation().getSceneY(), true);
						}
					}
				}
				else
				{
					//new MenuEntry("Use", "<col=ff9040>Glassblowing pipe<col=ffffff> -> <col=ff9040>Molten glass", 1775, 31, 5, 9764864, false)
					//client.insertMenuItem("BANK", "<col=ffff>" + bankDef.getName(), MenuOpcode.GAME_OBJECT_SECOND_OPTION.getId(), bank.getId(), bank.getLocalLocation().getSceneX(), bank.getLocalLocation().getSceneY(), true);

					//crafting
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
		if (!(event.getOption().equals("BANK") || event.getOption().equals("BLOW") || event.getOption().equals("SELECT GLASS") || event.getOption().equals("DEPOSIT") || event.getOption().equals("WITHDRAW")))
		{
			return;
		}
		if (glassblowingPipe.found())
		{
			if (event.getMenuOpcode() == GAME_OBJECT_SECOND_OPTION && event.getOption().equals("BANK"))
			{
				event.setOption("Bank");
			}
			else
			{
				event.consume();
			}
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

		moltenGlass.clear();
		glassItems.clear();
		glassblowingPipe.clear();
		for (int idx = 0; idx < items.size(); idx++)
		{
			final int id = items.get(idx).getId();
			glassblowingPipe.testAndAdd(id, idx);
			moltenGlass.testAndAdd(id, idx);
			glassItems.testAndAdd(id, idx);
		}
	}
}
