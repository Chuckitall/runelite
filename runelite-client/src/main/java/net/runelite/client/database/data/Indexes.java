/*
 * This file is generated by jOOQ.
 */
package net.runelite.client.database.data;


import javax.annotation.processing.Generated;
import net.runelite.client.database.data.tables.Loottrackerevents;
import net.runelite.client.database.data.tables.Loottrackerlink;
import net.runelite.client.database.data.tables.Loottrackerloot;
import net.runelite.client.database.data.tables.TmorphSets;
import net.runelite.client.database.data.tables.User;
import org.jooq.Index;
import org.jooq.OrderField;
import org.jooq.impl.Internal;


/**
 * A class modelling indexes of tables of the <code>PUBLIC</code> schema.
 */
@Generated(
	value = {
		"http://www.jooq.org",
		"jOOQ version:3.12.3"
	},
	comments = "This class is generated by jOOQ"
)
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class Indexes
{

	// -------------------------------------------------------------------------
	// INDEX definitions
	// -------------------------------------------------------------------------

	public static final Index PRIMARY_KEY_B = Indexes0.PRIMARY_KEY_B;
	public static final Index FK_LOOTTRACKERDROP_INDEX_6 = Indexes0.FK_LOOTTRACKERDROP_INDEX_6;
	public static final Index FK_LOOTTRACKEREVENT_INDEX_6 = Indexes0.FK_LOOTTRACKEREVENT_INDEX_6;
	public static final Index FK_USER_INDEX_6 = Indexes0.FK_USER_INDEX_6;
	public static final Index PRIMARY_KEY_6 = Indexes0.PRIMARY_KEY_6;
	public static final Index TMORPH_SETS_SET_NAME_UINDEX = Indexes0.TMORPH_SETS_SET_NAME_UINDEX;
	public static final Index PRIMARY_KEY_2 = Indexes0.PRIMARY_KEY_2;
	public static final Index UN_USERNAME_INDEX_2 = Indexes0.UN_USERNAME_INDEX_2;

	// -------------------------------------------------------------------------
	// [#1459] distribute members to avoid static initialisers > 64kb
	// -------------------------------------------------------------------------

	private static class Indexes0
	{
		public static Index PRIMARY_KEY_B = Internal.createIndex("PRIMARY_KEY_B", Loottrackerevents.LOOTTRACKEREVENTS, new OrderField[]{Loottrackerevents.LOOTTRACKEREVENTS.UNIQUEID}, true);
		public static Index FK_LOOTTRACKERDROP_INDEX_6 = Internal.createIndex("FK_LOOTTRACKERDROP_INDEX_6", Loottrackerlink.LOOTTRACKERLINK, new OrderField[]{Loottrackerlink.LOOTTRACKERLINK.DROPUNIQUEID}, false);
		public static Index FK_LOOTTRACKEREVENT_INDEX_6 = Internal.createIndex("FK_LOOTTRACKEREVENT_INDEX_6", Loottrackerlink.LOOTTRACKERLINK, new OrderField[]{Loottrackerlink.LOOTTRACKERLINK.EVENTUNIQUEID}, false);
		public static Index FK_USER_INDEX_6 = Internal.createIndex("FK_USER_INDEX_6", Loottrackerlink.LOOTTRACKERLINK, new OrderField[]{Loottrackerlink.LOOTTRACKERLINK.USERUNIQUEID}, false);
		public static Index PRIMARY_KEY_6 = Internal.createIndex("PRIMARY_KEY_6", Loottrackerloot.LOOTTRACKERLOOT, new OrderField[]{Loottrackerloot.LOOTTRACKERLOOT.UNIQUEID}, true);
		public static Index TMORPH_SETS_SET_NAME_UINDEX = Internal.createIndex("TMORPH_SETS_SET_NAME_UINDEX", TmorphSets.TMORPH_SETS, new OrderField[]{TmorphSets.TMORPH_SETS.SET_NAME}, true);
		public static Index PRIMARY_KEY_2 = Internal.createIndex("PRIMARY_KEY_2", User.USER, new OrderField[]{User.USER.UNIQUEID}, true);
		public static Index UN_USERNAME_INDEX_2 = Internal.createIndex("UN_USERNAME_INDEX_2", User.USER, new OrderField[]{User.USER.USERNAME}, true);
	}
}
