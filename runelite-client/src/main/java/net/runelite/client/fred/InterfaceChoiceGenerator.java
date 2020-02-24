package net.runelite.client.fred;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.ScriptCallbackEvent;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.fred.events.InterfaceChoice;
import org.apache.commons.lang3.ArrayUtils;

@Slf4j
class InterfaceChoiceGenerator
{
	private String skill_options_string = null;
	private final ScriptStackTools stack;
	private final EventBus eventBus;

	public InterfaceChoiceGenerator(ScriptStackTools stack, EventBus eventBus)
	{
		this.stack = stack;
		this.eventBus = eventBus;
		eventBus.subscribe(ScriptCallbackEvent.class, this, this::onScriptCallbackEvent);
	}

	void onScriptCallbackEvent(ScriptCallbackEvent callback)
	{
		switch (callback.getEventName())
		{
			case "ChatboxMultiBuilt":
			{
				int numberOfOps = stack.copyIntFromStack(0);
				String[] ops = stack.slurpStringsFromStack(5);
				String header = stack.popStringOffStack();
				ops = ArrayUtils.subarray(ops, 0, numberOfOps);
				InterfaceChoice event = new InterfaceChoice(WidgetInfo.DIALOG_OPTION, header, ops);
				eventBus.post(InterfaceChoice.class, event);
				log.debug("Event {}", event);
				stack.pasteIntToStack(0, event.getRequestedOp());
				break;
			}
			case "SkillMulti_Start":
			{
				skill_options_string = stack.copyStringFromStack(0);
				break;
			}
			case "SkillMulti_End":
			{
				int numberOfOps = stack.copyIntFromStack(0);
				stack.pasteIntToStack(0, 0);
				String[] ops = skill_options_string.split("\\|");
				for (int i = 0; i < ops.length; i++)
				{
					log.info("Option[{}] = \"{}\"", i, ops[i]);
				}
				InterfaceChoice event = new InterfaceChoice(WidgetInfo.MULTI_SKILL_MENU, "SkillMulti", ops);
				eventBus.post(InterfaceChoice.class, event);
				log.debug("Skill Event {}", event);
				if (!event.free())
				{
					stack.pasteIntToStack(0, WidgetInfo.PACK(270, 13 + event.getRequestedOp()));
				}
				skill_options_string = null;
				break;
			}
			case "OnMushroomTeleportWidgetBuilt":
			{
				int[] options = stack.copyIntsFromStack(4);
				ArrayUtils.reverse(options);
				String[] optionStrings = {"House", "Valley", "Swamp", "Meadow"};
				InterfaceChoice event = new InterfaceChoice(WidgetInfo.FOSSIL_MUSHROOM_TELEPORT, "MushroomTeleport", optionStrings);
				eventBus.post(InterfaceChoice.class, event);
				if (!event.free())
				{
					stack.pasteIntToStack(4, options[event.getRequestedOp()-1]);
				}
				break;
			}
		}
	}
}
