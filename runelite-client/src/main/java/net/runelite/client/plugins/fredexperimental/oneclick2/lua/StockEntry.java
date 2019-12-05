package net.runelite.client.plugins.fredexperimental.oneclick2.lua;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.MenuEntry;

@Slf4j
public class StockEntry
{
	public int op;
	public int id;
	public int p0;
	public int p1;
	public String option;
	public String target;
	public boolean forceLeftClick;

	public StockEntry()
	{
		this.op = 0;
		this.id = 0;
		this.p0 = 0;
		this.p1 = 0;
		this.option = "";
		this.target = "";
		this.forceLeftClick = false;
	}

	public StockEntry(int op, int id, int p0, int p1, String option, String target, boolean forceLeftClick)
	{
		this.op = op;
		this.id = id;
		this.p0 = p0;
		this.p1 = p1;
		this.option = option;
		this.target = target;
		this.forceLeftClick = forceLeftClick;
	}

	public StockEntry(MenuEntry e)
	{
		op = e.getOpcode();
		id = e.getIdentifier();
		p0 = e.getParam0();
		p1 = e.getParam1();
		option = e.getOption();
		target = e.getTarget();
		forceLeftClick = e.isForceLeftClick();
	}

	public static MenuEntry build(StockEntry e)
	{
		return new MenuEntry(e.option, e.target, e.id, e.op, e.p0, e.p1, e.forceLeftClick);
	}
}
