package net.runelite.client.plugins.fred.api.lua.library.dialog;


import net.runelite.api.QueryResults;
import net.runelite.api.queries.DialogQuery;
import net.runelite.client.RuneLite;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

public class FindDialog extends OneArgFunction
{
	@Override
	public LuaValue call(LuaValue arg)
	{
		if (!arg.isstring())
		{
			return argerror(1, "Must be the text to search for.");
		}
		return RuneLite.getClient().map(f -> new DialogQuery().textContains(arg.checkjstring()).result(f)).filter(f -> f.size() > 0).map(QueryResults::first).map(CoerceJavaToLua::coerce).orElse(NIL);
	}
}
