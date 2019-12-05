package net.runelite.client.plugins.fredexperimental.striker;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Point;
import net.runelite.client.plugins.fred.util.Random;
import org.jetbrains.annotations.NotNull;

import static net.runelite.client.plugins.fredexperimental.striker.StrikerMode.OFF;

@Slf4j
public class Striker
{
	private static StrikerPlugin plugin;

	private static ScheduledExecutorService threadExec = null;
	private static ScheduledExecutorService reservedExec = null;

	private static final Object readLock = new Object();

	@Getter(AccessLevel.PUBLIC)
	private static StrikerMode mode = OFF;

	static void init(StrikerPlugin plugin)
	{
		Striker.plugin = plugin;
		mode = OFF;
	}

	static void setEnabled(StrikerMode nMode)
	{
		if (mode != nMode)
		{
			if (mode == OFF)
			{
				threadExec = Executors.newSingleThreadScheduledExecutor();
				reservedExec = Executors.newSingleThreadScheduledExecutor();
				StrikerRobot.setEnabled(nMode == StrikerMode.ROBOT);
				StrikerEvent.setEnabled(nMode == StrikerMode.EVENT);
			}
			else if (nMode == OFF)
			{
				//actions to do when disabled
				reservedExec.shutdown();
				try
				{
					reservedExec.awaitTermination(1000, TimeUnit.MILLISECONDS);
				}
				catch (InterruptedException e)
				{
					reservedExec.shutdownNow();
					e.printStackTrace();
				}
				reservedExec = null;

				threadExec.shutdown();
				try
				{
					threadExec.awaitTermination(1000, TimeUnit.MILLISECONDS);
				}
				catch (InterruptedException e)
				{
					threadExec.shutdownNow();
					e.printStackTrace();
				}
				threadExec = null;
				StrikerRobot.setEnabled(false);
				StrikerEvent.setEnabled(false);
			}
			else
			{
				StrikerRobot.setEnabled(nMode == StrikerMode.ROBOT);
				StrikerEvent.setEnabled(nMode == StrikerMode.EVENT);
			}

			plugin.setLockClient(plugin.getConfig().getLockWindow());
			mode = nMode;
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

	public static void delayMS(int min, int max)
	{
		delayMS(Random.nextInt(min, max));
	}

	public static void moveMouse(Point p)
	{
		if (StrikerRobot.isRobotEnabled())
		{
			StrikerRobot.mouseMove(p);
		}
		else if (StrikerEvent.isEventEnabled())
		{
			StrikerEvent.moveMouse(p);
		}
	}

	public static void clickMouse(Point p, int buttonId)
	{
		if (StrikerRobot.isRobotEnabled())
		{
			StrikerRobot.clickMouse(p, buttonId);
		}
		else if (StrikerEvent.isEventEnabled())
		{
			StrikerEvent.clickMouse(p, buttonId);
		}
	}

	public static void typeKey(int key)
	{
		if (StrikerRobot.isRobotEnabled())
		{
			StrikerRobot.keyPressAndRelease(key);
		}
		else if (StrikerEvent.isEventEnabled())
		{
			StrikerEvent.typeKey(key);
		}
	}

	@Synchronized("readLock")
	public static ScheduledFuture<?> schedule(@NotNull Runnable command, long delay)
	{
		if (threadExec != null)
		{
			return threadExec.schedule(command, delay, TimeUnit.MILLISECONDS);
		}
		return null;
	}

	@Synchronized("readLock")
	public static ScheduledFuture<?> schedule(@NotNull Callable<?> callable, long delay)
	{
		if (threadExec != null)
		{
			threadExec.schedule(callable, delay, TimeUnit.MILLISECONDS);
		}
		return null;
	}

	@Synchronized("readLock")
	public static ScheduledFuture<?> scheduleAtFixedRate(@NotNull Runnable command, long initialDelay, long period)
	{
		if (threadExec != null)
		{
			return threadExec.scheduleAtFixedRate(command, initialDelay, period, TimeUnit.MILLISECONDS);
		}
		return null;
	}

	@Synchronized("readLock")
	public static ScheduledFuture<?> scheduleWithFixedDelay(@NotNull Runnable command, long initialDelay, long delay)
	{
		if (threadExec != null)
		{
			return threadExec.scheduleWithFixedDelay(command, initialDelay, delay, TimeUnit.MILLISECONDS);
		}
		return null;
	}
}
