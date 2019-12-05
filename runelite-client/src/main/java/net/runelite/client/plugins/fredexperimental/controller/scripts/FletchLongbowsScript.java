package net.runelite.client.plugins.fredexperimental.controller.scripts;

import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.Locatable;
import net.runelite.api.LocatableQueryResults;
import net.runelite.api.Point;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.fred.api.controllers._Banking;
import net.runelite.client.plugins.fred.api.controllers._Inventory;
import net.runelite.client.plugins.fred.api.controllers._Vision;
import net.runelite.client.plugins.fred.api.other.Pair;
import net.runelite.client.plugins.fred.api.wrappers._Area;
import net.runelite.client.plugins.fred.api.wrappers._Item;
import net.runelite.client.plugins.fred.api.wrappers._TilePath;
import net.runelite.client.plugins.fredexperimental.controller.ControllerPlugin;
import net.runelite.client.plugins.fredexperimental.controller.Script;
import net.runelite.client.plugins.fredexperimental.controller.listeners.InventoryItemsListener;
import net.runelite.client.plugins.fredexperimental.striker.Striker;
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity;
import net.runelite.client.ui.overlay.components.table.TableAlignment;
import net.runelite.client.ui.overlay.components.table.TableComponent;

import static net.runelite.api.ItemID.KNIFE;
import static net.runelite.api.ItemID.MAPLE_LOGS;
import static net.runelite.api.ItemID.MAPLE_LONGBOW_U;
import static net.runelite.api.ItemID.MAPLE_SHORTBOW_U;

@Slf4j
public class FletchLongbowsScript extends Script implements InventoryItemsListener
{
	private Set<Integer> fletchedItemsIds = null;
	private boolean haveLogs = false;
	private boolean haveKnife = false;
	private boolean haveFletchedItems = false;

	//control flags.
	//Set these in events.
	//Use these to pick next action.
	boolean needsBank = false;
	boolean needsCraft = false;

	@Override
	public void init(ControllerPlugin context)
	{
		super.init(context);
		fletchedItemsIds = ImmutableSet.of(MAPLE_LONGBOW_U, MAPLE_SHORTBOW_U);
		haveLogs = false;
		haveKnife = false;
		haveFletchedItems = false;

		//flags
		needsBank = false;
		needsCraft = false;
		onInventoryItemsChanged(context.getInventoryItems());
	}

	@Override
	public void cleanup()
	{
		super.cleanup();
		fletchedItemsIds = null;
		haveLogs = false;
		haveKnife = false;
		haveFletchedItems = false;

		//flags
		needsBank = false;
		needsCraft = false;
	}

	@Override
	public void onInventoryItemsChanged(List<_Item> inventory)
	{
		//compare current inventory w/ what it was to determine deltas
		//cache values we need for setting flags
		boolean tempHaveLogs = haveLogs;

		//read in new values
		haveLogs = inventory.stream().anyMatch(f -> f.getId() == MAPLE_LOGS);
		haveKnife = inventory.stream().anyMatch(f -> f.getId() == KNIFE);
		haveFletchedItems = inventory.stream().anyMatch(f -> fletchedItemsIds.contains(f.getId()));

		if (!haveLogs)
		{
			//ran out of molten glass
			log.debug("Out of Logs");
			needsBank = true;
		}
		if (!haveKnife)
		{
			needsBank = true;
		}
		if (!needsCraft && haveLogs && haveKnife && !haveFletchedItems)
		{
			needsCraft = true;
		}
	}

	@Override
	public Optional<Pair<String, Runnable>> getNextAction(ControllerPlugin ctx)
	{
		_Banking bank = ctx.getBankController();
		_Inventory inventory = ctx.getInventoryController();
		_Vision vision = ctx.getVisionController();
		if (bank.isBankOpen() && haveFletchedItems)
		{
			Runnable r = () ->
			{
				fletchedItemsIds.forEach(bank::doDeposit);
				Striker.delayMS(600, 1200);
			};
			return Optional.of(Pair.of("Deposit fletched", r));
		}
		if (bank.isBankOpen() && ctx.getFreeInvSpaces() > 0)
		{
			Runnable r = () ->
			{
				bank.doWithdraw(MAPLE_LOGS);
				Striker.delayMS(600, 1200);
			};
			return Optional.of(Pair.of("Withdraw maple logs", r));
		}
		if (bank.isBankOpen())
		{
			Runnable r = () ->
			{
				bank.doCloseBank();
				Striker.delayMS(250, 400);
			};
			needsCraft  = true;
			needsBank = false;
			return Optional.of(Pair.of("Closing bank", r));
		}
		if (needsBank && bank.isNearBank())
		{
			needsBank = false;
			Runnable r = () ->
			{
				bank.doOpenBank();
				Striker.delayMS(250, 400);
			};
			return Optional.of(Pair.of("Opening bank", r));
		}
		if (!bank.isBankOpen() && haveKnife && haveLogs && needsCraft)
		{
			//get inventory widgets.
//			Point pos = vision.getClickPoint(item.getCanvasBounds(true));
//			Striker.clickMouse(pos, 1);
//			Striker.delayMS(25, 100);
			Runnable r = () ->
			{
				log.debug("fuck");
				boolean temp = inventory.doClick(KNIFE);
				Striker.delayMS(250, 400);
				if (temp)
				{
					temp = inventory.doClick(MAPLE_LOGS);
					Striker.delayMS(250, 400);
				}
				if (temp)
				{
					needsCraft = false;
				}
			};
			return Optional.of(Pair.of("Use knife", r));
		}

		Widget craftingOptionsContainer = ctx.getClient().getWidget(270, 13);

		if (craftingOptionsContainer != null && !craftingOptionsContainer.isSelfHidden())
		{
			craftingOptionsContainer.revalidate();
			Widget[] options = craftingOptionsContainer.getStaticChildren();

			if (options != null)
			{
				Widget poweredOrb = null;
				log.debug("options: {}", options.length);
				for (Widget w : options)
				{
					log.debug("w -> group: {}, id: {}, hidden: {}, name: {}", WidgetInfo.TO_GROUP(w.getId()), WidgetInfo.TO_CHILD(w.getId()), w.isHidden(), w.getName());
					if (!w.isSelfHidden() && w.getName().equals("<col=ff9040>Maple longbow</col>"))
					{
						poweredOrb = w;
						break;
					}
				}
				if (poweredOrb != null)
				{
					final Widget target = poweredOrb;
					target.revalidate();
					Runnable r = () ->
					{
						target.revalidate();
						Striker.delayMS(100);
						Point clickPoint = vision.getClickPoint(target.getBounds());
						if (clickPoint != null)
						{
							Striker.clickMouse(clickPoint, 1);
							Striker.delayMS(1000, 2000);
						}
					};
					return Optional.of(Pair.of("Select widget", r));
				}
			}
		}

		return Optional.empty();
	}

	@Override
	public LayoutableRenderableEntity getWindowInfo()
	{
		TableComponent tableComponent = new TableComponent();
		tableComponent.setColumnAlignments(TableAlignment.LEFT, TableAlignment.RIGHT);
		tableComponent.addRow("Logs", haveLogs + "");
		tableComponent.addRow("Fletched", haveFletchedItems + "");
		tableComponent.addRow("Knife", haveKnife + "");
		tableComponent.addRow("Need Bank", needsBank + "");
		tableComponent.addRow("Need Craft", needsCraft + "");
		return tableComponent;
	}

	@Override
	public Set<_Area> debugGetAreas(ControllerPlugin ctx)
	{
		return ImmutableSet.of();
	}

	@Override
	public Set<_TilePath> debugGetTilePaths(ControllerPlugin ctx)
	{
		return ImmutableSet.of();
	}

	@Override
	public Set<GameObject> debugGetGameObject(ControllerPlugin ctx)
	{
		_Vision vision = ctx.getVisionController();
		Client client = ctx.getClient();
		GameObjectQuery objsQ = new GameObjectQuery();
		objsQ.filter(Objects::nonNull).filter(f -> f.getConvexHull() != null).filter(vision::isGameObjectOnScreen);
		LocatableQueryResults<GameObject> results = objsQ.result(client);

		List<Actor> actors1 = new ArrayList<>();
		actors1.addAll(ctx.getClient().getNpcs());
		actors1.addAll(ctx.getClient().getPlayers());
		Set<Locatable> actors = new HashSet<>(actors1);

		List<GameObject> gObjs = results.stream().filter(f -> !actors.contains(f)).collect(Collectors.toList());
		return ImmutableSet.copyOf(gObjs);
	}
}
