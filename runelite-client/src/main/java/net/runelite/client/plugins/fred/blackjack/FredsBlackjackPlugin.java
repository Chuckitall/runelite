/*
 * Copyright (c) 2018 gazivodag <https://github.com/gazivodag>
 * Copyright (c) 2019 lucwousin <https://github.com/lucwousin>
 * Copyright (c) 2019 infinitay <https://github.com/Infinitay>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.fred.blackjack;


import com.google.inject.Provides;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.MenuOpcode;
import net.runelite.api.NPC;
import net.runelite.api.Skill;
import net.runelite.api.Varbits;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ClientTick;
import net.runelite.client.events.ConfigChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.util.Text;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.flexo.Flexo;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.fred.util.ExtUtils;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.HotkeyListener;
import org.apache.commons.lang3.RandomUtils;

import javax.inject.Inject;
import javax.inject.Singleton;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static net.runelite.api.MenuOpcode.MENU_ACTION_DEPRIORITIZE_OFFSET;
import static net.runelite.api.MenuOpcode.NPC_FIRST_OPTION;

/**
 * Authors gazivodag longstreet
 */
@PluginDescriptor(
		name = "Fred's Blackjack",
		description = "Allows for one - click blackjacking, both knocking out and pickpocketing",
		tags = {"fred", "flexo", "blackjack", "thieving"},
	type = PluginType.FRED
)
@Singleton
@Slf4j
public class FredsBlackjackPlugin extends Plugin
{
	private static final String SUCCESS_BLACKJACK = "You smack the bandit over the head and render them unconscious.";
	private static final String FAILED_BLACKJACK = "Your blow only glances off the bandit's head.";
	private static final String ATTEMPT_PICKPOCKET = "You attempt to pick the Menaphite's pocket.";
	private static final String ATTEMPT_PICKPOCKET2 = "You need to empty your coin pouches before you can continue pickpocketing.";
	private static final String ATTEMPT_PICKPOCKET3 = "You don't have enough inventory space to do that.";
	private static final String ATTEMPT_PICKPOCKET4 = "You attempt to pick the bandit's pocket.";
	private static final String NOT_HIDDEN_BLACKJACK = "Perhaps I shouldn't do this here, I think another Menaphite will see me.";
	private static final String CANT_REACH = "I can't reach that!";
	private static final String NOT_IN_COMBAT1 = "You can't do this during combat.";
	private static final String NOT_IN_COMBAT2 = "You can't pickpocket during combat.";

	private static final String TAG = "Mark";
	private static final String UNTAG = "Un - Mark";

	private static final String[] targets = {"bandit", "menaphite thug"};

	private static final int FOOD_ID = ItemID.JUG_OF_WINE;
	private int food_idx = -1;

	private static final int POLLNIVNEACH_REGION = 13358;
	private long nextKnockOutTick = 0;
	int seqFailed = 0;
	private int oldState = -1;
	int state = 0; //0 knock - out/pickpocket, 1 pickpocket low health, 2 talk - to because health is very low, 3 runaway

	@Inject
	private Client client;
	@Inject
	private BlackjackConfig config;
	@Inject
	private EventBus eventBus;
	@Inject
	private BlackjackOverlay overlay;
	@Inject
	private BlackjackSceneOverlay sceneOverlay;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private KeyManager keyManager;

	@Getter
	private Rectangle clickArea;

	@Inject
	private FredBlackjackInput inputListener;

	private ScheduledExecutorService threadExec = null;
	private ScheduledFuture scriptFuture = null;
	private ScheduledFuture mouseFuture = null;
	private ExtUtils utils;

	private HotkeyListener toggleScriptKeybind;
	private HotkeyListener triggerDebug1Keybind;

	@Setter(AccessLevel.PACKAGE)
	private boolean hotKeyPressed = false;

	@Getter(AccessLevel.PACKAGE)
	private boolean moveMouse = false;
	@Getter(AccessLevel.PACKAGE)
	private boolean clickMouse = false;
	@Getter(AccessLevel.PACKAGE)
	private boolean fuck = false;

	@Getter(AccessLevel.PACKAGE)
	private Flexo flexo;

	private int healthMin;
	private int healthMax;

	private int mode = 0;//0 we knock out, 1 we pickpocket, 2 we eat, 3 we examine, 4 we run away
	private int count = 0;
	private int rollover = 5;

	@Getter(AccessLevel.PACKAGE)
	private NPC target = null;

	@Provides
	BlackjackConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BlackjackConfig.class);
	}

	private void toggleMoveMouse(boolean nVal)
	{
		if (moveMouse == nVal)
		{
			return;
		}
		moveMouse = nVal;
		if (!moveMouse)
		{
			mouseFuture.cancel(false);
		}
		else
		{
			mouseFuture = threadExec.scheduleAtFixedRate(() ->
			{
				if (target != null && clickArea != null)
				{
					if (!clickArea.contains(client.getMouseCanvasPosition().asNativePoint()))
					{

						java.awt.Point cp = utils.getClickPoint(clickArea).asNativePoint();
						log.debug("cp: {}", cp);
						flexo.mouseMove(cp);
					}
				}
 			}, 0, 20, MILLISECONDS);
		}
	}

	@Override
	protected void startUp() throws Exception
	{
		threadExec = Executors.newSingleThreadScheduledExecutor();
		try
		{
			flexo = new Flexo();
		}
		catch (AWTException e)
		{
			e.printStackTrace();
		}
		this.utils = new ExtUtils(client, flexo);
		this.fuck = false;
		this.mode = 0;
		this.state = 0;
		this.oldState = -1;
		this.clickArea = null;

		toggleScriptKeybind = new HotkeyListener(config::toggleScript)
		{
			@Override
			public void hotkeyPressed()
			{
				toggleMoveMouse(!moveMouse);
			}
		};
		triggerDebug1Keybind = new HotkeyListener(config::debugKey1)
		{
			@Override
			public void hotkeyPressed()
			{
				clickMouse = !clickMouse;
			}
		};
		keyManager.registerKeyListener(inputListener);
		keyManager.registerKeyListener(toggleScriptKeybind);
		keyManager.registerKeyListener(triggerDebug1Keybind);
		addSubscriptions();
		updateConfig();
		overlayManager.add(overlay);
		overlayManager.add(sceneOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		if (scriptFuture != null && !scriptFuture.isCancelled())
		{
			scriptFuture.cancel(true);
			scriptFuture = null;
		}
		if (mouseFuture != null && !mouseFuture.isCancelled())
		{
			mouseFuture.cancel(true);
			mouseFuture = null;
		}

		keyManager.unregisterKeyListener(inputListener);
		keyManager.unregisterKeyListener(toggleScriptKeybind);
		keyManager.unregisterKeyListener(triggerDebug1Keybind);
		eventBus.unregister(this);
		overlayManager.remove(overlay);
		overlayManager.remove(sceneOverlay);
	}

	private void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals("fredblackjack"))
		{
			updateConfig();
		}
	}

	private void addSubscriptions()
	{
		//eventBus.subscribe(ScriptCallbackEvent.class, this, this::onScriptCallbackEvent);
		eventBus.subscribe(ConfigChanged.class, this, this::onConfigChanged);
		eventBus.subscribe(GameTick.class, this, this::onGameTick);
		eventBus.subscribe(ClientTick.class, this, this::onClientTick);
		eventBus.subscribe(ChatMessage.class, this, this::onChatMessage);
		eventBus.subscribe(ItemContainerChanged.class, this, this::onItemContainerChanged);
		eventBus.subscribe(MenuEntryAdded.class, this, this::onMenuEntryAdded);
		eventBus.subscribe(MenuOptionClicked.class, this, this::onMenuOptionClicked);
		eventBus.subscribe(NpcDespawned.class, this, this::onNpcDespawned);
	}

	boolean shouldRunPlugin()
	{
		return client.getGameState() == GameState.LOGGED_IN &&
				client.getVar(Varbits.QUEST_THE_FEUD) >= 13 &&
				Objects.requireNonNull(client.getLocalPlayer()).getWorldLocation().getRegionID() == POLLNIVNEACH_REGION;
	}

	private void onMenuEntryAdded(MenuEntryAdded event)
	{
		int type = event.getOpcode();
		int oType = type;
		if (type >= MENU_ACTION_DEPRIORITIZE_OFFSET)
		{
			type -= MENU_ACTION_DEPRIORITIZE_OFFSET;
		}
		if (client.getCachedNPCs()[(event.getIdentifier())] != null && Arrays.asList(targets).contains(Text.standardize(client.getCachedNPCs()[(event.getIdentifier())].getName(), true)))
		{
			if (NPC_FIRST_OPTION.getId() == type)
			{
				final String target = ColorUtil.prependColorTag(Text.removeTags(event.getTarget()), Color.BLUE);
				event.setTarget(target);
				event.setModified();
			}
			else if (hotKeyPressed && type == MenuOpcode.EXAMINE_NPC.getId())
			{
				client.insertMenuItem(
					(target != null && target.getIndex() == event.getIdentifier()) ? UNTAG : TAG,
					event.getTarget(),
					MenuOpcode.RUNELITE.getId(),
					event.getIdentifier(),
					event.getParam0(),
					event.getParam1(),
					false
				);
			}
		}

		final int id = event.getIdentifier();
		if (event.getOpcode() == MenuOpcode.NPC_FIRST_OPTION.getId() && Arrays.asList(targets).contains(Text.standardize(client.getCachedNPCs()[(event.getIdentifier())].getName(), true)))
		{
			if (mode == 0)
			{
				event.setOption("KNOCK");
				event.setModified();
			}
			else if (mode == 1)
			{
				event.setOption("STEAL");
				event.setModified();
			}
			else if (mode == 2)
			{
				event.setOption("EAT");
				event.setModified();
			}
			else if (mode == 3)
			{
				event.setOption("EXAMINE");
				event.setModified();
			}
			else if (mode == 4)
			{
				event.setOption("RUNAWAY");
				event.setModified();
			}
		}
	}

	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (event.getMenuOpcode() == MenuOpcode.RUNELITE)
		{
			final int id = event.getIdentifier();
			final NPC[] cachedNPCs = client.getCachedNPCs();
			final NPC npc = cachedNPCs[id];
			if (npc == null || npc.getName() == null)
			{
				return;
			}
			if (event.getOption().equals(TAG))
			{
				target = npc;
			}
			else if (event.getOption().equals(UNTAG))
			{
				if (target != null && target.getIndex() == id)
				{
					target = null;
				}
			}
			event.consume();
		}
		else
		{
			//final MenuEntry entry = event.getMenuEntry();
			if (event.getOpcode() == MenuOpcode.NPC_FIRST_OPTION.getId() && Arrays.asList(targets).contains(Text.standardize(client.getCachedNPCs()[(event.getIdentifier())].getName(), true)))
			{
				switch (event.getOption())
				{
					case "KNOCK":
						event.setOpcode(MenuOpcode.NPC_FIFTH_OPTION.getId());
						event.setOption("Knock - Out");
						break;
					case "STEAL":
						event.setOpcode(MenuOpcode.NPC_THIRD_OPTION.getId());
						event.setOption("Pickpocket");
						break;
					case "EAT":
						event.setOpcode(MenuOpcode.ITEM_FIRST_OPTION.getId());
						event.setIdentifier(FOOD_ID);
						event.setParam0(food_idx);
						event.setParam1(WidgetInfo.INVENTORY.getId());
						event.setOption("Drink");
						break;
					case "EXAMINE":
						event.setOpcode(MenuOpcode.EXAMINE_NPC.getId());
						event.setOption("Examine");
						break;
					case "RUNAWAY":
						event.setParam0( - 1);
						event.setParam1(WidgetInfo.EQUIPMENT_CAPE.getId());
						event.setOpcode(MenuOpcode.CC_OP.getId());
						event.setOption("Kandarin Monastery");
						event.setIdentifier(2);
						toggleMoveMouse(false);
						clickMouse = false;
						break;
				}
			}
		}

	}

	private void onClientTick(ClientTick clientTick)
	{
		if (!shouldRunPlugin() || client.getLocalPlayer() == null)
		{
			return;
		}
//		log.debug("client tick");
		if (target != null && target.getAnimation() == -1 && target.getWorldLocation().getRegionID() == client.getLocalPlayer().getWorldLocation().getRegionID())
		{
			Rectangle area = target.getConvexHull().getBounds();
			java.awt.Point center = area.getLocation();
			center.translate((int) (area.getWidth() / 2.0d), (int) (area.getHeight() / 2.0d));
			area.setSize((int) (area.getWidth() * .5d), (int) (area.getHeight() * .5d));
			center.translate((int) ( - area.getWidth() / 2.0d), (int) ( - area.getHeight() / 2.0d));
			area.setLocation(center);
			this.clickArea = area;
		}
		else
		{
			if (target == null || target.getWorldLocation().getRegionID() != client.getLocalPlayer().getWorldLocation().getRegionID())
			{
				this.clickArea = null;
			}
		}
	}

	private void onGameTick(GameTick gameTick)
	{
		if (!shouldRunPlugin())
		{
			return;
		}
//		log.debug("gametick");
		if (oldState != state)
		{
			oldState = state;
			if (state == 3)
			{
				mode = 4;
			}
			else if (state == 0)
			{
				mode = 0; //we knockout
			}
			else if (state == 1)
			{
				mode = 1; //we pickpocket
			}
			else if (state == 2)
			{
				if (food_idx == -1)
				{
					mode = 3; // we examine
				}
				else
				{
					mode = 2; // We eat
				}
			}
		}
		else
		{
			if (state == 3)
			{
				mode = 4;
			}
			else if (state == 0)
			{
				if (nextKnockOutTick >= client.getTickCount())
				{
					mode = 1; //we pickpocket
				}
				else
				{
					mode = 0; //we knockout
				}
			}
			else if (state == 2)
			{
				if (client.getBoostedSkillLevel(Skill.HITPOINTS) >= healthMax)
				{
					state = 0;
				}
				else if (food_idx == -1)
				{
					mode = 3; // we examine
				}
				else
				{
					mode = 2; // We eat
				}
			}
		}

		if (target != null && client.getLocalPlayer() != null && target.getWorldLocation().getRegionID() == client.getLocalPlayer().getWorldLocation().getRegionID())
		{
			if (target.getAnimation() != -1)
			{
				target.setAnimation( - 1);
				target.setActionFrame(0);
			}
		}

		if (clickMouse && clickArea != null && target != null && target.getConvexHull() != null)
		{
			if (scriptFuture == null || scriptFuture.isDone())
			{
				scriptFuture = threadExec.schedule(() ->
				{
					if (target.getConvexHull().contains(client.getMouseCanvasPosition().asNativePoint()))
					{
						flexo.mousePressAndRelease(1);
					}
				}, flexo.getMinDelay(), MILLISECONDS);
			}
		}
	}

	private void onChatMessage(ChatMessage event)
	{
		if ((event.getType() != ChatMessageType.SPAM && event.getType() != ChatMessageType.GAMEMESSAGE && event.getType() != ChatMessageType.ENGINE) || !(event.getMessage().equals(CANT_REACH) || event.getMessage().equals(NOT_IN_COMBAT1) || event.getMessage().equals(NOT_IN_COMBAT2) || event.getMessage().equals(NOT_HIDDEN_BLACKJACK) || event.getMessage().equals(SUCCESS_BLACKJACK) || event.getMessage().equals(FAILED_BLACKJACK) || event.getMessage().equals(ATTEMPT_PICKPOCKET2) || event.getMessage().equals(ATTEMPT_PICKPOCKET) || event.getMessage().equals(ATTEMPT_PICKPOCKET3) || event.getMessage().equals(ATTEMPT_PICKPOCKET4)))
		{
			return;
		}

		if (event.getMessage().equals(NOT_IN_COMBAT1) || event.getMessage().equals(NOT_IN_COMBAT2))
		{
			state = 3;
			fuck = true;
		}
		else if (event.getMessage().equals(NOT_HIDDEN_BLACKJACK) || event.getMessage().equals(CANT_REACH))
		{
			toggleMoveMouse(false);
			clickMouse = false;
		}
		else if (event.getMessage().equals(SUCCESS_BLACKJACK) && state == 0)
		{
			seqFailed = 0;
			nextKnockOutTick = client.getTickCount() + RandomUtils.nextInt(3, 4);
		}
		else if (event.getMessage().equals(FAILED_BLACKJACK) && state == 0)
		{
			seqFailed += 1;
			if (client.getBoostedSkillLevel(Skill.HITPOINTS) < healthMin)
			{
				state = 1;
			}
		}
		else if ((event.getMessage().equals(ATTEMPT_PICKPOCKET) || event.getMessage().equals(ATTEMPT_PICKPOCKET2) || event.getMessage().equals(ATTEMPT_PICKPOCKET3) || event.getMessage().equals(ATTEMPT_PICKPOCKET4)) && state == 1)
		{
			state = 2;
		}
	}

	private void onItemContainerChanged(ItemContainerChanged event)
	{
		final ItemContainer itemContainer = event.getItemContainer();
		final List<Item> items = Arrays.asList(itemContainer.getItems());

		if (itemContainer != client.getItemContainer(InventoryID.INVENTORY))
		{
			return;
		}
		this.food_idx = -1;

		for (int i = 0; i < items.size(); i++)
		{
			final int itemId = items.get(i).getId();

			if (itemId == FOOD_ID)
			{
				this.food_idx = i;
				break;
			}
		}
		if (this.food_idx == -1)
		{
			this.toggleMoveMouse(false);
			this.clickMouse = false;
		}
	}

	private void onNpcDespawned(NpcDespawned event)
	{
		if (event.getNpc() == target)
		{
			target = null;
		}
	}

	private void updateConfig()
	{
		this.healthMin = config.healthMin();
		this.healthMax = config.healthMax();
	}
}
