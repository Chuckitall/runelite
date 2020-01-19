package net.runelite.client.plugins.groovy.data;

import io.sentry.config.FileResourceLoader;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import lombok.Value;
import net.runelite.client.plugins.groovy.GroovyCore;

public class ScriptData
{

	@Value
	public static class ScriptEntry
	{
		int id;
		String category;
		String name;
	}

	private static final Map<Integer, ScriptEntry> map = new HashMap<>();
	static
	{
		try
		{
			//Creating an InputStream object
			//creating an InputStreamReader object
			BufferedReader reader = new BufferedReader(new InputStreamReader(GroovyCore.class.getResourceAsStream("script_decode.txt")));
			String str;
			while ((str = reader.readLine()) != null)
			{
				int i = Integer.parseInt(str.split(":")[0]);
				String c = str.split(":")[1].split(",")[0];
				String n = str.split(":")[1].split(",")[1];
				map.put(i, new ScriptEntry(i, c, n));
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			map.put(-1, new ScriptEntry(-1, "ERROR", "error"));
		}
	}

	public static ScriptEntry lookup(int id)
	{
		return map.getOrDefault(id, map.get(-1));
	}
}
