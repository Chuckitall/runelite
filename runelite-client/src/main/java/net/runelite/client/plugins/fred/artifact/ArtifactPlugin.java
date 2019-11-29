package net.runelite.client.plugins.fred.artifact;

import com.google.common.collect.ImmutableList;
import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.Tile;
import net.runelite.api.Varbits;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.fred.api.other.Tuples;
import net.runelite.client.plugins.fred.api.other.Tuples.T2;
import net.runelite.client.ui.overlay.OverlayManager;
import org.apache.commons.lang3.ArrayUtils;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Authors gazivodag longstreet
 */
@PluginDescriptor(
	name = "Fred's Artifact",
	description = "Helps steal artifacts for //todo",
	tags = {"fred", "favor", "thieving"},
	type = PluginType.FRED
)
@Singleton
@Slf4j
public class ArtifactPlugin extends Plugin
{
	@Getter
	private ArtifactState state = ArtifactState.INVALID;

	@Getter(AccessLevel.PACKAGE)
	private List<NPC> guards = new ArrayList<>();

	@Getter
	private boolean markHouse = false;

	private static final int[] REGIONS = {6971, 7227, 6970, 7226};
	private static final int[] GUARD_IDS = {6971, 7227, 6970, 7226};
	boolean shouldRunPlugin()
	{
		return client.getGameState() == GameState.LOGGED_IN &&
			client.getVar(Varbits.KOUREND_FAVOR_PISCARILIUS) >= 75 &&
			ArrayUtils.contains(REGIONS, Objects.requireNonNull(client.getLocalPlayer()).getWorldLocation().getRegionID());
	}

	private boolean isGuard(NPC npc)
	{
		return ArrayUtils.contains(GUARD_IDS, npc.getId());
	}

	@Inject
	@Getter
	private Client client;
	@Inject
	private ArtifactConfig config;
	@Inject
	private WindowOverlay windowOverlay;
	@Inject
	private SceneOverlay sceneOverlay;
	@Inject
	private OverlayManager overlayManager;

	enum ArtifactState
	{
		INVALID(-1, "INVALID"),
		NO_TASK(0, "No task."),
		NORTH(1, "Northern house"),

		SOUTH(3, "Southern house"),

		SOUTH_WEST(4, "South-western house", ImmutableList.of(
			Tuples.of(new WorldPoint(1749, 3730, 0), 29634),
			Tuples.of(new WorldPoint(1749, 3735, 1), 27774)
		)),

		NORTH_WEST(6, "North-western house", ImmutableList.of(
			Tuples.of(new WorldPoint(1750, 3756, 0), 27634),
			Tuples.of(new WorldPoint(1750, 3763, 1), 27776)
		)),

		CAUGHT(7, "Failed"),

		GETAWAY(8, "Return to capt.", ImmutableList.of(
			Tuples.of(new WorldPoint(1749, 3730, 1), 29635),//south-west
			Tuples.of(new WorldPoint(1776, 3730, 1), 29635),//south-east
			Tuples.of(new WorldPoint(1750, 3756, 1), 27635) //north-west
		)),

		WEST(-1, "Western house"),
		SOUTH_EAST(-1, "South-eastern house", ImmutableList.of(
			Tuples.of(new WorldPoint(1776, 3730, 0), 27634),
			Tuples.of(new WorldPoint(1773, 3730, 1), 27772)
		)),;

		private static final Map<Integer, ArtifactState> BY_ID = buildById();

		private static Map<Integer, ArtifactState> buildById()
		{
			HashMap<Integer, ArtifactState> byId = new HashMap<>();
			for (ArtifactState b : ArtifactState.values())
			{
				byId.put(b.getValue(), b);
			}
			return Collections.unmodifiableMap(byId);
		}

		static ArtifactState byId(int id)
		{
			return BY_ID.get(id);
		}

		@Getter
		int value;
		@Getter
		String message;

		List<T2<WorldPoint, Integer>> targets;

		private Tile getTile(Client client, WorldPoint point)
		{
			return client.getScene().getTiles()[point.getPlane()][point.getX() - client.getBaseX()][point.getY() - client.getBaseY()];
		}

		public GameObject getTargetObject(Client client)
		{
			// Negate to have the furthest first
			// Order by position
			// And then by id
			return targets.stream()
				.filter(f -> f.get_1().isInScene(client))
				.map(o -> Tuples.of(getTile(client, o.get_1()).getGameObjects(), o.get_2()))
				.flatMap(o -> Arrays.stream(o.get_1()).filter(j -> j.getId() == o.get_2())).min(Comparator.comparing(
					// Negate to have the furthest first
					(GameObject obj) -> -obj.getLocalLocation().distanceTo(client.getLocalPlayer().getLocalLocation()))
					// Order by position
					.thenComparing(GameObject::getLocalLocation, Comparator.comparing(LocalPoint::getX)
						.thenComparing(LocalPoint::getY))
					// And then by id
					.thenComparing(GameObject::getId)).orElse(null);
		}

		ArtifactState(int value, String message)
		{
			this.value = value;
			this.message = message;
			this.targets = ImmutableList.of();
		}

		ArtifactState(int value, String message, List<T2<WorldPoint, Integer>> targets)
		{
			this.value = value;
			this.message = message;

		}
	}

	@Provides
	ArtifactConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ArtifactConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		//init config
		updateConfig();
		overlayManager.add(windowOverlay);
		overlayManager.add(sceneOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(windowOverlay);
		overlayManager.remove(sceneOverlay);
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals("artifact"))
		{
			updateConfig();
		}
	}

	@Subscribe
	private void onVarbitChanged(VarbitChanged v)
	{
		if (v.getIndex() == Varbits.ARTIFACT_STATE.getId())
		{
			this.state = ArtifactState.byId(client.getVar(Varbits.ARTIFACT_STATE));
		}
	}

	private void updateArtifactVarbit()
	{
		this.state = ArtifactState.byId(client.getVar(Varbits.ARTIFACT_STATE));
	}

	@Subscribe
	private void onNpcSpawned(NpcSpawned event)
	{
		if (isGuard(event.getNpc()) && !guards.contains(event.getNpc()))
		{
			guards.add(event.getNpc());
		}
	}

	@Subscribe
	private void onNpcDespawned(NpcDespawned event)
	{
		if (isGuard(event.getNpc()))
		{
			guards.remove(event.getNpc());
		}
	}

	private void updateConfig()
	{
		this.markHouse = config.markHouse();
	}
}
