package net.runelite.client.plugins.hotkeys.script;

public enum SourceTypes
{
	ANY("Any"),
	SELF("Self"),
	OPPONENT("Opponent");

	private String name;

	SourceTypes(String s)
	{
		this.name = s;
	}

	@Override
	public String toString()
	{
		return this.name;
	}

	public static SourceTypes getByName(String s)
	{
		for (SourceTypes sourceTypes : SourceTypes.values())
		{
			if (s.equals(sourceTypes.toString()))
			{
				return sourceTypes;
			}
		}
		return SourceTypes.ANY;
	}
}
