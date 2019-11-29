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

package net.runelite.client.plugins.fredexperimental.blackjack;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("fredblackjack2")
public interface BlackjackConfig extends Config
{
	@ConfigItem(
		position = 0,
		keyName = "enableTracking",
		name = "AutoTrack",
		description = "Enables autotracking."
	)
	default boolean enableTracking()
	{
		return false;
	}

	@ConfigItem(
		position = 1,
		keyName = "enableClicking",
		name = "AutoClick",
		description = "Enables auto-clicking.",
		hidden = true,
		unhide = "enableTracking"
	)
	default boolean enableClicking()
	{
		return false;
	}

//
//	@ConfigItem(
//		position = 10,
//		keyName = "focusHelper",
//		name = "Window Focusable",
//		description = "Enables window focus."
//	)
//	default boolean focusHelper()
//	{
//		return true;
//	}

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
