package net.runelite.client.plugins.fredexperimental.beanshell.interfaces;

import java.util.List;
import net.runelite.api.events.MenuOpened;
import net.runelite.client.plugins.fred.api.scripting.StockEntry;

public interface BeanshellMatcher
{
	List<StockEntry> added(StockEntry e);
	StockEntry clicked(StockEntry e);
	boolean peak(int op, int id);
	boolean isMatch(StockEntry e);
	void tick();
	void opened(MenuOpened e);
}
