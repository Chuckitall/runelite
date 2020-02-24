package net.runelite.client.fred;

import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.eventbus.EventBus;

@SuppressWarnings("FieldCanBeLocal")
@Singleton
@Slf4j
public class FredManager
{
	private final EventBus eventBus;
	private final ScriptStackTools stackTools;
	private final InterfaceChoiceGenerator interfaceChoiceGenerator;

	@Inject
	private FredManager(Client client, EventBus eventBus)
	{
		this.eventBus = eventBus;
		this.stackTools = new ScriptStackTools(client);
		this.interfaceChoiceGenerator = new InterfaceChoiceGenerator(stackTools, eventBus);
	}
}
