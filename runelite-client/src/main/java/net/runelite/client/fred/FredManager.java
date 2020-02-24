package net.runelite.client.fred;

import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.ScriptCallbackEvent;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.fred.events.InterfaceChoice;
import org.apache.commons.lang3.ArrayUtils;

@SuppressWarnings("FieldCanBeLocal")
@Singleton
@Slf4j
public class FredManager
{
	private final EventBus eventBus;

	private final ScriptStackTools stackTools;

	private final InterfaceChoiceManager interfaceChoiceManager = new InterfaceChoiceManager();

	@Inject
	private FredManager(Client client, EventBus eventBus)
	{
//		this.client = client;
		this.eventBus = eventBus;

		stackTools = new ScriptStackTools(client);

		eventBus.subscribe(ScriptCallbackEvent.class, interfaceChoiceManager, interfaceChoiceManager::onScriptCallbackEvent);
	}

	private class InterfaceChoiceManager
	{
		private String skill_options_string = null;
		void onScriptCallbackEvent(ScriptCallbackEvent callback)
		{
			switch (callback.getEventName())
			{
				case "ChatboxMultiBuilt":
				{
					int numberOfOps = stackTools.copyIntFromStack(0);
					String[] ops = stackTools.slurpStringsFromStack(5);
					String header = stackTools.popStringOffStack();
					ops = ArrayUtils.subarray(ops, 0, numberOfOps);
					InterfaceChoice event = new InterfaceChoice(WidgetInfo.DIALOG_OPTION, header, ops);
					eventBus.post(InterfaceChoice.class, event);
					log.debug("Event {}", event);
					stackTools.pasteIntToStack(0, event.getRequestedOp());
					break;
				}
				case "SkillMulti_Start":
				{
					skill_options_string = stackTools.copyStringFromStack(0);
					break;
				}
				case "SkillMulti_End":
				{
					int numberOfOps = stackTools.copyIntFromStack(0);
					stackTools.pasteIntToStack(0, 0);
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
						stackTools.pasteIntToStack(0, WidgetInfo.PACK(270, 13 + event.getRequestedOp()));
					}
					skill_options_string = null;
					break;
				}
				case "OnMushroomTeleportWidgetBuilt":
				{
					int[] options = stackTools.copyIntsFromStack(4);
					ArrayUtils.reverse(options);
					String[] optionStrings = {"House", "Valley", "Swamp", "Meadow"};
					InterfaceChoice event = new InterfaceChoice(WidgetInfo.FOSSIL_MUSHROOM_TELEPORT, "MushroomTeleport", optionStrings);
					eventBus.post(InterfaceChoice.class, event);
					if (!event.free())
					{
						stackTools.pasteIntToStack(4, options[event.getRequestedOp()-1]);
					}
					break;
				}
			}
		}
	}


}
