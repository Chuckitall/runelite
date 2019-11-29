package net.runelite.client.plugins.fredexperimental.oneclick2.lualibs;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.MenuOpcode;
import net.runelite.client.plugins.fredexperimental.oneclick2.Oneclick2;
import net.runelite.client.plugins.fredexperimental.oneclick2.lua.StockEntry;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.LibFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

@Slf4j
public class MenuEntryLib extends TwoArgFunction
{
	private Oneclick2 plugin;
	public MenuEntryLib(Oneclick2 plugin)
	{
		log.debug("Plugin {}", plugin);
		this.plugin = plugin;
	}

	/** Perform one-time initialization on the library by creating a table
	 * containing the library functions, adding that table to the supplied environment,
	 * adding the table to package.loaded, and returning table as the return value.
	 * @param modname the module name supplied if this is loaded via 'require'.
	 * @param env the environment to load into, typically a Globals instance.
	 */
	public LuaValue call(LuaValue modname, LuaValue env)
	{
		LuaTable library = tableOf();
		library.set("empty", new empty());
		library.set("clicked", new clicked());
		library.set("added", new added());

		env.set("menu", library);
		env.get("package").get("loaded").set("menu", library);
		return library;
	}

	static class empty extends ZeroArgFunction
	{
		@Override
		public LuaValue call()
		{
			return CoerceJavaToLua.coerce(new StockEntry());
		}
	}

	static class clicked extends LibFunction
	{
		@Override
		public Varargs invoke(Varargs args)
		{
			String option = args.checkjstring(1);
			String target = args.checkjstring(2);
			int op = args.checkint(3);
			int id = args.checkint(4);
			int p0 = args.checkint(5);
			int p1 = args.checkint(6);
			return CoerceJavaToLua.coerce(new StockEntry(op, id, p0, p1, option, target, false));
		}
	}

	static class added extends LibFunction
	{
		@Override
		public Varargs invoke(Varargs args)
		{
			String option = args.checkjstring(1);
			String target = args.checkjstring(2);
			int p0 = args.checkint(3);
			int p1 = args.checkint(4);

			return CoerceJavaToLua.coerce(new StockEntry(0, 0, p0, p1, option, target, false));
		}
	}

}