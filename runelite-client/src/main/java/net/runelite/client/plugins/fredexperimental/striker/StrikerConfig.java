/*
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
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
 *
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
package net.runelite.client.plugins.fredexperimental.striker;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigTitleSection;
import net.runelite.client.config.Title;

@ConfigGroup("striker")
public interface StrikerConfig extends Config
{

	//Overlay Section
	@ConfigTitleSection(
		keyName = "strikerStub",
		name = "Striker",
		description = "",
		position = 0
	)
	default Title strikerStub()
	{
		return new Title();
	}

	@ConfigItem(
		position = 0,
		keyName = "strikerMode",
		name = "Striker Mode",
		description = "Shows clicking area and points etc.",
		titleSection = "strikerStub"
	)
	default StrikerMode getStrikerMode()
	{
		return StrikerMode.OFF;
	}

	@ConfigItem(
		position = 1,
		keyName = "lockWindow",
		name = "Window Lock",
		description = "Enables window anti-focus/click"
	)
	default boolean getLockWindow()
	{
		return false;
	}

	@ConfigItem(
		position = 1,
		keyName = "overlayEnabled",
		name = "Overlay Enabled",
		description = "Shows clicking area and points etc.",
		titleSection = "strikerStub"
	)
	default boolean getOverlayEnabled()
	{
		return true;
	}

	@ConfigItem(
		position = 2,
		keyName = "debugNPCs",
		name = "Debug NPCs",
		description = "Draws clickArea and clickPoints across all visible npc's",
		titleSection = "strikerStub",
		hidden = true,
		unhide = "overlayEnabled"
	)
	default boolean getDebugNPCs()
	{
		return false;
	}

	@ConfigItem(
		position = 3,
		keyName = "debugPlayers",
		name = "Debug Players",
		description = "Draws clickArea and clickPoints across all visible players",
		titleSection = "strikerStub",
		hidden = true,
		unhide = "overlayEnabled"
	)
	default boolean getDebugPlayers()
	{
		return false;
	}

	@ConfigItem(
		position = 4,
		keyName = "debugGroundItems",
		name = "Debug Ground Items",
		description = "Draws clickArea and clickPoints across all visible ground items",
		titleSection = "strikerStub",
		hidden = true,
		unhide = "overlayEnabled"
	)
	default boolean getDebugGroundItems()
	{
		return false;
	}
	//End Overlay Section

	//Start Robot section
	@ConfigTitleSection(
		keyName = "robotStub",
		name = "Robot",
		description = "Java robot settings",
		position = 1
	)
	default Title robotStub()
	{
		return new Title();
	}

//	@ConfigItem(
//		position = 0,
//		keyName = "robotEnabled",
//		name = "Robot Enabled",
//		description = "Enable the Robot interface.",
//		titleSection = "robotStub"
//	)
//	default boolean getRobotEnabled()
//	{
//		return true;
//	}

	@ConfigItem(
		position = 1,
		keyName = "minDelayAmount",
		name = "Min Delay",
		description = "Minimum delay that is applied to every action at the end (ms)",
		titleSection = "robotStub"
//		hidden = true,
//		unhide = "robotEnabled"
	)
	default int getMinDelayAmt()
	{
		return 45;
	}

	@ConfigItem(
		position = 2,
		keyName = "reactionTime",
		name = "Reaction Time",
		description = "The base time between actions (ms)",
		titleSection = "robotStub"
//		hidden = true,
//		unhide = "robotEnabled"
	)
	default int getReactionTimeVariation()
	{
		return 80;
	}

	@ConfigItem(
		position = 3,
		keyName = "mouseDragSpeed",
		name = "Mouse drag speed",
		description = "The speed at which steps are executed. Keep at 49? cuz jagex mouse recorder?",
		titleSection = "robotStub"
//		hidden = true,
//		unhide = "robotEnabled"
	)
	default int getMouseDragSpeed()
	{
		return 49;
	}


	@ConfigItem(
		position = 4,
		keyName = "overshoots",
		name = "Overshoots",
		description = "Higher number = more overshoots",
		titleSection = "robotStub"
//		hidden = true,
//		unhide = "robotEnabled"
	)
	default int getOvershoots()
	{
		return 4;
	}

	@ConfigItem(
		position = 5,
		keyName = "variatingFlow",
		name = "Flow - Variating",
		description = "",
		titleSection = "robotStub"
//		hidden = true,
//		unhide = "robotEnabled"
	)
	default boolean getVariatingFlow()
	{
		return true;
	}

	@ConfigItem(
		position = 6,
		keyName = "slowStartupFlow",
		name = "Flow - Slow startup",
		description = "",
		titleSection = "robotStub"
//		hidden = true,
//		unhide = "robotEnabled"
	)
	default boolean getSlowStartupFlow()
	{
		return true;
	}


	@ConfigItem(
		position = 7,
		keyName = "slowStartup2Flow",
		name = "Flow - Slow startup 2",
		description = "",
		titleSection = "robotStub"
//		hidden = true,
//		unhide = "robotEnabled"
	)
	default boolean getSlowStartup2Flow()
	{
		return true;
	}

	@ConfigItem(
		position = 8,
		keyName = "jaggedFlow",
		name = "Flow - Jagged",
		description = "",
		titleSection = "robotStub"
//		hidden = true,
//		unhide = "robotEnabled"
	)
	default boolean getJaggedFlow()
	{
		return true;
	}

	@ConfigItem(
		position = 9,
		keyName = "interruptedFlow",
		name = "Flow - Interrupted",
		description = "",
		titleSection = "robotStub"
//		hidden = true,
//		unhide = "robotEnabled"
	)
	default boolean getInterruptedFlow()
	{
		return false;
	}


	@ConfigItem(
		position = 10,
		keyName = "interrupted2Flow",
		name = "Flow - Interrupted 2",
		description = "",
		titleSection = "robotStub"
//		hidden = true,
//		unhide = "robotEnabled"
	)
	default boolean getInterrupted2Flow()
	{
		return false;
	}

	@ConfigItem(
		position = 11,
		keyName = "stoppingFlow",
		name = "Flow - Stopping",
		description = "",
		titleSection = "robotStub"
//		hidden = true,
//		unhide = "robotEnabled"
	)
	default boolean getStoppingFlow()
	{
		return false;
	}

	@ConfigItem(
		position = 12,
		keyName = "deviationSlopeDivider",
		name = "Deviation slope divider",
		description = "",
		titleSection = "robotStub"
//		hidden = true,
//		unhide = "robotEnabled"
	)
	default int getDeviationSlopeDivider()
	{
		return 10;
	}


	@ConfigItem(
		position = 13,
		keyName = "noisinessDivider",
		name = "Noisiness divider",
		description = "",
		titleSection = "robotStub"
//		hidden = true,
//		unhide = "robotEnabled"
	)
	default String getNoisinessDivider()
	{
		return "2.0D";
	}
	//End Robot Section

	//Event Section
	@ConfigTitleSection(
		keyName = "eventStub",
		name = "Event",
		description = "Mouse/Keyboard events settings",
		position = 2
	)
	default Title eventStub()
	{
		return new Title();
	}

//	@ConfigItem(
//		position = 0,
//		keyName = "eventEnabled",
//		name = "Event Enabled",
//		description = "Enable the Event interface.",
//		titleSection = "eventStub"
//	)
//	default boolean getEventEnabled()
//	{
//		return true;
//	}
	//End Event Section
}