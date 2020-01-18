

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
//import net.runelite.api.Client;
import net.runelite.api.events.*
import net.runelite.api.MenuOpcode
import net.runelite.api.util.*;
import net.runelite.api.widgets.WidgetInfo;
//import net.runelite.client.eventbus.EventBus;
//import net.runelite.client.events.*;
//import net.runelite.client.menus.MenuManager
//import net.runelite.client.plugins.fred.api.other.Tuples.T2;
//import net.runelite.client.plugins.fred.api.other.Tuples;
//import net.runelite.client.plugins.groovy.script.ScriptedPlugin;
//import net.runelite.client.plugins.groovy.script.ScriptContext;
import net.runelite.client.plugins.groovy.script.ScriptedPlugin;
//import net.runelite.client.ui.overlay.OverlayManager;

@CompileStatic
@InheritConstructors
class BattleStaffs extends ScriptedPlugin {
	void onMenuOptionClicked(MenuOptionClicked e) {
		if (e.getOption().equalsIgnoreCase("<col=0000ff>Cast")) {
			e.setOption("Cast");
			_client.setSelectedSpellName("<col=00ff00>Charge Water Orb</col>");
			_client.setSelectedSpellWidget(WidgetInfo.SPELL_CHARGE_WATER_ORB.getId());
		}
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
		println("hello world: " + WidgetInfo.SPELL_CHARGE_WATER_ORB.getId());
		_eventBus.subscribe(MenuOptionClicked.class as Class<Event>, this, this.&onMenuOptionClicked);
		_eventBus.subscribe(MenuEntryAdded.class as Class<Event>, this, this.&onMenuAdded);
	}

	void shutdown() {
		println("goodbye world");
		_eventBus.unregister(this);
	}
}
