/*
 * Copyright (c) 2017, Robin <robin.weymans@gmail.com>
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
package net.runelite.api.events;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.MenuEntry;

/**
 * An event where a menu option has been clicked.
 * <p>
 * This event does not only trigger when clicking an option in a
 * right-clicked menu. The event will trigger for any left click
 * action performed (ie. clicking an item, walking to a tile, etc) as
 * well as any right-click menu option.
 * <p>
 * By default, when there is no action performed when left-clicking,
 * it seems that this event still triggers with the "Cancel" action.
 */
@Getter
public class HiddenMenuOptionClicked extends MenuOptionClicked
{
	public HiddenMenuOptionClicked(String option, String target, int identifier, int opcode, int param0, int param1, boolean forceLeftClick)
	{
		super(option, target, identifier, opcode, param0, param1, forceLeftClick);
	}

	public HiddenMenuOptionClicked(String option, String target, int identifier, int opcode, int param0, int param1, boolean forceLeftClick, boolean authentic, int mouseButton)
	{
		super(option, target, identifier, opcode, param0, param1, forceLeftClick, authentic, mouseButton);
	}

	public HiddenMenuOptionClicked(MenuOptionClicked source)
	{
		super(source.getOption(), source.getTarget(), source.getIdentifier(), source.getOpcode(), source.getParam0(), source.getParam1(), source.isForceLeftClick(), source.isAuthentic(), source.getMouseButton());
	}
}
