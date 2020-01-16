package net.runelite.client.plugins.hotkeys.utils;

import java.awt.Canvas;
import java.awt.event.MouseEvent;
import java.util.concurrent.ArrayBlockingQueue;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.Point;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.plugins.stretchedmode.StretchedModeConfig;

@Slf4j
public class MenuEntrySwapConsumer implements Runnable
{
	private static final MenuEntry Cancel = new MenuEntry("Cancel", "", 0, 1006, 0, 0, false);

	private ArrayBlockingQueue<MenuEntry> entryQueue;
	@Setter
	private int minDelay;
	@Setter
	private int maxDelay;
	private final Client client;
	private final ConfigManager configManager;
	private boolean clicked = false;

	@Setter
	private MenuEntry menuEntryInProgress = null;

	MenuEntrySwapConsumer(ArrayBlockingQueue<MenuEntry> entriesQueue, int minDelay, int maxDelay, Client client, EventBus eventBus, ConfigManager configManager)
	{
		this.entryQueue = entriesQueue;
		this.minDelay = minDelay;
		this.maxDelay = maxDelay;
		this.configManager = configManager;

		this.client = client;

		eventBus.subscribe(MenuOptionClicked.class, this, this::onMenuOptionClicked);
		eventBus.subscribe(MenuEntryAdded.class, this, this::onMenuEntryAdded);
	}

	private void onMenuOptionClicked(MenuOptionClicked menuOptionClicked)
	{
		if (menuEntryInProgress != null)
		{

			if (menuEntryInProgress.getOpcode() == 23)
			{
				//trigger effects of a walk here event
				//client.getScene().walkToTargetTile(menuEntryInProgress.getParam0(), menuEntryInProgress.getParam1());

				//replace entry with cancel to prevent an excess walk at real cursor position
				menuEntryInProgress = Cancel;
			}
			menuOptionClicked.setMenuEntry(menuEntryInProgress);
			menuEntryInProgress = null;

		}
	}

	private void onMenuEntryAdded(MenuEntryAdded menuEntryAdded)
	{
		if (menuEntryInProgress != null)
		{
			client.setLeftClickMenuEntry(menuEntryInProgress);
		}
	}

	@Override
	public void run()
	{
		processAutomaticLoop();
	}

	private void processAutomaticLoop()
	{
		while (true)
		{
			try
			{
				if (!entryQueue.isEmpty() && menuEntryInProgress == null)
				{
					menuEntryInProgress = entryQueue.poll();
					mouseEventDownUp();
					int randomDelay = getRandomIntBetweenRange(minDelay, maxDelay);
					Thread.sleep(randomDelay);
					continue;
				}
				Thread.sleep(5);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				break;
			}
		}
	}

	private void mouseEventDownUp()
	{
		if (menuEntryInProgress != null)
		{
			Point p = client.getMouseCanvasPosition();
			Canvas canvas = client.getCanvas();
			if (p.getX() == -1)
			{
				p = new Point(getRandomIntBetweenRange(1, canvas.getWidth()), getRandomIntBetweenRange(1, canvas.getHeight()));
			}
			long time = System.currentTimeMillis();

			if (client.isStretchedEnabled())
			{
				double scale = 1 + ((double) configManager.getConfig(StretchedModeConfig.class).scalingFactor() / 100);

				MouseEvent mousePressed = new MouseEvent(canvas, MouseEvent.MOUSE_PRESSED, time, 0, (int) (p.getX() * scale), (int) (p.getY() * scale), 1, false, 1);
				canvas.dispatchEvent(mousePressed);
				MouseEvent mouseReleased = new MouseEvent(canvas, MouseEvent.MOUSE_RELEASED, time, 0, (int) (p.getX() * scale), (int) (p.getY() * scale), 1, false, 1);
				canvas.dispatchEvent(mouseReleased);
			}
			else
			{
				MouseEvent mousePressed = new MouseEvent(canvas, MouseEvent.MOUSE_PRESSED, time, 0, p.getX(), p.getY(), 1, false, 1);
				canvas.dispatchEvent(mousePressed);
				MouseEvent mouseReleased = new MouseEvent(canvas, MouseEvent.MOUSE_RELEASED, time, 0, p.getX(), p.getY(), 1, false, 1);
				canvas.dispatchEvent(mouseReleased);
			}
		}
	}

	private static int getRandomIntBetweenRange(int min, int max)
	{
		return (int) ((Math.random() * ((max - min) + 1)) + min);
	}
}