package net.runelite.client.fred;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Data;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.MenuOpcode;
import net.runelite.api.Point;
import net.runelite.api.VarClientInt;
import net.runelite.api.VarClientStr;
import net.runelite.api.Varbits;
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
	private final ExecutorService executor;

	@Inject
	private FredMenu(Client client, ClientThread clientThread, EventBus eventBus)
	{
		this.client = client;
		this.clientThread = clientThread;
		this.eventBus = eventBus;
		this.executor = Executors.newSingleThreadExecutor();

		eventBus.subscribe(GameTick.class, this, this::onTick);
		eventBus.subscribe(MenuEntryAdded.class, this, this::onMenuEntryAdded);
		eventBus.subscribe(MenuOptionClicked.class, this, this::onMenuEntryClicked);
	}

	public static class _Request
	{
		public final int qty;
		public final boolean noted;
		public _Request(int qty, boolean noted)
		{
			this.qty = qty;
			this.noted = noted;
		}
	}

	private final HashMap<Integer, _Request[]> cachedRequests = new HashMap<>();
	private boolean latch = false;
	private Widget latched = null;
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
//
				clientThread.invoke(() -> this.client.runScript(681, null));
				clientThread.invoke(() -> this.client.runScript(299, 0, 0));
				this.qty = 0;
			}
		}
		else if(latched != null)
		{
			click(latched.getBounds());
			latched = null;
		}
	}

	private void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (latch)
		{
			cachedRequests.clear();
			latch = false;
		}
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
				if (!cachedRequests.containsKey(itemID))
				{
					//we dont have a cached response, so build and send an event.
					BankQtyInput toSend = new BankQtyInput(itemID, name, qty);
					eventBus.post(BankQtyInput.class, toSend);
					cachedRequests.put(itemID, toSend.getSortedQtyOps());
				}

				_Request[] response = cachedRequests.get(itemID);
				for (_Request i : response)
				{
					String action = "W(" + i.qty + "|" + (i.noted ? "noted" : "item") + ")";
					client.insertMenuItem(action, event.getTarget(), event.getOpcode(), event.getIdentifier(), event.getParam0(), i.qty, false);
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
				String[] body = event.getOption().substring(2, event.getOption().length()-1).split("\\|");
				String qty_ = body[0];
				String noted_ = body[1];
				int qty_temp = -1;
				int noted_temp = -1;
				try
				{
					qty_temp = Integer.parseInt(qty_);
				}
				catch (NumberFormatException ignored)
				{

				}
				if (noted_.equalsIgnoreCase("noted"))
				{
					noted_temp = 1;
				}
				else if(noted_.equalsIgnoreCase("item"))
				{
					noted_temp = 0;
				}
				log.debug("qty {}, noted {}", qty_temp, noted_temp);
				if (noted_temp > -1 && qty_temp > 0)
				{
					qty = qty_temp;
					event.setOption("Withdraw-X");
					event.setIdentifier(6);
					event.setOpcode(MenuOpcode.CC_OP_LOW_PRIORITY.getId());
					event.setParam1(bankContainer.getId());
					if (client.getVar(Varbits.BANK_NOTE_FLAG) != noted_temp)
					{
//						if(noted_temp == 0)
//						{
//							dispatch(new MenuEntry("Item", "", 1, 57, -1, 786452, false));
//						}
//						else
//						{
//							dispatch(new MenuEntry("Note", "", 1, 57, -1, 786454, false));
//						}
						event.consume();
						dispatch(new MenuEntry(noted_temp == 0 ? "Item" : "Note", "", 1, 57, -1, noted_temp == 0 ? 786452 : 786454, false));
						dispatch(event);
						latched = client.getWidget(noted_temp == 1 ? 786452 : 786454);
//						if(noted_temp == 1)
//						{
//							latched = client.getWidget(786452);
//						}
//						else
//						{
//							latched = client.getWidget(786454);
//						}
					}
				}
			}
		}
	}

	private void dispatch(MenuEntry entry)
	{
		clientThread.invoke(() -> client.invokeMenuAction(
			entry.getParam0(), entry.getParam1(), entry.getOpcode(), entry.getIdentifier(), entry.getOption(), entry.getTarget(), 0, 0
		));
	}

	/**
	 * This method must be called on a new
	 * thread, if you try to call it on
	 * {@link net.runelite.client.callback.ClientThread}
	 * it will result in a crash/desynced thread.
	 */
	public void click(Rectangle rectangle)
	{
		assert !client.isClientThread();

		Point p = getClickPoint(rectangle);

		if (p.getX() < 1 || p.getY() < 1)
		{
			return;
		}

		click(p);
	}

	public void click(Point p)
	{
		assert !client.isClientThread();

		if (client.isStretchedEnabled())
		{
			double scale = client.getScalingFactor();
			final int x = (int) (p.getX() * scale);
			final int y = (int) (p.getY() * scale);
			final Point click = new Point(x, y);

			eventDispatcher(501, click);
			eventDispatcher(502, click);
			eventDispatcher(500, click);
			return;
		}
		eventDispatcher(501, p);
		eventDispatcher(502, p);
		eventDispatcher(500, p);
	}

	public Point getClickPoint(Rectangle rect)
	{
		final int x = (int) (rect.getX() + getRandomIntBetweenRange((int) rect.getWidth() / 6 * -1, (int) rect.getWidth() / 6) + rect.getWidth() / 2);
		final int y = (int) (rect.getY() + getRandomIntBetweenRange((int) rect.getHeight() / 6 * -1, (int) rect.getHeight() / 6) + rect.getHeight() / 2);

		return new Point(x, y);
	}

	public int getRandomIntBetweenRange(int min, int max)
	{
		return (int) ((Math.random() * ((max - min) + 1)) + min);
	}

	private void eventDispatcher(int id, Point point)
	{
		MouseEvent e = new MouseEvent(
			client.getCanvas(), id,
			System.currentTimeMillis(),
			0, point.getX(), point.getY(),
			1, false, 1
		);

		client.getCanvas().dispatchEvent(e);
	}
}
