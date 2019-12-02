package net.runelite.client.plugins.fred.artifact;

import com.google.common.collect.ImmutableList;
import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.Varbits;
import net.runelite.api.coords.Angle;
import net.runelite.api.coords.Direction;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.queries.NPCQuery;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.ui.overlay.OverlayManager;
import org.apache.commons.lang3.ArrayUtils;

import javax.inject.Inject;
import javax.inject.Singleton;

import static net.runelite.api.coords.Direction.EAST;
import static net.runelite.api.coords.Direction.NORTH;
import static net.runelite.api.coords.Direction.SOUTH;
import static net.runelite.api.coords.Direction.WEST;

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
	private ArtifactTarget state = null;

	@Getter
	private WorldPoint markerPoint = null;

	@Getter
	private Optional<GameObject> targetObject = Optional.empty();

	@Getter
	private Optional<NPC> targetNPC = Optional.empty();

	@Getter(AccessLevel.PACKAGE)
	private List<NPC> guards = new ArrayList<>();

	private int favor = 0;
	private boolean inArea = false;

	private static final int ladderUpId = 27634;

	private static final int[] REGIONS = {6971, 7227, 6970, 7226};
	private static final List<Integer> GUARD_IDS = ImmutableList.of(6978, 7975, 6980, 6974, 6973, 6975, 6976, 6979, 6977);

	@Getter
	private boolean markHouse = false;

	private boolean inArea()
	{
		GameState gameState = client.getGameState();
		if (gameState != GameState.LOGGED_IN
			&& gameState != GameState.LOADING)
		{
			return false;
		}
		return ArrayUtils.contains(REGIONS, Objects.requireNonNull(client.getLocalPlayer()).getWorldLocation().getRegionID());
	}

	boolean shouldRunPlugin()
	{
		return inArea && favor >= 75;
	}


	private boolean isGuard(NPC npc)
	{
		return GUARD_IDS.contains(npc.getId());
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

	@Getter
	private List<WorldPoint> guardVision = new ArrayList<>();

	@Provides
	ArtifactConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ArtifactConfig.class);
	}

	@Override
	protected void startUp()
	{
		//init config
		state = null;
		markerPoint = null;
		targetNPC = Optional.empty();
		targetObject = Optional.empty();

		updateConfig();
		inArea = inArea();
		if (inArea)
		{
			guards = new NPCQuery().idEquals(GUARD_IDS).result(client).list;
			updateArtifactVarbit();
		}
		overlayManager.add(windowOverlay);
		overlayManager.add(sceneOverlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(windowOverlay);
		overlayManager.remove(sceneOverlay);
		state = null;
		markerPoint = null;
		targetNPC = Optional.empty();
		targetObject = Optional.empty();
	}

	private void calculateLineOfSight(List<WorldPoint> points, Direction d, WorldPoint p, int n)
	{
		WorldArea a = new WorldArea(p, 1, 1);
		WorldPoint temp;
		if (d == NORTH || d == SOUTH) //y++ or y--
		{
			int dy = (d == NORTH ? 1 : -1);
			if (a.canTravelInDirection(client, -1, dy))
			{
				temp = p.dx(-1).dy(dy);
				if (!points.contains(temp))
				{
					points.add(temp);
					if(n > 0)
					{
						calculateLineOfSight(points, d, temp, n - 1);
					}
				}
			}
			if (a.canTravelInDirection(client, 0, dy))
			{
				temp = p.dy(dy);
				if (!points.contains(temp))
				{
					points.add(temp);
					if(n > 0)
					{
						calculateLineOfSight(points, d, temp, n - 1);
					}
				}
			}
			if (a.canTravelInDirection(client, 1, dy))
			{
				temp = p.dx(1).dy(dy);
				if (!points.contains(temp))
				{
					points.add(temp);
					if(n > 0)
					{
						calculateLineOfSight(points, d, temp, n - 1);
					}
				}
			}
		}
		else if (d == EAST || d == WEST)//x++ or x--
		{
			int dx = (d == EAST ? 1 : -1);
			if (a.canTravelInDirection(client, dx, -1))
			{
				temp = p.dx(dx).dy(-1);
				if (!points.contains(temp))
				{
					points.add(temp);
					if(n > 0)
					{
						calculateLineOfSight(points, d, temp, n - 1);
					}
				}
			}
			if (a.canTravelInDirection(client, dx, 0))
			{
				temp = p.dx(dx);
				if (!points.contains(temp))
				{
					points.add(temp);
					if(n > 0)
					{
						calculateLineOfSight(points, d, temp, n - 1);
					}
				}
			}
			if (a.canTravelInDirection(client, dx, 1))
			{
				temp = p.dx(dx).dy(1);
				if (!points.contains(temp))
				{
					points.add(temp);
					if(n > 0)
					{
						calculateLineOfSight(points, d, temp, n - 1);
					}
				}
			}
		}
	}

	private List<WorldPoint> calculateLineOfSight(Actor a, int n)
	{
		List<WorldPoint> toRet = new ArrayList<>();
		Direction d = new Angle(a.getOrientation()).getNearestDirection();
		WorldPoint origin = a.getWorldLocation();

		calculateLineOfSight(toRet, d, origin, n);
//		WorldArea area = new WorldArea(origin, 1, 1);
		toRet.add(origin);
//		if (d == NORTH || d == SOUTH)
//		{
//			if(area.canTravelInDirection(client, 1, 0))
//			{
//				toRet.add(origin.dx(1));
//			}
//			if(area.canTravelInDirection(client, -1, 0))
//			{
//				toRet.add(origin.dx(-1));
//			}
//		}
//		else
//		{
//				if(area.canTravelInDirection(client, 0, 1))
//				{
//					toRet.add(origin.dy(1));
//				}
//				if(area.canTravelInDirection(client, 0, -1))
//				{
//					toRet.add(origin.dy(-1));
//				}
//		}
		return toRet.stream().distinct().collect(Collectors.toList());
	}

	@Subscribe
	private void onTick(GameTick event)
	{
		if (!inArea)
		{
			if(markerPoint != null)
			{
				client.clearHintArrow();
				markerPoint = null;
			}
			return;
		}
		if (state.getId() == 8)
		{
			List<WorldPoint> los = guards.stream().flatMap(f -> calculateLineOfSight(f, 3).stream()).distinct().collect(Collectors.toList());
			guardVision.clear();
			guardVision.addAll(los);
		}
		else
		{
			guardVision.clear();
		}
		if (client.hasHintArrow())
		{
			WorldPoint clientMarker = client.getHintArrowPoint();
			if (clientMarker.getPlane() != markerPoint.getPlane())
			{
				client.clearHintArrow();
				markerPoint = state.getMarkerPoint(client);
				if(markerPoint != null)
				{
					client.setHintArrow(markerPoint);
				}
			}
		}
		else
		{
			markerPoint = state.getMarkerPoint(client);
			if (markerPoint != null)
			{
				client.setHintArrow(markerPoint);
			}
		}

		targetObject = state.getTargetObject(client);
		targetNPC = state.getTargetNPC(client);
	}

	@Subscribe
	void onGameStateChanged(GameStateChanged event)
	{
		boolean pVal = inArea;
		if (event.getGameState() == GameState.LOADING)
		{
			inArea = inArea();
		}
		else if (event.getGameState() == GameState.LOGIN_SCREEN)
		{
			inArea = false;
		}
		if (!pVal || inArea)
		{
			return;
		}
		markerPoint = null;
		targetNPC = Optional.empty();
		targetObject = Optional.empty();
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
		if (inArea)
		{
			updateArtifactVarbit();
		}
	}

	private void updateArtifactVarbit()
	{
		this.favor = client.getVar(Varbits.KOUREND_FAVOR_PISCARILIUS);
		int varVal = client.getVar(Varbits.ARTIFACT_STATE);
		if (this.state == null || this.state.getId() != varVal)
		{
			this.state = ArtifactTarget.getByState(varVal);
			client.clearHintArrow();
		}
	}

	@Subscribe
	private void onNpcSpawned(NpcSpawned event)
	{
		if (!shouldRunPlugin())
		{
			return;
		}
		if (isGuard(event.getNpc()) && !guards.contains(event.getNpc()))
		{
			guards.add(event.getNpc());
		}
	}

	@Subscribe
	private void onNpcDespawned(NpcDespawned event)
	{
		guards.remove(event.getNpc());
	}

	private void updateConfig()
	{
		this.markHouse = config.markHouse();
	}
}
