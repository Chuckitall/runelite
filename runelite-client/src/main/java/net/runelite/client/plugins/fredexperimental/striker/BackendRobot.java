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

/*
Modified java.awt.Robot for use with RuneLitePlus. Hopefully we can make it stand far apart.
Uses
https://github.com/JoonasVali/NaturalMouseMotion
for mouse motion.
 */

package net.runelite.client.plugins.fredexperimental.striker;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ClientUI;

@Slf4j
class BackendRobot extends java.awt.Robot
{
	private static int minDelay = 45;
	private java.awt.Robot peer;

	private StrikerPlugin plugin;

	public BackendRobot(StrikerPlugin plugin) throws AWTException
	{
		super();
		this.plugin = plugin;
		if (GraphicsEnvironment.isHeadless())
		{
			log.error("Headless environment");
		}
		else
		{
			this.init(GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getDefaultScreenDevice());
		}
	}

	private void init(GraphicsDevice screen) throws AWTException
	{
		try
		{
			peer = new java.awt.Robot();
		}
		catch (Exception e)
		{
			log.error("Robot not supported on this system configuration.");
		}
	}

	private void pauseMS(int delayMS)
	{
		long initialMS = System.currentTimeMillis();
		while (System.currentTimeMillis() < initialMS + delayMS)
		{
			try
			{
				Thread.sleep(10);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public synchronized void mouseMove(int x, int y)
	{
		try
		{
			//TODO: Must be better way to determine titlebar width
			plugin.getCurrentMouseMotionFactory().build(ClientUI.frame.getX() + x + determineHorizontalOffset(), ClientUI.frame.getY() + y + determineVerticalOffset()).move();
			this.delay(getMinDelay());
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public synchronized void mousePress(int buttonID)
	{
		if (buttonID < 1 || buttonID > 5)
		{
			log.warn("Invalid mouse button ID {}. please use 1-5.", buttonID);
			return;
		}
		peer.mousePress(InputEvent.getMaskForButton(buttonID));
		this.delay(getMinDelay());
	}

	@Override
	public synchronized void mouseRelease(int buttonID)
	{
		if (buttonID < 1 || buttonID > 5)
		{
			log.warn("Invalid mouse button ID {}. please use 1-5.", buttonID);
			return;
		}
		peer.mouseRelease(InputEvent.getMaskForButton(buttonID));
		this.delay(getMinDelay());
	}

	//TODO: Symbols are nut supported at this time
	public synchronized void typeMessage(String message)
	{

		Random r = new Random();
		char[] charArray = message.toCharArray();
		for (char c : charArray)
		{
			keyPress(KeyEvent.getExtendedKeyCodeForChar(c));
			this.delay(93 + r.nextInt(getMinDelay()));
		}
		keyPress(KeyEvent.VK_ENTER);
		this.delay(93 + r.nextInt(getMinDelay()));
		ClientUI.allowInput = true;
	}

	private int getMinDelay()
	{
		Random random = new Random();
		int random1 = random.nextInt(minDelay);
		if (random1 < minDelay / 2)
		{
			random1 = random.nextInt(minDelay / 2) + minDelay / 2 + random.nextInt(minDelay / 2);
		}
		return random1;
	}

	private int getWheelDelay()
	{
		Random random = new Random();
		int random1 = random.nextInt(minDelay);
		if (random1 < minDelay / 2)
		{
			random1 = random.nextInt(minDelay / 2) + minDelay / 2 + random.nextInt(minDelay / 2);
		}
		return random1;
	}

	/**
	 * Rotates the scroll wheel on wheel-equipped mice.
	 *
	 * @param wheelAmt number of "notches" to move the mouse wheel
	 *                 Negative values indicate movement up/away from the user,
	 *                 positive values indicate movement down/towards the user.
	 * @since 1.4
	 */
	@Override
	public synchronized void mouseWheel(int wheelAmt)
	{
		for (int i : new int[wheelAmt])
		{
			peer.mouseWheel(wheelAmt);
			this.delay(getWheelDelay());
		}
	}

	/**
	 * Presses a given key.  The key should be released using the
	 * <code>keyRelease</code> method.
	 * <p>
	 * Key codes that have more than one physical key associated with them
	 * (e.g. <code>KeyEvent.VK_SHIFT</code> could mean either the
	 * left or right shift key) will map to the left key.
	 *
	 * @param keycode Key to press (e.g. <code>KeyEvent.VK_A</code>)
	 * @throws IllegalArgumentException if <code>keycode</code> is not
	 *                                  a valid key
	 * @see #keyRelease(int)
	 * @see KeyEvent
	 */
	@Override
	public synchronized void keyPress(int keycode)
	{
		peer.keyPress(keycode);
		this.delay(getMinDelay());
	}

	@Override
	public synchronized void keyRelease(int keycode)
	{
		peer.keyRelease(keycode);
		this.delay(getMinDelay());
	}

	public Color getPixelColor(int x, int y)
	{
		return peer.getPixelColor(x, y);
	}

	@Override
	public void delay(int ms)
	{
		pauseMS(ms);
	}

	public void delay()
	{
		this.delay(getMinDelay());
	}

	private int determineHorizontalOffset()
	{
		return plugin.getClientUI().getCanvasOffset().getX();
	}

	private int determineVerticalOffset()
	{
		return plugin.getClientUI().getCanvasOffset().getY();
	}
}
