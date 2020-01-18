import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import net.runelite.api.events.*
import net.runelite.api.MenuOpcode
import net.runelite.api.util.*;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.groovy.debugger.DebuggerWindow.LogLevel;
import net.runelite.client.plugins.groovy.script.ScriptedPlugin;

@CompileStatic
@InheritConstructors
class Runecrafting extends ScriptedPlugin {

//The goal of this is to go from
//Po=53, P1=72, Op=3, Id=34766, option=Craft-rune, target=<col=ffff>Altar Canvas<1003, 430>, authentic<true>
//to these if we have space in the inventory
//Param0=1 Param1=9764864 Opcode=34 Id=5509 MenuOption=Empty MenuTarget=Empty CanvasX=2094 CanvasY=1037 Authentic=true
//Param0=2 Param1=9764864 Opcode=34 Id=5510 MenuOption=Empty MenuTarget=Empty CanvasX=2132 CanvasY=1028 Authentic=true

	void onMenuOptionClicked(MenuOptionClicked e) {
		log(LogLevel.DEBUG, e.toString());
//		if (e.getOption().equalsIgnoreCase("<col=0000ff>Cast")) {
//			e.setOption("Cast");
//			_client.setSelectedSpellName("<col=00ff00>Charge Water Orb</col>");
//			_client.setSelectedSpellWidget(WidgetInfo.SPELL_CHARGE_WATER_ORB.getId());
//		}
	}

	//test
	void onMenuAdded(MenuEntryAdded e) {
		if (e.getOpcode() != MenuOpcode.EXAMINE_OBJECT.getId()) {
			return;
		}
		if (Text.standardize(e.getTarget()).equalsIgnoreCase("obelisk of water") && e.getOpcode() == 1002) {
			_client.insertMenuItem("<col=0000ff>Cast", "<col=00ff00>Charge Water Orb</col><col=ffffff> -> <col=ffff>Obelisk of Water", MenuOpcode.SPELL_CAST_ON_GAME_OBJECT.getId(), e.getIdentifier(), e.getParam0(), e.getParam1(), false);
		}
	}

	void startup() {
		log(LogLevel.WARN, "hello world: " + WidgetInfo.SPELL_CHARGE_WATER_ORB.getId());
		_eventBus.subscribe(MenuOptionClicked.class as Class<Event>, this, this.&onMenuOptionClicked);
		_eventBus.subscribe(MenuEntryAdded.class as Class<Event>, this, this.&onMenuAdded);
	}

	void shutdown() {
		log(LogLevel.WARN, "goodbye world");
		_eventBus.unregister(this);
	}
}
