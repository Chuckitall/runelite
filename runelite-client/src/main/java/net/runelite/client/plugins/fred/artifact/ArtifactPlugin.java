package net.runelite.client.plugins.fred.artifact;

import com.google.common.collect.ImmutableList;
import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import net.runelite.api.TileObject;
import net.runelite.api.Varbits;
import net.runelite.api.World;
import net.runelite.api.coords.Angle;
import net.runelite.api.coords.Direction;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameObjectChanged;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.queries.NPCQuery;
import net.runelite.api.queries.TileObjectQuery;
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
	private ArtifactState state = getArtifactById(-1);

	@Getter(AccessLevel.PACKAGE)
	private List<NPC> guards = new ArrayList<>();

	@Getter(AccessLevel.PACKAGE)
	private final List<GameObject> interestingObjects = new ArrayList<>();

	@Getter
	private boolean markHouse = false;

	private static final int[] REGIONS = {6971, 7227, 6970, 7226};

	private List<Integer> GUARD_IDS;

	private int favor = 0;

	boolean inArea = false;

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

	private List<GameObject> findGameObjects(List<T2<WorldPoint, Integer>> data)
	{
		List<GameObject> toRet = new ArrayList<>();
		for (T2<WorldPoint, Integer> datum : data)
		{
			Optional<GameObject> temp = Optional.ofNullable(new GameObjectQuery().idEquals(datum.get_2()).atWorldLocation(datum.get_1()).result(client).nearestTo(client.getLocalPlayer()));
			temp.ifPresent(toRet::add);
		}
		return toRet;
	}

	static class ArtifactState
	{
		@Getter
		private final int value;

		@Getter
		private final String message;

		@Getter
		private final List<T2<Integer, WorldPoint>> targetsData;

		public boolean objMatches(int id, WorldPoint loc)
		{
			//List<T2<_Tile, Integer>> idMatched = targetsData.stream().filter(f -> f.get_2() == obj.getId()).collect(Collectors.toList());
			//idMatched.stream().filter(f->f.get_1().equals(_Tile.fromGameObject(obj.getWorldLocation()))).collect(Collectors.toList());
			for (T2<Integer, WorldPoint> targetsDatum : targetsData)
			{
				if (targetsDatum.get_1() == id && targetsDatum.get_2().getX() == loc.getX() && targetsDatum.get_2().getY() == loc.getY())
				{
					return true;
				}
			}
			return false;
		}

		ArtifactState(int value, String message)
		{
			this.value = value;
			this.message = message;
			this.targetsData = ImmutableList.copyOf(Collections.emptyList());
		}

		ArtifactState(int value, String message, List<T2<Integer, WorldPoint>> targetsData)
		{
			this.value = value;
			this.message = message;
			this.targetsData = ImmutableList.copyOf(targetsData);
		}
	}

	@Provides
	ArtifactConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ArtifactConfig.class);
	}

	@Override
	protected void startUp()
	{
		//init config
		state = getArtifactById(-1);
		updateConfig();
		GUARD_IDS = ImmutableList.of(6978, 7975, 6980, 6974, 6973, 6975, 6976, 6979, 6977);
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
		state = getArtifactById(-1);
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
		return toRet.stream().distinct().collect(Collectors.toList());
	}

	@Subscribe
	private void onTick(GameTick event)
	{
		List<WorldPoint> los = guards.stream().flatMap(f -> calculateLineOfSight(f, 3).stream()).distinct().collect(Collectors.toList());
		guardVision.clear();
		guardVision.addAll(los);
		if (client.hasHintArrow() && client.getHintArrowPoint().getPlane() != client.getPlane())
		{
			client.clearHintArrow();
		}
		if (!client.hasHintArrow() && interestingObjects.stream().anyMatch(f -> f.getPlane() == client.getPlane()))
		{
			interestingObjects.stream().filter(f -> f.getPlane() == client.getPlane()).min(Comparator.comparing(g -> g.getWorldLocation().distanceTo(Objects.requireNonNull(client.getLocalPlayer()).getWorldLocation()))).map(TileObject::getWorldLocation).ifPresent(client::setHintArrow);
		}
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
		interestingObjects.clear();
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals("artifact"))
		{
			updateConfig();
		}
	}

	private ArtifactState getArtifactById(int id)
	{
		ArtifactState toRet;
		switch (id)
		{
			case 7:
				toRet = new ArtifactState(7, "Failed");
				break;
			case 8:
				toRet = new ArtifactState(8, "Return to capt.", ImmutableList.of(
					Tuples.of(27635, new WorldPoint(1749, 3730, 1)),//south-west
					Tuples.of(27635, new WorldPoint(1776, 3730, 1)),//south-east
					Tuples.of(27635, new WorldPoint(1768, 3733, 1)),//south
					Tuples.of(27635, new WorldPoint(1750, 3756, 1)), //north-west
					Tuples.of(27635, new WorldPoint(1751, 3751, 1)) //west
				));
				break;
			case 0:
				toRet = new ArtifactState(0, "No task.");
				break;
			case 1:
				toRet = new ArtifactState(1, "Northern house", ImmutableList.of(
					Tuples.of(27771, new WorldPoint(1767, 3750, 0))
				));
				break;
			case 3:
				toRet = new ArtifactState(3, "Southern house", ImmutableList.of(
					Tuples.of(27634, new WorldPoint(1768, 3733, 0)),
					Tuples.of(27773, new WorldPoint(1764, 3735, 1))
				));
				break;
			case 4:
				toRet = new ArtifactState(4, "South-western house", ImmutableList.of(
					Tuples.of(27634, new WorldPoint(1749, 3730, 0)),
					Tuples.of(27774, new WorldPoint(1749, 3735, 1))
				));
				break;
			case 6:
				toRet = new ArtifactState(6, "North-western house", ImmutableList.of(
					Tuples.of(27634, new WorldPoint(1750, 3756, 0)),
					Tuples.of(27776, new WorldPoint(1750, 3763, 1))
				));
				break;
			case 2:
				toRet = new ArtifactState(2, "South-eastern house", ImmutableList.of(
					Tuples.of(27634, new WorldPoint(1776, 3730, 0)),
					Tuples.of(27772, new WorldPoint(1773, 3730, 1))
				));
				break;
			case 5:
				toRet = new ArtifactState(5, "Western house", ImmutableList.of(
					Tuples.of(27634, new WorldPoint(1751, 3751, 0)),
					Tuples.of(27775, new WorldPoint(1747, 3749, 1))
				));
				break;
			default:
				toRet = new ArtifactState(-1, "INVALID");
				break;
		}
		return toRet;
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
		int varbVal = client.getVar(Varbits.ARTIFACT_STATE);
		if (this.state.getValue() != varbVal)
		{
			this.state = getArtifactById(varbVal);
			this.interestingObjects.clear();
			client.clearHintArrow();
			TileObjectQuery<GameObject, GameObjectQuery> q = new GameObjectQuery().idEquals(state.getTargetsData().stream().map(T2::get_1).collect(Collectors.toList())).filter(this::checkGameObject);
			this.interestingObjects.addAll(q.result(client).list);
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

	private boolean checkGameObject(GameObject obj)
	{
		WorldPoint loc = Optional.of(obj).map(f -> Tuples.of(f.getSceneMinLocation(), f.getPlane())).map(f -> WorldPoint.fromScene(client, f.get_1().getX(), f.get_1().getY(), f.get_2())).get();
		return state.objMatches(obj.getId(), loc);
	}

	@Subscribe
	private void onGameObjectSpawned(GameObjectSpawned event)
	{
		if(!shouldRunPlugin())
		{
			return;
		}
		if (checkGameObject(event.getGameObject()))
		{
			this.interestingObjects.add(event.getGameObject());
		}
	}

	@Subscribe
	private void onGameObjectDespawned(GameObjectDespawned event)
	{
		this.interestingObjects.remove(event.getGameObject());
	}

	@Subscribe
	private void onGameObjectChanged(GameObjectChanged event)
	{
		GameObject previous = event.getPrevious();
		GameObject gameObject = event.getGameObject();

		interestingObjects.remove(previous);
		if (checkGameObject(gameObject))
		{
			interestingObjects.add(gameObject);
		}
	}

	private void updateConfig()
	{
		this.markHouse = config.markHouse();
	}
}
