package net.runelite.client.plugins.fredexperimental.smelter;

import com.google.common.collect.ImmutableList;
import com.google.inject.Singleton;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.Arrays;
import java.util.concurrent.ScheduledFuture;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.fred.api.controllers._Banking;
import net.runelite.client.plugins.fred.api.wrappers._Item;
import net.runelite.client.plugins.fred.api.wrappers._Tile;
import net.runelite.client.plugins.fred.api.wrappers._WidgetItem;
import net.runelite.client.plugins.fredexperimental.smelter.SmelterItem._SmelterItem;
import net.runelite.client.plugins.fredexperimental.striker.Striker;
import net.runelite.client.plugins.fredexperimental.striker.StrikerUtils;

@Slf4j
@Singleton
public class SmeltingController
{
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private ScheduledFuture<?> task = null;

	private final SmelterPlugin plugin;
	private final _Banking bankController;

	@Inject
	public SmeltingController(SmelterPlugin plugin, _Banking bankController)
	{
		this.plugin = plugin;
		this.bankController = bankController;
	}

	private void tick()
	{
		Client client = plugin.getClient();
		SmelterLocation location = plugin.getLocation();
		SmelterItem recipe = plugin.getProducing();
		if (client == null || location == null || recipe == null)
		{
			log.error("wtf?");
			disable();
			return;
		}

		if (!location.getBoundsArea().contains(_Tile.ofPlayer(client)))
		{
			disable();
			return;
		}

		Player player = client.getLocalPlayer();
		if (player == null)
		{
			disable();
			return;
		}

		if (plugin.isSmelting())
		{
			return;
		}

		_Tile serverPos = _Tile.ofPlayer(client);
		_Tile clientPos = _Tile.fromWorld(WorldPoint.fromLocal(client, player.getLocalLocation()));
		log.debug("clientPos {} -> serverPos {}", clientPos, serverPos);
		if (serverPos == null)
		{
			return;
		}

		if (!serverPos.equals(clientPos))
		{
			log.debug("Moving");
			return;
		}
		else
		{
			Striker.delayMS(200);
		}

		ImmutableList<_Item> inventoryItems = plugin.getInventoryItems();
		_SmelterItem[] inputs = recipe.getIngredients().toArray(new _SmelterItem[0]);
		int[] inputQtys = new int[inputs.length];
		for (int i = 0; i < inputs.length; i++)
		{
			final _SmelterItem input = inputs[i];
			inputQtys[i] = (int) inventoryItems.stream().filter(f -> input.getId() == f.getId()).count();
		}
		_SmelterItem[] outputs = recipe.getProducts().toArray(new _SmelterItem[0]);
		int[] outputQtys = new int[outputs.length];
		for (int i = 0; i < outputs.length; i++)
		{
			final _SmelterItem output = outputs[i];
			outputQtys[i] = (int) inventoryItems.stream().filter(f -> output.getId() == f.getId()).count();
		}

		_SmelterItem[] catalysts = recipe.getCatalysts().toArray(new _SmelterItem[0]);
		int[] catalystQtys = new int[catalysts.length];
		for (int i = 0; i < catalysts.length; i++)
		{
			final _SmelterItem catalyst = catalysts[i];
			catalystQtys[i] = (int) inventoryItems.stream().filter(f -> f.getId() == catalyst.getId()).count();
		}

		Widget furnaceWidgets = client.getWidget(270, 13);
		Widget furnaceTargetWidget = null;
		boolean furnaceOpen = false;
		if (furnaceWidgets != null && !furnaceWidgets.isHidden())
		{
			furnaceOpen = true;
			Widget[] furnaceOptions = furnaceWidgets.getStaticChildren();
			log.debug("furnaceOptions {} {}", furnaceOptions == null ? "null" : "valid", furnaceOptions != null ? furnaceOptions.length : -1);
			if (furnaceOptions != null)
			{
				for (Widget w : furnaceOptions)
				{
					log.debug("w -> group: {}, id: {}, hidden: {}, name: {}", WidgetInfo.TO_GROUP(w.getId()), WidgetInfo.TO_CHILD(w.getId()), w.isHidden(), w.getName());
					if (!w.isHidden() && w.getName().equals("<col=ff9040>Molten glass</col>"))
					{
						furnaceTargetWidget = w;
						break;
					}
				}
			}
		}

		if (Arrays.stream(inputQtys).anyMatch(f -> f == 0) || Arrays.stream(catalystQtys).anyMatch(f -> f == 0))
		{
			log.debug("input {}", inputQtys);
			log.debug("output {}", outputQtys);
			log.debug("catalyst {}", catalystQtys);
			//if (bankOpen)
			if (bankController.isBankOpen())
			{
				if (Arrays.stream(outputQtys).anyMatch(f -> f > 0))
				{
					for (_SmelterItem output : outputs)
					{
						int oCount = (int)inventoryItems.stream().filter(f -> f.getId() == output.getId()).count();
						if (oCount > 0)
						{
							_WidgetItem tItem = bankController.getInventoryItem(output.getId());
							if (tItem != null)
							{
								log.debug("deposit item");
								bankController.doDeposit(tItem);
							}
							else
							{
								log.debug("tItem was null");
							}
						}
					}
				}
				else if (Arrays.stream(catalystQtys).anyMatch(f -> f == 0))
				{

					log.debug("Need to withdraw catalyst");
				}
				else
				{
					for (_SmelterItem input : inputs)
					{
						int iCount = (int)inventoryItems.stream().filter(f -> f.getId() == input.getId()).count();
						if (iCount == 0)
						{
							_WidgetItem tItem = bankController.getBankItem(input.getId());
							if (tItem == null || !bankController.isBankItemInView(tItem))
							{
								log.debug("tItem: {}", tItem);
								disable();
								break;
							}
							else
							{
								log.debug("withdraw");
								bankController.doWithdraw(tItem);
							}
						}
						else if (iCount == input.getQty())
						{
							continue;
						}
						else
						{
							log.debug("iCount {} != input.getQty {}", iCount, input.getQty());
							disable();
							break;
						}
					}
				}
			}
			else
			{
				if (bankController.isNearBank())
				{
					bankController.doOpenBank();
				}
				else
				{
					log.debug("Cant click bank");
					disable();
				}
			}
		}
		else if (bankController.isBankOpen())
		{
			bankController.doCloseBank();
			log.debug("Need to close bank!!!");
		}
		else
		{
			if (furnaceOpen)
			{
				if (furnaceTargetWidget != null && !furnaceTargetWidget.isHidden())
				{
					log.debug("Need to click recipe!");
					Striker.clickMouse(StrikerUtils.getClickPoint(furnaceTargetWidget.getBounds()), 1);
					Striker.delayMS(1000);
				}
				else
				{
					log.debug("No recipe tile found");
					disable();
				}
			}
			else
			{
				GameObject furnace = plugin.getFurnace();
				Shape furnaceHull = furnace != null ? furnace.getConvexHull() : null;
				Rectangle furnaceBounds = furnaceHull != null ? furnaceHull.getBounds() : null;
				Rectangle bounds = client.getWidget(164, 5) != null ? client.getWidget(164, 5).getBounds() : null;

				if (bounds != null && furnaceBounds != null && bounds.contains(furnaceBounds))
				{
					log.debug("Need to click furnace");
					Striker.clickMouse(StrikerUtils.getClickPoint(furnaceBounds), 1);
					Striker.delayMS(200);
				}
				else
				{
					log.debug("Cant click furnace: {}, furnaceHull: {}, furnaceBounds: {}, bounds: {}", furnace, furnaceHull, furnaceBounds, bounds);
					disable();
				}
			}
		}
	}

	public boolean isRunning()
	{
		return task != null && !task.isCancelled();
	}

	public boolean enable()
	{
		if (task == null || task.isCancelled())
		{
			task = Striker.scheduleWithFixedDelay(this::tick, 250, 600);
			return true;
		}
		return false;
	}

	public boolean disable()
	{
		if (task != null)
		{
			if (!task.isCancelled())
			{
				task.cancel(true);
			}
			task = null;
			return true;
		}
		return false;
	}
}
