import java.util.*;
import java.awt.geom.Point2D;

/**
 * Representation of AStar algorithm. Performs the A* search on a 2D-grid given
 * a agentMap, a start and end point. G Cost for each Forward Move is 1, and
 * different for different tiles depending on the situation of the agent
 * Heuristic is the Manhattan distance Coding 
 * v
 * @author bennygmate
 */
public class AStar {
	private final Point2D.Double startPoint;
	private final Point2D.Double endPoint;
	private final Map<Point2D.Double, Character> agentMap;
	private Map<Point2D.Double, Point2D.Double> successPath;
	private Map<Point2D.Double, Integer> gCost;
	private Map<Point2D.Double, Integer> fCost;
	private static final int INFINITY = 999999;

	/**
	 * Constructor
	 * 
	 * @param agentMap
	 *            the agentMap containing information about the environment
	 * @param startPoint
	 *            the starting point we begin to search from
	 * @param endPoint
	 *            the endPoint point which we will try to find the shortest path
	 *            to
	 */
	public AStar(Map<Point2D.Double, Character> agentMap, Point2D.Double startPoint, Point2D.Double endPoint) {
		this.agentMap = agentMap;
		this.startPoint = startPoint;
		this.endPoint = endPoint;
		this.successPath = new HashMap<>();
		this.gCost = new HashMap<>();
		this.fCost = new HashMap<>();
	}

	/**
	 * Performs an A* search on the agent Map environment from start to end
	 * Point(2D) and fills the success path with points to move around map (note
	 * this one is only commented as they get repetitive
	 * 
	 * @Pre Destination is reachable
	 * @param haveKey
	 *            Agent holds key
	 * @param haveAxe
	 *            Agent holds axe
	 * @param numDyna
	 *            Agent has number of dynamites
	 * @param haveRaft
	 *            Agent has a raft
	 * @param usingRaft
	 *            Agent is using the raft
	 */
	public void callSearch(boolean haveKey, boolean haveAxe, int numDyna, boolean haveRaft, boolean usingRaft) {
		FCostSort fcs = new FCostSort();
		PriorityQueue<Point2D.Double> openSet = new PriorityQueue<>(10, fcs);
		Set<Point2D.Double> closedSet = new HashSet<>();
		// Initialise agentMap with default value of infinity
		for (int y = WorldModel.MAX_MAP_Y; y >= -WorldModel.MAX_MAP_Y; y--) {
			for (int x = -WorldModel.MAX_MAP_X; x <= WorldModel.MAX_MAP_X; x++) {
				gCost.put(new Point2D.Double(x, y), INFINITY);
				fCost.put(new Point2D.Double(x, y), INFINITY);
			}
		}
		// Initialise start position
		gCost.put(startPoint, 0);
		fCost.put(startPoint, ManhattanDistanceHeuristic(startPoint, endPoint));
		openSet.add(startPoint);
		while (!openSet.isEmpty()) {
			Point2D.Double currentTile = openSet.remove();
			// If finished exit getSuccessPath() can be called to reconstruct
			// the path
			if (currentTile.equals(endPoint)) {
				return;
			}
			openSet.remove(currentTile); // Remove from open
			closedSet.add(currentTile); // Mark as seen
			// Add west, east, north, south tiles, like a plus sign
			for (int p = 0; p < 4; p++) {
				int nextX = (int) currentTile.getX();
				int nextY = (int) currentTile.getY();
				switch (p) {
				case 0:
					nextX += 1;
					break; // Tile East
				case 1:
					nextX -= 1;
					break; // Tile West
				case 2:
					nextY += 1;
					break; // Tile North
				case 3:
					nextY -= 1;
					break; // Tile South
				}
				Point2D.Double nextPoint = new Point2D.Double(nextX, nextY);
				// If next tile seen or not able to be passed, skip
				char tile = agentMap.get(nextPoint);
				if (!WorldModel.tilePassWithItem(tile, haveKey, haveAxe, numDyna, haveRaft, usingRaft)) {
					continue; // Tile can't be passed
				}
				if (closedSet.contains(nextPoint))
					continue;
				// Cost between neighbouring tiles is 1 as assumed
				int nextPoint_gCost = gCost.get(currentTile) + 1;
				// g(new) > g(old), skip
				if (nextPoint_gCost >= gCost.get(nextPoint))
					continue;
				// Else, this path is lowest cost so far, put in path
				successPath.put(nextPoint, currentTile);
				gCost.put(nextPoint, nextPoint_gCost);
				fCost.put(nextPoint, nextPoint_gCost + ManhattanDistanceHeuristic(nextPoint, endPoint));
				// If not on open add, so explore later
				if (!openSet.contains(nextPoint))
					openSet.add(nextPoint);
			}
		}
	}

	/**
	 * Performs an A* search on the agent Map environment from start to end
	 * Point(2D) and fills the success path with points to move around map (This
	 * is when agent is using raft and wants to go to new island). This is how
	 * the agent moves across (by having different g costs)
	 * 
	 * @Pre Destination is reachable
	 * @param haveKey
	 *            Agent holds key
	 * @param haveAxe
	 *            Agent holds axe
	 * @param numDyna
	 *            Agent has number of dynamites
	 * @param haveRaft
	 *            Agent has a raft
	 * @param usingRaft
	 *            Agent is using the raft
	 */
	public void callIslandSearch(boolean haveKey, boolean haveAxe, int numDyna, boolean haveRaft, boolean usingRaft) {
		FCostSort fcs = new FCostSort();
		PriorityQueue<Point2D.Double> openSet = new PriorityQueue<>(10, fcs);
		Set<Point2D.Double> closedSet = new HashSet<>();
		// Initialise agentMap with default value of infinity
		for (int y = WorldModel.MAX_MAP_Y; y >= -WorldModel.MAX_MAP_Y; y--) {
			for (int x = -WorldModel.MAX_MAP_X; x <= WorldModel.MAX_MAP_X; x++) {
				gCost.put(new Point2D.Double(x, y), INFINITY);
				fCost.put(new Point2D.Double(x, y), INFINITY);
			}
		}
		// Initialise start position
		gCost.put(startPoint, 0);
		fCost.put(startPoint, ManhattanDistanceHeuristic(startPoint, endPoint));
		openSet.add(startPoint);
		while (!openSet.isEmpty()) {
			Point2D.Double currentTile = openSet.remove();
			if (currentTile.equals(endPoint))
				return;
			openSet.remove(currentTile);
			closedSet.add(currentTile);
			for (int p = 0; p < 4; p++) {
				int nextX = (int) currentTile.getX();
				int nextY = (int) currentTile.getY();
				switch (p) {
				case 0:
					nextX += 1;
					break;
				case 1:
					nextX -= 1;
					break;
				case 2:
					nextY += 1;
					break;
				case 3:
					nextY -= 1;
					break;
				}
				Point2D.Double nextPoint = new Point2D.Double(nextX, nextY);
				char tile = agentMap.get(nextPoint);
				if (!WorldModel.tilePassWithItem(tile, haveKey, haveAxe, numDyna, haveRaft, usingRaft)) {
					continue;
				}
				if (closedSet.contains(nextPoint))
					continue;
				int nextPoint_gCost = gCost.get(currentTile) + 1;
				if (tile == WorldModel.OBSTACLE_WATER) {
					nextPoint_gCost -= 1;
				}
				if (haveAxe && tile == WorldModel.OBSTACLE_TREE) {
					nextPoint_gCost += 2; // Bit more expensive to cut through
											// trees
				}

				if (nextPoint_gCost >= gCost.get(nextPoint))
					continue;
				successPath.put(nextPoint, currentTile);
				gCost.put(nextPoint, nextPoint_gCost);
				fCost.put(nextPoint, nextPoint_gCost + ManhattanDistanceHeuristic(nextPoint, endPoint));
				if (!openSet.contains(nextPoint))
					openSet.add(nextPoint);
			}
		}
	}

	/**
	 * Performs an A* search on the agent Map environment from start to end
	 * Point(2D) and fills the success path with points to move around map (This
	 * is when agent crossed waters and want to go back home through the water
	 * to water)
	 * 
	 * @Pre Destination is reachable
	 * @param haveKey
	 *            Agent holds key
	 */
	public void callRiverSearch(boolean haveKey) {
		FCostSort fcs = new FCostSort();
		PriorityQueue<Point2D.Double> openSet = new PriorityQueue<>(10, fcs);
		Set<Point2D.Double> closedSet = new HashSet<>();
		for (int y = WorldModel.MAX_MAP_Y; y >= -WorldModel.MAX_MAP_Y; y--) {
			for (int x = -WorldModel.MAX_MAP_X; x <= WorldModel.MAX_MAP_X; x++) {
				gCost.put(new Point2D.Double(x, y), INFINITY);
				fCost.put(new Point2D.Double(x, y), INFINITY);
			}
		}
		gCost.put(startPoint, 0);
		fCost.put(startPoint, ManhattanDistanceHeuristic(startPoint, endPoint));
		openSet.add(startPoint);
		while (!openSet.isEmpty()) {
			Point2D.Double currentTile = openSet.remove();
			if (currentTile.equals(endPoint))
				return;
			openSet.remove(currentTile);
			closedSet.add(currentTile);
			for (int p = 0; p < 4; p++) {
				int nextX = (int) currentTile.getX();
				int nextY = (int) currentTile.getY();
				switch (p) {
				case 0:
					nextX += 1;
					break; // Tile East
				case 1:
					nextX -= 1;
					break; // Tile West
				case 2:
					nextY += 1;
					break; // Tile North
				case 3:
					nextY -= 1;
					break; // Tile South
				}
				Point2D.Double nextPoint = new Point2D.Double(nextX, nextY);
				// If next tile seen or not able to be passed, skip
				char tile = agentMap.get(nextPoint);
				if (!WorldModel.tilePassWithItem(tile, haveKey, true, 1, true, true)) {
					continue; // Tile can't be passed
				}
				if (closedSet.contains(nextPoint))
					continue;
				// Cost between neighbouring tiles is 1 as assumed
				int nextPoint_gCost = gCost.get(currentTile) + 1;
				if (tile == WorldModel.OBSTACLE_SPACE) {
					nextPoint_gCost += 1;
				}
				if (tile == WorldModel.OBSTACLE_WATER) {
					nextPoint_gCost += 1;
				}
				if (tile == WorldModel.OBSTACLE_TREE) {
					nextPoint_gCost += 2; // Bit more expensive to cut through
											// trees
				}
				if (tile == WorldModel.OBSTACLE_WALL) {
					nextPoint_gCost += 160; // expensive to blast
				}
				if (tile == WorldModel.TOOL_TREASURE) {
					nextPoint_gCost -= 160; // WIN
				}
				if (nextPoint_gCost >= gCost.get(nextPoint))
					continue;
				successPath.put(nextPoint, currentTile);
				gCost.put(nextPoint, nextPoint_gCost);
				fCost.put(nextPoint, nextPoint_gCost + ManhattanDistanceHeuristic(nextPoint, endPoint));
				if (!openSet.contains(nextPoint))
					openSet.add(nextPoint);
			}
		}
	}

	/**
	 * Performs an A* search on the agent Map environment from start to end
	 * Point(2D) and fills the success path with points to move around map (This
	 * is when agent is using raft and wants use dynamite to blow up wall). This
	 * is how the agent moves across (by having different g costs)
	 * 
	 * @Pre Destination is reachable
	 * @param haveKey
	 *            Agent holds key
	 * @param haveAxe
	 *            Agent holds axe
	 * @param numDyna
	 *            Agent has number of dynamites
	 * @param haveRaft
	 *            Agent has a raft
	 * @param usingRaft
	 *            Agent is using the raft
	 */
	public void callDynamiteSearch(boolean haveKey, boolean haveAxe, int numDyna, boolean haveRaft, boolean usingRaft) {
		FCostSort fcs = new FCostSort();
		PriorityQueue<Point2D.Double> openSet = new PriorityQueue<>(10, fcs);
		Set<Point2D.Double> closedSet = new HashSet<>();
		// Initialise agentMap with default value of infinity
		for (int y = WorldModel.MAX_MAP_Y; y >= -WorldModel.MAX_MAP_Y; y--) {
			for (int x = -WorldModel.MAX_MAP_X; x <= WorldModel.MAX_MAP_X; x++) {
				gCost.put(new Point2D.Double(x, y), INFINITY);
				fCost.put(new Point2D.Double(x, y), INFINITY);
			}
		}
		gCost.put(startPoint, 0);
		fCost.put(startPoint, ManhattanDistanceHeuristic(startPoint, endPoint));
		openSet.add(startPoint);
		while (!openSet.isEmpty()) {
			Point2D.Double currentTile = openSet.remove();
			if (currentTile.equals(endPoint))
				return;
			openSet.remove(currentTile);
			closedSet.add(currentTile);
			for (int p = 0; p < 4; p++) {
				int nextX = (int) currentTile.getX();
				int nextY = (int) currentTile.getY();
				switch (p) {
				case 0:
					nextX += 1;
					break;
				case 1:
					nextX -= 1;
					break;
				case 2:
					nextY += 1;
					break;
				case 3:
					nextY -= 1;
					break;
				}
				Point2D.Double nextPoint = new Point2D.Double(nextX, nextY);
				char tile = agentMap.get(nextPoint);
				if (!WorldModel.tilePassDynamite(tile, haveKey, haveAxe, numDyna, haveRaft, usingRaft)) {
					continue; // Tile can't be passed
				}
				if (closedSet.contains(nextPoint))
					continue;
				int nextPoint_gCost = gCost.get(currentTile) + 1;
				if (haveRaft && tile == WorldModel.OBSTACLE_WATER) {
					nextPoint_gCost += 1; // Make going path using water more
											// expensive
					usingRaft = true;
				}
				if (usingRaft) {
					if (tile == WorldModel.OBSTACLE_WATER) {
						nextPoint_gCost -= 50;
						// MAKE IT CHEAPER TO GO WATER TO DEST
						// This is because, of islands/rivers
					}
				}
				if (tile == WorldModel.OBSTACLE_WALL) {
					nextPoint_gCost += 100; // Make it cost lots to use dynamite
				}
				// g(new) > g(old), skip
				if (nextPoint_gCost >= gCost.get(nextPoint))
					continue;
				// Else, this path is lowest cost so far, put in path
				successPath.put(nextPoint, currentTile);
				gCost.put(nextPoint, nextPoint_gCost);
				fCost.put(nextPoint, nextPoint_gCost + ManhattanDistanceHeuristic(nextPoint, endPoint));
				// If not on open add, so explore later
				if (!openSet.contains(nextPoint))
					openSet.add(nextPoint);
			}
		}
	}

	/**
	 * Performs an A* search on the agent Map environment from start to end
	 * Point(2D) and fills the success path with points to move around map (This
	 * is when agent is travelling from water to water)
	 */
	public void callWaterSearch() {
		FCostSort fcs = new FCostSort();
		PriorityQueue<Point2D.Double> openSet = new PriorityQueue<>(10, fcs);
		Set<Point2D.Double> closedSet = new HashSet<>();
		for (int y = WorldModel.MAX_MAP_Y; y >= -WorldModel.MAX_MAP_Y; y--) {
			for (int x = -WorldModel.MAX_MAP_X; x <= WorldModel.MAX_MAP_X; x++) {
				gCost.put(new Point2D.Double(x, y), INFINITY);
				fCost.put(new Point2D.Double(x, y), INFINITY);
			}
		}
		gCost.put(startPoint, 0);
		fCost.put(startPoint, ManhattanDistanceHeuristic(startPoint, endPoint));
		openSet.add(startPoint);
		while (!openSet.isEmpty()) {
			Point2D.Double currentTile = openSet.remove();
			if (currentTile.equals(endPoint)) {
				return;
			}
			openSet.remove(currentTile);
			closedSet.add(currentTile);
			for (int p = 0; p < 4; p++) {
				int nextX = (int) currentTile.getX();
				int nextY = (int) currentTile.getY();
				switch (p) {
				case 0:
					nextX += 1;
					break; // Tile East
				case 1:
					nextX -= 1;
					break; // Tile West
				case 2:
					nextY += 1;
					break; // Tile North
				case 3:
					nextY -= 1;
					break; // Tile South
				}
				Point2D.Double nextPoint = new Point2D.Double(nextX, nextY);
				char tile = agentMap.get(nextPoint);
				if (!WorldModel.tilePassWater(tile))
					continue;
				if (closedSet.contains(nextPoint))
					continue;
				int nextPoint_gCost = gCost.get(currentTile) + 1;
				if (nextPoint_gCost >= gCost.get(nextPoint))
					continue;
				successPath.put(nextPoint, currentTile);
				gCost.put(nextPoint, nextPoint_gCost);
				fCost.put(nextPoint, nextPoint_gCost + ManhattanDistanceHeuristic(nextPoint, endPoint));
				if (!openSet.contains(nextPoint))
					openSet.add(nextPoint);
			}
		}
	}

	/**
	 * Performs an A* search on the agent Map environment from start to end
	 * Point(2D) and fills the success path with points to move around map (This
	 * is when agent is first travelling to their first water tile)
	 */
	public void firstWaterSearch(boolean haveKey, boolean haveAxe) {
		FCostSort fcs = new FCostSort();
		PriorityQueue<Point2D.Double> openSet = new PriorityQueue<>(10, fcs);
		Set<Point2D.Double> closedSet = new HashSet<>();
		for (int y = WorldModel.MAX_MAP_Y; y >= -WorldModel.MAX_MAP_Y; y--) {
			for (int x = -WorldModel.MAX_MAP_X; x <= WorldModel.MAX_MAP_X; x++) {
				gCost.put(new Point2D.Double(x, y), INFINITY);
				fCost.put(new Point2D.Double(x, y), INFINITY);
			}
		}
		gCost.put(startPoint, 0);
		fCost.put(startPoint, ManhattanDistanceHeuristic(startPoint, endPoint));
		openSet.add(startPoint);
		while (!openSet.isEmpty()) {
			Point2D.Double currentTile = openSet.remove();
			if (currentTile.equals(endPoint)) {
				return;
			}
			openSet.remove(currentTile);
			closedSet.add(currentTile);
			for (int p = 0; p < 4; p++) {
				int nextX = (int) currentTile.getX();
				int nextY = (int) currentTile.getY();
				switch (p) {
				case 0:
					nextX += 1;
					break; // Tile East
				case 1:
					nextX -= 1;
					break; // Tile West
				case 2:
					nextY += 1;
					break; // Tile North
				case 3:
					nextY -= 1;
					break; // Tile South
				}
				Point2D.Double nextPoint = new Point2D.Double(nextX, nextY);
				char tile = agentMap.get(nextPoint);
				if (!WorldModel.tilePassWithItem(tile, haveKey, haveAxe, 0, true, false)) {
					continue; // Tile can't be passed
				}
				if (closedSet.contains(nextPoint))
					continue;
				int nextPoint_gCost = gCost.get(currentTile) + 1;
				if (nextPoint_gCost >= gCost.get(nextPoint))
					continue;
				successPath.put(nextPoint, currentTile);
				gCost.put(nextPoint, nextPoint_gCost);
				fCost.put(nextPoint, nextPoint_gCost + ManhattanDistanceHeuristic(nextPoint, endPoint));
				if (!openSet.contains(nextPoint))
					openSet.add(nextPoint);
			}
		}
	}

	/**
	 * Performs an A* search on the agent Map environment from start to end
	 * Point(2D) and fills the success path with points to move around map (This
	 * is when agent wants to know if a raft is needed to reach a goal)
	 * 
	 * @return integer of gcost < 0, if only need raft to reach goal
	 */
	public int callTheoreticalRaftSearch() {
		successPath.clear();
		FCostSort fcs = new FCostSort();
		PriorityQueue<Point2D.Double> openSet = new PriorityQueue<>(10, fcs);
		Set<Point2D.Double> closedSet = new HashSet<>();
		for (int y = WorldModel.MAX_MAP_Y; y >= -WorldModel.MAX_MAP_Y; y--) {
			for (int x = -WorldModel.MAX_MAP_X; x <= WorldModel.MAX_MAP_X; x++) {
				gCost.put(new Point2D.Double(x, y), INFINITY);
				fCost.put(new Point2D.Double(x, y), INFINITY);
			}
		}
		gCost.put(startPoint, 0);
		fCost.put(startPoint, ManhattanDistanceHeuristic(startPoint, endPoint));
		openSet.add(startPoint);
		while (!openSet.isEmpty()) {
			Point2D.Double currentTile = openSet.remove();
			if (currentTile.equals(endPoint)) {
				return (gCost.get(currentTile));
			}
			openSet.remove(currentTile); // Remove from open
			closedSet.add(currentTile); // Mark as seen
			for (int p = 0; p < 4; p++) {
				int nextX = (int) currentTile.getX();
				int nextY = (int) currentTile.getY();
				switch (p) {
				case 0:
					nextX += 1;
					break; // Tile East
				case 1:
					nextX -= 1;
					break; // Tile West
				case 2:
					nextY += 1;
					break; // Tile North
				case 3:
					nextY -= 1;
					break; // Tile South
				}
				Point2D.Double nextPoint = new Point2D.Double(nextX, nextY);
				// if (agentMap.get(nextPoint) == null) continue;
				char tile = agentMap.get(nextPoint);
				if (!WorldModel.tilePassWithItem(tile, false, false, 0, true, true)) {
					continue;
				}
				if (closedSet.contains(nextPoint))
					continue;
				int nextPoint_gCost = gCost.get(currentTile);
				if (tile == WorldModel.OBSTACLE_SPACE) {
					nextPoint_gCost -= 0; // Make it free to use space
				}
				if (tile == WorldModel.OBSTACLE_TREE) {
					nextPoint_gCost += 160; // Make it bit more expensive to go
											// through trees
				}
				if (tile == WorldModel.OBSTACLE_WATER) {
					nextPoint_gCost -= 0;
				}
				if (tile == WorldModel.OBSTACLE_WALL) {
					nextPoint_gCost += 160; // Make it cost lots to use dynamite
				}
				// g(new) > g(old), skip
				if (nextPoint_gCost >= gCost.get(nextPoint))
					continue;
				// Else, this path is lowest cost so far, put in path
				successPath.put(nextPoint, currentTile);
				gCost.put(nextPoint, nextPoint_gCost);
				fCost.put(nextPoint, nextPoint_gCost + ManhattanDistanceHeuristic(nextPoint, endPoint));
				// If not on open add, so explore later
				if (!openSet.contains(nextPoint))
					openSet.add(nextPoint);
			}
		}
		return 0;
	}

	/**
	 * Performs an A* search on the agent Map environment from start to end
	 * Point(2D) and fills the success path with points to move around map (This
	 * is when agent wants to know if dynamites is needed to reach a goal)
	 * 
	 * @return integer of gcost < 0, if only gain by dynamites by reaching goal
	 */
	public int callTheoreticalDynaSearch() {
		FCostSort fcs = new FCostSort();
		PriorityQueue<Point2D.Double> openSet = new PriorityQueue<>(10, fcs);
		Set<Point2D.Double> closedSet = new HashSet<>();
		for (int y = WorldModel.MAX_MAP_Y; y >= -WorldModel.MAX_MAP_Y; y--) {
			for (int x = -WorldModel.MAX_MAP_X; x <= WorldModel.MAX_MAP_X; x++) {
				gCost.put(new Point2D.Double(x, y), INFINITY);
				fCost.put(new Point2D.Double(x, y), INFINITY);
			}
		}
		gCost.put(startPoint, 0);
		fCost.put(startPoint, ManhattanDistanceHeuristic(startPoint, endPoint));
		openSet.add(startPoint);
		while (!openSet.isEmpty()) {
			Point2D.Double currentTile = openSet.remove();
			if (currentTile.equals(endPoint)) {
				return (gCost.get(currentTile));
			}
			openSet.remove(currentTile); // Remove from open
			closedSet.add(currentTile); // Mark as seen
			for (int p = 0; p < 4; p++) {
				int nextX = (int) currentTile.getX();
				int nextY = (int) currentTile.getY();
				switch (p) {
				case 0:
					nextX += 1;
					break; // Tile East
				case 1:
					nextX -= 1;
					break; // Tile West
				case 2:
					nextY += 1;
					break; // Tile North
				case 3:
					nextY -= 1;
					break; // Tile South
				}
				Point2D.Double nextPoint = new Point2D.Double(nextX, nextY);
				char tile = agentMap.get(nextPoint);
				if (!WorldModel.tilePassDynamite(tile, true, true, 1, true, true)) {
					continue; // Tile can't be passed
				}
				if (closedSet.contains(nextPoint))
					continue;
				int nextPoint_gCost = gCost.get(currentTile);
				if (tile == WorldModel.OBSTACLE_SPACE) {
					nextPoint_gCost -= 0; // Make it free to use space
				}
				if (tile == WorldModel.OBSTACLE_WALL) {
					nextPoint_gCost += 160; // Make it cost lots to use dynamite
				}
				if (tile == WorldModel.TOOL_DYNAMITE) {
					nextPoint_gCost -= 160; // Make it Beneficial to get the
											// dynamite
				}
				if (tile == WorldModel.OBSTACLE_TREE) {
					nextPoint_gCost += 160;
				}
				if (tile == WorldModel.OBSTACLE_WATER) {
					nextPoint_gCost += 320; // Make going path using water more
											// expensive
				}
				// g(new) > g(old), skip
				if (nextPoint_gCost >= gCost.get(nextPoint))
					continue;
				// Else, this path is lowest cost so far, put in path
				successPath.put(nextPoint, currentTile);
				gCost.put(nextPoint, nextPoint_gCost);
				fCost.put(nextPoint, nextPoint_gCost + ManhattanDistanceHeuristic(nextPoint, endPoint));
				// If not on open add, so explore later
				if (!openSet.contains(nextPoint))
					openSet.add(nextPoint);
			}
		}
		return 0;
	}

	/**
	 * Computes the Manhattan distance for start and end point, this is an
	 * admissible heuristic
	 * 
	 * @param startPoint
	 *            the starting point
	 * @param endPoint
	 *            the ending point
	 * @return integer of Manhattan distance from start to end point
	 */
	private int ManhattanDistanceHeuristic(Point2D.Double startPoint, Point2D.Double endPoint) {
		int absX = Math.abs((int) startPoint.getX() - (int) endPoint.getX());
		int absY = Math.abs((int) startPoint.getY() - (int) endPoint.getY());
		return (absX + absY);
	}

	/**
	 * Returns minimum path from start to goal as determined in search() or
	 * empty linked list if no path was found.
	 * 
	 * @Pre above searches are called before calling this method
	 * @return LinkedList of Point2D that form a path start to goal (backwards)
	 *         without start, else null
	 */
	public LinkedList<Point2D.Double> getSuccessPath() {
		LinkedList<Point2D.Double> copyPath = new LinkedList<>();
		Point2D.Double currPoint = endPoint;
		while (successPath.get(currPoint) != null) {
			copyPath.add(currPoint);
			currPoint = successPath.get(currPoint);
		}
		return copyPath;
	}

	/**
	 * Comparison method in the java utility Points with lower f Costs come
	 * earlier (higher priority given)
	 */
	private class FCostSort implements Comparator<Point2D.Double> {
		@Override
		public int compare(Point2D.Double pointOne, Point2D.Double pointTwo) {
			return (fCost.get(pointOne) - fCost.get(pointTwo));
		}
	}
}
