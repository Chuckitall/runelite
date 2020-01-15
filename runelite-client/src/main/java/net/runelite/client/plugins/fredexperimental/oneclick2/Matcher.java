package net.runelite.client.plugins.fredexperimental.oneclick2;

import java.util.List;
import net.runelite.client.plugins.fred.api.scripting.ScriptPlugin;
import net.runelite.client.plugins.fred.api.scripting.StockEntry;

public interface Matcher<E extends ScriptPlugin>
{
	int getUuid();
	String getPath();
	String getName();
	E getPlugin();

	String getResolvedName();

	List<String> getDebugLines();
	boolean peak(int op, int id);
	void tick();
	boolean isMatch(StockEntry e);
	StockEntry doAdd(StockEntry e);
	StockEntry onClick(StockEntry e);
	void refresh(StockEntry e);
}
