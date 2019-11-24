import java.util.*;
import java.awt.geom.Point2D;

/**
 * Representation of WorldModel class Maintains world model of environment and
 * agent tools
 * 
 * @author bennygmate
 */

public class WorldModel {
	// Directions
	final static int NORTH = 0;
	final static int EAST = 1;
	final static int SOUTH = 2;
	final static int WEST = 3;
	// Direction of Agent
	final static char FACING_NORTH = '^';
	final static char FACING_EAST = '>';
	final static char FACING_SOUTH = 'v';
	final static char FACING_WEST = '<';
	// Obstacles
	final static char OBSTACLE_SPACE = ' ';
	final static char OBSTACLE_TREE = 'T';
	final static char OBSTACLE_DOOR = '-';
	final static char OBSTACLE_WALL = '*';
	final static char OBSTACLE_WATER = '~';
	final static char OBSTACLE_UNSEEN = '?';
	final static char OBSTACLE_BOUNDARY = '.';
	// Temporary wall, to mark the blasting
	final static char OBSTACLE_BLASTWALL = 'W';
	// Tools
	final static char TOOL_AXE = 'a';
	final static char TOOL_KEY = 'k';
	final static char TOOL_DYNAMITE = 'd';
	final static char TOOL_TREASURE = '$';
	// Tool Inventory
	private boolean holdAxe;
	private boolean holdKey;
	private int numDynamite;
	private boolean holdTreas;
	private boolean holdRaft;
	private boolean usingRaft;
	// Agent Instructions
	final static char INSTRUCT_LEFT = 'L';
	final static char INSTRUCT_RIGHT = 'R';
	final static char INSTRUCT_FORWARD = 'F';
	final static char INSTRUCT_CHOP = 'C';
	final static char INSTRUCT_BLAST = 'B';
	final static char INSTRUCT_UNLOCK = 'U';
	// Map Size assuming environment is no larger than 80 x 80
	final static int MAX_MAP_X = 80;
	final static int MAX_MAP_Y = 80;
	// Class variables
	private Map<Point2D.Double, Character> map;
	private int agentX;
	private int agentY;
	private int agentFacing;
	// Performance Measure
	private int instructCounter;
	// Location of useful Tools
	private boolean isTreasVisible;
	private Point2D.Double treasureLocation;
	private LinkedList<Point2D.Double> axeLocations;
	private LinkedList<Point2D.Double> keyLocations;
	private LinkedList<Point2D.Double> dynamiteLocations;
	private LinkedList<Point2D.Double> treeLocations;
	private LinkedList<Point2D.Double> wallLocations;

	/**
	 * Constructor
	 */
	public WorldModel() {
		// Tools initially holding none
		this.holdAxe = false;
		this.holdKey = false;
		this.numDynamite = 0;
		this.holdTreas = false;
		this.holdRaft = false;
		this.usingRaft = false;
		// Performance Measure initialise
		this.instructCounter = 0;
		// Assume Agent starts at origin (0,0)
		this.agentX = 0;
		this.agentY = 0;
		// Fill map with unseen
		this.map = new HashMap<>();
		// Agent start origin is (0,0), so environment boundary is no larger
		// than 160 x 160
		for (int x = -MAX_MAP_X; x <= MAX_MAP_X; x++) {
			for (int y = -MAX_MAP_Y; y <= MAX_MAP_Y; y++) {
				this.map.put(new Point2D.Double(x, y), OBSTACLE_UNSEEN);
			}
		}
		// From Agent POV, facing NORTH
		this.agentFacing = SOUTH;
		this.map.put(new Point2D.Double(0, 0), FACING_NORTH);
		// Location variables
		this.isTreasVisible = false;
		this.axeLocations = new LinkedList<>();
		this.keyLocations = new LinkedList<>();
		this.dynamiteLocations = new LinkedList<>();
		this.treeLocations = new LinkedList<>();
		this.wallLocations = new LinkedList<>();
	}

	/**
	 * Updates the agent's perception based on environment View is rotated so to
	 * align with static initial map direction (NORTH) Tiles in the view are
	 * updated to agent map (overwritten) Stores new new tools as seen: axe,
	 * key, dynamite, treasure If treasure is visible it sets it as true for
	 * agent planning
	 * 
	 * @param view
	 *            2D array containing 5x5 grid with agent in middle
	 */
	public void updateFromView(char view[][]) {
		// Rotate view clockwise so view is facing NORTH (Agent starting
		// direction)
		int rotationNum = 0;
		switch (agentFacing) {
		case NORTH:
			break;
		case SOUTH:
			rotationNum = 2;
			break;
		case WEST:
			rotationNum = 3;
			break;
		case EAST:
			rotationNum = 1;
			break;
		}
		for (int i = 0; i < rotationNum; ++i) {
			view = rotateClockwise(view);
		}
		// Update view as according to initial offset
		for (int y = 0; y < 5; y++) {
			for (int x = 0; x < 5; x++) {
				char currTile = view[y][x];
				int xFinal = agentX + (x - 2);
				int yFinal = agentY + (2 - y);
				// Agent tile, update facing
				if (x == 2 && y == 2) {
					switch (agentFacing) {
					case NORTH:
						currTile = FACING_NORTH;
						break;
					case SOUTH:
						currTile = FACING_SOUTH;
						break;
					case WEST:
						currTile = FACING_WEST;
						break;
					case EAST:
						currTile = FACING_EAST;
						break;
					}
				}
				Point2D.Double newPoint = new Point2D.Double(xFinal, yFinal);
				// Save the locations of important tools
				if (currTile == TOOL_AXE && !axeLocations.contains(newPoint)) {
					axeLocations.add(newPoint);
				} else if (currTile == TOOL_KEY && !keyLocations.contains(newPoint)) {
					keyLocations.add(newPoint);
				} else if (currTile == TOOL_DYNAMITE && !dynamiteLocations.contains(newPoint)) {
					dynamiteLocations.add(newPoint);
				} else if (currTile == TOOL_TREASURE && !isTreasVisible) {
					treasureLocation = newPoint;
					isTreasVisible = true;
				} else if (currTile == OBSTACLE_TREE && !treeLocations.contains(newPoint)) {
					treeLocations.add(newPoint);
				} else if (currTile == OBSTACLE_WALL && !wallLocations.contains(newPoint)) {
					wallLocations.add(newPoint);
				}
				if (currTile == OBSTACLE_SPACE) {
					if (treeLocations.contains(newPoint)) {
						// Chopped down tree
						treeLocations.remove(newPoint);
					} else if (wallLocations.contains(newPoint)) {
						wallLocations.remove(newPoint);
					}
				}
				// Update tile in map
				map.put(newPoint, currTile);
			}
		}
	}

	/**
	 * Update agent map and tool inventory before agent acts
	 * 
	 * @param move
	 *            the move (as a character) that has been made
	 */
	public void updateWorldModel(char move) {
		instructCounter++;
		move = Character.toUpperCase(move); // Uppercase Instructions
		// Get tile directly in front of us, this is the tile we will be moving
		// onto in this next move
		Point2D.Double currTilePoint = new Point2D.Double(agentX, agentY);
		Point2D.Double nextTilePoint = getFrontTile(currTilePoint);
		char frontTile = map.get(nextTilePoint);

		switch (move) {
		case 'L': // Turn left
			switch (agentFacing) {
			case NORTH:
				agentFacing = WEST;
				break;
			case SOUTH:
				agentFacing = EAST;
				break;
			case WEST:
				agentFacing = SOUTH;
				break;
			case EAST:
				agentFacing = NORTH;
				break;
			}
			break;
		case 'R': // Turn right
			switch (agentFacing) {
			case NORTH:
				agentFacing = EAST;
				break;
			case SOUTH:
				agentFacing = WEST;
				break;
			case WEST:
				agentFacing = NORTH;
				break;
			case EAST:
				agentFacing = SOUTH;
				break;
			}
			break;
		case 'F': // (Try to) move forward
			// Can't move forward into tree, door or wall
			if (frontTile == OBSTACLE_TREE)
				break;
			if (frontTile == OBSTACLE_DOOR)
				break;
			if (frontTile == OBSTACLE_WALL)
				break;
			// Agent moving to water has a raft
			if (frontTile == OBSTACLE_WATER) {
				holdRaft = false; // we used our raft
				usingRaft = true;
			}
			// Agent loses raft once back on land
			if (tilePassNoItem(frontTile) && usingRaft) {
				usingRaft = false;
			}
			// Agent moves to a tool
			if (frontTile == TOOL_DYNAMITE) {
				dynamiteLocations.remove(nextTilePoint);
				numDynamite++;
			} else if (frontTile == TOOL_AXE) {
				axeLocations.remove(nextTilePoint);
				holdAxe = true;
			} else if (frontTile == TOOL_KEY) {
				keyLocations.remove(nextTilePoint);
				holdKey = true;
			} else if (frontTile == TOOL_TREASURE) {
				// treasureLocation = null;
				holdTreas = true;
			}
			// Update agent location
			switch (agentFacing) {
			case NORTH:
				agentY += 1;
				break;
			case SOUTH:
				agentY -= 1;
				break;
			case WEST:
				agentX -= 1;
				break;
			case EAST:
				agentX += 1;
				break;
			}
			break;
		case 'C': // (try to) chop down tree using an axe
			if (frontTile == OBSTACLE_TREE && !usingRaft) {
				holdRaft = true;
				treeLocations.remove(nextTilePoint);
			}
			break;
		case 'B': // (try to) blast a wall or tree, using dynamite
			if (frontTile == OBSTACLE_BLASTWALL) {
				numDynamite--;
			}
			if (frontTile == OBSTACLE_WALL) {
				numDynamite--;
			}
			if (frontTile == OBSTACLE_TREE) {
				numDynamite--;
			}
			break;
		case 'U': // (try to) unlock door
			break;
		}
	}

	/**
	 * Rotates a 2D array representing a map clockwise.
	 * 
	 * @param oldView
	 *            2D array to rotate clockwise
	 * @return 2D array rotated in a clockwise direction
	 */
	private static char[][] rotateClockwise(char[][] oldView) {
		final int X = oldView.length;
		final int Y = oldView[0].length;
		char[][] rotatedView = new char[Y][X];
		for (int row = 0; row < X; row++) {
			for (int column = 0; column < Y; column++) {
				rotatedView[column][X - 1 - row] = oldView[row][column];
			}
		}
		return rotatedView;
	}

	/**
	 * Determines if a tile is passable (no items) given the game
	 * representations
	 * 
	 * @param tile
	 *            checking representation to see if can pass
	 * @return true if tile is possible without any items
	 */
	public static boolean tilePassNoItem(char tile) {
		if (tile == OBSTACLE_SPACE)
			return true;
		if (tile == TOOL_AXE)
			return true;
		if (tile == TOOL_KEY)
			return true;
		if (tile == TOOL_DYNAMITE)
			return true;
		if (tile == TOOL_TREASURE)
			return true;
		if (tile == FACING_NORTH)
			return true;
		if (tile == FACING_SOUTH)
			return true;
		if (tile == FACING_WEST)
			return true;
		if (tile == FACING_EAST)
			return true;
		return false;
	}

	/**
	 * Determines if a tile is passable (with items) given the game
	 * representations
	 * 
	 * @param tile
	 *            checking representation to see if can pass
	 * @param hasKey
	 *            Agent holds key, door can pass
	 * @param hasAxe
	 *            Agent holds axe, tree can pass
	 * @param numDyna
	 *            Agent holds some dynamites
	 * @param haveRaft
	 *            Agent chopped down a tree, so has a raft
	 * @param usingRaft
	 *            Agent stepped onto water following using raft
	 * @return true if tile is possible with specific items, false otherwise
	 */
	public static boolean tilePassWithItem(char tile, boolean haveKey, boolean haveAxe, int numDyna, boolean haveRaft,
			boolean usingRaft) {
		if (tilePassNoItem(tile))
			return true;
		if (tile == OBSTACLE_DOOR && haveKey)
			return true;
		if (tile == OBSTACLE_TREE && haveAxe)
			return true;
		if (tile == OBSTACLE_WATER && haveRaft)
			return true;
		if (tile == OBSTACLE_WATER && usingRaft)
			return true;
		if (tile == OBSTACLE_WALL && numDyna != 0)
			return true;
		if (tile == OBSTACLE_BLASTWALL && numDyna != 0)
			return true;
		return false;
	}

	/**
	 * Determines if a tile is passable (specific to water) given the game
	 * representations
	 * 
	 * @param tile
	 *            checking representation to see if can pass
	 * @return true if tile is possible on water
	 */
	public static boolean tilePassWater(char tile) {
		if (tile == FACING_NORTH)
			return true;
		if (tile == FACING_SOUTH)
			return true;
		if (tile == FACING_WEST)
			return true;
		if (tile == FACING_EAST)
			return true;
		if (tile == OBSTACLE_WATER)
			return true;
		return false;
	}

	/**
	 * Determines if a tile is passable (With dynamites and other tools) given
	 * the game representations
	 * 
	 * @param hasKey
	 *            Agent holds key, door can pass
	 * @param hasAxe
	 *            Agent holds axe, tree can pass
	 * @param numDyna
	 *            Agent holds some dynamites
	 * @param haveRaft
	 *            Agent chopped down a tree, so has a raft
	 * @param usingRaft
	 *            Agent stepped onto water following using raft
	 * @return true if tile is possible with specific items, false otherwise
	 */
	public static boolean tilePassDynamite(char tile, boolean haveKey, boolean haveAxe, int numDyna, boolean haveRaft,
			boolean usingRaft) {
		if (tilePassNoItem(tile))
			return true;
		if (tile == OBSTACLE_DOOR && haveKey)
			return true;
		if (tile == OBSTACLE_TREE && haveAxe)
			return true;
		if (tile == OBSTACLE_WATER && haveRaft)
			return true;
		if (tile == OBSTACLE_WATER && usingRaft)
			return true;
		if (tile == OBSTACLE_WALL && numDyna != 0)
			return true;
		if (tile == OBSTACLE_BLASTWALL && numDyna != 0)
			return true;
		return false;
	}

	/**
	 * Parser to getFrontTile with agentFacing
	 * 
	 * @param behindTile
	 *            the tile behind front tile
	 * @return point of tile in front
	 */
	public Point2D.Double getFrontTile(Point2D.Double behindTile) {
		return getFrontTile(behindTile, agentFacing);
	}

	/**
	 * Returns tile in front of behindTile.
	 * 
	 * @param tile
	 *            the tile we wish to look in front of
	 * @param curDirection
	 *            the direction we are facing (NORTH, EAST, SOUTH, WEST)
	 * @return point of tile in front
	 */
	public Point2D.Double getFrontTile(Point2D.Double behindTile, int agentFacing) {
		int frontX = (int) behindTile.getX();
		int frontY = (int) behindTile.getY();
		switch (agentFacing) {
		case NORTH:
			frontY += 1;
			break;
		case SOUTH:
			frontY -= 1;
			break;
		case WEST:
			frontX -= 1;
			break;
		case EAST:
			frontX += 1;
			break;
		}
		return new Point2D.Double(frontX, frontY);
	}

	/**
	 * @return true if agent holds treasure
	 */
	public boolean holdTreas() {
		return holdTreas;
	}

	/**
	 * @return true if agent holds raft
	 */
	public boolean holdRaft() {
		return holdRaft;
	}

	/**
	 * @return true if agent using raft
	 */
	public boolean usingRaft() {
		return usingRaft;
	}

	/**
	 * @return agent holding integer of dynamites
	 */
	public int getNumDyna() {
		return numDynamite;
	}

	/**
	 * @return true if agent can see treasure
	 */
	public boolean isTreasVisible() {
		return isTreasVisible;
	}

	/**
	 * @return agent location as a 2D Point
	 */
	public Point2D.Double getAgentLoc() {
		return new Point2D.Double(agentX, agentY);
	}

	/**
	 * @return agent facing direction
	 */
	public int getDir() {
		return agentFacing;
	}

	/**
	 * @return true if agent holds key
	 */
	public boolean holdKey() {
		return holdKey;
	}

	/**
	 * @return true if agent holds axe
	 */
	public boolean holdAxe() {
		return holdAxe;
	}

	/**
	 * @return the agent world model map
	 */
	public Map<Point2D.Double, Character> getMap() {
		return map;
	}

	/**
	 * @return location of treasure as 2D Point
	 */
	public Point2D.Double getTreasLoc() {
		return treasureLocation;
	}

	/**
	 * @return Location of keys
	 */
	public List<Point2D.Double> getKeyLocs() {
		return keyLocations;
	}

	/**
	 * @return Location of axes
	 */
	public List<Point2D.Double> getAxeLocs() {
		return axeLocations;
	}

	/**
	 * @return Location of dynamites
	 */
	public List<Point2D.Double> getDynaLocs() {
		return dynamiteLocations;
	}

	/**
	 * @return Location of trees
	 */
	public List<Point2D.Double> getTreeLocs() {
		return treeLocations;
	}

	/**
	 * @return Location of Walls
	 */
	public List<Point2D.Double> getWallLocs() {
		return wallLocations;
	}

}
