import groovy.transform.InheritConstructors
import io.reactivex.functions.Consumer
import net.runelite.api.MenuOpcode
import net.runelite.api.events.GameTick
import net.runelite.api.events.MenuEntryAdded
import net.runelite.api.events.MenuOptionClicked
import net.runelite.api.events.SoundEffectPlayed
import net.runelite.api.events.VolumeChanged
import net.runelite.api.util.Text
import net.runelite.client.events.ConfigChanged
import net.runelite.client.fred.events.InterfaceChoice
import net.runelite.client.fred.events.SortMenusEvent
import net.runelite.client.plugins.groovy.debugger.DebuggerWindow.LogLevel
import net.runelite.client.plugins.groovy.script.ScriptedPlugin

@InheritConstructors
class Cat extends ScriptedPlugin
{
	boolean subbed = false;
	Object lifecycle = new Object();
	String targetWord = "";
	boolean targetInterfaceSearch(InterfaceChoice event, String heading, String target)
	{
		boolean toRet = false;
		if (event.free() && event.getHeaderText().equalsIgnoreCase(heading))
		{
			for(int i = 1; i <= event.getOptionCount() && event.free(); i++)
			{
				if(event.getOption(i).equalsIgnoreCase(target))
				{
					event.requestOption(i);
					toRet = !event.free()
				}
			}
		}
		return toRet;
	}

	void setInterfaceOption(InterfaceChoice event, String target)
	{
		for(int i = 1; i <= event.getOptionCount() && event.free(); i++)
		{
			String op = event.getOption(i).toLowerCase();
			if (op.equalsIgnoreCase(target))
			{
				event.requestOption(i);
			}
		}
	}

	void onInterfaceChoice(InterfaceChoice event)
	{
		log(LogLevel.DEBUG, event.toString())
		if(targetWord.length() > 0)
		{
			for(int i = 1; i <= event.getOptionCount() && event.free(); i++)
			{
				String op = event.getOption(i).toLowerCase();
				if(op.containsIgnoreCase(targetWord))
				{
					event.requestOption(i);
					targetWord = "";
				}
			}
		}
	}

	void onMenuEntryAdded(MenuEntryAdded e)
	{
		if ((e.getOpcode() > MenuOpcode.MENU_ACTION_DEPRIORITIZE_OFFSET ? MenuOpcode.of(e.getOpcode()-MenuOpcode.MENU_ACTION_DEPRIORITIZE_OFFSET) : e.getMenuOpcode()) == MenuOpcode.NPC_FIFTH_OPTION && e.getOption().equalsIgnoreCase("Interact") && Text.standardize(e.getTarget()).equalsIgnoreCase("Kitten"))
		{
			_client.insertMenuItem("Guess age", e.getTarget(), e.getOpcode(), e.getIdentifier(), e.getParam0(), e.getParam1(), false);
			_client.insertMenuItem("Stroke", e.getTarget(), e.getOpcode(), e.getIdentifier(), e.getParam0(), e.getParam1(), false);
			if(!subbed)
			{
				_eventBus.subscribe(InterfaceChoice.class, lifecycle, this.&onInterfaceChoice as Consumer<InterfaceChoice>)
				_eventBus.subscribe(MenuOptionClicked.class, lifecycle, this::onMenuOptionClicked as Consumer<MenuOptionClicked>);
				_eventBus.subscribe(SortMenusEvent.class, lifecycle, this::onSortMenus as Consumer<SortMenusEvent>);
				subbed = true;
			}
		}
		//Param0=0 Param1=0 Opcode=2013 Id=28927 MenuOption=Interact MenuTarget=<col=ffff00>Kitten CanvasX=464 CanvasY=347 Authentic=true
	}

	void onSoundEffectPlayed(SoundEffectPlayed event)
	{
		if(event.getSoundId() == 547 || event.getSoundId() == 550 || event.getSoundId() == 2500)
		{
			log(LogLevel.DEBUG, "Muting sound: " + event.getSoundId())
			event.consume();
		}
		if(!event.isConsumed())
		{
			log(LogLevel.DEBUG, "sound effect played -> <id: " + event.getSoundId() + ", delay: " + event.getDelay() + ", npcID: " + event.getNpcid() + ", source: " + event.getSource() + ">");
		}
	}

	void onMenuOptionClicked(MenuOptionClicked e)
	{
		if ((e.getOpcode() > MenuOpcode.MENU_ACTION_DEPRIORITIZE_OFFSET ? MenuOpcode.of(e.getOpcode()-MenuOpcode.MENU_ACTION_DEPRIORITIZE_OFFSET) : e.getMenuOpcode()) == MenuOpcode.NPC_FIFTH_OPTION && (e.getOption().equalsIgnoreCase("Stroke") || e.getOption().equalsIgnoreCase("Guess age")) && Text.standardize(e.getTarget()).equalsIgnoreCase("Kitten"))
		{
			targetWord = e.getOption().toLowerCase();
			e.setOption("Interact")
		}
		log(LogLevel.DEBUG, e.toString());
	}

	void onSortMenus(SortMenusEvent event)
	{
		log(LogLevel.DEBUG, event.toString());
	}

	void onGameTick(GameTick tick)
	{
		_eventBus.unregister(lifecycle);
		subbed = false;
	}

	void onVolumeChanged(VolumeChanged event)
	{
		int volume = event.getType() == VolumeChanged.Type.AREA ? _client.getAreaSoundEffectVolume() : (event.getType() == VolumeChanged.Type.EFFECTS ? _client.getSoundEffectVolume() : _client.getMusicVolume())
		log(LogLevel.DEBUG, "Volume " + event.getType().toString() + " changed to " + volume)
	}

	void startup()
	{
		_eventBus.subscribe(MenuEntryAdded.class, this, this::onMenuEntryAdded as Consumer<MenuEntryAdded>);
		_eventBus.subscribe(SoundEffectPlayed.class, this, this::onSoundEffectPlayed as Consumer<SoundEffectPlayed>);
		_eventBus.subscribe(VolumeChanged.class, this, this::onVolumeChanged as Consumer<VolumeChanged>);
		log(LogLevel.DEBUG, "Sound effect volume: " + _client.getSoundEffectVolume())

	}

	void shutdown() {
		_eventBus.unregister(this);
		_eventBus.unregister(lifecycle);
		log(LogLevel.DEBUG,"Shutting Down");
	}
}
