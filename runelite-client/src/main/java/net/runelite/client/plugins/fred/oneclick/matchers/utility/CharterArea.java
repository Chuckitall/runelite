package net.runelite.client.plugins.fred.oneclick.matchers.utility;

import lombok.Getter;
import net.runelite.api.ObjectID;
import net.runelite.client.plugins.fred.api.wrappers._Area;
import net.runelite.client.plugins.fred.api.wrappers._Tile;
import net.runelite.client.plugins.fred.api.wrappers._TilePath;
import net.runelite.client.plugins.fred.api.builders._TilePathBuilder;

@Getter
public enum CharterArea
{
	PORT_PHASMATYS(
		_Area.buildComplex()
			.startingAt(_Tile.at(3698, 3492))
			.column(0, 6 )
			.column(0, 6 )
			.column(4, 2 )
			.column(4, 10)
			.column(4, 10)
			.column(4, 2 )
			.column(4, 2 )
			.build(),
		_TilePathBuilder
			.startingAt(_Tile.at(3687, 3471))
			.delta(4, 2)
			.delta(5, 1)
			.delta(2, 4)
			.delta(0, 5)
			.delta(0, 5)
			.delta(1, 5)
			.build(),
		ObjectID.BANK_DEPOSIT_BOX_29106
	),
	PORT_KHAZARD(
		_Area.buildComplex()
			.startingAt(_Tile.at(2670, 3144))
			.column(5, 3 )
			.column(5, 2 )
			.column(0, 7 )
			.column(0, 8 )
			.column(0, 1 )
			.build(),
		_TilePathBuilder
			.startingAt(_Tile.at(2664, 3160))
			.delta(-3, -3)
			.delta( 2, -3)
			.delta( 4, -4)
			.delta( 4,  0)
			.build(),
		ObjectID.BANK_DEPOSIT_BOX
	);

	private final _Area charterTraderArea;
	private final _TilePath pathToShips;
	private final int depBoxId;
	CharterArea(_Area area, _TilePath pathToShips, int depBoxId)
	{
		this.charterTraderArea = area;
		this.pathToShips = pathToShips;
		this.depBoxId = depBoxId;
	}
}
