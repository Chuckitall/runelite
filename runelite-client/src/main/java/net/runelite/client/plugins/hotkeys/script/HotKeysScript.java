package net.runelite.client.plugins.hotkeys.script;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.Keybind;
import net.runelite.client.plugins.hotkeys.utils.ExtUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

@Data
@Slf4j
public class HotKeysScript implements Runnable
{
	private String name;

	private List<ImmutablePair<String, String>> statements;

	private Keybind hotkey;

	public HotKeysScript(String name, Keybind hotkey)
	{
		this.name = name;
		this.statements = new ArrayList<>();
		this.hotkey = hotkey;
	}

	public String getStatementsAsText()
	{
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> e : statements)
		{
			sb.append("[");
			sb.append(e.getKey());
			sb.append(":");
			sb.append(e.getValue());
			sb.append("]");
			sb.append("\n");
		}
		return sb.toString();
	}

	public void setStatementsFromText(String text)
	{
		statements.clear();
		List<String> parsed = new ArrayList<>(Arrays.asList(ArrayUtils.nullToEmpty(StringUtils.substringsBetween(text, "[", "]"))));

/*		if (ArrayUtils.nullToEmpty(StringUtils.substringsBetween(text, "[", "]")))
		{
			log.info("no valid substrings between");
			parsed = new ArrayList<>();
		}*/
		for (String s : parsed)
		{
			String[] separated = s.split(":");
			if (!ExtUtils.isValidActionType(separated[0]))
			{
				continue;
			}
			if (separated.length != 2)
			{
				if (separated.length == 1)
				{
					statements.add(ImmutablePair.of(separated[0], ""));
					continue;
				}
				continue;
			}
			statements.add(ImmutablePair.of(separated[0], separated[1]));
		}
	}

	public void run()
	{

	}
}
