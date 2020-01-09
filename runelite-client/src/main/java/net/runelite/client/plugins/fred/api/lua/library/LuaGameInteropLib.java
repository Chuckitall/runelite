package net.runelite.client.plugins.fred.api.lua.library;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.fred.api.lua.library.craftingInterface.FindCraftingOption;
import net.runelite.client.plugins.fred.api.lua.library.craftingInterface.IsCraftingWidgetOpen;
import net.runelite.client.plugins.fred.api.lua.library.dialog.FindDialog;
import net.runelite.client.plugins.fred.api.lua.library.dialog.IsDialogOpen;
import net.runelite.client.plugins.fred.api.lua.library.npc.GetAllNpc;
import net.runelite.client.plugins.fred.api.lua.library.npc.GetClosestNpc;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;

@Slf4j
public class LuaGameInteropLib extends TwoArgFunction
{
	public LuaGameInteropLib()
	{
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
		library.set("getAllNpc", new GetAllNpc());
		library.set("getClosestNpc", new GetClosestNpc());

		//dialog
		library.set("isDialogOpen", new IsDialogOpen());
		library.set("findDialog", new FindDialog());

		//crafting
		library.set("isCraftingWidgetOpen", new IsCraftingWidgetOpen());
		library.set("findCraftingOption", new FindCraftingOption());

		env.set("game", library);
		env.get("package").get("loaded").set("game", library);
		//		System.out.println( env.get("table").get("length").call( LuaValue.tableOf() ) );
		return library;
	}
}