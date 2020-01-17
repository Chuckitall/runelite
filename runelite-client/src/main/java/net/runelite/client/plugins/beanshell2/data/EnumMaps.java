package net.runelite.client.plugins.beanshell2.data;

import java.util.HashMap;
import java.util.Map;
import net.runelite.api.MenuOpcode;
import net.runelite.api.widgets.WidgetInfo;

public class EnumMaps
{
	static final Map<String, WidgetInfo> WIDGET_INFO = new HashMap<>();
	static final Map<String, MenuOpcode> MENU_OPCODE = new HashMap<>();

	static
	{
		for (WidgetInfo info : WidgetInfo.values())
		{
			WIDGET_INFO.put(info.name(), info);
		}
		for (MenuOpcode opcode : MenuOpcode.values())
		{
			MENU_OPCODE.put(opcode.name(), opcode);
		}
	}

	public static WidgetInfo WidgetInfo(String name)
	{
		return WIDGET_INFO.get(name);
	}

	public static MenuOpcode MenuOpcode(String name)
	{
		return MENU_OPCODE.get(name);
	}
}
