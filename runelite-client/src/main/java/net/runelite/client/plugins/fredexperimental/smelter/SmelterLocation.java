package net.runelite.client.plugins.fredexperimental.smelter;

import lombok.Getter;
import net.runelite.api.ObjectID;
import net.runelite.client.plugins.fred.api.builders._TilePathBuilder;
import net.runelite.client.plugins.fred.api.wrappers._Area;
import net.runelite.client.plugins.fred.api.wrappers._Tile;
import net.runelite.client.plugins.fred.api.wrappers._TilePath;

@Getter
public enum SmelterLocation
{
	PORT_PHASMATYS(
		_Area.buildRect()
			.from(_Tile.at(3698, 3463))
			.to(  _Tile.at(3678, 3483))
			.build(),
		_TilePathBuilder
			.startingAt(_Tile.at(3689, 3468))
			.delta(1, 3)
			.delta(-4, 3)
			.delta(-2, 0)
			.delta(0, 5)
			.build(),
		ObjectID.FURNACE_24009,
		ObjectID.BANK_BOOTH_16642
	);

	private final _Area boundsArea;
	private final _TilePath pathToFurnace;
	private final int smelterId;
	private final int bankBoothId;
	SmelterLocation(_Area boundsArea, _TilePath pathToFurnace, int smelterId, int bankBoothId)
	{
		this.boundsArea = boundsArea;
		this.pathToFurnace = pathToFurnace;
		this.smelterId = smelterId;
		this.bankBoothId = bankBoothId;
	}
}
