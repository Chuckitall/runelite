package net.runelite.client.plugins.fred.api.controllers;

import com.google.common.collect.Lists;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.Point;
import net.runelite.client.plugins.fredexperimental.striker.StrikerUtils;

@Singleton
@Slf4j
public class _Vision
{
	private Client client;

	@Inject
	public _Vision(Client client)
	{
		this.client = client;
	}

	public Point getCentroid(Shape hull)
	{
		List<Point> verts = Lists.newArrayList();
		PathIterator pathIterator = hull.getPathIterator(null);
		double[] coordinates = new double[6];
		while (!pathIterator.isDone())
		{
			int type = pathIterator.currentSegment(coordinates);
			if (type == PathIterator.SEG_LINETO || type == PathIterator.SEG_MOVETO)
			{
				verts.add(new Point((int) coordinates[0], (int) coordinates[1]));
			}
			pathIterator.next();
		}

		double x = 0.;
		double y = 0.;
		int pointCount = verts.size();
		for (int i = 0; i < pointCount - 1; i++)
		{
			final Point p = verts.get(i);
			x += p.getX();
			y += p.getY();
		}
		return new Point((int)(x / pointCount), (int)(y / pointCount));
	}

	public Point getClickPoint(Shape hull)
	{
		if (hull == null)
		{
			return null;
		}
		Point pos = client.getMouseCanvasPosition();
		if (hull.contains(Point.toNative(pos)))
		{
			return pos;
		}

		Rectangle bounds = hull.getBounds();
		if (bounds != null)
		{
			Point perspectivePoint;
			int failCount = 0;
			do
			{
				perspectivePoint = StrikerUtils.getClickPoint(bounds);
			}
			while (!hull.contains(Point.toNative(perspectivePoint)) && ++failCount <= 100);
			if (failCount > 100)
			{
				return null;
			}
			//log.debug("Got point {} after {} attempts", perspectivePoint, failCount);
			return perspectivePoint;
		}
		return null;
	}

	public boolean isGameObjectOnScreen(GameObject o)
	{
		Rectangle bounds = client.getCanvas().getBounds();
		Shape hull = (o != null) ? o.getConvexHull() : null;
		boolean failed = bounds == null || hull == null;
		int c = 0;
		while (c++ < 100 && !failed)
		{
			Point p = getClickPoint(hull);
			failed = p == null || (!bounds.contains(Point.toNative(p)));
		}
		return !failed;
	}
}
