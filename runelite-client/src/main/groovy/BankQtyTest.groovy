import groovy.transform.InheritConstructors
import io.reactivex.functions.Consumer
import net.runelite.api.ItemID
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

	int[] birdhouse_seeds = [
		ItemID.BARLEY_SEED, ItemID.HAMMERSTONE_SEED, ItemID.YANILLIAN_SEED, ItemID.ASGARNIAN_SEED, ItemID.JUTE_SEED, ItemID.WILDBLOOD_SEED, ItemID.KRANDORIAN_SEED
	];
	int[] birdhouses = [
		ItemID.MAHOGANY_BIRD_HOUSE, ItemID.YEW_BIRD_HOUSE
	];
	void onBankQtyInput(BankQtyInput event)
	{
		if (event.getItemID() == FLAX) {
			event.requestQtyOption(60, true)
		}
		else if (event.getItemID() == BUCKET_OF_SAND) {
			event.requestQtyOption(14, false)
		}
		else if (event.getItemID() == UNPOWERED_ORB) {
			event.requestQtyOption(24, false)
		}
		else if (birdhouse_seeds.contains(event.getItemID())) {
			event.requestQtyOption(40, false)
		}
		else if (birdhouses.contains(event.getItemID())) {
			event.requestQtyOption(4, false)
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
