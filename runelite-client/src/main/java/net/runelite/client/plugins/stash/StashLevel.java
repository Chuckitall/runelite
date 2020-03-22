package net.runelite.client.plugins.stash;

import lombok.AccessLevel;
import lombok.Getter;

import static net.runelite.client.plugins.stash.STASHUnit.*;

public enum StashLevel
{
	BEGINNER(
		GYPSY_TENT_ENTRANCE,
		FINE_CLOTHES_ENTRANCE,
		BOB_AXES_ENTRANCE
	),
	EASY(
		NEAR_A_SHED_IN_LUMBRIDGE_SWAMP,
		DRAYNOR_VILLAGE_MARKET,
		OUTSIDE_THE_LEGENDS_GUILD_GATES,
		NEAR_THE_ENTRANA_FERRY_IN_PORT_SARIM,
		DRAYNOR_MANOR_BY_THE_FOUNTAIN,
		CROSSROADS_NORTH_OF_DRAYNOR_VILLAGE,
		VARROCK_PALACE_LIBRARY,
		OUTSIDE_THE_FALADOR_PARTY_ROOM,
		CATHERBY_BEEHIVE_FIELD,
		ROAD_JUNCTION_NORTH_OF_RIMMINGTON,
		OUTSIDE_KEEP_LE_FAYE,
		OUTSIDE_THE_DIGSITE_EXAM_CENTRE,
		MUBARIZS_ROOM_AT_THE_DUEL_ARENA,
		NEAR_HERQUINS_SHOP_IN_FALADOR,
		AUBURYS_SHOP_IN_VARROCK,
		ON_THE_BRIDGE_TO_THE_MISTHALIN_WIZARDS_TOWER,
		LIMESTONE_MINE,
		MUDSKIPPER_POINT,
		AL_KHARID_SCORPION_MINE,
		WHEAT_FIELD_NEAR_THE_LUMBRIDGE_WINDMILL,
		RIMMINGTON_MINE,
		UPSTAIRS_IN_THE_ARDOUGNE_WINDMILL,
		TAVERLEY_STONE_CIRCLE,
		NEAR_THE_PARROTS_IN_ARDOUGNE_ZOO,
		OUTSIDE_THE_FISHING_GUILD,
		ROAD_JUNCTION_SOUTH_OF_SINCLAIR_MANSION,
		NEAR_THE_SAWMILL_OPERATORS_BOOTH,
		OUTSIDE_VARROCK_PALACE_COURTYARD,
		SOUTH_OF_THE_GRAND_EXCHANGE
	),
	MEDIUM(
		CENTRE_OF_CANIFIS,
		EAST_OF_THE_BARBARIAN_VILLAGE_BRIDGE,
		CASTLE_WARS_BANK,
		GNOME_STRONGHOLD_BALANCING_ROPE,
		OBSERVATORY,
		DIGSITE,
		SHANTAY_PASS,
		OUTSIDE_CATHERBY_BANK,
		OUTSIDE_HARRYS_FISHING_SHOP_IN_CATHERBY,
		NORTH_OF_EVIL_DAVES_HOUSE_IN_EDGEVILLE,
		ENTRANCE_OF_THE_ARCEUUS_LIBRARY,
		NORTH_OF_MOUNT_KARUULM,
		MAUSOLEUM_OFF_THE_MORYTANIA_COAST,
		SOUTH_OF_THE_SHRINE_IN_TAI_BWO_WANNAI_VILLAGE,
		BARBARIAN_OUTPOST_OBSTACLE_COURSE,
		OUTSIDE_YANILLE_BANK,
		OGRE_CAGE_IN_KING_LATHAS_TRAINING_CAMP,
		HICKTONS_ARCHERY_EMPORIUM,
		LUMBRIDGE_SWAMP_CAVES,
		OUTSIDE_THE_SEERS_VILLAGE_COURTHOUSE,
		TZHAAR_WEAPONS_STORE,
		WEST_OF_THE_SHAYZIEN_COMBAT_RING,
		OUTSIDE_DRAYNOR_VILLAGE_JAIL
	),
	HARD(
		CHAOS_TEMPLE_IN_THE_SOUTHEASTERN_WILDERNESS,
		TOP_FLOOR_OF_THE_LIGHTHOUSE,
		NOTERAZZOS_SHOP_IN_THE_WILDERNESS,
		MOUNTAIN_CAMP_GOAT_ENCLOSURE,
		SHILO_VILLAGE_BANK,
		NORTHEAST_CORNER_OF_THE_KHARAZI_JUNGLE,
		IN_THE_MIDDLE_OF_JIGGIG,
		HOSIDIUS_MESS,
		FISHING_GUILD_BANK,
		OUTSIDE_THE_GREAT_PYRAMID_OF_SOPHANEM,
		WEST_SIDE_OF_THE_KARAMJA_BANANA_PLANTATION,
		GNOME_GLIDER_ON_WHITE_WOLF_MOUNTAIN,
		INSIDE_THE_DIGSITE_EXAM_CENTRE,
		VOLCANO_IN_THE_NORTHEASTERN_WILDERNESS,
		AGILITY_PYRAMID
	),
	ELITE(
		CHAPEL_IN_WEST_ARDOUGNE,
		NEAR_A_LADDER_IN_THE_WILDERNESS_LAVA_MAZE,
		WARRIORS_GUILD_BANK,
		SOUTHEAST_CORNER_OF_THE_FISHING_PLATFORM,
		ON_TOP_OF_TROLLHEIM_MOUNTAIN,
		ENTRANCE_OF_THE_CAVERN_UNDER_THE_WHIRLPOOL,
		SHAYZIEN_WAR_TENT,
		NEAR_THE_GEM_STALL_IN_ARDOUGNE_MARKET,
		NEAR_A_RUNITE_ROCK_IN_THE_FREMENNIK_ISLES,
		ENTRANCE_OF_THE_CAVE_OF_DAMIS,
		SOUTHEAST_CORNER_OF_THE_MONASTERY,
		OUTSIDE_THE_SLAYER_TOWER_GARGOYLE_ROOM,
		FOUNTAIN_OF_HEROES,
		HALFWAY_DOWN_TROLLWEISS_MOUNTAIN,
		OUTSIDE_THE_LEGENDS_GUILD_DOOR,
		OUTSIDE_THE_BAR_BY_THE_FIGHT_ARENA
	),
	MASTER(
		CRYSTALLINE_MAPLE_TREES,
		OUTSIDE_KRIL_TSUTSAROTHS_ROOM,
		WARRIORS_GUILD_BANK_29047,
		TENT_IN_LORD_IORWERTHS_ENCAMPMENT,
		ENTRANA_CHAPEL,
		OUTSIDE_THE_WILDERNESS_AXE_HUT,
		TZHAAR_GEM_STORE,
		WELL_OF_VOYAGE,
		KING_BLACK_DRAGONS_LAIR,
		BARROWS_CHEST,
		DEATH_ALTAR,
		OUTSIDE_MUDKNUCKLES_HUT,
		NEAR_THE_PIER_IN_ZULANDRA,
		SOUTHEAST_CORNER_OF_LAVA_DRAGON_ISLE,
		BEHIND_MISS_SCHISM_IN_DRAYNOR_VILLAGE,
		BY_THE_BEAR_CAGE_IN_VARROCK_PALACE_GARDENS,
		CENTRE_OF_THE_CATACOMBS_OF_KOUREND,
		SOUL_ALTAR,
		NORTHWESTERN_CORNER_OF_THE_ENCHANTED_VALLEY,
		TOP_FLOOR_OF_THE_YANILLE_WATCHTOWER,
		NORTHERN_WALL_OF_CASTLE_DRAKAN,
		_7TH_CHAMBER_OF_JALSAVRAH
	);

	@Getter(AccessLevel.PUBLIC)
	private final STASHUnit[] units;

	StashLevel(STASHUnit... units)
	{
		this.units = units;
	}
}
