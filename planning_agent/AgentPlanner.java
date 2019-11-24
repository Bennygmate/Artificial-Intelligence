import java.awt.geom.Point2D;
import java.util.*;

/**
 * AgentPlanner class. Makes decisions about the next move (or moves) to make
 * based on internal world map (the map environment). Functions by adding moves
 * to a move queue which are carried out first before deciding what other moves
 * to make.
 * 
 * @author z3460693
 */
public class AgentPlanner {
	private WorldModel wm;
	private Queue<Character> agentMoves;
	private int dynasNeeded;
	private boolean axeNeeded;
	private boolean needRaft;

	/**
	 * Constructor
	 */
	public AgentPlanner() {
		this.wm = new WorldModel();
		this.agentMoves = new LinkedList<>();
		this.dynasNeeded = 0;
		this.axeNeeded = false;
		this.needRaft = false;
	}

	public char agentPerception(char view[][]) {
		wm.updateFromView(view); // Update our wm view
		// Knowledge base + logical inference --> adds to pending moves
		if (agentMoves.isEmpty())
			agentPlanner();
		// Feed action back to the agent
		if (!agentMoves.isEmpty())
			return agentAction();
		return 0;
	}

	public char agentAction() {
		char moveToMake = agentMoves.remove();
		wm.updateWorldModel(moveToMake);
		return moveToMake;
	}

	/**
	 * This is the agent planner and feeds instructions This method adds a move
	 * to agentMove queue, that is when agent has no action plan
	 */
	public void agentPlanner() {
		while (agentMoves.isEmpty()) {
			// Priority 1: Have treasure do A* traversal to starting location
			// (0,0)
			if (wm.holdTreas()) {
				AgentReach ar = new AgentReach(wm.getMap(), wm.getAgentLoc(), new Point2D.Double(0, 0));
				if (ar.reach(wm.holdKey(), wm.holdAxe(), wm.getNumDyna(), wm.holdRaft(), wm.usingRaft())) {
					// Just go home
					makePathAStar(wm.getAgentLoc(), new Point2D.Double(0, 0), wm.getDir(), wm.holdKey(), wm.holdAxe(),
							wm.getNumDyna(), wm.holdRaft(), wm.usingRaft());
					break;
				} else {
					// Chop tree to get raft back, or use dynamite to blast home
					if (getRaftUsingAxe(wm.getAgentLoc()) == true) {
						pathToGetRaft();
						break;
					}
					if (wm.getNumDyna() > 0) {
						if (theoreticalRaftPathAStar(wm.getAgentLoc(), new Point2D.Double(0, 0), wm.getDir())) {
							break;
						}
						makeDynaPathAStar(wm.getTreasLoc(), new Point2D.Double(0, 0), wm.getDir(), wm.holdKey(),
								wm.holdAxe(), wm.getNumDyna(), wm.holdRaft(), wm.usingRaft());
					}
				}
			}
			// Priority 2: Can see treasure
			if (wm.isTreasVisible()) {
				theoreticalDynaPathCounter(wm.getAgentLoc(), wm.getTreasLoc(), wm.getDir());
				theoreticalRaftPathCounter(wm.getAgentLoc(), wm.getTreasLoc(), wm.getDir());
				// Simply grab treasure, if don't need raft or dynamite or axe
				if (!needRaft && dynasNeeded == 0 && !axeNeeded) {
					makePathAStar(wm.getAgentLoc(), wm.getTreasLoc(), wm.getDir(), wm.holdKey(), wm.holdAxe(), 0, false,
							false);
				}
				// If this is the case we can just BLAST THROUGH
				if (dynasNeeded <= wm.getNumDyna() && !axeNeeded && !needRaft) {
					if (theoreticalRaftPathAStar(wm.getAgentLoc(), wm.getTreasLoc(), wm.getDir())) {
						break;
					}
				}
				// If it gets to here, you need to use dynamite and use raft at
				// the same time so use special pathing
				if (needRaft && dynasNeeded > 0 && dynasNeeded <= wm.getNumDyna() && !axeNeeded) {
					makeBackLandPathAStar(wm.getAgentLoc(), wm.getTreasLoc(), wm.getDir(), wm.holdKey(), true);
					break;
				}
				// If need Raft to get to treasure
				if (needRaft) {
					if (!wm.holdRaft() && wm.holdAxe()) {
						if (getRaftUsingAxe(wm.getAgentLoc())) {
							pathToGetRaft();
						}
					}
					// If we know we can get the raft back following treasure,
					// grab it
					if (wm.holdRaft() || wm.usingRaft()) {
						if (getRaftBackAfterTool(wm.getTreasLoc())) {
							if (theoreticalRaftPathAStar(wm.getAgentLoc(), wm.getTreasLoc(), wm.getDir())) {
								break;
							}
						}
					}
					// If we have enough dynamite to get it from our location,
					// see if we can blast it
					if (!wm.usingRaft() && dynasNeeded <= wm.getNumDyna()) {
						// grab treasure if not using raft and don't have to
						// cross island
						if (theoreticalDynaPathAStar(wm.getAgentLoc(), wm.getTreasLoc(), wm.getDir())) {
							break;
						} else {
							// CROSS ISLAND TO GET IT
							makeBackLandPathAStar(wm.getAgentLoc(), wm.getTreasLoc(), wm.getDir(), wm.holdKey(), true);
							break;
						}
					}
				}
				// If we hold enough dynamite to even consider blasting to
				// treasure
				if (dynasNeeded <= wm.getNumDyna() && dynasNeeded != 0) {
					// If we can get to treasure using dynamite we win!
					if (!needRaft) {
						if (theoreticalDynaPathAStar(wm.getAgentLoc(), wm.getTreasLoc(), wm.getDir())) {
							break;
						}
					}
					// If we can get raft, we grab it
					if (needRaft && getRaftBackAfterTool(wm.getTreasLoc()) && !wm.holdRaft()) {
						if (getRaftUsingAxe(wm.getAgentLoc())) {
							pathToGetRaft();
							break;
						}
					}
					// If we can get to treasure using dynamite we win!, by boat
					if (wm.holdRaft() || wm.usingRaft()) {
						if (needRaft && getRaftBackAfterTool(wm.getTreasLoc())) {
							makeDynaPathAStar(wm.getAgentLoc(), wm.getTreasLoc(), wm.getDir(), wm.holdKey(),
									wm.holdAxe(), wm.getNumDyna(), wm.holdRaft(), wm.usingRaft());
							break;
						}
					}
				}
				// If we don't hold key/axe, we might need to use dynamite to
				// get it at this point
				boolean toolObtained = false;
				if (!wm.holdKey() && !wm.getKeyLocs().isEmpty()) {
					for (Point2D.Double KeyLoc : wm.getAxeLocs()) {
						if (theoreticalDynaPathAStar(wm.getAgentLoc(), KeyLoc, wm.getDir())) {
							toolObtained = true;
							break;
						}
					}
				}
				if (toolObtained)
					break;
				if (axeNeeded && !wm.holdAxe() && !wm.getAxeLocs().isEmpty()) {
					for (Point2D.Double axeLoc : wm.getAxeLocs()) {
						if (theoreticalDynaPathAStar(wm.getAgentLoc(), axeLoc, wm.getDir())) {
							toolObtained = true;
							break;
						}
					}
				}
				if (toolObtained)
					break;
			}
			// Priority 3: Pick up visible free tools
			if (getFreeTools())
				break;

			// Priority 4: Explore the island for unseen locations
			if (!wm.usingRaft()) {
				// Explore for free first
				if (exploreSpace())
					break;
				// Explore by letting chop down one tree
				if (wm.holdAxe() && !wm.holdRaft()) {
					if (getRaftUsingAxe(wm.getAgentLoc())) {
						pathToGetRaft();
						break;
					}
				}
				// If explored all space already
				if (wm.holdRaft()) {
					// If we know there is at least 2 trees, chop some to
					// explore
					if (wm.getTreeLocs().size() > 10 || chopTreesUsingAxe(wm.getAgentLoc(), 2)) {
						if (exploreTree())
							break;
					}
					// Use the raft to first explore the waters now
					if (exploreAnotherWater())
						break;

				}
			}
			if (wm.usingRaft()) {
				// Now on water to explore all the waters
				if (exploreWaters())
					break;
				// If have explored all the waters, there might be another
				// island to explore
				if (exploreAnotherIsland())
					break;
			}

		}
	}

	/**
	 * Helper function to explore space that can be reached via space
	 * 
	 * @return true if the going to the space can reveal more info about the
	 *         map, false is not
	 */
	private boolean exploreSpace() {
		AgentExplorer ae = new AgentExplorer(wm.getMap(), wm.getAgentLoc());
		Point2D.Double exploreFree = ae.getFreeTile(false, false, false);
		if (!exploreFree.equals(wm.getAgentLoc())) {
			makePathAStar(wm.getAgentLoc(), exploreFree, wm.getDir(), wm.holdKey(), false, 0, false, false);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Helper function to explore trees that can be reached via space
	 * 
	 * @Pre Agent holds an axe
	 * @return true if the going to the tree & chopping can reveal more info
	 *         about the map, false is not
	 */
	private boolean exploreTree() {
		AgentExplorer ae = new AgentExplorer(wm.getMap(), wm.getAgentLoc());
		Point2D.Double exploreCut = ae.getFreeTile(wm.holdKey(), true, false);
		if (!exploreCut.equals(wm.getAgentLoc())) {
			makePathAStar(wm.getAgentLoc(), exploreCut, wm.getDir(), wm.holdKey(), true, 0, false, false);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Helper function to explore the first water after all space and trees are
	 * explored
	 * 
	 * @Pre Agent holds an axe & raft
	 * @return true if exploring the first water point reveals more information
	 */
	private boolean exploreAnotherWater() {
		AgentExplorer ae = new AgentExplorer(wm.getMap(), wm.getAgentLoc());
		Point2D.Double exploreAnotherWater = ae.getFreeTile(wm.holdKey(), false, true);
		if (!exploreAnotherWater.equals(wm.getAgentLoc())) {
			makeFirstWaterPathAStar(wm.getAgentLoc(), exploreAnotherWater, wm.getDir(), wm.holdKey(), false);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Helper function to explore the water after first used raft all space and
	 * trees are explored
	 * 
	 * @Pre Agent is using the raft, has axe as well
	 * @return true if going to the water point reveals more information
	 */
	private boolean exploreWaters() {
		AgentExplorer ae = new AgentExplorer(wm.getMap(), wm.getAgentLoc());
		Point2D.Double exploreWater = ae.getWaterTile();
		if (!exploreWater.equals(wm.getAgentLoc())) {
			makeWaterPathAStar(wm.getAgentLoc(), exploreWater, wm.getDir());
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Helper function to explore another island after the other island is fully
	 * explored
	 * 
	 * @Pre Agent is using the raft, has axe as well
	 * @return true if the going to another island will reveal more information
	 */
	private boolean exploreAnotherIsland() {
		AgentExplorer ae = new AgentExplorer(wm.getMap(), wm.getAgentLoc());
		Point2D.Double exploreBackLand = ae.getFreeTile(wm.holdKey(), wm.holdAxe(), true);
		if (!exploreBackLand.equals(wm.getAgentLoc())) {
			makeBackIslandPathAStar(wm.getAgentLoc(), exploreBackLand, wm.getDir(), wm.holdKey(), true);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Helper function for agent to pick up free tools of key, axe and dynamites
	 * Free means can use key to unlock and pick up other things, using axe is
	 * not free as Raft may be a precious resource, and can't use dynamites to
	 * pick up more here
	 * 
	 * @return true if agent can pick up the tool for free
	 */
	private boolean getFreeTools() {
		if (!wm.holdKey() && !wm.getKeyLocs().isEmpty()) {
			for (Point2D.Double keyLoc : wm.getKeyLocs()) {
				AgentReach ar = new AgentReach(wm.getMap(), wm.getAgentLoc(), keyLoc);
				if (ar.reach(false, false, 0, false, false)) {
					if (!wm.usingRaft()) {
						makePathAStar(wm.getAgentLoc(), keyLoc, wm.getDir(), false, false, 0, false, false);
						return true;
					}
				}
				if (ar.reach(false, false, 0, false, true)) {
					if (wm.usingRaft() && getRaftBackAfterTool(keyLoc)) {
						makePathAStar(wm.getAgentLoc(), keyLoc, wm.getDir(), false, false, 0, false, true);
						return true;
					}
				}
			}
		}
		if (!wm.holdAxe() && !wm.getAxeLocs().isEmpty()) {
			for (Point2D.Double axeLoc : wm.getAxeLocs()) {
				AgentReach ar = new AgentReach(wm.getMap(), wm.getAgentLoc(), axeLoc);
				if (ar.reach(wm.holdKey(), false, 0, false, false)) {
					if (!wm.usingRaft()) {
						makePathAStar(wm.getAgentLoc(), axeLoc, wm.getDir(), wm.holdKey(), false, 0, false, false);
						return true;
					}
				}
				if (ar.reach(wm.holdKey(), false, 0, false, true)) {
					if (wm.usingRaft() && getRaftBackAfterTool(axeLoc)) {
						makePathAStar(wm.getAgentLoc(), axeLoc, wm.getDir(), wm.holdKey(), false, 0, false, true);
						return true;
					}
				}
			}
		}
		if (!wm.getDynaLocs().isEmpty()) {
			for (Point2D.Double dynaLoc : wm.getDynaLocs()) {
				AgentReach ar = new AgentReach(wm.getMap(), wm.getAgentLoc(), dynaLoc);
				if (!wm.usingRaft()) {
					if (ar.reach(wm.holdKey(), false, 0, false, false)) {
						makePathAStar(wm.getAgentLoc(), dynaLoc, wm.getDir(), wm.holdKey(), false, 0, false, false);
						return true;
					}
					if (getRaftBackAfterTool(dynaLoc) && !wm.holdRaft()) {
						if (!wm.holdRaft()) {
							if (getRaftUsingAxe(wm.getAgentLoc())) {
								pathToGetRaft();
								break;
							}
						}
						makeIslandPathAStar(wm.getAgentLoc(), dynaLoc, wm.getDir(), wm.holdKey(), false, 0, true,
								false);
						return true;
					}

				}
				if (wm.usingRaft()) {
					if (ar.reach(wm.holdKey(), false, 0, false, true)) {
						if (getRaftBackAfterTool(dynaLoc)) {
							makeBackIslandPathAStar(wm.getAgentLoc(), dynaLoc, wm.getDir(), wm.holdKey(), wm.holdAxe());
							return true;
						}
					}
				}
			}
		}
		if (wm.holdAxe() && !wm.holdRaft() && !wm.usingRaft()) {
			if (getRaftUsingAxe(wm.getAgentLoc())) {
				pathToGetRaft();
				return true;
			}
		}
		return false;
	}

	/**
	 * Helper function to see if it is possible to get raft using an axe from
	 * fromLoc
	 * 
	 * @Pre Agent has axe
	 * @param fromLoc
	 *            Point2D to check whether the visible map has trees nearby
	 * @return true if agent can get raft using an axe from the location
	 */
	private boolean getRaftUsingAxe(Point2D.Double fromLoc) {
		if (!wm.getTreeLocs().isEmpty()) {
			for (Point2D.Double treeLoc : wm.getTreeLocs()) {
				AgentReach nr = new AgentReach(wm.getMap(), fromLoc, treeLoc);
				if (nr.reach(wm.holdKey(), wm.holdAxe(), wm.getNumDyna(), wm.holdRaft(), wm.usingRaft())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Helper function to see if it safe to chop trees, if there are many
	 * 
	 * @Pre Agent has axe
	 * @param fromLoc
	 *            Point2D to check whether the visible map has trees nearby
	 * @param safeToChop
	 *            amount of trees deemed appropriate so that can chop to explore
	 *            more
	 * @return true there are enough trees near the location that are safe to
	 *         chop
	 */
	private boolean chopTreesUsingAxe(Point2D.Double fromLoc, int safeToChop) {
		int counter = 0;
		int fromX = (int) fromLoc.getX();
		int fromY = (int) fromLoc.getY();
		if (!wm.getTreeLocs().isEmpty()) {
			for (Point2D.Double treeLoc : wm.getTreeLocs()) {
				int xDiff = Math.abs((int) treeLoc.getX() - fromX);
				int yDiff = Math.abs((int) treeLoc.getY() - fromY);
				if (xDiff > 10 || yDiff > 10)
					continue;
				AgentReach nr = new AgentReach(wm.getMap(), fromLoc, treeLoc);
				if (nr.reach(wm.holdKey(), wm.holdAxe(), wm.getNumDyna(), wm.holdRaft(), wm.usingRaft())) {
					counter++;
					if (counter == safeToChop) {
						return true;
					}
				}
			}
		}
		if (!wm.getTreeLocs().isEmpty()) {
			for (Point2D.Double treeLoc : wm.getTreeLocs()) {
				int xDiff = Math.abs((int) treeLoc.getX() - fromX);
				int yDiff = Math.abs((int) treeLoc.getY() - fromY);
				if (xDiff <= 10 || yDiff <= 10)
					continue;
				AgentReach nr = new AgentReach(wm.getMap(), fromLoc, treeLoc);
				if (nr.reach(wm.holdKey(), wm.holdAxe(), wm.getNumDyna(), wm.holdRaft(), wm.usingRaft())) {
					counter++;
					if (counter == safeToChop) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Helper function to make a path to get a raft
	 * 
	 * @Pre There has to be a reachable tree nearby
	 */
	private void pathToGetRaft() {
		if (!wm.getTreeLocs().isEmpty()) {
			for (Point2D.Double treeLoc : wm.getTreeLocs()) {
				AgentReach nr = new AgentReach(wm.getMap(), wm.getAgentLoc(), treeLoc);
				if (nr.reach(wm.holdKey(), wm.holdAxe(), 0, false, false)) {
					makePathAStar(wm.getAgentLoc(), treeLoc, wm.getDir(), wm.holdKey(), wm.holdAxe(), 0, false, false);
					return;
				}
			}
		}
	}

	/**
	 * Helper function to see if can get raft back after getting a tool
	 * 
	 * @Pre agent holds an axe
	 * @param toolLoc
	 *            location of the tool that can check visible trees nearby
	 * @return true if there is a tree reachable for free near the tool
	 *         location, false otherwise
	 */
	private boolean getRaftBackAfterTool(Point2D.Double toolLoc) {
		if (!wm.getTreeLocs().isEmpty()) {
			for (Point2D.Double treeLoc : wm.getTreeLocs()) {
				AgentReach nr = new AgentReach(wm.getMap(), toolLoc, treeLoc);
				if (nr.reach(wm.holdKey(), wm.holdAxe(), 0, false, false)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Performs an A* search on agent map from startLoc to endLoc location given
	 * agent direction and tools held Obtains path and creates a list of moves
	 * that the agent takes to reach the endLoc and adds to agentMoves queue
	 * 
	 * @Pre endLoc can be reached already
	 * @param startLoc
	 *            the location of agent
	 * @param endLoc
	 *            the location of destination agent wants to reach
	 * @param curDirection
	 *            direction agent is facing
	 * @param hasKey
	 *            if the agent holds a key
	 * @param hasAxe
	 *            if the agent holds an axe
	 * @param numDyna
	 *            if the agent has any dynamites
	 * @param haveRaft
	 *            if the agent has a raft
	 * @param usingRaft
	 *            if the agent is already using raft
	 */
	private void makePathAStar(Point2D.Double startLoc, Point2D.Double endLoc, int curDirection, boolean hasKey,
			boolean hasAxe, int numDyna, boolean haveRaft, boolean usingRaft) {
		// New AStar search
		AStar a = new AStar(wm.getMap(), startLoc, endLoc);
		a.callSearch(hasKey, hasAxe, numDyna, haveRaft, usingRaft);
		// Get optimal path
		LinkedList<Point2D.Double> path = a.getSuccessPath();
		// add startLocing position to end of path (before reversal)
		path.addLast(startLoc);

		// Iterate through moves in reverse so they are presented as moves from
		// startLoc -> endLoc
		// Not taking last tile as it is our destination (t = 0)
		for (int t = path.size() - 1; t >= 1; t--) {
			Point2D.Double currPoint = path.get(t);

			// Check what direction we are going in (NORTH, SOUTH, WEST, EAST)
			int directionHeaded = getNextTileDir(currPoint, path.get(t - 1));
			// Get list of rotation moves needed before we go forward
			LinkedList<Character> alignMoves = getMoveAlign(curDirection, directionHeaded);
			// Add rotation moves to agentMoves
			agentMoves.addAll(alignMoves);
			// Update curDirection to reflect rotation
			curDirection = directionHeaded;

			// Check if we need to cut down a tree or unlock a door
			char nextTile = wm.getMap().get(wm.getFrontTile(currPoint, curDirection));
			if (nextTile == WorldModel.OBSTACLE_TREE) {
				agentMoves.add(WorldModel.INSTRUCT_CHOP);
			} else if (nextTile == WorldModel.OBSTACLE_DOOR) {
				agentMoves.add(WorldModel.INSTRUCT_UNLOCK);
			}
			agentMoves.add(WorldModel.INSTRUCT_FORWARD); // Now we also need 1
															// forward move
		}
	}

	/**
	 * Performs an A* search on agent map from startLoc to endLoc location given
	 * agent direction and tools held Obtains path and creates a list of moves
	 * that the agent takes to reach the endLoc and adds to agentMoves queue On
	 * this path agent prefers to stay on water until it reaches another island
	 *
	 * @Pre endLoc can be reached already
	 * @param startLoc
	 *            the location of agent
	 * @param endLoc
	 *            the location of destination agent wants to reach
	 * @param curDirection
	 *            direction agent is facing
	 * @param hasKey
	 *            if the agent holds a key
	 * @param hasAxe
	 *            if the agent holds an axe
	 * @param numDyna
	 *            if the agent has any dynamites
	 * @param haveRaft
	 *            if the agent has a raft
	 * @param usingRaft
	 *            if the agent is already using raft
	 */
	private void makeIslandPathAStar(Point2D.Double startLoc, Point2D.Double endLoc, int curDirection, boolean hasKey,
			boolean hasAxe, int numDyna, boolean haveRaft, boolean usingRaft) {
		AStar a = new AStar(wm.getMap(), startLoc, endLoc);
		a.callIslandSearch(hasKey, hasAxe, numDyna, haveRaft, usingRaft);
		LinkedList<Point2D.Double> path = a.getSuccessPath();
		path.addLast(startLoc);
		for (int t = path.size() - 1; t >= 1; t--) {
			Point2D.Double currPoint = path.get(t);
			int directionHeaded = getNextTileDir(currPoint, path.get(t - 1));
			LinkedList<Character> alignMoves = getMoveAlign(curDirection, directionHeaded);
			agentMoves.addAll(alignMoves);
			curDirection = directionHeaded;
			char nextTile = wm.getMap().get(wm.getFrontTile(currPoint, curDirection));
			if (nextTile == WorldModel.OBSTACLE_TREE) {
				agentMoves.add(WorldModel.INSTRUCT_CHOP);
			} else if (nextTile == WorldModel.OBSTACLE_DOOR) {
				agentMoves.add(WorldModel.INSTRUCT_UNLOCK);
			}
			agentMoves.add(WorldModel.INSTRUCT_FORWARD);
		}
	}

	/**
	 * Performs an A* search on agent map from startLoc to endLoc location given
	 * agent direction and tools held Obtains path and creates a list of moves
	 * that the agent takes to reach the endLoc and adds to agentMoves queue On
	 * this path agent prefers to stay on water until it reaches another island
	 * by which it is forced to use dynamites
	 * 
	 * @Pre endLoc can be reached already
	 * @param startLoc
	 *            the location of agent
	 * @param endLoc
	 *            the location of destination agent wants to reach
	 * @param curDirection
	 *            direction agent is facing
	 * @param hasKey
	 *            if the agent holds a key
	 * @param hasAxe
	 *            if the agent holds an axe
	 * @param numDyna
	 *            if the agent has any dynamites
	 * @param haveRaft
	 *            if the agent has a raft
	 * @param usingRaft
	 *            if the agent is already using raft
	 */
	private void makeDynaPathAStar(Point2D.Double startLoc, Point2D.Double endLoc, int curDirection, boolean hasKey,
			boolean hasAxe, int numDyna, boolean haveRaft, boolean usingRaft) {
		AStar a = new AStar(wm.getMap(), startLoc, endLoc); // New AStar search
		a.callDynamiteSearch(hasKey, hasAxe, numDyna, haveRaft, usingRaft);
		LinkedList<Point2D.Double> path = a.getSuccessPath(); // Get optimal
																// path
		path.addLast(startLoc); // add startLocing position to end of path
								// (before reversal)
		for (int t = path.size() - 1; t >= 1; t--) {
			Point2D.Double currPoint = path.get(t);
			int directionHeaded = getNextTileDir(currPoint, path.get(t - 1));
			LinkedList<Character> alignMoves = getMoveAlign(curDirection, directionHeaded);
			agentMoves.addAll(alignMoves);
			curDirection = directionHeaded;
			char nextTile = wm.getMap().get(wm.getFrontTile(currPoint, curDirection));
			if (nextTile == WorldModel.OBSTACLE_TREE) {
				agentMoves.add(WorldModel.INSTRUCT_CHOP);
			} else if (nextTile == WorldModel.OBSTACLE_DOOR) {
				agentMoves.add(WorldModel.INSTRUCT_UNLOCK);
			} else if (nextTile == WorldModel.OBSTACLE_WALL) {
				agentMoves.add(WorldModel.INSTRUCT_BLAST);
			}
			agentMoves.add(WorldModel.INSTRUCT_FORWARD); // Now we also need 1
															// forward move
		}
	}

	/**
	 * Performs an A* search on agent map from startLoc to endLoc location given
	 * agent direction and tools held Obtains path and creates a list of moves
	 * that the agent takes to reach the endLoc and adds to agentMoves queue On
	 * this path once agent first hits the water by using the raft, the path is
	 * broken and stops there
	 * 
	 * @Pre endLoc can be reached already
	 * @param startLoc
	 *            the location of agent
	 * @param endLoc
	 *            the location of destination agent wants to reach
	 * @param curDirection
	 *            direction agent is facing
	 * @param hasKey
	 *            if the agent holds a key
	 * @param hasAxe
	 *            if the agent holds an axe
	 */
	private void makeFirstWaterPathAStar(Point2D.Double startLoc, Point2D.Double endLoc, int curDirection,
			boolean hasKey, boolean hasAxe) {
		AStar a = new AStar(wm.getMap(), startLoc, endLoc); // New AStar search
		a.firstWaterSearch(hasKey, hasAxe);
		LinkedList<Point2D.Double> path = a.getSuccessPath();
		path.addLast(startLoc);
		for (int t = path.size() - 1; t >= 1; t--) {
			Point2D.Double currPoint = path.get(t);
			int directionHeaded = getNextTileDir(currPoint, path.get(t - 1));
			LinkedList<Character> alignMoves = getMoveAlign(curDirection, directionHeaded);
			agentMoves.addAll(alignMoves);
			curDirection = directionHeaded;
			char nextTile = wm.getMap().get(wm.getFrontTile(currPoint, curDirection));
			agentMoves.add(WorldModel.INSTRUCT_FORWARD);
			if (nextTile == WorldModel.OBSTACLE_WATER) {
				break;
			}
		}
	}

	/**
	 * Performs an A* search on agent map from startLoc to endLoc location given
	 * agent direction and tools held Obtains path and creates a list of moves
	 * that the agent takes to reach the endLoc and adds to agentMoves queue On
	 * this path the agent is already on water, and once it hits the land for
	 * first time it stops
	 * 
	 * @Pre endLoc can be reached already
	 * @param startLoc
	 *            the location of agent
	 * @param endLoc
	 *            the location of destination agent wants to reach
	 * @param curDirection
	 *            direction agent is facing
	 * @param hasKey
	 *            if the agent holds a key
	 * @param hasAxe
	 *            if the agent holds an axe
	 */
	private void makeBackLandPathAStar(Point2D.Double startLoc, Point2D.Double endLoc, int curDirection, boolean hasKey,
			boolean hasAxe) {
		AStar a = new AStar(wm.getMap(), startLoc, endLoc); // New AStar search
		a.callRiverSearch(hasKey);
		LinkedList<Point2D.Double> path = a.getSuccessPath();
		path.addLast(startLoc);
		for (int t = path.size() - 1; t >= 1; t--) {
			Point2D.Double currPoint = path.get(t);
			int directionHeaded = getNextTileDir(currPoint, path.get(t - 1));
			LinkedList<Character> alignMoves = getMoveAlign(curDirection, directionHeaded);
			agentMoves.addAll(alignMoves);
			curDirection = directionHeaded;
			char nextTile = wm.getMap().get(wm.getFrontTile(currPoint, curDirection));
			if (nextTile == WorldModel.OBSTACLE_TREE) {
				agentMoves.add(WorldModel.INSTRUCT_CHOP);
			} else if (nextTile == WorldModel.OBSTACLE_DOOR) {
				agentMoves.add(WorldModel.INSTRUCT_UNLOCK);
			} else if (nextTile == WorldModel.OBSTACLE_WALL) {
				agentMoves.add(WorldModel.INSTRUCT_BLAST);
			}
			agentMoves.add(WorldModel.INSTRUCT_FORWARD);
		}
	}

	/**
	 * Performs an A* search on agent map from startLoc to endLoc location given
	 * agent direction and tools held Obtains path and creates a list of moves
	 * that the agent takes to reach the endLoc and adds to agentMoves queue On
	 * this path there is only water tiles to be moves across
	 * 
	 * @Pre endLoc can be reached already
	 * @param startLoc
	 *            the location of agent
	 * @param endLoc
	 *            the location of destination agent wants to reach
	 * @param curDirection
	 *            direction agent is facing
	 * @param hasKey
	 *            if the agent holds a key
	 * @param hasAxe
	 *            if the agent holds an axe
	 */
	private void makeWaterPathAStar(Point2D.Double startLoc, Point2D.Double endLoc, int curDirection) {
		AStar a = new AStar(wm.getMap(), startLoc, endLoc);
		a.callWaterSearch();
		LinkedList<Point2D.Double> path = a.getSuccessPath();
		path.addLast(startLoc);
		for (int t = path.size() - 1; t >= 1; t--) {
			Point2D.Double currPoint = path.get(t);
			int directionHeaded = getNextTileDir(currPoint, path.get(t - 1));
			LinkedList<Character> alignMoves = getMoveAlign(curDirection, directionHeaded);
			agentMoves.addAll(alignMoves);
			curDirection = directionHeaded;
			agentMoves.add(WorldModel.INSTRUCT_FORWARD);
		}
	}

	/**
	 * Performs an A* search on agent map from startLoc to endLoc location given
	 * agent direction and tools held Obtains path and creates a list of moves
	 * that the agent takes to reach the endLoc and adds to agentMoves queue On
	 * this path the agent is already on water, and wants to make it back to a
	 * specific island
	 * 
	 * @Pre endLoc can be reached already
	 * @param startLoc
	 *            the location of agent
	 * @param endLoc
	 *            the location of destination agent wants to reach
	 * @param curDirection
	 *            direction agent is facing
	 * @param hasKey
	 *            if the agent holds a key
	 * @param hasAxe
	 *            if the agent holds an axe
	 */
	private void makeBackIslandPathAStar(Point2D.Double startLoc, Point2D.Double endLoc, int curDirection,
			boolean hasKey, boolean hasAxe) {
		AStar a = new AStar(wm.getMap(), startLoc, endLoc);
		a.callIslandSearch(hasKey, hasAxe, 0, true, true);
		LinkedList<Point2D.Double> path = a.getSuccessPath();
		path.addLast(startLoc);
		for (int t = path.size() - 1; t >= 1; t--) {
			Point2D.Double currPoint = path.get(t);
			int directionHeaded = getNextTileDir(currPoint, path.get(t - 1));
			LinkedList<Character> alignMoves = getMoveAlign(curDirection, directionHeaded);
			agentMoves.addAll(alignMoves);
			curDirection = directionHeaded;
			char nextTile = wm.getMap().get(wm.getFrontTile(currPoint, curDirection));
			if (nextTile == WorldModel.OBSTACLE_TREE) {
				agentMoves.add(WorldModel.INSTRUCT_CHOP);
			} else if (nextTile == WorldModel.OBSTACLE_DOOR) {
				agentMoves.add(WorldModel.INSTRUCT_UNLOCK);
			}
			agentMoves.add(WorldModel.INSTRUCT_FORWARD);
		}
	}

	/**
	 * Performs an A* search on agent map from startLoc to endLoc location given
	 * agent direction and tools held Obtains path and creates a list of moves
	 * that the agent takes to reach the endLoc and adds to agentMoves queue On
	 * this path the water is free to travel across, so will know whether or not
	 * the gcost will be < 0 to be beneficial to the agent to travel to the
	 * destination
	 * 
	 * @Pre endLoc can be reached already
	 * @param startLoc
	 *            the location of agent
	 * @param endLoc
	 *            the location of destination agent wants to reach
	 * @param curDirection
	 *            direction agent is facing
	 * @return true if agent can get to the location for free (given a raft)
	 */
	private boolean theoreticalRaftPathAStar(Point2D.Double startLoc, Point2D.Double endLoc, int curDirection) {
		AStar a = new AStar(wm.getMap(), startLoc, endLoc);
		int gcost = a.callTheoreticalRaftSearch();
		if (endLoc == wm.getTreasLoc()) {
			gcost--;
		}
		LinkedList<Point2D.Double> path = a.getSuccessPath();
		if (path.size() == 0)
			return false;
		path.addLast(startLoc);

		for (int t = path.size() - 1; t >= 1; t--) {
			Point2D.Double currPoint = path.get(t);
			int directionHeaded = getNextTileDir(currPoint, path.get(t - 1));
			LinkedList<Character> alignMoves = getMoveAlign(curDirection, directionHeaded);
			if (gcost < 0)
				agentMoves.addAll(alignMoves);
			curDirection = directionHeaded;
			if (gcost < 0) {
				agentMoves.add(WorldModel.INSTRUCT_FORWARD);
			}
		}

		if (gcost < 0)
			return true;
		return false;
	}

	/**
	 * Performs an A* search on agent map from startLoc to endLoc location given
	 * agent direction and tools held Obtains path and creates a list of moves
	 * that the agent takes to reach the endLoc and adds to agentMoves queue On
	 * this path the dynamites are accounted for, and made cheaper if the agent
	 * can obtain more dynamites on the way
	 * 
	 * @Pre endLoc can be reached already
	 * @param startLoc
	 *            the location of agent
	 * @param endLoc
	 *            the location of destination agent wants to reach
	 * @param curDirection
	 *            direction agent is facing
	 * @return true if agent can benefit by using dynamites to the destination
	 */
	private boolean theoreticalDynaPathAStar(Point2D.Double startLoc, Point2D.Double endLoc, int curDirection) {
		dynasNeeded = 0;
		AStar a = new AStar(wm.getMap(), startLoc, endLoc);
		int gcost = a.callTheoreticalDynaSearch();
		if (endLoc == new Point2D.Double(0, 0)) {
			gcost = gcost - (wm.getNumDyna() * 160);
			gcost--;
		}
		char tileCheck = wm.getMap().get(endLoc);
		if (tileCheck == WorldModel.TOOL_TREASURE) {
			gcost = gcost - (wm.getNumDyna() * 160);
			gcost--;
		}
		if (tileCheck == WorldModel.TOOL_AXE) {
			gcost = gcost - (wm.getNumDyna() * 160);
			gcost--;
		}
		if (tileCheck == WorldModel.TOOL_DYNAMITE) {
			for (int p = 0; p < 4; p++) {
				int nextX = (int) endLoc.getX();
				int nextY = (int) endLoc.getY();
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
				char nextTile = wm.getMap().get(nextPoint);
				if (nextTile == WorldModel.TOOL_DYNAMITE) {
					gcost = gcost - 160;
				}
			}
		}

		LinkedList<Point2D.Double> path = a.getSuccessPath();
		if (path.size() == 0)
			return false;
		path.addLast(startLoc);
		for (int i = path.size() - 1; i >= 1; i--) {
			Point2D.Double currPoint = path.get(i);
			int directionHeaded = getNextTileDir(currPoint, path.get(i - 1));
			LinkedList<Character> alignMoves = getMoveAlign(curDirection, directionHeaded);
			if (gcost < 0)
				agentMoves.addAll(alignMoves);
			curDirection = directionHeaded;
			char nextTile = wm.getMap().get(wm.getFrontTile(currPoint, curDirection));
			if (gcost < 0) {
				if (nextTile == WorldModel.OBSTACLE_TREE) {
					agentMoves.add(WorldModel.INSTRUCT_CHOP);
				} else if (nextTile == WorldModel.OBSTACLE_DOOR) {
					agentMoves.add(WorldModel.INSTRUCT_UNLOCK);
				} else if (nextTile == WorldModel.OBSTACLE_WALL) {
					agentMoves.add(WorldModel.INSTRUCT_BLAST);
				} else if (nextTile == WorldModel.TOOL_AXE) {
					agentMoves.add(WorldModel.INSTRUCT_FORWARD);
					return true;
				}
				agentMoves.add(WorldModel.INSTRUCT_FORWARD); // Now we also need
																// 1 forward
																// move
			} else {
				if (nextTile == WorldModel.OBSTACLE_WATER) {
					needRaft = true;
				}
				if (nextTile == WorldModel.OBSTACLE_WALL) {
					dynasNeeded++;
				}
			}
		}
		if (gcost < 0)
			return true;
		return false;
	}

	/**
	 * Helper function to count how many dynamites agent needs to reach the
	 * destination Performs an A* search on agent map from startLoc to endLoc
	 * location given agent direction and tools held Obtains path and creates a
	 * list of moves that the agent takes to reach the endLoc and adds to
	 * agentMoves queue On this path the dynamites are accounted for, and made
	 * cheaper if the agent can obtain more dynamites on the way
	 * 
	 * @Pre endLoc can be reached already
	 * @param startLoc
	 *            the location of agent
	 * @param endLoc
	 *            the location of destination agent wants to reach
	 * @param curDirection
	 *            direction agent is facing
	 */
	private void theoreticalDynaPathCounter(Point2D.Double startLoc, Point2D.Double endLoc, int curDirection) {
		dynasNeeded = 0;
		AStar a = new AStar(wm.getMap(), startLoc, endLoc);
		a.callTheoreticalDynaSearch();
		LinkedList<Point2D.Double> path = a.getSuccessPath();
		if (path.size() != 0) {
			path.addLast(startLoc);
			for (int i = path.size() - 1; i >= 1; i--) {
				Point2D.Double currPoint = path.get(i);
				int directionHeaded = getNextTileDir(currPoint, path.get(i - 1));
				curDirection = directionHeaded;
				char nextTile = wm.getMap().get(wm.getFrontTile(currPoint, curDirection));
				if (nextTile == WorldModel.OBSTACLE_WALL) {
					dynasNeeded++;
				} else if (nextTile == WorldModel.OBSTACLE_TREE) {
					axeNeeded = true;
				}
			}
		}
	}

	/**
	 * Helper function to see whether or not agent needs a raft to the
	 * destination Performs an A* search on agent map from startLoc to endLoc
	 * location given agent direction and tools held Obtains path and creates a
	 * list of moves that the agent takes to reach the endLoc and adds to
	 * agentMoves queue On this path the water is free to travel across, so will
	 * know whether or not the gcost will be < 0 to be beneficial to the agent
	 * to travel to the destination.
	 * 
	 * @Pre endLoc can be reached already
	 * @param startLoc
	 *            the location of agent
	 * @param endLoc
	 *            the location of destination agent wants to reach
	 * @param curDirection
	 *            direction agent is facing
	 */
	private void theoreticalRaftPathCounter(Point2D.Double startLoc, Point2D.Double endLoc, int curDirection) {
		AStar a = new AStar(wm.getMap(), startLoc, endLoc);
		a.callTheoreticalRaftSearch();
		LinkedList<Point2D.Double> path = a.getSuccessPath();
		path.addLast(startLoc);
		for (int i = path.size() - 1; i >= 1; i--) {
			Point2D.Double currPoint = path.get(i);
			int directionHeaded = getNextTileDir(currPoint, path.get(i - 1));
			curDirection = directionHeaded;
			char nextTile = wm.getMap().get(wm.getFrontTile(currPoint, curDirection));
			if (nextTile == WorldModel.OBSTACLE_WATER) {
				needRaft = true;
			}
		}
	}

	/**
	 * Helper function for the direction travelling from the startLoc point to
	 * endLoc point.
	 * 
	 * @param startLoc
	 *            the point to startLoc
	 * @param endLoc
	 *            the point to end
	 * @return agent direction to move towards (NORTH, EAST, SOUTH, WEST) to get
	 *         from startLoc to endLoc or -1 if none to change
	 */
	private int getNextTileDir(Point2D.Double startLoc, Point2D.Double endLoc) {
		int xDiff = (int) (endLoc.getX() - startLoc.getX());
		int yDiff = (int) (endLoc.getY() - startLoc.getY());
		int endingDir = -1;
		if (xDiff != 0) {
			if (xDiff < 0) {
				endingDir = WorldModel.WEST;
			} else {
				endingDir = WorldModel.EAST;
			}
		} else if (yDiff != 0) {
			if (yDiff < 0) {
				endingDir = WorldModel.SOUTH;
			} else {
				endingDir = WorldModel.NORTH;
			}
		}
		return endingDir;
	}

	/**
	 * Returns a list of moves for agent so initial and final directions are
	 * aligned.
	 * 
	 * @param initialDirection
	 *            direction player is facing
	 * @param finalDirection
	 *            final direction player should be facing
	 * @return linked list of instruction (L/R) so that agent is facing
	 *         finalDirection, null if same
	 */
	private LinkedList<Character> getMoveAlign(int initialDirection, int finalDirection) {
		LinkedList<Character> listMove = new LinkedList<>();
		int difference = initialDirection - finalDirection;
		if (difference == 0) {
			return listMove;
		} else if (difference == 2 || difference == -2) {
			listMove.add(WorldModel.INSTRUCT_LEFT);
			listMove.add(WorldModel.INSTRUCT_LEFT);
		} else if (difference == -1 || difference == 3) {
			listMove.add(WorldModel.INSTRUCT_RIGHT);
		} else if (difference == 1 || difference == -3) {
			listMove.add(WorldModel.INSTRUCT_LEFT);
		}
		return listMove;
	}

}