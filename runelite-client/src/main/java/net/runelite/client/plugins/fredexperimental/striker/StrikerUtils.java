
/*
 *
 *   Copyright (c) 2019, Zeruth < TheRealNull@gmail.com > 
 *   All rights reserved.
 *
 *   Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions are met:
 *
 *   1. Redistributions of source code must retain the above copyright notice, this
 *      list of conditions and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *
 *   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *   ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *   WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *   DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *   ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *   (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *   LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *   ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *   (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *   SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package net.runelite.client.plugins.fredexperimental.striker;

import java.awt.Rectangle;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.GameObject;
import net.runelite.api.Point;

@Slf4j
public class StrikerUtils
{
	private static StrikerPlugin plugin;

	static void init(StrikerPlugin plugin)
	{
		StrikerUtils.plugin = plugin;
	}

	public static Point canvasToNativePoint(Point p)
	{
		Point p2 = new Point(0, 0);
		if ((p.getX() > 0 && p.getX() < plugin.getClient().getCanvas().getWidth()) && (p.getY() > 0 && p.getY() < plugin.getClient().getCanvas().getHeight()))
		{
			if (plugin.getClient().isStretchedEnabled())
			{
				double scale = 1 + (plugin.getScaleFactor() / 100);
				p2 = new Point((int) (p.getX() * scale), (int) (p.getY() * scale));
				log.debug("scale {} point {} -> {}", scale, p, p2);
			}
			else
			{
				log.debug("No changed {}", p);
				p2 = new Point(p.getX(), p.getY());
			}
		}
		else
		{
			log.warn("Offscreen point {} attempted", p);
		}
		return p2;
	}

//	public static Point nativeToCanvasPoint(Point p)
//	{
//		Point p2 = new Point(0, 0);
//		if ((p.getX() > 0 && p.getX() < plugin.getClient().getStretchedDimensions().getWidth()) && (p.getY() > 0 && p.getY() < plugin.getClient().getCanvas().getHeight()))
//		{
//			if (plugin.getClient().isStretchedEnabled())
//			{
//				double scale = 1 + (plugin.getScaleFactor() / 100);
//				p2 = new Point((int) (p.getX() * scale), (int) (p.getY() * scale));
//				log.debug("scale {} point {} -> {}", scale, p, p2);
//			}
//			else
//			{
//				log.debug("No changed {}", p);
//				p2 = new Point(p.getX(), p.getY());
//			}
//		}
//		else
//		{
//			log.warn("Offscreen point {} attempted", p);
//		}
//		return p2;
//	}

	/*
	Should pass un-stretched rectangle. Will return random point within rectangle
	*/
	public static Point getClickPoint(Rectangle target)
	{
		if (StrikerUtils.plugin == null)
		{
			log.error("StrikerUtils not initialized");
			return new Point(0, 0);
		}
		else if (target != null && target.width > 0 && target.height > 0)
		{
			Rectangle temp = StrikerUtils.getScaledRect(target, 0.5);
			if (temp.height <= 0 || temp.width <= 0)
			{
				return new Point(-1, -1);
			}
			Random r = new Random();
			Point p = new Point(temp.x + r.nextInt(temp.width), temp.y + r.nextInt(temp.height));
			if ((p.getX() > 0 && p.getX() < plugin.getClient().getCanvas().getWidth()) && (p.getY() > 0 && p.getY() < plugin.getClient().getCanvas().getHeight()))
			{
					return p;
			}
			else
			{
//				log.error("target {} results in an off screen point {}. Rotate Camera!", temp, p);
				return new Point(-1, -1);
			}
		}
		else
		{
//			log.error("target was null!");
			return new Point(-1, -1);
		}
	}


	public static Rectangle getClickArea(GameObject object)
	{
		return getClickArea(object, .9d);
	}

	public static Rectangle getClickArea(GameObject object, double scale)
	{
		if (object == null || object.getConvexHull() == null || scale <= 0.05d)
		{
			log.error("click area requested with object: {}, scale: {}, hull: {}", object, scale, (object != null) ? object.getConvexHull() : "-1");
			return new Rectangle(0, 0, 0, 0);
		}
		return getScaledRect(object.getConvexHull().getBounds(), scale);
	}

	public static Rectangle getClickArea(Actor actor)
	{
		return getClickArea(actor, 0.9d);
	}

	public static Rectangle getClickArea(Actor actor, double scale)
	{
		if (actor == null || actor.getConvexHull() == null || scale <= 0.05d)
		{
			log.error("click area requested with object: {}, scale: {}, hull: {}", actor, scale, (actor != null) ? actor.getConvexHull() : "-1");
			return new Rectangle(0, 0, 0, 0);
		}
		return getScaledRect(actor.getConvexHull().getBounds(), scale);
	}

	public static Rectangle getScaledRect(Rectangle boundBox, double scale)
	{
		if (boundBox == null || scale <= 0.05d)
		{
			log.error("scaled rectangle requested with boundBox: {}, scale: {}", boundBox, scale);
			return new Rectangle(0, 0, 0, 0);
		}
		Rectangle area = (Rectangle) boundBox.clone();
		java.awt.Point center = area.getLocation();
		center.translate((int) (area.getWidth() / 2.0d), (int) (area.getHeight() / 2.0d));
		area.setSize((int) (area.getWidth() * scale), (int) (area.getHeight() * scale));
		center.translate((int) (-area.getWidth() / 2.0d), (int) (-area.getHeight() / 2.0d));
		area.setLocation(center);
		return area;
	}
}
