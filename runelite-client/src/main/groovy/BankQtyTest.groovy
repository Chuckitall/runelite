import groovy.transform.InheritConstructors
import io.reactivex.functions.Consumer
import net.runelite.api.Varbits
import net.runelite.client.fred.events.BankQtyInput
import net.runelite.client.plugins.groovy.debugger.DebuggerWindow.LogLevel
import net.runelite.client.plugins.groovy.script.ScriptedPlugin

import static net.runelite.api.ItemID.FLAX
import static net.runelite.api.ItemID.PURE_ESSENCE
@InheritConstructors
class BankQtyTest extends ScriptedPlugin
{
	void onBankQtyInput(BankQtyInput event)
	{
		if(event.getItemID() == PURE_ESSENCE)
		{

		}
		else if(event.getItemID() == FLAX)
		{
			event.requestQtyOption(30)
		}
	}

	void startup()
	{
		log(LogLevel.DEBUG,"Starting up");
		_client.setVarbit(Varbits.WITHDRAW_X_AMOUNT, 12);
		_eventBus.subscribe(BankQtyInput.class, this, this.&onBankQtyInput as Consumer<BankQtyInput>);
	}

	void shutdown() {
		_eventBus.unregister(this);
		log(LogLevel.DEBUG,"Shutting Down");
	}
}
