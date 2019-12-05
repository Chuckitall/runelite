package net.runelite.client.plugins.fred.oneclick.util;

import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.LocatableQueryResults;
import net.runelite.api.queries.GameObjectQuery;

@UtilityClass
public class OneclickConstants
{

	@Getter(AccessLevel.PUBLIC)
	private static final Set<Integer> BANK_OBJECT_IDS = ImmutableSet.of(
		34801, //varrock west bank south bank booth
		10356  //ardy south bank middle bank booth
	);

	private GameObjectQuery bankQuery = new GameObjectQuery().idEquals(10356);
	public LocatableQueryResults findBanks(Client client, int maxDist)
	{
		if (client.getLocalPlayer() != null)
		{
			return bankQuery.isWithinDistance(client.getLocalPlayer().getWorldLocation(), maxDist).result(client);
		}
		return new LocatableQueryResults<GameObject>(Collections.emptySet());
	}
}
