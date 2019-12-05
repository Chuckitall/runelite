package net.runelite.client.plugins.fredexperimental.striker;

import java.awt.AWTException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Point;

@Slf4j
class StrikerRobot
{
	private static StrikerPlugin plugin;

	@Getter(AccessLevel.PUBLIC)
	private static boolean robotEnabled;

	private static BackendRobot backendRobot = null;

	static void init(StrikerPlugin plugin)
	{
		StrikerRobot.plugin = plugin;
		if (backendRobot != null)
		{
			return;
		}
		try
		{
			backendRobot = new BackendRobot(plugin);
		}
		catch (AWTException e)
		{
			log.error("Cant create robot backend", e);
		}
	}

	static void setEnabled(boolean enable)
	{
		if (enable != robotEnabled)
		{
//			if (enable)
//			{
//				//actions to do when enabled
//			}
//			else
//			{
//				//actions to do when disabled
//			}
			robotEnabled = enable;
		}
	}

	public static void mouseMove(Point p)
	{
		if (!robotEnabled)
		{
			return;
		}
		Point realPoint = StrikerUtils.canvasToNativePoint(p);
		if (realPoint == null)
		{
			return;
		}
		backendRobot.mouseMove(realPoint.getX(), realPoint.getY());
	}

	public static void clickMouse(Point p, int buttonID)
	{
		if (!robotEnabled)
		{
			return;
		}
		Point pActual = plugin.getClient().getMouseCanvasPosition();
		Point pNative = StrikerUtils.canvasToNativePoint(p);
		if (pActual.getX() != p.getX() || pActual.getY() != p.getY())
		{
			mouseMove(p);
		}
		log.debug("Click point {} with button {}. Cursor position {}, nativePosition {}", p, buttonID, pActual, pNative);

		backendRobot.mousePress(buttonID);
		backendRobot.mouseRelease(buttonID);
	}

	public static void mousePressAndRelease(int buttonID)
	{
		if (!robotEnabled)
		{
			return;
		}

		backendRobot.mousePress(buttonID);
		backendRobot.mouseRelease(buttonID);
	}

//	public static ScheduledFuture keyPress(int keycode)
//	{
//		if (!robotEnabled)
//		{
//			return null;
//		}
//		return threadExec.schedule(() -> backendRobot.keyPress(keycode), 0, TimeUnit.MILLISECONDS);
//	}
//
//	public static ScheduledFuture keyRelease(int keycode)
//	{
//		if (!robotEnabled)
//		{
//			return null;
//		}
//		return threadExec.schedule(() -> backendRobot.keyRelease(keycode), 0, TimeUnit.MILLISECONDS);
//	}

	public static void keyPressAndRelease(int keycode)
	{
		if (!robotEnabled)
		{
			return;
		}
		backendRobot.keyPress(keycode);
		backendRobot.keyRelease(keycode);
	}
//
//	public static ScheduledFuture holdKey(int keycode, int timeMS)
//	{
//		log.error("NOT IMPLEMENTED!");
//		return null;
//	}
}
