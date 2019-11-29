package net.runelite.client.plugins.fred.miner;




import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by npruff on 8/24/2019.
 **/

@AllArgsConstructor
@Getter(AccessLevel.PACKAGE)
public enum MinerActivity
{
	IDLE("IDLE"),
	MINING("Mining");

	private final String actionString;
}
