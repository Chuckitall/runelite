package net.runelite.client.plugins.hotkeys;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.client.config.Keybind;
import net.runelite.client.plugins.hotkeys.script.HotKeysAutoScript;
import net.runelite.client.plugins.hotkeys.script.HotKeysScript;

@AllArgsConstructor
@Getter
public class HotKeysPreset
{
	private List<HotKeysScript> scripts;
	private List<HotKeysAutoScript> autoScripts;
	private Keybind toggleKeybind;
	private Keybind prayFlickKeybind;
	private Keybind togglePrayFlickKeybind;
	private boolean keepSalveOn;
	private boolean mainOverlayOn;
	private boolean autoOverlayOn;
}
