/*
 * Copyright (c) 2019, ganom <https://github.com/Ganom>
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
package net.runelite.client.plugins.fred.wintertodt;

import com.google.inject.Provides;
import java.util.Arrays;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;


import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.MessageNode;
import net.runelite.api.Player;
import net.runelite.api.Varbits;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.events.ConfigChanged;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;

import javax.inject.Inject;
import java.awt.*;
import java.time.Duration;
import java.time.Instant;

import static net.runelite.api.AnimationID.*;
import static net.runelite.api.GameState.LOADING;
import static net.runelite.api.ItemID.*;


@PluginDescriptor(
	name = "Fred's Wintertodt",
	description = "Wintertodt bot",
	tags = {"flexo", "wintertodt", "bot", "fredwinter", "fred"},
	type = PluginType.FRED
)
@Slf4j
public class FredWinterPlugin extends Plugin
{
	@Getter(AccessLevel.PACKAGE)
	@Inject
	private Client client;

	@Getter(AccessLevel.PACKAGE)
	@Inject
	private FredWinterConfig config;

	@Getter(AccessLevel.PACKAGE)
	@Inject
	private KeyManager keyManager;


	@Getter(AccessLevel.PACKAGE)
	@Inject
	private OverlayManager overlayManager;

	@Getter(AccessLevel.PACKAGE)
	@Inject
	private EventBus eventBus;

	@Inject
	private FredWinterOverlay fredWinterOverlay;

	@Inject
	private ChatMessageManager chatMessageManager;

	private static final int WINTERTODT_REGION = 6462;

	static final int WINTERTODT_ROOTS_MULTIPLIER = 10;
	static final int WINTERTODT_KINDLING_MULTIPLIER = 25;

	@Getter(AccessLevel.PACKAGE)
	private FredWinterActivity currentActivity = FredWinterActivity.IDLE;

	@Getter(AccessLevel.PACKAGE)
	private int numRoots;

	@Getter(AccessLevel.PACKAGE)
	private int numKindling;

	@Getter(AccessLevel.PACKAGE)
	private boolean hasKnife;

	@Getter(AccessLevel.PACKAGE)
	private boolean hasTinderbox;

	@Getter(AccessLevel.PACKAGE)
	private boolean hasHammer;

	@Getter(AccessLevel.PACKAGE)
	private boolean isInWintertodt;

	@Getter(AccessLevel.PACKAGE)
	private int points;

	@Getter(AccessLevel.PACKAGE)
	private int percent_left;

	@Inject
	private FlexoController flexoController;

	private Instant lastActionTime;

	private int previousTimerValue;
	private Color damageNotificationColor;

	private boolean subscribed;

	@Provides
	FredWinterConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(FredWinterConfig.class);
	}

	@Override
	protected void startUp()
	{
		this.damageNotificationColor = config.damageNotificationColor();
		addSubscriptions();
		handleWintertodtRegion();
		reset();

		overlayManager.add(fredWinterOverlay);
		flexoController.startUp();
	}

	@Override
	protected void shutDown() throws Exception
	{
		eventBus.unregister(this);
		eventBus.unregister("fred-inside-wintertodt");
		reset();
		overlayManager.remove(fredWinterOverlay);
		flexoController.shutDown();
	}

	private void addSubscriptions()
	{
		eventBus.subscribe(ConfigChanged.class, this, this::onConfigChanged);
		eventBus.subscribe(GameStateChanged.class, this, this::onGameStateChanged);
		eventBus.subscribe(VarbitChanged.class, this, this::onVarbitChanged);
		flexoController.addSubscriptions();
	}

	private void wintertodtSubscriptions(boolean subscribe)
	{
		if (subscribe)
		{
			eventBus.subscribe(GameTick.class, "fred-inside-wintertodt", this::onGameTick);
			eventBus.subscribe(ChatMessage.class, "fred-inside-wintertodt", this::onChatMessage);
			eventBus.subscribe(AnimationChanged.class, "fred-inside-wintertodt", this::onAnimationChanged);
			eventBus.subscribe(ItemContainerChanged.class, "fred-inside-wintertodt", this::onItemContainerChanged);
		}
		else
		{
			eventBus.unregister("fred-inside-wintertodt");
		}
		flexoController.wintertodtSubscriptions(subscribe);
	}

	private void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals("FredWinter"))
		{
			return;
		}

		this.damageNotificationColor = config.damageNotificationColor();
	}

	protected void reset()
	{
		numRoots = 0;
		numKindling = 0;
		ItemContainer i = client.getItemContainer(InventoryID.INVENTORY);
		Item[] items = (i != null) ? i.getItems() : new Item[0];
		hasHammer = Arrays.stream(items).anyMatch(f -> f.getId() == HAMMER);
		hasKnife = Arrays.stream(items).anyMatch(f -> f.getId() == KNIFE);
		hasTinderbox = Arrays.stream(items).anyMatch(f -> f.getId() == TINDERBOX);
		currentActivity = FredWinterActivity.IDLE;
		lastActionTime = null;
		flexoController.reset();
	}

	private boolean isInWintertodtRegion()
	{
		if (client.getLocalPlayer() != null)
		{
			return client.getLocalPlayer().getWorldLocation().getRegionID() == WINTERTODT_REGION;
		}

		return false;
	}

	private void handleWintertodtRegion()
	{
		if (isInWintertodtRegion())
		{
			if (!isInWintertodt)
			{
				reset();
				log.debug("Entered Wintertodt!");
			}
			isInWintertodt = true;

			if (!subscribed)
			{
				wintertodtSubscriptions(true);
				subscribed = true;
			}
		}
		else
		{
			if (isInWintertodt)
			{
				log.debug("Left Wintertodt!");
				reset();
			}

			isInWintertodt = false;

			if (subscribed)
			{
				wintertodtSubscriptions(false);
				subscribed = false;
			}
		}
	}

	private void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == LOADING)
		{
			handleWintertodtRegion();
		}
	}

	private void onGameTick(GameTick event)
	{
		Widget w = client.getWidget(WidgetInfo.WINTERTODT_POINTS);
		if (w != null && !w.isHidden() && w.getText().startsWith("Points<br>"))
		{
			points = Integer.parseInt(w.getText().replace("Points<br>", ""));
		}
		else
		{
			points = 0;
		}
		w = client.getWidget(WidgetInfo.WINTERTODT_PERCENTAGE);
		if (w != null && !w.isHidden() && w.getText().startsWith("Wintertodt's Energy: "))
		{
			percent_left = Integer.parseInt(w.getText().replace("Wintertodt's Energy: ", "").replace("%", ""));
		}
		else
			{
			percent_left = 0;
		}
		checkActionTimeout();
	}

	private void onVarbitChanged(VarbitChanged varbitChanged)
	{
		int timerValue = client.getVar(Varbits.WINTERTODT_TIMER);
		if (timerValue != previousTimerValue)
		{
			int timeToNotify = config.roundNotification();
			if (timeToNotify > 0)
			{
				int timeInSeconds = timerValue * 30 / 50;
				int prevTimeInSeconds = previousTimerValue * 30 / 50;

				log.debug("Seconds left until round start: {}", timeInSeconds);

//				if (prevTimeInSeconds > timeToNotify && timeInSeconds <= timeToNotify)
//				{
//					//notifier.notify("Wintertodt round is about to start");
//				}
			}

			previousTimerValue = timerValue;
		}
	}

	private void checkActionTimeout()
	{
		if (currentActivity == FredWinterActivity.IDLE)
		{
			return;
		}

		int currentAnimation = client.getLocalPlayer() != null ? client.getLocalPlayer().getAnimation() : -1;
		if (currentAnimation != IDLE || lastActionTime == null)
		{
			return;
		}

		Duration actionTimeout = Duration.ofSeconds(3);
		Duration sinceAction = Duration.between(lastActionTime, Instant.now());

		if (sinceAction.compareTo(actionTimeout) >= 0)
		{
			log.debug("Activity timeout!");
			setActivity(FredWinterActivity.IDLE);
		}
	}

	private void onChatMessage(ChatMessage chatMessage)
	{
		ChatMessageType chatMessageType = chatMessage.getType();

		if (chatMessageType != ChatMessageType.GAMEMESSAGE && chatMessageType != ChatMessageType.SPAM)
		{
			return;
		}

		MessageNode messageNode = chatMessage.getMessageNode();
		final FredWinterInterruptType interruptType;

		if (messageNode.getValue().startsWith("The cold of"))
		{
			interruptType = FredWinterInterruptType.COLD;
		}
		else if (messageNode.getValue().startsWith("The freezing cold attack"))
		{
			interruptType = FredWinterInterruptType.SNOWFALL;
		}
		else if (messageNode.getValue().startsWith("The brazier is broken and shrapnel"))
		{
			interruptType = FredWinterInterruptType.BRAZIER;
		}
		else if (messageNode.getValue().startsWith("You have run out of bruma roots"))
		{
			interruptType = FredWinterInterruptType.OUT_OF_ROOTS;
		}
		else if (messageNode.getValue().startsWith("Your inventory is too full"))
		{
			interruptType = FredWinterInterruptType.INVENTORY_FULL;
		}
		else if (messageNode.getValue().startsWith("You fix the brazier"))
		{
			interruptType = FredWinterInterruptType.FIXED_BRAZIER;
		}
		else if (messageNode.getValue().startsWith("You light the brazier"))
		{
			interruptType = FredWinterInterruptType.LIT_BRAZIER;
		}
		else if (messageNode.getValue().startsWith("The brazier has gone out."))
		{
			interruptType = FredWinterInterruptType.BRAZIER_WENT_OUT;
		}
		else
		{
			return;
		}

		boolean wasInterrupted = false;
		boolean wasDamaged = false;

		switch (interruptType)
		{
			case COLD:
			case BRAZIER:
			case SNOWFALL:
//				wasDamaged = true;

				// Recolor message for damage notification
				messageNode.setRuneLiteFormatMessage(ColorUtil.wrapWithColorTag(messageNode.getValue(), this.damageNotificationColor));
				chatMessageManager.update(messageNode);
				client.refreshChat();

				// all actions except woodcutting are interrupted from damage
				if (currentActivity != FredWinterActivity.WOODCUTTING)
				{
					wasInterrupted = true;
				}

				break;
			case INVENTORY_FULL:
			case OUT_OF_ROOTS:
			case BRAZIER_WENT_OUT:
				wasInterrupted = true;
				break;
			case LIT_BRAZIER:
			case FIXED_BRAZIER:
				wasInterrupted = true;
				break;
		}
//		if (wasDamaged && client.getBoostedSkillLevel(Skill.HITPOINTS) < 20)
//		{
////			flexoController.setShouldEat(true);
//		}
		if (wasInterrupted)
		{
			setActivity(FredWinterActivity.IDLE);
		}
	}

	private void onAnimationChanged(final AnimationChanged event)
	{
		final Player local = client.getLocalPlayer();

		if (event.getActor() != local)
		{
			return;
		}

		if (local == null)
		{
			return;
		}
		final int animId = local.getAnimation();
		switch (animId)
		{
			case WOODCUTTING_BRONZE:
			case WOODCUTTING_IRON:
			case WOODCUTTING_STEEL:
			case WOODCUTTING_BLACK:
			case WOODCUTTING_MITHRIL:
			case WOODCUTTING_ADAMANT:
			case WOODCUTTING_RUNE:
			case WOODCUTTING_DRAGON:
			case WOODCUTTING_INFERNAL:
			case WOODCUTTING_3A_AXE:
			case WOODCUTTING_CRYSTAL:
				setActivity(FredWinterActivity.WOODCUTTING);
				break;

			case FLETCHING_BOW_CUTTING:
				setActivity(FredWinterActivity.FLETCHING);
				break;

			case LOOKING_INTO:
				setActivity(FredWinterActivity.FEEDING_BRAZIER);
				break;

			case FIREMAKING:
				setActivity(FredWinterActivity.LIGHTING_BRAZIER);
				break;

			case CONSTRUCTION:
				setActivity(FredWinterActivity.FIXING_BRAZIER);
				break;
		}
	}

	private void onItemContainerChanged(ItemContainerChanged event)
	{
		final ItemContainer container = event.getItemContainer();

		if (container != client.getItemContainer(InventoryID.INVENTORY))
		{
			return;
		}

		final Item[] inv = container.getItems();

		numRoots = 0;
		numKindling = 0;

		for (Item item : inv)
		{
			switch (item.getId())
			{
				case BRUMA_ROOT:
					++numRoots;
					break;
				case BRUMA_KINDLING:
					++numKindling;
					break;
				case KNIFE:
					hasKnife = true;
					break;
				case HAMMER:
					hasHammer = true;
					break;
				case TINDERBOX:
					hasTinderbox = true;
					break;
			}
		}

		//If we're currently fletching but there are no more roots, go ahead and abort fletching immediately
		if (numRoots == 0 && currentActivity == FredWinterActivity.FLETCHING)
		{
			setActivity(FredWinterActivity.IDLE);
		}
		//Otherwise, if we're currently feeding the brazier but we've run out of both roots and kindling, abort the feeding activity
		else if (numRoots == 0 && numKindling == 0 && currentActivity == FredWinterActivity.FEEDING_BRAZIER)
		{
			setActivity(FredWinterActivity.IDLE);
		}
	}

	private void setActivity(FredWinterActivity action)
	{
		currentActivity = action;
		lastActionTime = Instant.now();
	}
}
