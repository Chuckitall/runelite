package net.runelite.client.fred;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Data;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.MenuOpcode;
import net.runelite.api.VarClientInt;
import net.runelite.api.VarClientStr;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.ScriptCallbackEvent;
import net.runelite.api.events.WidgetHiddenChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.util.Text;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.fred.events.BankQtyInput;
import net.runelite.client.plugins.Plugin;

@Singleton
@Slf4j
public class FredMenu
{
	private final Client client;
	private final ClientThread clientThread;
	private final EventBus eventBus;

	//Used to manage custom non-player menu options

	@Inject
	private FredMenu(Client client, ClientThread clientThread, EventBus eventBus)
	{
		this.client = client;
		this.clientThread = clientThread;
		this.eventBus = eventBus;

		eventBus.subscribe(GameTick.class, this, this::onTick);
		eventBus.subscribe(MenuEntryAdded.class, this, this::onMenuEntryAdded);
		eventBus.subscribe(MenuOptionClicked.class, this, this::onMenuEntryClicked);
		eventBus.subscribe(WidgetHiddenChanged.class, this, this::onWidgetHiddenChanged);
	}

	private final HashMap<Integer, int[]> cachedRequests = new HashMap<>();
	private boolean latch = false;
	private int qty = -1;

	private void onTick(GameTick e)
	{
		latch = true;
		Widget widget = client.getWidget(162, 45);
		if (widget != null && !widget.isHidden())
		{
			if (qty > 0)
			{
				client.setVar(VarClientInt.INPUT_TYPE, 7);
				client.setVar(VarClientStr.INPUT_TEXT, String.valueOf(qty));
				clientThread.invoke(() -> this.client.runScript(681, null));
				clientThread.invoke(() -> this.client.runScript(299, 0, 0));
				this.qty = 0;
			}
		}
	}

	private void onWidgetHiddenChanged(WidgetHiddenChanged event)
	{
		if (event.getWidget() == null)
		{
			return;
		}
		int group = WidgetInfo.TO_GROUP(event.getWidget().getId());
		int child = WidgetInfo.TO_CHILD(event.getWidget().getId());
		boolean wasHidden = event.isHidden();
		if (group == 162 && child == 40 && !wasHidden)
		{
//			client.setVar(VarClientInt.INPUT_TYPE, 7);
//			client.setVar(VarClientStr.INPUT_TEXT, String.valueOf(qty));
//			clientThread.invoke(() -> { this.client.runScript(681, null); });
//			clientThread.invoke(() -> this.client.runScript(299, 0, 0));
//			this.qty = 0;
			log.debug("WidgetHiddenChanged: [{}, {}] -> [isHidden: {}]",group, child, event.getWidget().isHidden());
		}
	}

	private void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (latch)
		{
			cachedRequests.clear();
			latch = false;
		}
		//option=Withdraw-1, target=<col=ff9040>Pure essence</col>, identifier=1, opcode=57, param0=138, param1=786443, forceLeftClick=false
		Widget bankContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
		if(bankContainer == null || bankContainer.isHidden())
		{
			return;
		}
		if (MenuOpcode.CC_OP.getId() == event.getOpcode() && bankContainer.getId() == event.getParam1() && event.getOption().equalsIgnoreCase("Withdraw-1"))
		{
			Widget owner = null;
			try
			{
				owner = bankContainer.getChild(event.getParam0());
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			if (owner != null)
			{
				int itemID = owner.getItemId();
				int qty = owner.getItemQuantity();
				String name = Text.standardize(owner.getName());
				if (!cachedRequests.containsKey(qty))
				{
					//we dont have a cached response, so build and send an event.
					BankQtyInput toSend = new BankQtyInput(itemID, name, qty);
					eventBus.post(BankQtyInput.class, toSend);
					cachedRequests.put(itemID, toSend.getSortedQtyOps());
				}

				int[] response = cachedRequests.get(itemID);
				for (int i : response)
				{
					client.insertMenuItem("W(" + i + ")", event.getTarget(), event.getOpcode(), event.getIdentifier(), event.getParam0(), i, false);
				}
			}
		}
		//if (latch)
	}


	private void onMenuEntryClicked(MenuOptionClicked event)
	{
		Widget bankContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
		if(bankContainer == null || bankContainer.isHidden())
		{
			return;
		}
		if (MenuOpcode.CC_OP.getId() == event.getOpcode() && event.getOption().startsWith("W(") && event.getOption().endsWith(")") && event.getOpcode() == MenuOpcode.CC_OP.getId())
		{
			Widget owner = null;
			try
			{
				owner = bankContainer.getChild(event.getParam0());
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			if (owner != null)
			{
				qty = event.getParam1();
				event.setOption("Withdraw-X");
				event.setIdentifier(6);
				event.setOpcode(MenuOpcode.CC_OP_LOW_PRIORITY.getId());
				event.setParam1(bankContainer.getId());
			}
		}
	}

	/**
	 * Managing withdraw qty/deposit qty
	 **/
}
