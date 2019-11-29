package net.runelite.client.plugins.fredexperimental.oneclick2.lualibs;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.fred.api.wrappers._Item;
import net.runelite.client.plugins.fredexperimental.oneclick2.Oneclick2;
import org.apache.commons.lang3.ArrayUtils;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.LibFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;

@Slf4j
public class InventoryQueryLib extends TwoArgFunction
{
	private Oneclick2 plugin;
	public InventoryQueryLib(Oneclick2 plugin)
	{
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
//		(id, id2, id3, ...) returns -> _Item containing any of the aforementioned item ids."
		library.set("findAll", new findItems());
		library.set("findFirst", new firstItem());
		library.set("setSelected", new setSelected());

		env.set("inv", library);
		env.get("package").get("loaded").set("inv", library);
		//		System.out.println( env.get("table").get("length").call( LuaValue.tableOf() ) );
		return library;
	}

	class findItems extends LibFunction
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
			int[] ids = new int[args.narg()];
			for (int i = 1; i <= args.narg(); i++)
			{
				ids[i - 1] = (args.isnumber(i)) ? args.checkint(i) : -1;
			}
			_Item[] items = plugin.getItemContainer(InventoryID.INVENTORY).stream().filter(f -> ArrayUtils.contains(ids, f.getId())).toArray(_Item[]::new);
			return CoerceJavaToLua.coerce(items);
		}
	}

	class firstItem extends LibFunction
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
			int[] ids = new int[args.narg()];
			for (int i = 1; i <= args.narg(); i++)
			{
				ids[i - 1] = (args.isnumber(i)) ? args.checkint(i) : -1;
			}
			Optional<LuaValue> first = plugin.getItemContainer(InventoryID.INVENTORY).stream().filter(f -> ArrayUtils.contains(ids, f.getId())).findFirst().map(CoerceJavaToLua::coerce);
			return first.orElse(NIL);
		}
	}

	class setSelected extends LibFunction
	{
		public LuaValue call()
		{
			return argerror("One argument expected");
		}

		public LuaValue call(LuaValue a)
		{
			_Item param = (_Item) CoerceLuaToJava.coerce(a, _Item.class);
			if (param != null)
			{
				Client c = plugin.getClient();
				if (c != null)
				{
					c.setSelectedItemSlot(param.getIdx());
					c.setSelectedItemID(param.getId());
					c.setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
					return LuaValue.TRUE;
				}
				else
				{
					return error("Client could not be found!");
				}
			}
			else
			{
				return argerror(1, "Arg must be _Item.class");
			}
		}
	}
}
