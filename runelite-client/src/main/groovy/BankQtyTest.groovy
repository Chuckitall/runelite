import groovy.transform.InheritConstructors
import io.reactivex.functions.Consumer
import net.runelite.api.Varbits
import net.runelite.client.fred.events.BankQtyInput
import net.runelite.client.plugins.groovy.debugger.DebuggerWindow.LogLevel
import net.runelite.client.plugins.groovy.script.ScriptedPlugin

import static net.runelite.api.ItemID.FLAX
import static net.runelite.api.ItemID.PURE_ESSENCE
import static net.runelite.api.ItemID.BUCKET_OF_SAND
import static net.runelite.api.ItemID.UNPOWERED_ORB

@InheritConstructors
class BankQtyTest extends ScriptedPlugin
{
	void onBankQtyInput(BankQtyInput event)
	{
		if(event.getItemID() == FLAX)
		{
			event.requestQtyOption(60, true)
		}
		else if(event.getItemID() == BUCKET_OF_SAND)
		{
			event.requestQtyOption(14, false)
		}
		else if(event.getItemID() == UNPOWERED_ORB)
		{
			event.requestQtyOption(24, false)
		}
	}

	void startup()
	{
		log(LogLevel.DEBUG,"Starting up");
		_eventBus.subscribe(BankQtyInput.class, this, this.&onBankQtyInput as Consumer<BankQtyInput>);
	}

	void shutdown() {
		_eventBus.unregister(this);
		log(LogLevel.DEBUG,"Shutting Down");
	}
}
