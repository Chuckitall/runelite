package net.runelite.client.plugins.fredexperimental.beanshell.interfaces;

import net.runelite.client.plugins.fred.api.scripting.StockEntry;

public interface BeanshellMatcher
{
	StockEntry added(StockEntry e);
	StockEntry clicked(StockEntry e);
	boolean peak(int op, int id);
	boolean isMatch(StockEntry e);
	void tick();
}
