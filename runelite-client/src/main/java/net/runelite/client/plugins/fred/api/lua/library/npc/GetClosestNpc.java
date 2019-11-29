package net.runelite.client.plugins.fred.api.lua.library.npc;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.queries.NPCQuery;
import net.runelite.client.RuneLite;
import net.runelite.client.plugins.fred.api.wrappers._NPC;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

@Slf4j
public class GetClosestNpc extends TwoArgFunction
{
	@Override
	public LuaValue call(LuaValue arg1, LuaValue arg2)
	{
		if (!arg1.istable())
		{
			return argerror(1, "arg should be table containing all valid npc id's to find.");
		}
		if (!arg2.isint())
		{
			return argerror(2, "arg should be an int describing the max distance from the player to look.");
		}

		LuaTable table = arg1.checktable();
		int[] ids = new int[table.length()];
		for (int i = 1; i <= table.length(); i++)
		{
			int id = table.get(i).optint(-1);
			ids[i - 1] = id;
		}
		int dist = arg2.checkint();
		Optional<Client> client = RuneLite.getClient();
		Optional<Player> player = client.map(Client::getLocalPlayer);
		Optional<WorldPoint> localPlayerLoc = player.map(Player::getWorldLocation);
		if (localPlayerLoc.isPresent()) //this assures us that client and player are also present
		{
			_NPC result =  Optional.ofNullable(new NPCQuery().idEquals(ids).isWithinDistance(localPlayerLoc.get(), dist).result(client.get()).nearestTo(player.get())).map(_NPC::from).orElse(null);
			log.debug("{}", result);
			return CoerceJavaToLua.coerce(result);
		}
		return LuaValue.NIL;
	}
}
