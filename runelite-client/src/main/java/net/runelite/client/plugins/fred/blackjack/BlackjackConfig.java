/*
 * Copyright (c) 2019, gazivodag <https://github.com/gazivodag>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.runelite.client.plugins.fred.blackjack;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;

@ConfigGroup("fredblackjack")
public interface BlackjackConfig extends Config
{
	@ConfigItem(
		position = 0,
		keyName = "toggleScript",
		name = "Enable Script",
		description = "Starts automatically blackjacking."
	)
	default Keybind toggleScript()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
		position = 1,
		keyName = "debugKey1",
		name = "Debug1 Keybind",
		description = "triggers debug1 task flag."
	)
	default Keybind debugKey1()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
		position = 2,
		keyName = "debugKey2",
		name = "Debug2 Keybind",
		description = "triggers debug2 task flag."
	)
	default Keybind debugKey2()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
		position = 3,
		keyName = "debugKey3",
		name = "Debug3 Keybind",
		description = "triggers debug3 task flag."
	)
	default Keybind debugKey3()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
		position = 4,
		keyName = "debugKey4",
		name = "Debug4 Keybind",
		description = "triggers debug4 task flag."
	)
	default Keybind debugKey4()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
		keyName = "healthMin",
		name = "Min Health to allow\'d",
		description = "If you fail a knock-out and health is less then x, break aggro and remove all interaction."
	)
	default int healthMin()
	{
		return 20;
	}

	@ConfigItem(
		keyName = "healthMax",
		name = "Health to re-add interaction.",
		description = "After health is less then min, waits till health is greater then y to reenable all interaction."
	)
	default int healthMax()
	{
		return 28;
	}
}
