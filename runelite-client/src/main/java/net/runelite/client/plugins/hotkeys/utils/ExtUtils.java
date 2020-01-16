package net.runelite.client.plugins.hotkeys.utils;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemDefinition;
import net.runelite.api.ItemID;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.PlayerAppearance;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.api.VarPlayer;
import net.runelite.api.Varbits;
import net.runelite.api.kit.KitType;
import net.runelite.api.queries.InventoryWidgetItemQuery;
import net.runelite.api.queries.NPCQuery;
import net.runelite.api.queries.PlayerQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.game.PlayerContainer;
import net.runelite.client.game.PlayerManager;
import net.runelite.client.plugins.hotkeys.HotKeysPlugin;
import net.runelite.client.plugins.hotkeys.script.HotKeysAutoScript;
import net.runelite.client.plugins.hotkeys.script.HotKeysScript;
import net.runelite.client.plugins.hotkeys.utils.data.Entries;
import net.runelite.client.plugins.hotkeys.utils.data.MultipleIdItems;
import net.runelite.client.plugins.hotkeys.utils.data.PrayerMenuActions;
import net.runelite.client.plugins.hotkeys.utils.data.SpellMenuActions;
import net.runelite.client.util.Clipboard;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

@Slf4j
public class ExtUtils {
	public static final List<String> actionTypes = new ArrayList<>(Arrays.asList("AUTOPRAY", "BREAKIFFALSE", "BREAKIFTRUE", "CASTLASTTARGET", "DROP", "EAT", "EQUIP", "HITLASTTARGET", "LEFTCLICKCAST", "MOVETOTARGET", "POT", "PRAY", "PRAYOFF", "PRAYON", "QUICKPRAYER", "SCHEDULE", "SENDCHAT", "SPEC", "SPECON", "SPELL"));

	private static final ImmutableMap<String, int[]> potMap = ImmutableMap.<String, int[]>builder(). //1, 2, 3, 4
			put("ANTIPOISON", new int[]{ItemID.ANTIPOISON1, ItemID.ANTIPOISON2, ItemID.ANTIPOISON3, ItemID.ANTIPOISON4}).
			put("GUTHIX_REST", new int[]{ItemID.GUTHIX_REST1, ItemID.GUTHIX_REST2, ItemID.GUTHIX_REST3, ItemID.GUTHIX_REST4}).
			put("PRAYER_POTION", new int[]{ItemID.PRAYER_POTION1, ItemID.PRAYER_POTION2, ItemID.PRAYER_POTION3, ItemID.PRAYER_POTION4}).
			put("RANGING_POTION", new int[]{ItemID.RANGING_POTION1, ItemID.RANGING_POTION2, ItemID.RANGING_POTION3, ItemID.RANGING_POTION4}).
			put("SARADOMIN_BREW", new int[]{ItemID.SARADOMIN_BREW1, ItemID.SARADOMIN_BREW2, ItemID.SARADOMIN_BREW3, ItemID.SARADOMIN_BREW4}).
			put("SUPER_COMBAT_POTION", new int[]{ItemID.SUPER_COMBAT_POTION1, ItemID.SUPER_COMBAT_POTION2, ItemID.SUPER_COMBAT_POTION3, ItemID.SUPER_COMBAT_POTION4}).
			put("SUPER_RESTORE", new int[]{ItemID.SUPER_RESTORE1, ItemID.SUPER_RESTORE2, ItemID.SUPER_RESTORE3, ItemID.SUPER_RESTORE4}).
			put("XERICS_AID", new int[]{ItemID.XERICS_AID_1_20981, ItemID.XERICS_AID_2_20982, ItemID.XERICS_AID_3_20983, ItemID.XERICS_AID_4_20984}).
			put("REVITALISATION", new int[]{ItemID.REVITALISATION_1_20957, ItemID.REVITALISATION_2_20958, ItemID.REVITALISATION_3_20959, ItemID.REVITALISATION_4_20960}).
			put("STAMINA", new int[]{ItemID.STAMINA_POTION1, ItemID.STAMINA_POTION2, ItemID.STAMINA_POTION3, ItemID.STAMINA_POTION4}).
			put("MAGIC_POTION", new int[]{ItemID.MAGIC_POTION1, ItemID.MAGIC_POTION2, ItemID.MAGIC_POTION3, ItemID.MAGIC_POTION4}).build();

	public static void runAutoScript(HotKeysAutoScript script, HotKeysPlugin plugin) {
		if (!script.isEnabled()) {
			return;
		}
		log.info("Running auto script: " + script.getName());
		Client client = plugin.getClient();
		List<ImmutablePair<String, String>> statementList = script.getStatements();
		List<MenuEntry> entriesToAdd = new ArrayList<>();
		outerLoop:
		for (ImmutablePair<String, String> statement : statementList) {
			switch (statement.getKey()) {
				case "BREAKIFTRUE": {
					if (evaluateConditional(statement.getValue(), plugin))
					{
						break outerLoop;
					}
					else
					{
						break;
					}
				}
				case "BREAKIFFALSE":
				{
					if (!evaluateConditional(statement.getValue(), plugin))
					{
						break outerLoop;
					}
					else
					{
						break;
					}
				}
				case "EQUIP":
					entriesToAdd.addAll(getMultiSwapEntries(statement.getValue(), plugin));
					break;
				case "PRAY":
					entriesToAdd.add(PrayerMenuActions.getEntry(Prayer.valueOf(statement.getValue())));
					break;
				case "QUICKPRAYER":
					entriesToAdd.add(Entries.quickPrayer);
					break;
				case "PRAYON":
					if (!client.isPrayerActive(Prayer.valueOf(statement.getValue())))
					{
						entriesToAdd.add(PrayerMenuActions.getEntry(Prayer.valueOf(statement.getValue())));
					}
					break;
				case "PRAYOFF":
					if (client.isPrayerActive(Prayer.valueOf(statement.getValue())))
					{
						entriesToAdd.add(PrayerMenuActions.getEntry(Prayer.valueOf(statement.getValue())));
					}
					break;
				case "SPELL":
					entriesToAdd.add(SpellMenuActions.valueOf(statement.getValue()).getEntry());
					break;
				case "SPEC":
					if (statement.getValue().equals("2"))
					{
						entriesToAdd.add(Entries.spec);
						entriesToAdd.add(Entries.spec);
					}
					else
					{
						entriesToAdd.add(Entries.spec);
					}
					break;
				case "SPECON":
					if (plugin.getClient().getVar(VarPlayer.SPECIAL_ATTACK_ENABLED) != 1)
					{
						entriesToAdd.add(Entries.spec);
					}
					break;
				case "EAT":
					int foodID = Integer.parseInt(statement.getValue());
					int foodSlot = findItem(Integer.parseInt(statement.getValue()), plugin);
					if (foodSlot != -1)
					{
						MenuEntry entry = Entries.consume.clone();
						entry.setIdentifier(foodID);
						entry.setParam0(foodSlot);
						entriesToAdd.add(entry);
					}
					break;
				case "POT":
					int potID = -1;
					int potSlot = -1;
					for (int i = 0; i <= 3; i++)
					{
						int tempID;
						tempID = potMap.get(statement.getValue())[i];
						if (findItem(tempID, plugin) != -1)
						{
							potID = tempID;
							potSlot = findItem(potID, plugin);
							break;
						}
					}
					if (potID != -1)
					{
						MenuEntry entry = Entries.consume.clone();
						entry.setIdentifier(potID);
						entry.setParam0(potSlot);
						entriesToAdd.add(entry);
					}
					break;
				case "HITLASTTARGET":
				{
					Actor lastTarget = plugin.getLastTarget();
					if (lastTarget == null)
					{
						break;
					}
					if (lastTarget instanceof NPC)
					{
						NPC npcTarget = new NPCQuery().idEquals(((NPC) lastTarget).getId()).result(client).first();
						if (npcTarget != null)
						{
							MenuEntry entry = Entries.AttackNPC.clone();
							entry.setIdentifier(((NPC) lastTarget).getIndex());
							entry.setTarget(lastTarget.getName());
							entriesToAdd.add(entry);
							//client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Attempting to attack NPC '" + lastTarget.getName() + "' at index " + ((NPC) lastTarget).getIndex(), null);
						}
						else
						{
							//client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "npcTarget null", null);
						}
					}
					else if (lastTarget instanceof Player)
					{
						Player playerTarget = new PlayerQuery().nameEquals(lastTarget.getName()).result(client).first();
						if (playerTarget != null)
						{
							MenuEntry entry = Entries.AttackPlayer.clone();
							entry.setIdentifier(playerTarget.getPlayerId());
							entry.setTarget(playerTarget.getName());
							entriesToAdd.add(entry);
							//client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Attempting to attack Player '" + lastTarget.getName() + "' with PID " + ((Player) lastTarget).getPlayerId(), null);
						}
						else
						{
							//client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "playerTarget null", null);
						}
					}
					/*else
					{
						client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "No NPC or Player found as last target", null);
					}*/
					break;
				}
				case "CASTLASTTARGET":
				{
					Actor lastTarget = plugin.getLastTarget();
					if (lastTarget instanceof NPC)
					{
						MenuEntry entry = Entries.CastNPC.clone();
						entry.setIdentifier(((NPC) lastTarget).getIndex());
						entry.setTarget(lastTarget.getName());
						entriesToAdd.add(entry);
						//client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Attempting to cast at NPC '" + lastTarget.getName() + "' at index " + ((NPC) lastTarget).getIndex(), null);
					}
					else if (lastTarget instanceof Player)
					{

						MenuEntry entry = Entries.CastPlayer.clone();
						entry.setIdentifier(((Player) lastTarget).getPlayerId());
						entry.setTarget(lastTarget.getName());
						entriesToAdd.add(entry);
						//client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Attempting to cast at Player '" + lastTarget.getName() + "' with PID " + ((Player) lastTarget).getPlayerId(), null);
					}
					else
					{
						//client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "No NPC or Player found as last target", null);
					}
					break;
				}
				case "MOVETOTARGET":
				{
					Actor lastTarget = plugin.getLastTarget();
					if (lastTarget instanceof Player)
					{

						MenuEntry entry = Entries.FollowPlayer.clone();
						entry.setIdentifier(((Player) lastTarget).getPlayerId());
						entry.setTarget(lastTarget.getName());
						entriesToAdd.add(entry);
					}
					break;
				}
				case "DROP": {
					List<MenuEntry> dropEntries = ObjectUtils.defaultIfNull(getDropEntries(stringToIntArray(statement.getValue()), plugin), new ArrayList<MenuEntry>());
					entriesToAdd.addAll(dropEntries);
					break;
				}
				case "SCHEDULE":
				{
					int delay;
					String[] splitted = statement.getValue().split(",");
					if (splitted.length != 2)
					{
						break;
					}
					try
					{
						delay = Integer.parseInt(splitted[1]);
					}
					catch (NumberFormatException ex)
					{
						break;
					}
					for (HotKeysScript s : plugin.getScripts())
					{
						if (s.getName().equals(splitted[0]))
						{
							plugin.getScheduledScripts().add(Pair.of(s, delay));
						}
					}
					break;
				}
				case "USEITEM":
				{
					String[] splitted = statement.getValue().split(",");
					int first;
					int second;
					if (splitted.length != 2)
					{
						break;
					}
					try
					{
						first = Integer.parseInt(splitted[0]);
					}
					catch (NumberFormatException ex)
					{
						break;
					}
					try
					{
						second = Integer.parseInt(splitted[1]);
					}
					catch (NumberFormatException ex)
					{
						break;
					}
					MenuEntry entry = Entries.itemUseOnItem.clone();
					entry.setIdentifier(first);
					int slot = findItem(first, plugin);
					if (slot == -1)
					{
						return;
					}
					int slot2 = findItem(second, plugin);
					if (slot2 == -1)
					{
						return;
					}

					entry.setParam0(findItem(first, plugin));
					client.setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
					client.setSelectedItemSlot(slot2);
					client.setSelectedItemID(second);
					entriesToAdd.add(entry);
					break;
				}
				/*
				case "SENDCHAT":
				{
					plugin.getClientThread().invoke(() ->
					{
						client.setVar(VarClientStr.CHATBOX_TYPED_TEXT, statement.getValue());
						client.runScript(ScriptID.CHATBOX_INPUT, ChatMessageType.PUBLICCHAT, statement.getValue());
						client.setVar(VarClientStr.CHATBOX_TYPED_TEXT, "");
					});
					break;
				}*/
			}
		}
		addEntries(entriesToAdd, plugin);
	}

	//private static Set<Integer> neckSlotItems = Sets.of(ItemID.AMULET_OF_FURY, ItemID.AMULET_OF_TORTURE, ItemID.NECKLACE_OF_ANGUISH, ItemID.OCCULT_NECKLACE);
	private static Set<Integer> neckSlotItems = new HashSet<>(Arrays.asList(ItemID.AMULET_OF_FURY, ItemID.AMULET_OF_TORTURE, ItemID.NECKLACE_OF_ANGUISH, ItemID.OCCULT_NECKLACE));

	public static void runScript(HotKeysScript script, HotKeysPlugin plugin) {
		log.info("Running script: " + script.getName());
		Client client = plugin.getClient();
		List<ImmutablePair<String, String>> statementList = script.getStatements();
		List<MenuEntry> entriesToAdd = new ArrayList<>();
		outerLoop:
		for (ImmutablePair<String, String> statement : statementList) {
			switch (statement.getKey()) {
				case "BREAKIFTRUE": {
					if (evaluateConditional(statement.getValue(), plugin)) {
						break outerLoop;
					} else {
						break;
					}
				}
				case "BREAKIFFALSE":
				{
					if (!evaluateConditional(statement.getValue(), plugin))
					{
						break outerLoop;
					}
					else
					{
						break;
					}
				}
				case "EQUIP":
					entriesToAdd.addAll(getMultiSwapEntries(statement.getValue(), plugin));
					break;
				case "PRAY":
					entriesToAdd.add(PrayerMenuActions.getEntry(Prayer.valueOf(statement.getValue())));
					break;
				case "QUICKPRAYER":
					entriesToAdd.add(Entries.quickPrayer);
					break;
				case "PRAYON":
					if (!client.isPrayerActive(Prayer.valueOf(statement.getValue())))
					{
						entriesToAdd.add(PrayerMenuActions.getEntry(Prayer.valueOf(statement.getValue())));
					}
					break;
				case "PRAYOFF":
					if (client.isPrayerActive(Prayer.valueOf(statement.getValue())))
					{
						entriesToAdd.add(PrayerMenuActions.getEntry(Prayer.valueOf(statement.getValue())));
					}
					break;
				case "SPELL":
					entriesToAdd.add(SpellMenuActions.valueOf(statement.getValue()).getEntry());
					break;
				case "SPEC":
					if (statement.getValue().equals("2"))
					{
						entriesToAdd.add(Entries.spec);
						entriesToAdd.add(Entries.spec);
					}
					else
					{
						entriesToAdd.add(Entries.spec);
					}
					break;
				case "SPECON":
					if (plugin.getClient().getVar(VarPlayer.SPECIAL_ATTACK_ENABLED) != 1)
					{
						entriesToAdd.add(Entries.spec);
					}
					break;
				case "EAT":
					int foodID = Integer.parseInt(statement.getValue());
					int foodSlot = findItem(Integer.parseInt(statement.getValue()), plugin);
					if (foodSlot != -1)
					{
						MenuEntry entry = Entries.consume.clone();
						entry.setIdentifier(foodID);
						entry.setParam0(foodSlot);
						entriesToAdd.add(entry);
					}
					break;
				case "POT":
					int potID = -1;
					int potSlot = -1;
					for (int i = 0; i <= 3; i++)
					{
						int tempID;
						tempID = potMap.get(statement.getValue())[i];
						if (findItem(tempID, plugin) != -1)
						{
							potID = tempID;
							potSlot = findItem(potID, plugin);
							break;
						}
					}
					if (potID != -1)
					{
						MenuEntry entry = Entries.consume.clone();
						entry.setIdentifier(potID);
						entry.setParam0(potSlot);
						entriesToAdd.add(entry);
					}
					break;
				case "HITLASTTARGET":
				{
					Actor lastTarget = plugin.getLastTarget();
					if (lastTarget == null)
					{
						break;
					}
					if (lastTarget instanceof NPC)
					{
						NPC npcTarget = new NPCQuery().idEquals(((NPC) lastTarget).getId()).result(client).first();
						if (npcTarget != null)
						{
							MenuEntry entry = Entries.AttackNPC.clone();
							entry.setIdentifier(((NPC) lastTarget).getIndex());
							entry.setTarget(lastTarget.getName());
							entriesToAdd.add(entry);
							//client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Attempting to attack NPC '" + lastTarget.getName() + "' at index " + ((NPC) lastTarget).getIndex(), null);
						}
						else
						{
							//client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "npcTarget null", null);
						}
					}
					else if (lastTarget instanceof Player)
					{
						Player playerTarget = new PlayerQuery().nameEquals(lastTarget.getName()).result(client).first();
						if (playerTarget != null)
						{
							MenuEntry entry = Entries.AttackPlayer.clone();
							entry.setIdentifier(playerTarget.getPlayerId());
							entry.setTarget(playerTarget.getName());
							entriesToAdd.add(entry);
							//client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Attempting to attack Player '" + lastTarget.getName() + "' with PID " + ((Player) lastTarget).getPlayerId(), null);
						}
						else
						{
							//client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "playerTarget null", null);
						}
					}
					/*else
					{
						client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "No NPC or Player found as last target", null);
					}*/
					break;
				}
				case "CASTLASTTARGET":
				{
					Actor lastTarget = plugin.getLastTarget();
					if (lastTarget instanceof NPC)
					{
						MenuEntry entry = Entries.CastNPC.clone();
						entry.setIdentifier(((NPC) lastTarget).getIndex());
						entry.setTarget(lastTarget.getName());
						entriesToAdd.add(entry);
						//client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Attempting to cast at NPC '" + lastTarget.getName() + "' at index " + ((NPC) lastTarget).getIndex(), null);
					}
					else if (lastTarget instanceof Player)
					{

						MenuEntry entry = Entries.CastPlayer.clone();
						entry.setIdentifier(((Player) lastTarget).getPlayerId());
						entry.setTarget(lastTarget.getName());
						entriesToAdd.add(entry);
						//client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Attempting to cast at Player '" + lastTarget.getName() + "' with PID " + ((Player) lastTarget).getPlayerId(), null);
					}
					else
					{
						//client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "No NPC or Player found as last target", null);
					}
					break;
				}
				case "MOVETOTARGET":
				{
					Actor lastTarget = plugin.getLastTarget();
					if (lastTarget instanceof Player)
					{

						MenuEntry entry = Entries.FollowPlayer.clone();
						entry.setIdentifier(((Player) lastTarget).getPlayerId());
						entry.setTarget(lastTarget.getName());
						entriesToAdd.add(entry);
					}
					break;
				}
				case "DROP":
				{
					List<MenuEntry> dropEntries = ObjectUtils.defaultIfNull(getDropEntries(stringToIntArray(statement.getValue()), plugin), new ArrayList<MenuEntry>());
					entriesToAdd.addAll(dropEntries);
					break;
				}
				case "SCHEDULE":
				{
					int delay;
					String[] splitted = statement.getValue().split(",");
					if (splitted.length != 2)
					{
						break;
					}
					try
					{
						delay = Integer.parseInt(splitted[1]);
					}
					catch (NumberFormatException ex)
					{
						break;
					}
					for (HotKeysScript s : plugin.getScripts())
					{
						if (s.getName().equals(splitted[0]))
						{
							plugin.getScheduledScripts().add(Pair.of(s, delay));
						}
					}
					break;
				}
				case "USEITEM":
				{
					String[] splitted = statement.getValue().split(",");
					int first;
					int second;
					if (splitted.length != 2)
					{
						break;
					}
					try
					{
						first = Integer.parseInt(splitted[0]);
					}
					catch (NumberFormatException ex)
					{
						break;
					}
					try
					{
						second = Integer.parseInt(splitted[1]);
					}
					catch (NumberFormatException ex)
					{
						break;
					}
					MenuEntry entry = Entries.itemUseOnItem.clone();
					entry.setIdentifier(first);
					int slot = findItem(first, plugin);
					if (slot == -1)
					{
						return;
					}
					int slot2 = findItem(second, plugin);
					if (slot2 == -1)
					{
						return;
					}

					entry.setParam0(findItem(first, plugin));
					client.setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
					client.setSelectedItemSlot(slot2);
					client.setSelectedItemID(second);
					entriesToAdd.add(entry);
					break;
				}
			}
		}
		addEntries(entriesToAdd, plugin);
	}

	public static boolean isValidActionType(String s) {
		return actionTypes.contains(s);
	}

	public static void copyGear(Client client) {
		final ItemContainer e = client.getItemContainer(InventoryID.EQUIPMENT);

		if (e == null) {
			return;
		}

		final StringBuilder sb = new StringBuilder();

		for (Item item : e.getItems()) {
			if (item.getId() == -1 || item.getId() == 0) {
				continue;
			}

			sb.append(item.getId());
			sb.append(",");
		}

		final String string = sb.toString();
		Clipboard.store(string.replaceAll(",$", ""));
	}

	private static List<MenuEntry> getMultiSwapEntries(String itemsListRaw, HotKeysPlugin plugin)
	{
		List<MenuEntry> listToReturn = new ArrayList<>();
		if (StringUtils.isEmpty(itemsListRaw))
		{
			return listToReturn;
		}
		final int[] ints = Arrays.stream(itemsListRaw.split(",")).map(String::trim).mapToInt(Integer::parseInt).toArray();
		for (int itemID : ints)
		{
			int modifiedId = getGroupsId(itemID, plugin);
			final int slot = findItem(modifiedId, plugin);
			if (slot != -1)
			{
				final MenuEntry entry = Entries.equipItem.clone();
				entry.setIdentifier(modifiedId);
				entry.setParam0(slot);
				listToReturn.add(entry);
			}
		}
		return listToReturn;
	}

	private static int findItem(int itemID, HotKeysPlugin plugin)
	{
		Item item;
		Item[] items;
		final ItemContainer itemContainer = plugin.getClient().getItemContainer(InventoryID.INVENTORY);
		if (itemContainer == null)
		{
			return -1;
		}
		items = itemContainer.getItems();
		if (plugin.isKeepSalveOn())
		{
			int amuletID = -1;
			Player p = plugin.getClient().getLocalPlayer();
			if (p != null)
			{
				PlayerAppearance playerAppearance = p.getPlayerAppearance();
				if (playerAppearance != null)
				{
					amuletID = ObjectUtils.defaultIfNull(playerAppearance.getEquipmentId(KitType.AMULET), -1);
				}
			}

			for (int slot = 0; slot < items.length; slot++)
			{
				item = items[slot];
				if (item != null && itemID == item.getId())
				{
					if (amuletID == ItemID.SALVE_AMULET_E && neckSlotItems.contains(itemID))
					{
						return -1;
					}
					return slot;
				}
			}
		}
		else
		{
			for (int slot = 0; slot < items.length; slot++)
			{
				item = items[slot];
				if (item != null && itemID == item.getId())
				{
					return slot;
				}
			}
		}
		return -1;
	}

	public static void flick(HotKeysPlugin plugin)
	{
		Client client = plugin.getClient();
		int firstClickDelay = getRandomIntBetweenRange(1, 30);
		int secondClickDelay = getRandomIntBetweenRange(70, 200);
		if (client.getVar(Varbits.QUICK_PRAYER) == 1)
		{
			addEntry(Entries.quickPrayer, plugin, firstClickDelay);
			addEntry(Entries.quickPrayer, plugin, secondClickDelay);

		}
		else if (client.isPrayerActive(Prayer.PROTECT_FROM_MAGIC))
		{
			addEntry(PrayerMenuActions.getEntry(Prayer.PROTECT_FROM_MAGIC), plugin, firstClickDelay);
			addEntry(PrayerMenuActions.getEntry(Prayer.PROTECT_FROM_MAGIC), plugin, secondClickDelay);
		}
		else if (client.isPrayerActive(Prayer.PROTECT_FROM_MISSILES))
		{
			addEntry(PrayerMenuActions.getEntry(Prayer.PROTECT_FROM_MISSILES), plugin, firstClickDelay);
			addEntry(PrayerMenuActions.getEntry(Prayer.PROTECT_FROM_MISSILES), plugin, secondClickDelay);
		}
		else if (client.isPrayerActive(Prayer.PROTECT_FROM_MELEE))
		{
			addEntry(PrayerMenuActions.getEntry(Prayer.PROTECT_FROM_MELEE), plugin, firstClickDelay);
			addEntry(PrayerMenuActions.getEntry(Prayer.PROTECT_FROM_MELEE), plugin, secondClickDelay);
		}
		else if (client.isPrayerActive(Prayer.AUGURY))
		{
			addEntry(PrayerMenuActions.getEntry(Prayer.AUGURY), plugin, firstClickDelay);
			addEntry(PrayerMenuActions.getEntry(Prayer.AUGURY), plugin, secondClickDelay);
		}
		else if (client.isPrayerActive(Prayer.RIGOUR))
		{
			addEntry(PrayerMenuActions.getEntry(Prayer.RIGOUR), plugin, firstClickDelay);
			addEntry(PrayerMenuActions.getEntry(Prayer.RIGOUR), plugin, secondClickDelay);
		}
		else if (client.isPrayerActive(Prayer.PIETY))
		{
			addEntry(PrayerMenuActions.getEntry(Prayer.PIETY), plugin, firstClickDelay);
			addEntry(PrayerMenuActions.getEntry(Prayer.PIETY), plugin, secondClickDelay);
		}
	}

	private static void addEntries(List<MenuEntry> menuEntries, HotKeysPlugin plugin)
	{
		plugin.getMenuEntrySwapHandler().addEntries(menuEntries);
	}

	private static void addEntry(MenuEntry menuEntry, HotKeysPlugin plugin)
	{
		plugin.getMenuEntrySwapHandler().addEntry(menuEntry);
	}

	private static void addEntry(MenuEntry menuEntry, HotKeysPlugin plugin, int delay)
	{
		plugin.getMenuEntrySwapHandler().addEntry(menuEntry, delay);
	}

	public static WidgetInfo checkleftClickCastPress(HotKeysScript script)
	{
		for (ImmutablePair<String, String> statement : script.getStatements())
		{
			if (statement.getKey().equals("LEFTCLICKCAST"))
			{
				return SpellMenuActions.valueOf(statement.getValue()).getInfo();
			}
		}
		return null;
	}

	public static boolean shouldLeftClickCast(HotKeysPlugin plugin)
	{
		for (HotKeysScript script : plugin.getScripts())
		{
			for (ImmutablePair<String, String> statement : script.getStatements())
			{
				if (statement.getKey().equals("LEFTCLICKCAST"))
				{
					return true;
				}
			}
		}
		return false;
	}

	public static void setSelectedSpell(WidgetInfo widgetInfo, HotKeysPlugin plugin)
	{
		Widget widget = plugin.getClient().getWidget(widgetInfo);
		plugin.getClient().setSelectedSpellName(widget.getName());
		plugin.getClient().setSelectedSpellWidget(widget.getId());
		plugin.getClient().setSelectedSpellChildIndex(-1);
		plugin.setLastSpellSelected(widgetInfo);
	}

	private static int getRandomIntBetweenRange(int min, int max)
	{
		return (int) ((Math.random() * ((max - min) + 1)) + min);
	}

	private static int getGroupsId(int itemID, HotKeysPlugin plugin)
	{
		for (List<Integer> group : MultipleIdItems.multiIdItemsGroup)
		{
			if (group.contains(itemID))
			{
				for (Integer id : group)
				{
					if (findItem(id, plugin) != -1)
					{
						return id;
					}
				}
			}
		}
		return itemID;
	}

	private static List<MenuEntry> getDropEntries(int[] itemIds, HotKeysPlugin plugin)
	{
		Item item = null;
		Item[] items = null;
		final ItemContainer itemContainer = plugin.getClient().getItemContainer(InventoryID.INVENTORY);
		if (itemContainer == null)
		{
			return null;
		}
		items = itemContainer.getItems();

		List<MenuEntry> arrayToReturn = new ArrayList<>();

		for (int slot = items.length - 1; slot >= 0; slot--)
		{
			item = items[slot];
			if (item == null)
			{
				continue;
			}
			ItemDefinition itemDefinition = plugin.getItemManager().getItemDefinition(item.getId());
			if (IntStream.of(itemIds).boxed().collect(Collectors.toList()).contains(itemDefinition.getId()))
			{
				arrayToReturn.add(new MenuEntry("Drop", "<col=ff9040>" + itemDefinition.getName() + "</col>", item.getId(), 37, slot, 9764864, false));
			}
		}
		return arrayToReturn;
	}

	private static int[] stringToIntArray(String string)
	{
		return Arrays.stream(string.split(",")).map(String::trim).mapToInt(Integer::parseInt).toArray();
	}

	public static String getInventoryIdText(HotKeysPlugin plugin)
	{
		StringBuilder stringBuilder = new StringBuilder();
		List<WidgetItem> invItems = new InventoryWidgetItemQuery().result(plugin.getClient()).list;
		for (WidgetItem item : invItems)
		{
			ItemDefinition itemDefinition = plugin.getItemManager().getItemDefinition(item.getId());
			stringBuilder.append(itemDefinition.getName() + "    " + itemDefinition.getId() + "\n");
		}
		return stringBuilder.toString(); //finish this implementation!!!!!!!!!!!!!!!!!!!!!
	}

	private static boolean evaluateConditional(String statement, HotKeysPlugin plugin)
	{
		if (statement.contains("&&") || statement.contains("||"))
		{
			int andIndex = statement.indexOf("&&");
			int orIndex = statement.indexOf("||");
			if (andIndex == -1)
			{
				String firstStatement = statement.substring(0, orIndex - 1);
				String nextStatement = statement.substring(orIndex + 2);
				return evaluateConditional(firstStatement, plugin) || evaluateConditional(nextStatement, plugin);
			}
			else if (orIndex == -1)
			{
				String firstStatement = statement.substring(0, andIndex - 1);
				String nextStatement = statement.substring(andIndex + 2);
				return evaluateConditional(firstStatement, plugin) && evaluateConditional(nextStatement, plugin);
			}
			else if (andIndex > orIndex)
			{
				String firstStatement = statement.substring(0, orIndex - 1);
				String nextStatement = statement.substring(orIndex + 2);
				return evaluateConditional(firstStatement, plugin) || evaluateConditional(nextStatement, plugin);
			}
			else
			{
				String firstStatement = statement.substring(0, andIndex - 1);
				String nextStatement = statement.substring(andIndex + 2);
				return evaluateConditional(firstStatement, plugin) && evaluateConditional(nextStatement, plugin);
			}
		}

		//handle ! to negate

		String arg = ObjectUtils.defaultIfNull(StringUtils.substringBetween(statement, "(", ")"), "");
		if (statement.contains("isEquipped"))
		{
			return isEquipped(arg, plugin);
		}
		else if (statement.contains("inventoryFull"))
		{
			return inventoryFull(plugin);
		}
		else if (statement.contains("amountInInventory"))
		{
			return amountInInventory(arg, plugin);//id,min,max
		}
		else if (statement.contains("currentLevel"))
		{
			return currentLevel(arg, plugin);//skill,min,max
		}
		else if (statement.contains("specRemaining"))
		{
			return specRemaining(arg, plugin);//min,max
		}
		else if (statement.contains("isEquippedOpponent"))
		{
			return isEquippedOpponent(arg, plugin);
		}
		return false;
	}

	private static boolean isEquipped(String id, HotKeysPlugin plugin)
	{
		Client client = plugin.getClient();
		int itemId;
		try
		{
			itemId = Integer.parseInt(id);
			log.info("itemid " + itemId);
		}
		catch (NumberFormatException ex)
		{
			return false;
		}
		Item[] items = null;
		final ItemContainer itemContainer = client.getItemContainer(InventoryID.EQUIPMENT);
		if (itemContainer == null)
		{
			return false;
		}
		items = itemContainer.getItems();
		for (Item item : items)
		{
			if (item.getId() == itemId)
			{
				return true;
			}
		}
		return false;
	}

	private static boolean inventoryFull(HotKeysPlugin plugin)
	{
		Client client = plugin.getClient();
		Item[] items = null;
		final ItemContainer itemContainer = client.getItemContainer(InventoryID.INVENTORY);
		if (itemContainer == null)
		{
			return false;
		}
		items = itemContainer.getItems();
		if (items.length == 28)
		{
			return true;
		}
		return false;
	}

	private static boolean amountInInventory(String args, HotKeysPlugin plugin)//id,min,max
	{
		Client client = plugin.getClient();
		int[] argsArray = stringToIntArray(args);
		if (argsArray.length != 3)
		{
			return false;
		}
		Item[] items;
		final ItemContainer itemContainer = client.getItemContainer(InventoryID.INVENTORY);
		if (itemContainer == null)
		{
			return false;
		}
		items = itemContainer.getItems();
		int id = argsArray[0];
		int min = argsArray[1];
		int max = argsArray[2];
		int counter = 0;
		for (Item i : items)
		{
			if (i.getId() == id)
			{
				counter += i.getQuantity();
			}
		}
		if (counter >= min && counter <= max)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	private static boolean currentLevel(String args, HotKeysPlugin plugin)
	{
		Client client = plugin.getClient();
		String[] splitted = args.split(",");
		if (splitted.length != 3)
		{
			return false;
		}
		Skill skill = Skill.valueOf(splitted[0]);
		int min = -1;
		int max = -1;
		try
		{
			min = Integer.parseInt(splitted[1]);
			max = Integer.parseInt(splitted[2]);
		}
		catch (NumberFormatException ex)
		{
			return false;
		}
		int level = client.getBoostedSkillLevel(skill);
		if (level >= min && level <= max)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	private static boolean specRemaining(String args, HotKeysPlugin plugin)
	{
		Client client = plugin.getClient();
		String[] splitted = args.split(",");
		if (splitted.length != 2)
		{
			return false;
		}
		int min = -1;
		int max = -1;
		try
		{
			min = Integer.parseInt(splitted[0]);
			max = Integer.parseInt(splitted[1]);
		}
		catch (NumberFormatException ex)
		{
			return false;
		}
		int spec = client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT) / 10;
		if (spec >= min && spec <= max)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	private static boolean isEquippedOpponent(String id, HotKeysPlugin plugin)
	{
		int itemId;
		try
		{
			itemId = Integer.parseInt(id);
			log.info("itemid " + itemId);
		}
		catch (NumberFormatException ex)
		{
			return false;
		}
		final PlayerManager playerManager = plugin.getPlayerManager();
		if (!(plugin.getLastTarget() instanceof Player))
		{
			log.info("Returned because last target not instanceof Player");
			return false;
		}
		final Player opponent = (Player) plugin.getLastTarget();
		final PlayerContainer opponentContainer = playerManager.getPlayer(opponent);
		if (opponentContainer == null)
		{
			log.info("Returned because opponentContainer was null");
			return false;
		}
		for (KitType kitType : KitType.values())
		{
			log.info(kitType.getName());
			PlayerAppearance opponentAppearance = opponentContainer.getPlayer().getPlayerAppearance();
			if (kitType == KitType.RING || kitType == KitType.AMMUNITION || opponentAppearance == null)
			{
				continue;
			}

			final int idToCheck = opponentAppearance.getEquipmentId(kitType);
			log.info("idToCheck" + idToCheck);
			if (itemId == idToCheck)
			{
				return true;
			}
		}
		return false;
	}
}