import groovy.transform.CompileStatic;
import groovy.transform.InheritConstructors;
import net.runelite.api.events.*;
import net.runelite.api.MenuOpcode;
import net.runelite.api.util.*;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.groovy.debugger.DebuggerWindow.LogLevel;
import net.runelite.client.plugins.groovy.script.ScriptedPlugin;

@InheritConstructors
class BattleStaffs extends ScriptedPlugin {
	void onMenuOptionClicked(MenuOptionClicked e) {
		if (e.getOption().equalsIgnoreCase("<col=0000ff>Cast")) {
			e.setOption("Cast");
			_client.setSelectedSpellName("<col=00ff00>Charge Earth Orb</col>");
			_client.setSelectedSpellWidget(WidgetInfo.SPELL_CHARGE_EARTH_ORB.getId());
			log(LogLevel.TRACE, "Clicked menu\n\t$e");
		}
	}

	void onMenuAdded(MenuEntryAdded e) {
		if (e.getOpcode() != MenuOpcode.EXAMINE_OBJECT.getId()) {
			return;
		}
		if (Text.standardize(e.getTarget()).equalsIgnoreCase("obelisk of earth") && e.getOpcode() == 1002) {
//			log(LogLevel.INFO, "Added Menu after seeing\n\t$e");
			_client.insertMenuItem("<col=0000ff>Cast", "<col=00ff00>Charge Earth Orb</col><col=ffffff> -> <col=ffff>Obelisk of Eartg", MenuOpcode.SPELL_CAST_ON_GAME_OBJECT.getId(), e.getIdentifier(), e.getParam0(), e.getParam1(), false);
		}
	}

	void startup() {
		log(LogLevel.WARN, "hello world: " + WidgetInfo.SPELL_CHARGE_EARTH_ORB.getId());
		_eventBus.subscribe(MenuOptionClicked.class as Class<Event>, this, this.&onMenuOptionClicked);
		_eventBus.subscribe(MenuEntryAdded.class as Class<Event>, this, this.&onMenuAdded);
	}

	void shutdown() {
		log(LogLevel.WARN, "goodbye world");
		_eventBus.unregister(this);
	}
}
