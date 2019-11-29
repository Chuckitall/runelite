package net.runelite.client.plugins.fred.util;

import java.util.Arrays;
import javax.annotation.Nonnull;
import joptsimple.internal.Strings;
import lombok.EqualsAndHashCode;
import net.runelite.api.MenuEntry;
import net.runelite.api.util.Text;
import net.runelite.client.menus.AbstractComparableEntry;

/**
 * Created by npruff on 8/23/2019.
 */

@EqualsAndHashCode(callSuper = true)
public class MultiTargetComparableEntry extends AbstractComparableEntry
{

	String option;
	String[] targets;
	boolean strictOption;
	boolean strictTargets;

	/**
	 * If two entries are both suppose to be left click,
	 * the entry with the higher priority will be selected.
	 * This only effects left click priority entries.
	 */
	public MultiTargetComparableEntry(String option, String[] targets, boolean strictOption, boolean strictTargets)
	{
		this.option = Text.standardize(option);
		if (targets != null)
		{
			this.targets = Arrays.stream(targets).map(Text::standardize).toArray(String[]::new);
		}
		else
		{
			this.targets = null;
		}
		this.strictOption = strictOption;
		this.strictTargets = strictTargets;
	}

	private boolean nonEmptyTargetExists()
	{
		boolean valid = false;
		if (targets != null)
		{
			for (String target : targets)
			{
				if (!Strings.isNullOrEmpty(target))
				{
					valid = true;
					break;
				}
			}
		}
		return valid;
	}

	public boolean matches(@Nonnull MenuEntry entry)
	{
		String opt = Text.standardize(entry.getOption());

		if (strictOption && !opt.equals(option) || !strictOption && !opt.contains(option))
		{
			return false;
		}

		boolean match = false;
		if (strictTargets || nonEmptyTargetExists())
		{
			String tgt = Text.standardize(entry.getTarget());
			for (String target : targets)
			{
				if (!Strings.isNullOrEmpty(target) && ((strictTargets && tgt.equals(target)) || (!strictTargets && tgt.contains(target))))
				{
					match = true;
					break;
				}
			}
		}

		return match;
	}
}
