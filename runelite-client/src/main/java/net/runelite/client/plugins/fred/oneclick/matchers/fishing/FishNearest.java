package net.runelite.client.plugins.fred.oneclick.matchers.fishing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.InventoryID;
import net.runelite.api.MenuEntry;
import net.runelite.api.MenuOpcode;
import net.runelite.api.NPC;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.queries.NPCQuery;
import net.runelite.client.plugins.fred.api.wrappers._Item;
import net.runelite.client.plugins.fred.api.wrappers._ItemContainer;
import net.runelite.client.plugins.fred.oneclick.util.MenuEntryMatcher;
import org.apache.commons.lang3.ArrayUtils;

import static net.runelite.api.MenuOpcode.ITEM_USE;

@Singleton
@Slf4j
public class FishNearest extends MenuEntryMatcher
{
	private List<_Item> fishingTools = new ArrayList<>();
	private _ItemContainer inventory = null;

	private Map<_FishTool, NPC> spotsNPC = new HashMap<>();
	private _FishTool tool = null;

	@Override
	public void init()
	{
		super.init();
		spotsNPC.clear();
		fishingTools.clear();
	}

	@Override
	public void onGameTick()
	{
		spotsNPC.clear();
		tool = null;
		if (inventory != null)
		{
			List<_FishTool> activeScans = new ArrayList<>();
			for (_Item fishingTool : fishingTools)
			{
				Optional<_FishTool> toolInfo = Arrays.stream(_FishTool.values()).filter(f -> ArrayUtils.contains(f.getToolIds(), fishingTool.getId())).findFirst();
				if(!toolInfo.isPresent())
				{
					continue;
				}
				activeScans.add(toolInfo.get());
			}
			for (_FishTool activeScan : activeScans)
			{
				NPC temp = new NPCQuery().idEquals(activeScan.getSpotIds()).result(client).nearestTo(client.getLocalPlayer());
				spotsNPC.put(activeScan, temp);
			}
		}
	}

	@Override
	public void onInventoryChanged(List<_Item> items)
	{
		super.onInventoryChanged(items);
		inventory = _ItemContainer.create(client, InventoryID.INVENTORY);

		if (inventory != null)
		{
			fishingTools.clear();
			fishingTools.addAll(inventory.getItems(Arrays.stream(_FishTool.values()).flatMapToInt(f -> Arrays.stream(f.getToolIds())).toArray()));
			log.debug("Have {} fishing tool in inventory.", fishingTools.size());
			log.debug("fishtool -> {}", fishingTools.size() > 0 ? fishingTools.get(0) : null);
		}
	}

	@Override
	public boolean addedMatches(MenuEntryAdded added)
	{
		Optional<_FishTool> tool_ = fishingTools.stream().filter(f -> f.getId() == added.getIdentifier()).findFirst().flatMap(k -> Arrays.stream(_FishTool.values()).filter(j -> ArrayUtils.contains(j.getToolIds(), k.getId())).findFirst());

		if (inventory == null || spotsNPC.size() == 0 || added.getOpcode() != ITEM_USE.getId() || !tool_.isPresent())
		{
			return false;
		}
		tool = tool_.get();
		if (fishingTools.stream().anyMatch(f->f.getId() == added.getIdentifier() && added.getParam0() == f.getIdx() && ArrayUtils.contains(tool.getToolIds(), f.getId())) && spotsNPC.containsKey(tool) && spotsNPC.get(tool) != null)
		{
			return true;
		}
		log.debug("testing failed!");
		return false;
	}


	@Override
	public MenuEntry generate(MenuEntryAdded added)
	{
		if (tool != null)
		{
			NPC npc = spotsNPC.get(tool);
			if (npc != null)
			{
				return new MenuEntry(
					"<col=0090f6>FISH",
					"<col=ffff00>" + npc.getName(),
					npc.getIndex(),
					MenuOpcode.NPC_FIRST_OPTION.getId(),
					0,
					0,
					false
				);
			}
		}
		return null;
	}

	@Override
	public boolean clickedMatches(MenuOptionClicked clicked)
	{
		return clicked.getOption().equals("<col=0090f6>FISH");
	}

	@Override
	public void clicked(MenuOptionClicked clicked)
	{
		if (clicked.getOption().equals("<col=0090f6>FISH"))
		{
			clicked.setIdentifier(spotsNPC.get(tool).getIndex());
			clicked.setOption(tool.getOption());
			log.debug("{}", clicked.toString());
			tool = null;
		}
		else
		{
			clicked.consume();
		}
	}
}
