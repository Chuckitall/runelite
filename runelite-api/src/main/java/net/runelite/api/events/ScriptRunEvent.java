package net.runelite.api.events;

import lombok.Data;
import net.runelite.api.ScriptEvent;
import net.runelite.api.widgets.Widget;
/**
 * A callback about a ScriptRunEvent (very low level for debugging only)
 **/
@Data
public class ScriptRunEvent implements Event, ScriptEvent
{
	private Object[] args;

	/**
	 * The Widget that was interacted with
	 */
	private Widget source;

	/**
	 * op idx of the menu entry clicked
	 */
	private int op;

	/**
	 * The name of the widget
	 */
	private String opbase;

	/**
	 * mouseX location relative to parent widget
	 */
	private int mouseX;
}
