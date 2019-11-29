/*
 *
 *   Copyright (c) 2019, Zeruth <TheRealNull@gmail.com>
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

package net.runelite.client.flexo;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Random;
import java.util.logging.Logger;

public class FlexoMouse
{
	/*
	Should pass unstretched coords, returns unstretched coords
	*/
	public static Point getClickPoint(Rectangle rect)
	{
		if (rect != null)
		{
			Random r = new Random();
			int x = -1;
			int y = -1;
			x = rect.x + r.nextInt(rect.width);
			y = rect.y + r.nextInt(rect.height);
			if (x > 0 && x < Flexo.client.getCanvas().getWidth())
			{
				if (y > 0 && y < Flexo.client.getCanvas().getHeight())
				{
					return new Point(x, y);
				}
			}
			Logger.getAnonymousLogger().warning("[RuneLit]Flexo - Off screen point attempted. Split the step, or rotate the screen.");
		}
		return null;
	}

	/*

	 */
	public static Rectangle getClickArea(Rectangle boundBox, double scale)
	{
		if (boundBox != null)
		{
			Rectangle area = (Rectangle) boundBox.clone();
			java.awt.Point center = area.getLocation();
			center.translate((int) (area.getWidth() / 2.0d), (int) (area.getHeight() / 2.0d));
			area.setSize((int) (area.getWidth() * scale), (int) (area.getHeight() * scale));
			center.translate((int) (-area.getWidth() / 2.0d), (int) (-area.getHeight() / 2.0d));
			area.setLocation(center);
			return area;
		}
		return null;
	}
}
