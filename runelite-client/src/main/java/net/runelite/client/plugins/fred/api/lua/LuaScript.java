package net.runelite.client.plugins.fred.api.lua;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.fred.api.other.Pair;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;

@Slf4j
public abstract class LuaScript<E extends Plugin>
{
	@Getter(AccessLevel.PUBLIC)
	private final int uuid;
	@Getter(AccessLevel.PUBLIC)
	private final String path;
	@Getter(AccessLevel.PUBLIC)
	private final String name;
	protected Globals luaGlobals;

	@Getter(AccessLevel.PROTECTED)
	private E plugin;

	protected LuaScript(E plugin, int uuid, String path, String name)
	{
		this.plugin = plugin;
		this.uuid = uuid;
		this.path = path;
		this.name = name;

		this.luaGlobals = this.initializeGlobals();

		try
		{
			String content = readFile(path.concat(name));
			this.luaGlobals.load(content, name).call();
		}
		catch (IOException e)
		{
			log.error("exception {}", e.getLocalizedMessage(), e.getCause());
			this.luaGlobals.get("dofile").call(LuaValue.valueOf(path.concat(name)));
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

	public String getResolvedName()
	{
		return getPath().concat(name);
	}

	protected abstract void initializeFunctions();
	protected abstract Globals initializeGlobals();

	protected abstract Pair<Boolean, String> isScriptValid();
	public abstract List<String> getDebugLines();

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
}
