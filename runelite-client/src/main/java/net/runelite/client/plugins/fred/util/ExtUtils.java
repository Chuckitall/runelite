package net.runelite.client.plugins.fred.util;

import java.awt.Rectangle;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.client.flexo.Flexo;

@Slf4j
public class ExtUtils
{
	private Client client;
	private Flexo flexo;
//	StretchedModeConfig stretchedConfig;

	public ExtUtils(Client client, Flexo flexo)
	{
		this.client = client;
		this.flexo = flexo;
	}

	public void handleInventoryClick(Rectangle rectangle, boolean click)
	{
		Point cp = getClickPoint(rectangle);
		Point tmp = client.getMouseCanvasPosition();
		java.awt.Point mousePos = new java.awt.Point(tmp.getX(), tmp.getY());

		if (cp.getX() >= 1)
		{
			if (!rectangle.contains(mousePos))
			{
				flexo.mouseMove(cp.getX(), cp.getY());
			}
			if (click)
			{
				flexo.mousePressAndRelease(1);
			}
		}
	}

	public void handlePointClick(Point pt, int range, boolean click)
	{
		Rectangle rectangle = new Rectangle(pt.getX() - range / 2, pt.getY() - range / 2, range, range);
		Point cp = getClickPoint(rectangle);
		Point tmp = client.getMouseCanvasPosition();
		java.awt.Point mousePos = new java.awt.Point(tmp.getX(), tmp.getY());

		if (cp.getX() >= 1)
		{
			if (!rectangle.contains(mousePos))
			{
				flexo.mouseMove(cp.getX(), cp.getY());
			}
			if (click)
			{
				flexo.mousePressAndRelease(1);
			}
		}
	}

	public Point getClickPoint(Rectangle rect)
	{
		int rand = (Math.random() <= 0.5) ? 1 : 2;
		int x = (int) (rect.getX() + (rand * 3) + rect.getWidth() / 2);
		int y = (int) (rect.getY() + (rand * 3) + rect.getHeight() / 2);
		log.debug("x {}, y {}", x, y);
		return getClickPoint(new Point(x, y));
//		return new Point(x, y);
	}

	public Point getClickPoint(Point pt)
	{
		if (client.isStretchedEnabled())
		{
			//double scale = 1 + (((double)stretchedConfig.scalingFactor()) / 100);
			double scale = 1 + ((Flexo.scale) / 100);
			Point pt2 = new Point((int) (pt.getX() * scale), (int) (pt.getY() * scale));
			log.debug("scale {} -> x {}, y {}", scale, pt2.getX(), pt2.getY());
			return pt2;
		}
		else
		{
			log.debug("No changed {}", pt);
			return pt;
		}
	}
}
