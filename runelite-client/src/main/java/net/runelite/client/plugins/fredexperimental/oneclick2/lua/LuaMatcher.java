package net.runelite.client.plugins.fredexperimental.oneclick2.lua;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.fred.api.lua.library.LuaGameInteropLib;
import net.runelite.client.plugins.fred.api.other.Pair;
import net.runelite.client.plugins.fredexperimental.oneclick2.Matcher;
import net.runelite.client.plugins.fred.api.scripting.ScriptPlugin;
import net.runelite.client.plugins.fred.api.scripting.StockEntry;
import net.runelite.client.plugins.fredexperimental.oneclick2.lua.libs.GameObjQueryLib;
import net.runelite.client.plugins.fredexperimental.oneclick2.lua.libs.InventoryQueryLib;
import net.runelite.client.plugins.fredexperimental.oneclick2.lua.libs.MenuEntryLib;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;
import org.luaj.vm2.lib.jse.JsePlatform;

@Slf4j
public class LuaMatcher<E extends ScriptPlugin> implements Matcher<E>
{
	@Getter(AccessLevel.PUBLIC)
	private final int uuid;
	@Getter(AccessLevel.PUBLIC)
	private final String path;
	@Getter(AccessLevel.PUBLIC)
	private final String name;

	@Getter(AccessLevel.PROTECTED)
	private E plugin;

	protected Globals luaGlobals;

	public LuaMatcher(E plugin, int uuid, String path, String name)
	{
		this.plugin = plugin;
		this.uuid = uuid;
		this.path = path;
		this.name = name;

		this.luaGlobals = this.initializeGlobals();

		try
		{
			String content = readFile(getPath().concat(getName()));
			this.luaGlobals.load(content, getName()).call();
		}
		catch (IOException e)
		{
			log.error("exception {}", e.getLocalizedMessage(), e.getCause());
			this.luaGlobals.get("dofile").call(LuaValue.valueOf(getPath().concat(getName())));
			e.printStackTrace();
		}

		Pair<Boolean, String> valid = this.isScriptValid();
		log.debug("isScriptValid {}", this.isScriptValid());
		if (valid.get_1())
		{
			this.initializeFunctions();
		}
		else
		{
			throw new IllegalArgumentException(valid.get_2() != null && valid.get_2().length() > 0 ? valid.get_2() : "NO ERROR MESSAGE GIVEN");
		}
	}

	@Override
	public String getResolvedName()
	{
		return getPath().concat(getName());
	}

	private static String readFile(String file) throws IOException
	{
		Path path = Paths.get(file);

		BufferedReader reader = Files.newBufferedReader(path);
		StringBuilder builder = new StringBuilder();
		String currentLine = reader.readLine();
		while (currentLine != null)
		{
			builder.append(currentLine);
			builder.append("\n");
			currentLine = reader.readLine();
		}
		reader.close();
		return builder.toString();
	}

	@Getter(AccessLevel.PUBLIC)
	private boolean dirty = true;

	private LuaFunction func_peak;
	private LuaFunction func_isMatch;

	@Override
	public boolean peak(int op, int id)
	{
		if (func_peak == null)
		{
			log.debug("peak not valid");
			return false;
		}
		try
		{
			LuaValue ret = func_peak.call(LuaValue.valueOf(op), LuaValue.valueOf(id));
//		log.debug("peak({} {}) -> {}", op, id, ret);
			if (ret != null && ret.isboolean())
			{
				return ret.toboolean();
			}
		}
		catch (Exception ignored)
		{

		}
		return false;
	}

	@Override
	public void tick()
	{
		this.dirty = true;
	}

	@Override
	public boolean isMatch(StockEntry e)
	{
		if (func_isMatch == null)
		{
			log.debug("isMatch not valid");
			return false;
		}
		try
		{
			LuaValue ret = func_isMatch.call(CoerceJavaToLua.coerce(e));
			//		log.debug("isMatch({}) -> {}", e, ret);
			if (ret != null && ret.isboolean())
			{
				return ret.toboolean();
			}
		}
		catch (Exception ignored)
		{

		}
		return false;
	}

	@Override
	public StockEntry doAdd(StockEntry e)
	{
		if (func_doAdd == null)
		{
			log.debug("doAdd not valid");
			return null;
		}
		try
		{
			LuaValue ret = func_doAdd.call(CoerceJavaToLua.coerce(e));
			if (ret != null && ret.isuserdata(StockEntry.class))
			{
				return (StockEntry) CoerceLuaToJava.coerce(ret, StockEntry.class);
			}
		}
		catch (Exception ignored)
		{

		}
		return null;
	}

	@Override
	public StockEntry onClick(StockEntry e)
	{
		if (func_onClick == null)
		{
			log.debug("onClick not valid");
			return null;
		}
		try
		{
			LuaValue ret = func_onClick.call(CoerceJavaToLua.coerce(e));
			if (ret != null && !ret.isnil() && ret.isuserdata(StockEntry.class))
			{
				return (StockEntry) CoerceLuaToJava.coerce(ret, StockEntry.class);
			}
		}
		catch (Exception ignored)
		{
		}
		return null;
	}

	@Override
	public final void refresh(StockEntry e)
	{
		if (this.dirty)
		{
			this.dirty = false;
			if (this.func_refresh == null)
			{
				log.debug("refresh not valid");
				return;
			}
			try
			{
				func_refresh.call(CoerceJavaToLua.coerce(e));
			}
			catch (Exception ignored)
			{
			}
		}
	}

	private LuaFunction func_doAdd;

	private LuaFunction func_onClick;

	private LuaFunction func_refresh;

	protected void initializeFunctions()
	{
		func_peak = luaGlobals.get("peak").checkfunction();
		func_isMatch = luaGlobals.get("isMatch").checkfunction();
		func_doAdd = luaGlobals.get("doAdd").checkfunction();
		func_refresh = luaGlobals.get("refresh").checkfunction();
		func_onClick = luaGlobals.get("onClick").checkfunction();
	}

	protected Globals initializeGlobals()
	{
		Globals luaGlobals = JsePlatform.standardGlobals();
		luaGlobals.load(new LuaGameInteropLib());
		luaGlobals.load(new MenuEntryLib<>(getPlugin()));
		luaGlobals.load(new GameObjQueryLib<>(getPlugin()));
		luaGlobals.load(new InventoryQueryLib<>(getPlugin()));
		return luaGlobals;
	}

	protected Pair<Boolean, String> isScriptValid()
	{
		if (luaGlobals.isnil()
			|| !luaGlobals.get("peak").isfunction()
			|| !luaGlobals.get("refresh").isfunction()
			|| !luaGlobals.get("isMatch").isfunction()
			|| !luaGlobals.get("doAdd").isfunction()
			|| !luaGlobals.get("onClick").isfunction())
		{
			String error = "Missing methods: " + (luaGlobals.isnil() ? "globals, " :
				(!luaGlobals.get("peak").isfunction() ? "peak, " : "")
					+ (!luaGlobals.get("refresh").isfunction() ? "refresh, " : "")
					+ (!luaGlobals.get("isMatch").isfunction() ? "isMatch, " : "")
					+ (!luaGlobals.get("doAdd").isfunction() ? "doAdd, " : "")
					+ (!luaGlobals.get("onClick").isfunction() ? "onClick, " : "")
			);
			error = error.substring(0, error.length() - 2);
			log.error("Lua file {} failed w/ error {}", getName(), error);
			return Pair.of(Boolean.FALSE, error);
		}
		return Pair.of(Boolean.TRUE, "");
	}

	@Override
	public List<String> getDebugLines()
	{
		return null;
	}
}
