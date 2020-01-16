package net.runelite.client.plugins.hotkeys.script;

public enum AutoScriptTypes
{
	NOT_SET(" "),
	XP_DROP("XP Drop"),
	GAME_TICK("Game Tick"),
	INVENTORY_CHANGED("Inventory Changed"),
	ANIMATION_PLAYED("Animation Started"),
	SOUND_EFFECT_PLAYED("Sound Effect Played"),
	SPEC_CHANGED("Spec Changed");

	private String name;

	AutoScriptTypes(String s)
	{
		this.name = s;
	}

	@Override
	public String toString()
	{
		return this.name;
	}

	public static AutoScriptTypes getByName(String s)
	{
		for (AutoScriptTypes autoScriptTypes : AutoScriptTypes.values())
		{
			if (s.equals(autoScriptTypes.toString()))
			{
				return autoScriptTypes;
			}
		}
		return AutoScriptTypes.NOT_SET;
	}
}
