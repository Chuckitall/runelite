package net.runelite.client.plugins.fred.util;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.coords.LocalPoint;

@Slf4j
public class FredCam
{
	private Client client;

	public FredCam(Client client)
	{
		this.client = client;
	}

	/**
	 * Calculates the angle (yaw) of target
	 * client.getLocalPlayer is the implied origin
	 *
	 * @param target The target to get angle of.
	 * @return The angle of target relative to player.
	 */
	public int getAngleOfTarget(Actor target)
	{
		return getAngleOfTarget(target != null ? target.getLocalLocation() : null);
	}

	/**
	 * Calculates the angle (yaw) of target
	 * client.getLocalPlayer().getLocalLocation() is the implied origin
	 *
	 * @param target The target to get angle of.
	 * @return The angle of target relative to player.
	 */
	public int getAngleOfTarget(LocalPoint target)
	{
		if (target == null)
		{
			return -1;
		}
		if (client == null || client.getLocalPlayer() == null || client.getLocalPlayer().getLocalLocation() == null)
		{
			log.error("Invalid state -> target {}, client {}, player {}, localLocation: {}", target, client, (client != null) ? client.getLocalPlayer() : "???", (client != null && client.getLocalPlayer() != null) ? client.getLocalPlayer().getLocalLocation() : "???");
			return -1;
		}

		LocalPoint origin = client.getLocalPlayer().getLocalLocation();
		int degree = (int) Math.toDegrees(Math.atan2(
			target.getY() - origin.getY(), target.getX() - origin.getX()));
		int a = ((degree >= 0 ? degree : 360 + degree) - 90) % 360;
		return a < 0 ? a + 360 : a;
	}

	/**
	 * Calculates the delta angle (yaw) between camera and target
	 * client.getLocalPlayer().getLocalLocation() is the implied origin
	 *
	 * @param target The target to plot on the circle to get angle.
	 * @return The angle of target relative to origin.
	 */
	public int getAngleToTarget(Actor target)
	{
		return getAngleToTarget(target != null ? target.getLocalLocation() : null);
	}

	/**
	 * Calculates the delta angle (yaw) between camera and target
	 * client.getLocalPlayer().getLocalLocation() is the implied origin
	 *
	 * @param target The target to plot on the circle to get angle.
	 * @return The angle of target relative to origin.
	 */
	public int getAngleToTarget(LocalPoint target)
	{
		return getAngleToTarget(getAngleOfTarget(target));
//		int camAngle = getYaw();
//		int tarAngle = getAngleOfTarget(target);
//		if (tarAngle < 0 || tarAngle >= 360)
//		{
//			log.error("getAngleOfTarget({}) produced an invalid result {}", target, tarAngle);
//			return 0;
//		}
//
//		double cYawCycleA = (tarAngle + 360) - camAngle; // Forwards across the 360 endpoint.
//		double cYawCycleB = tarAngle - (camAngle + 360); // Backwards across the 360 endpoint.
//		double cYawNorm = tarAngle - camAngle; // Change in-between [0, 359], no endpoint crossed.
//
//		double cYaw; // Defines the shortest change in yaw required.
//
//		// Compare the absolute differences to determine the shortest path.
//		if (Math.abs(cYawNorm) < Math.abs(cYawCycleA) && Math.abs(cYawNorm) < Math.abs(cYawCycleB))
//		{
//			cYaw = cYawNorm;
//		}
//		else if (Math.abs(cYawCycleA) < Math.abs(cYawNorm) && Math.abs(cYawCycleA) < Math.abs(cYawCycleB))
//		{
//			cYaw = cYawCycleA;
//		}
//		else
//		{
//			cYaw = cYawCycleB;
//		}
//		return (int) cYaw;
	}

	/**
	 * Calculates the delta angle (yaw) between camera and the targetYaw
	 * client.getLocalPlayer().getLocalLocation() is the implied origin
	 *
	 * @param targetYaw The yaw to get the angle to.
	 * @return The angle between targetYaw and cameraYaw.
	 */
	public int getAngleToTarget(int targetYaw)
	{
		int camAngle = getYaw();
		if (targetYaw < 0 || targetYaw >= 360)
		{
//			log.error("Invalid targetYaw {}", targetYaw);
			return 0;
		}

		double cYawCycleA = (targetYaw + 360) - camAngle; // Forwards across the 360 endpoint.
		double cYawCycleB = targetYaw - (camAngle + 360); // Backwards across the 360 endpoint.
		double cYawNorm = targetYaw - camAngle; // Change in-between [0, 359], no endpoint crossed.

		double cYaw; // Defines the shortest change in yaw required.

		// Compare the absolute differences to determine the shortest path.
		if (Math.abs(cYawNorm) < Math.abs(cYawCycleA) && Math.abs(cYawNorm) < Math.abs(cYawCycleB))
		{
			cYaw = cYawNorm;
		}
		else if (Math.abs(cYawCycleA) < Math.abs(cYawNorm) && Math.abs(cYawCycleA) < Math.abs(cYawCycleB))
		{
			cYaw = cYawCycleA;
		}
		else
		{
			cYaw = cYawCycleB;
		}
		return (int) cYaw;
	}

	/**
	 * Calculates the angular yaw from the
	 * funky Jagex implementation in degrees
	 *
	 * @return Camera yaw angle in degrees, bounded between (0, 359).
	 */
	public int getYaw()
	{
//		return (int)(((double) (360 * client.getCameraYaw())) / 2048.0d);
		return getYawConverted(client.getCameraYaw());
	}

	/**
	 * Calculates the angular pitch from the
	 * funky Jagex implementation in degrees
	 *
	 * @return Camera pitch angle in degrees, bounded between (0, 90).
	 */
	public int getPitch()
	{
//		return (int) (((double) (90 * (client.getCameraPitch() - 128))) / 384.0d);
		return getPitchConverted(client.getCameraPitch());
	}

	public static int getYawConverted(int jauAngle)
	{
		return (int)(((double) (360 * jauAngle)) / 2048.0d);
	}

	public static int getPitchConverted(int jauAngle)
	{
//		return (int)(((double) (360 * jauAngle)) / 2048.0d);
		return (int) (((double) (90 * (jauAngle - 128))) / 384.0d);
	}
}
