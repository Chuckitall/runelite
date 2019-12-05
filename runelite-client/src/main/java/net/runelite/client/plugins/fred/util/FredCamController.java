package net.runelite.client.plugins.fred.util;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.flexo.Flexo;

import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_UNDEFINED;

@Slf4j
public class FredCamController extends FredCam
{
	private Flexo flexo;
	private int timeout;
	private int tolerance;

	public FredCamController(Client client, Flexo flexo, int timeout, int tolerance)
	{
		super(client);
		this.flexo = flexo;
		this.timeout = timeout;
		this.tolerance = tolerance;
	}

	public void turnTo(Actor target)
	{
		this.turnTo(target, 10);
	}

	public void turnTo(Actor target, int yawDeviation)
	{
		this.turnTo(getAngleOfTarget(target), yawDeviation);
	}

	public void turnTo(LocalPoint target)
	{
		this.turnTo(target, 10);
	}

	public void turnTo(LocalPoint target, int yawDeviation)
	{
		this.turnTo(getAngleOfTarget(target), yawDeviation);
	}

	public void turnTo(int yawTarget, int yawDeviation)
	{
		if (yawTarget < 0 || yawTarget >= 360)
		{
			log.error("yawTarget {} Invalid", yawTarget);
			return;
		}
		int yawT = ((yawTarget + 360) + Random.nextInt(-yawDeviation, yawDeviation)) % 360;
		int deltaYaw = getAngleToTarget(yawT);
		int direction = deltaYaw > this.tolerance ? VK_RIGHT : (deltaYaw < this.tolerance ? VK_LEFT : VK_UNDEFINED);
		if (direction != VK_UNDEFINED)
		{
			long initialMS = System.currentTimeMillis();
			flexo.keyPress(direction);
			while (Math.abs(deltaYaw) > this.tolerance && (initialMS + this.timeout) > System.currentTimeMillis())
			{
				deltaYaw = getAngleToTarget(yawT);
				flexo.delay(flexo.getMinDelay());
			}
			flexo.keyRelease(direction);
		}
	}
}
