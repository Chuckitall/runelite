package net.runelite.client.plugins.fredexperimental.beanshell;

import bsh.EvalError;
import bsh.Interpreter;
import java.io.IOException;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.events.MenuOpened;
import net.runelite.client.plugins.fred.api.scripting.StockEntry;
import net.runelite.client.plugins.fredexperimental.beanshell.interfaces.BeanshellMatcher;

public class BeanshellScript implements BeanshellMatcher
{
	@Getter(AccessLevel.PUBLIC)
	private final int uuid;
	@Getter(AccessLevel.PUBLIC)
	private final String path;
	@Getter(AccessLevel.PUBLIC)
	private final String name;

	@Getter(AccessLevel.PUBLIC)
	private final BeanshellPlugin plugin;

	private boolean dirty = true;

	@Getter(AccessLevel.PUBLIC)
	private BeanshellMatcher matcher;

	public BeanshellScript(BeanshellPlugin plugin, int uuid, String path, String name)
	{
		this.plugin = plugin;
		this.uuid = uuid;
		this.path = path;
		this.name = name;
	}

	public String getResolvedName()
	{
		return getPath().concat(getName());
	}

	public void load()
	{
		matcher = null;
		try
		{
			Interpreter sandbox = new Interpreter();
			sandbox.source(getResolvedName());
			matcher = (BeanshellMatcher) sandbox.getInterface(BeanshellMatcher.class);
			dirty = true;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (EvalError evalError)
		{
			evalError.printStackTrace();
		}
	}

	@Override
	public List<StockEntry> added(StockEntry e)
	{
		if (matcher != null)
		{
			return matcher.added(e);
		}
		return null;
	}

	@Override
	public StockEntry clicked(StockEntry e)
	{
		if (matcher != null)
		{
			return matcher.clicked(e);
		}
		return null;
	}

	@Override
	public boolean peak(int op, int id)
	{
		if (matcher != null)
		{
			return matcher.peak(op, id);
		}
		return false;
	}

	@Override
	public boolean isMatch(StockEntry e)
	{
		if (matcher != null)
		{
			return matcher.isMatch(e);
		}
		return false;
	}

	@Override
	public void tick()
	{
		if (matcher != null)
		{
			matcher.tick();
		}
	}

	@Override
	public void opened(MenuOpened e)
	{
		if (matcher != null)
		{
			matcher.opened(e);
		}
	}
}
