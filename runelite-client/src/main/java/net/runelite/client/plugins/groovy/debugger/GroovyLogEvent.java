package net.runelite.client.plugins.groovy.debugger;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.runelite.api.events.Event;
import net.runelite.client.plugins.groovy.debugger.DebuggerWindow.LogLevel;

/**
 * An event where a Groovy Script posts a log message to the debugger
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroovyLogEvent implements Event
{
	/**
	 * The name of the Script posting the event
	 */
	private String name;

	/**
	 * The severity of the message
	 */
	private LogLevel logLevel;

	/**
	 * The contents of the message.
	 */
	private String message;
}
