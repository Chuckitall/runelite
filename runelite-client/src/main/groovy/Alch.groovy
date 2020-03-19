import groovy.transform.InheritConstructors
import io.reactivex.rxjava3.functions.Consumer;
import net.runelite.api.ItemID
import net.runelite.api.MenuOpcode
import net.runelite.api.Varbits
import net.runelite.api.events.ChatMessage
import net.runelite.api.events.MenuEntryAdded
import net.runelite.api.events.MenuOptionClicked
import net.runelite.api.events.ScriptCallbackEvent
import net.runelite.api.util.Text;
import net.runelite.api.vars.InterfaceTab
import net.runelite.api.widgets.WidgetInfo
import net.runelite.client.plugins.groovy.debugger.DebuggerWindow.LogLevel
import net.runelite.client.plugins.groovy.script.ScriptedPlugin

@InheritConstructors
class Alch extends ScriptedPlugin {
	int[] items_to_alch = [
		ItemID.WATER_BATTLESTAFF, ItemID.EARTH_BATTLESTAFF, ItemID.AIR_BATTLESTAFF, ItemID.FIRE_BATTLESTAFF,
		ItemID.MAPLE_LONGBOW, ItemID.YEW_SHORTBOW, ItemID.YEW_LONGBOW
	];

	int[] items_to_superheat = [
		ItemID.IRON_ORE
	];

	List<Tuple3<WidgetInfo, String, List<Integer>>> items_to_enchant = [
		new Tuple3<>(WidgetInfo.SPELL_LVL_1_ENCHANT, "Lvl-1 Enchant", [ItemID.SAPPHIRE_RING, ItemID.SAPPHIRE_AMULET, ItemID.SAPPHIRE_NECKLACE, ItemID.SAPPHIRE_BRACELET]),
		new Tuple3<>(WidgetInfo.SPELL_LVL_2_ENCHANT, "Lvl-2 Enchant", [ItemID.EMERALD_RING, ItemID.EMERALD_AMULET, ItemID.EMERALD_NECKLACE, ItemID.EMERALD_BRACELET]),
		new Tuple3<>(WidgetInfo.SPELL_LVL_3_ENCHANT, "Lvl-3 Enchant", [ItemID.RUBY_RING, ItemID.RUBY_AMULET, ItemID.RUBY_NECKLACE, ItemID.RUBY_BRACELET]),
		new Tuple3<>(WidgetInfo.SPELL_LVL_4_ENCHANT, "Lvl-4 Enchant", [ItemID.DIAMOND_RING, ItemID.DIAMOND_AMULET, ItemID.DIAMOND_NECKLACE, ItemID.DIAMOND_BRACELET])
	];

	List<Tuple3<WidgetInfo, String, Integer>> heads = [
		new Tuple3<>(WidgetInfo.SPELL_REANIMATE_GOBLIN, "Reanimate Goblin", ItemID.ENSOULED_GOBLIN_HEAD_13448),
		new Tuple3<>(WidgetInfo.SPELL_REANIMATE_MONKEY, "Reanimate Monkey", ItemID.ENSOULED_MONKEY_HEAD_13451),
		new Tuple3<>(WidgetInfo.SPELL_REANIMATE_IMP, "Reanimate Imp", ItemID.ENSOULED_IMP_HEAD_13454),
		new Tuple3<>(WidgetInfo.SPELL_REANIMATE_MINOTAUR, "Reanimate Minotaur", ItemID.ENSOULED_MINOTAUR_HEAD_13457),
		new Tuple3<>(WidgetInfo.SPELL_REANIMATE_SCORPION, "Reanimate Scorpion", ItemID.ENSOULED_SCORPION_HEAD_13460),
		new Tuple3<>(WidgetInfo.SPELL_REANIMATE_BEAR, "Reanimate Bear", ItemID.ENSOULED_BEAR_HEAD_13463),
		new Tuple3<>(WidgetInfo.SPELL_REANIMATE_UNICORN, "Reanimate Unicorn", ItemID.ENSOULED_UNICORN_HEAD_13466),
		new Tuple3<>(WidgetInfo.SPELL_REANIMATE_DOG, "Reanimate Dog", ItemID.ENSOULED_DOG_HEAD_13469),
		new Tuple3<>(WidgetInfo.SPELL_REANIMATE_CHAOS_DRUID, "Reanimate Chaos Druid", ItemID.ENSOULED_CHAOS_DRUID_HEAD_13472),
		new Tuple3<>(WidgetInfo.SPELL_REANIMATE_GIANT, "Reanimate Giant", ItemID.ENSOULED_GIANT_HEAD_13475),
		new Tuple3<>(WidgetInfo.SPELL_REANIMATE_OGRE, "Reanimate Ogre", ItemID.ENSOULED_OGRE_HEAD_13478),
		new Tuple3<>(WidgetInfo.SPELL_REANIMATE_ELF, "Reanimate Elf", ItemID.ENSOULED_ELF_HEAD_13481),
		new Tuple3<>(WidgetInfo.SPELL_REANIMATE_TROLL, "Reanimate Troll", ItemID.ENSOULED_TROLL_HEAD_13484),
		new Tuple3<>(WidgetInfo.SPELL_REANIMATE_HORROR, "Reanimate Horror", ItemID.ENSOULED_HORROR_HEAD_13487),
		new Tuple3<>(WidgetInfo.SPELL_REANIMATE_KALPHITE, "Reanimate Kalphite", ItemID.ENSOULED_KALPHITE_HEAD_13490),
		new Tuple3<>(WidgetInfo.SPELL_REANIMATE_DAGANNOTH, "Reanimate Dagannoth", ItemID.ENSOULED_DAGANNOTH_HEAD_13493),
		new Tuple3<>(WidgetInfo.SPELL_REANIMATE_BLOODVELD, "Reanimate Bloodveld", ItemID.ENSOULED_BLOODVELD_HEAD_13496),
		new Tuple3<>(WidgetInfo.SPELL_REANIMATE_TZHAAR, "Reanimate Tzhaar", ItemID.ENSOULED_TZHAAR_HEAD_13499),
		new Tuple3<>(WidgetInfo.SPELL_REANIMATE_DEMON, "Reanimate Demon", ItemID.ENSOULED_DEMON_HEAD_13502),
		new Tuple3<>(WidgetInfo.SPELL_REANIMATE_AVIANSIE, "Reanimate Aviansie", ItemID.ENSOULED_AVIANSIE_HEAD_13505),
		new Tuple3<>(WidgetInfo.SPELL_REANIMATE_ABYSSAL, "Reanimate Abyssal", ItemID.ENSOULED_ABYSSAL_HEAD_13508),
		new Tuple3<>(WidgetInfo.SPELL_REANIMATE_DRAGON, "Reanimate Dragon", ItemID.ENSOULED_DRAGON_HEAD_13511)
	];

	String[] validTargetFragments = ["High Level Alchemy", "Superheat", "Enchant", "Reanimate"];

	boolean latch = false;

	void onMenuClicked(MenuOptionClicked e) {
		if (!e.getOption().equalsIgnoreCase("<col=0000ff>Cast") || !validTargetFragments.any {f -> e.getTarget().containsIgnoreCase(f)}) {
			return;
		}
		if(e.getTarget().contains("High Level Alchemy")) {
			e.setOption("Cast");
//			_client.setSelectedSpellChildIndex(-1);
			log(LogLevel.DEBUG, "High Level Alchemy");
			_client.setSelectedSpellName("<col=00ff00>High Level Alchemy</col>");
			_client.setSelectedSpellWidget(WidgetInfo.SPELL_HIGH_LEVEL_ALCHEMY.getId());
			latch = true;
		}
		else if (e.getTarget().contains("Superheat"))
		{
			e.setOption("Cast");
//			_client.setSelectedSpellChildIndex(-1);
			log(LogLevel.DEBUG, "Superheat");
			_client.setSelectedSpellName("<col=00ff00>Superheat Item</col>");
			_client.setSelectedSpellWidget(WidgetInfo.SPELL_SUPERHEAT_ITEM.getId());
			latch = true;
		}
		else if(e.getTarget().contains("Enchant"))
		{
			Tuple3<WidgetInfo, String, List<Integer>> selected = items_to_enchant.stream().filter(f -> e.getTarget().contains(f.getV2())).findFirst().orElse(null);
			if(selected != null)
			{
				e.setOption("Cast");
//				_client.setSelectedSpellChildIndex(-1);
				log(LogLevel.DEBUG, "Enchant");
				_client.setSelectedSpellName("<col=00ff00>${selected.getV2()}</col>");
				_client.setSelectedSpellWidget(selected.getV1().getId());
				latch = true;
			}
		}
		else if(e.getTarget().contains("Reanimate"))
		{
			Tuple3<WidgetInfo, String, List<Integer>> selected = heads.stream().filter(f -> e.getTarget().contains(f.getV2())).findFirst().orElse(null);
			if(selected != null)
			{
				e.setOption("Reanimate");
//				_client.setSelectedSpellChildIndex(-1);
				log(LogLevel.DEBUG, "Head");
				_client.setSelectedSpellName("<col=00ff00>${selected.getV2()}</col>");
				_client.setSelectedSpellWidget(selected.getV1().getId());
				latch = true;
			}
		}
		else
		{
			return;
		}
		log(LogLevel.DEBUG, "menu hijacked -> ${e.getOption()}");
		log(LogLevel.WARN, "${_client.getSelectedSpellChildIndex()}|${_client.getSelectedSpellName()}|${_client.getSelectedSpellWidget()}")
//		_client.getSelectedSpellChildIndex();
//		_client.getSelectedSpellName();
//		_client.getSelectedSpellWidget();
	}

	void onMenuAdded(MenuEntryAdded e) {
		if (e.getOpcode() != MenuOpcode.ITEM_USE.getId())
		{
			return;
		}

		int spellbook = _client.getVar(Varbits.SPELLBOOK);

		if (spellbook == 0) //standard
		{
			if (items_to_alch.contains(e.getIdentifier()) || items_to_alch.contains(e.getIdentifier()-1))
			{
				_client.insertMenuItem("<col=0000ff>Cast", "<col=00ff00>High Level Alchemy</col><col=ffffff> -> <col=ff9040>${_client.getItemDefinition(e.getIdentifier()).getName()}", MenuOpcode.ITEM_USE_ON_WIDGET.getId(), e.getIdentifier(), e.param0, e.param1, false);
				return;
			}
			else if (items_to_superheat.contains(e.getIdentifier()))
			{
				_client.insertMenuItem("<col=0000ff>Cast", "<col=00ff00>Superheat Item</col><col=ffffff> -> <col=ff9040>${_client.getItemDefinition(e.getIdentifier()).getName()}", MenuOpcode.ITEM_USE_ON_WIDGET.getId(), e.getIdentifier(), e.param0, e.param1, false);
				return;
			}
			for (int i = 0; i < items_to_enchant.size(); i++)
			{
				if (items_to_enchant[i].getV3().contains(e.getIdentifier()))
				{
					_client.insertMenuItem("<col=0000ff>Cast", "<col=00ff00>${items_to_enchant[i].getV2()}</col><col=ffffff> -> <col=ff9040>${_client.getItemDefinition(e.getIdentifier()).getName()}", MenuOpcode.ITEM_USE_ON_WIDGET.getId(), e.getIdentifier(), e.param0, e.param1, false);
					return;
				}
			}
		}
//		else if (spellbook == 1) //ancients
//		{
//
//		}
//		else if (spellbook == 2) //lunar
//		{
//
//		}
		else if (spellbook == 3) //arrceus
		{
			for (int i = 0; i < heads.size(); i++)
			{
				if (heads[i].getV3().intValue() == e.getIdentifier())
				{
					_client.insertMenuItem("<col=0000ff>Cast", "<col=00ff00>${heads[i].getV2()}</col><col=ffffff> -> <col=ff9040>${_client.getItemDefinition(e.getIdentifier()).getName()}", MenuOpcode.ITEM_USE_ON_WIDGET.getId(), e.getIdentifier(), e.param0, e.param1, false);
					return;
				}
			}
		}
	}

	void onScriptCallbackEvent(ScriptCallbackEvent e)
	{
		if (e.getEventName().equalsIgnoreCase("OnSwitchTopLevel2"))
		{
			int[] intStack = _client.getIntStack();
			//log(LogLevel.ERROR, "Latch: ${latch}");
			if(latch)
			{
				//log(LogLevel.DEBUG, "intStack = ${intStack}");
				intStack[0] = InterfaceTab.INVENTORY.id;
				//log(LogLevel.WARN, "intStack = ${intStack}");
				latch = false;
			}
		}
//		else if (e.getEventName().containsIgnoreCase("watson"))
//		{
//			int[] stack = _scriptStackTools.copyIntsFromStack((e.getEventName().containsIgnoreCase("return")) ? 2 : 4);
//			log((e.getEventName().containsIgnoreCase("return")) ? LogLevel.DEBUG: LogLevel.TRACE, "${e.getEventName()} -> ${stack}");
//		}
//		else if (e.getEventName().containsIgnoreCase("1478_callback"))
//		{
//			int[] stack = _scriptStackTools.copyIntsFromStack(3);
//			log(LogLevel.DEBUG, "${e.getEventName()} -> ${stack}");
//		}
	}

	void onChatMessage(ChatMessage e)
	{
		if (Text.standardize(e.getMessage()).containsIgnoreCase("stash"))
		{
			log(LogLevel.WARN, e.toString());
		}
	}

	void startup() {
		_eventBus.subscribe(MenuOptionClicked.class, this, this.&onMenuClicked as Consumer<MenuOptionClicked>);
		_eventBus.subscribe(MenuEntryAdded.class, this, this.&onMenuAdded as Consumer<MenuEntryAdded>);
		_eventBus.subscribe(ScriptCallbackEvent.class, this, this.&onScriptCallbackEvent as Consumer<ScriptCallbackEvent>);
		_eventBus.subscribe(ChatMessage.class, this, this.&onChatMessage as Consumer<ChatMessage>);
		log(LogLevel.DEBUG, "Starting Up");
	}

	void shutdown() {
		_eventBus.unregister(this);
		log(LogLevel.DEBUG,"Shutting Down");
	}
}
