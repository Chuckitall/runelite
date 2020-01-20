import groovy.transform.CompileStatic;
import groovy.transform.InheritConstructors
import io.reactivex.functions.Consumer
import net.runelite.api.MenuOpcode
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded
import net.runelite.api.events.MenuOptionClicked
import net.runelite.api.events.ScriptCallbackEvent;
import net.runelite.api.vars.InterfaceTab
import net.runelite.api.widgets.WidgetInfo
import net.runelite.client.plugins.banktags.tabs.TabInterface
import net.runelite.client.plugins.groovy.debugger.DebuggerWindow.LogLevel
import net.runelite.client.plugins.groovy.script.ScriptedPlugin

@CompileStatic
@InheritConstructors
class Alch extends ScriptedPlugin {

	boolean latch = false;
	void onMenuClicked(MenuOptionClicked e) {
		if (e.getOption().equalsIgnoreCase("col=0000ff>Cast"))
		{
			_client.setSelectedSpellName("<col=00ff00>High Level Alchemy</col>");
			_client.setSelectedSpellWidget(WidgetInfo.SPELL_HIGH_LEVEL_ALCHEMY.getId());
			e.setOption("Cast");
			latch = true;
			log(LogLevel.DEBUG, "menu hijacked -> ${e.getOption()}");
		}
	}

	void onMenuAdded(MenuEntryAdded e) {
		if (e.getOpcode() != MenuOpcode.ITEM_USE.getId())
		{
			return;
		}
		if (e.getIdentifier() == 1396)
		{
			_client.insertMenuItem("<col=0000ff>Cast", "<col=00ff00>High Level Alchemy</col><col=ffffff> -> <col=ff9040>${_client.getItemDefinition(e.getIdentifier()).getName()}", MenuOpcode.ITEM_USE_ON_WIDGET.getId(), e.getIdentifier(), e.param0, e.param1, false);
		}
	}

	void onTick(GameTick e)
	{
		//latch = false;
	}

	void onScriptCallbackEvent(ScriptCallbackEvent e)
	{
		int[] intStack = _client.getIntStack();
		if (e.getEventName().equalsIgnoreCase("OnSwitchTopLevel2"))
		{
			log(LogLevel.DEBUG, "intStack = ${intStack}");
			intStack[0] = InterfaceTab.INVENTORY.id;
			log(LogLevel.WARN, "intStack = ${intStack}");
			latch = false;
		}
	}

	void startup() {
		_eventBus.subscribe(MenuOptionClicked.class, this, this.&onMenuClicked as Consumer<MenuOptionClicked>);
		_eventBus.subscribe(MenuEntryAdded.class, this, this.&onMenuAdded as Consumer<MenuEntryAdded>);
		_eventBus.subscribe(ScriptCallbackEvent.class, this, this.&onScriptCallbackEvent as Consumer<ScriptCallbackEvent>);
		_eventBus.subscribe(GameTick.class, this, this.&onTick as Consumer<GameTick>);
		log(LogLevel.DEBUG, "Starting Up");
	}

	void shutdown() {
		_eventBus.unregister(this);
		log(LogLevel.DEBUG,"Shutting Down");
	}
}
