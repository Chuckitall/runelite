package net.runelite.client.plugins.fred.api.lua.library.craftingInterface;


import net.runelite.api.queries.DialogQuery;
import net.runelite.client.RuneLite;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;

public class IsCraftingWidgetOpen extends ZeroArgFunction
{
	@Override
	public LuaValue call()
	{
		return RuneLite.getClient().map(f -> new CraftingInterfaceQuery().result(f)).filter(f -> f.size() > 0).map(f -> TRUE).orElse(FALSE);
	}
}
