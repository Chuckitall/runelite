package net.runelite.client.plugins.fredexperimental.striker;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Point;

@Slf4j
class StrikerEvent
{
	private static StrikerPlugin plugin;

	@Getter(AccessLevel.PUBLIC)
	private static boolean eventEnabled;

	static void init(StrikerPlugin plugin)
	{
		StrikerEvent.plugin = plugin;
		eventEnabled = false;
	}

	static void setEnabled(boolean enable)
	{
		if (enable != eventEnabled)
		{
			eventEnabled = enable;
		}
	}

	public static void delayMS(int milli)
	{
		long initialMS = System.currentTimeMillis();
		while (System.currentTimeMillis() < initialMS + milli)
		{
			try
			{
				Thread.sleep(1);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	public static void moveMouse(Point p)
	{
		if (!eventEnabled)
		{
			return;
		}
		Point pActual = plugin.getClient().getMouseCanvasPosition();
		Point pNative = StrikerUtils.canvasToNativePoint(p);
		if (pActual.getX() != p.getX() || pActual.getY() != p.getY())
		{
			log.debug("Move mouse to point {}", p);
			MouseEvent mouseEntered = new MouseEvent(plugin.getClient().getCanvas(), 504, System.currentTimeMillis(), 0, pNative.getX(), pNative.getY(), 0, false);
			plugin.getClient().getCanvas().dispatchEvent(mouseEntered);
			MouseEvent mouseExited = new MouseEvent(plugin.getClient().getCanvas(), 505, System.currentTimeMillis(), 0, pNative.getX(), pNative.getY(), 0, false);
			plugin.getClient().getCanvas().dispatchEvent(mouseExited);
			MouseEvent mouseMoved = new MouseEvent(plugin.getClient().getCanvas(), 503, System.currentTimeMillis(), 0, pNative.getX(), pNative.getY(), 0, false);
			plugin.getClient().getCanvas().dispatchEvent(mouseMoved);
			delayMS(40);
		}
	}

	public static void clickMouse(Point p, int buttonId)
	{
		if (!eventEnabled)
		{
			return;
		}
		Point pActual = plugin.getClient().getMouseCanvasPosition();
		Point pNative = StrikerUtils.canvasToNativePoint(p);
		if (pActual.getX() != p.getX() || pActual.getY() != p.getY())
		{
			moveMouse(p);
		}
		log.debug("Click point {} with button {}. Cursor position {}, nativePosition {}", p, buttonId, pActual, pNative);
		MouseEvent mousePressed = new MouseEvent(plugin.getClient().getCanvas(), 501, System.currentTimeMillis(), 0, pNative.getX(), pNative.getY(), 1, false, buttonId);
		plugin.getClient().getCanvas().dispatchEvent(mousePressed);
		delayMS(40);
		MouseEvent mouseReleased = new MouseEvent(plugin.getClient().getCanvas(), 502, System.currentTimeMillis(), 0, pNative.getX(), pNative.getY(), 1, false, buttonId);
		plugin.getClient().getCanvas().dispatchEvent(mouseReleased);
		MouseEvent mouseClicked = new MouseEvent(plugin.getClient().getCanvas(), 500, System.currentTimeMillis(), 0, pNative.getX(), pNative.getY(), 1, false, buttonId);
		plugin.getClient().getCanvas().dispatchEvent(mouseClicked);
	}

	//See KeyEvent.VK_...
	public static void typeKey(int key)
	{

		if (!eventEnabled)
		{
			return;
		}
		log.debug("Typing key {}", key);
//		if (plugin.getConfig().getLockWindow())
//		{
//			ClientUI.frame.setFocusable(true);
//		}
		KeyEvent keyPress = new KeyEvent(plugin.getClient().getCanvas(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, key, KeyEvent.CHAR_UNDEFINED);
		plugin.getClient().getCanvas().dispatchEvent(keyPress);
		delayMS(20);
		KeyEvent keyRelease = new KeyEvent(plugin.getClient().getCanvas(), KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, key, KeyEvent.CHAR_UNDEFINED);
		plugin.getClient().getCanvas().dispatchEvent(keyRelease);
		KeyEvent keyTyped = new KeyEvent(plugin.getClient().getCanvas(), KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, key, KeyEvent.CHAR_UNDEFINED);
		plugin.getClient().getCanvas().dispatchEvent(keyTyped);
		delayMS(20);
	}
}
