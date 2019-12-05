package net.runelite.client.plugins.fredexperimental.oneclick2.lualibs;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.InventoryID;
import net.runelite.client.plugins.fred.api.wrappers._GameObject;
import net.runelite.client.plugins.fredexperimental.oneclick2.Oneclick2;
import org.apache.commons.lang3.ArrayUtils;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.LibFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

@Slf4j
public class GameObjQueryLib extends TwoArgFunction
{
	private Oneclick2 plugin;
	public GameObjQueryLib(Oneclick2 plugin)
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
//		(id, id2, id3, ...) returns -> _Item containing any of the afformentioned item ids."
		library.set("findAll", new findObjects());
		library.set("findNearest", new nearestObject());

		env.set("gameObject", library);
		env.get("package").get("loaded").set("gameObject", library);
		//		System.out.println( env.get("table").get("length").call( LuaValue.tableOf() ) );
		return library;
	}

	class findObjects extends LibFunction
	{
		public LuaValue call()
		{
			return argerror(1, "At least one value expected");
		}

		public LuaValue call(LuaValue a)
		{
			LuaValue[] v = new LuaValue[] {a};
			return invoke(LuaValue.varargsOf(v)).arg1();
		}

		public LuaValue call(LuaValue a, LuaValue b)
		{
			LuaValue[] v = new LuaValue[] {a, b};
			return invoke(LuaValue.varargsOf(v)).arg1();
		}

		public LuaValue call(LuaValue a, LuaValue b, LuaValue c)
		{
			LuaValue[] v = new LuaValue[] {a, b, c};
			return invoke(LuaValue.varargsOf(v)).arg1();
		}

		public LuaValue call(LuaValue a, LuaValue b, LuaValue c, LuaValue d)
		{
			LuaValue[] v = new LuaValue[] {a, b, c, d};
			return invoke(LuaValue.varargsOf(v)).arg1();
		}

		public Varargs invoke(Varargs args)
		{
			//log.debug("findItems called with {}", args.narg());
			int[] ids = new int[args.narg()];
			for (int i = 1; i <= args.narg(); i++)
			{
				ids[i - 1] = (args.isnumber(i)) ? args.checkint(i) : -1;
			}
			_GameObject[] items = plugin.getGameObjects(Arrays.stream(ids).boxed().collect(Collectors.toList())).toArray(new _GameObject[0]);
			return CoerceJavaToLua.coerce(items);
		}
	}

	class nearestObject extends LibFunction
	{
		public LuaValue call()
		{
			return argerror(1, "At least one value expected");
		}

		public LuaValue call(LuaValue a)
		{
			LuaValue[] v = new LuaValue[] {a};
			return invoke(LuaValue.varargsOf(v)).arg1();
		}

		public LuaValue call(LuaValue a, LuaValue b)
		{
			LuaValue[] v = new LuaValue[] {a, b};
			return invoke(LuaValue.varargsOf(v)).arg1();
		}

		public LuaValue call(LuaValue a, LuaValue b, LuaValue c)
		{
			LuaValue[] v = new LuaValue[] {a, b, c};
			return invoke(LuaValue.varargsOf(v)).arg1();
		}

		public LuaValue call(LuaValue a, LuaValue b, LuaValue c, LuaValue d)
		{
			LuaValue[] v = new LuaValue[] {a, b, c, d};
			return invoke(LuaValue.varargsOf(v)).arg1();
		}

		public Varargs invoke(Varargs args)
		{
			//log.debug("firstItem called with {}", args.narg());
			int[] ids = new int[args.narg()];
			for (int i = 1; i <= args.narg(); i++)
			{
				ids[i - 1] = (args.isnumber(i)) ? args.checkint(i) : -1;
			}
			Optional<LuaValue> first = Optional.ofNullable(plugin.getNearestGameObject(Arrays.stream(ids).boxed().collect(Collectors.toList()))).map(CoerceJavaToLua::coerce);
			return first.orElse(NIL);
		}
	}
}
