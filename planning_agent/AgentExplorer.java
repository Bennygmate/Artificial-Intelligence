import java.util.*;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

/**
 * Representation of AgentExplorer class Contains methods to find points on the
 * map which will reveal hidden information Explores outwards in a 5x5 grid from
 * any given point
 * 
 * @author z3460693
 */
public class AgentExplorer {
	private final Map<Point2D.Double, Character> agentMap;
	private final Point2D.Double agentPoint;
	// Points of 24 surrounding points from agent location
	private static final List<Double> explorePoint = Arrays.asList(new Point2D.Double(-2, -2),
			new Point2D.Double(-2, -1), new Point2D.Double(-2, 0), new Point2D.Double(-2, 1), new Point2D.Double(-2, 2),
			new Point2D.Double(-1, -2), new Point2D.Double(-1, -1), new Point2D.Double(-1, 0),
			new Point2D.Double(-1, 1), new Point2D.Double(-1, 2), new Point2D.Double(0, -2), new Point2D.Double(0, -1),
			new Point2D.Double(0, 1), new Point2D.Double(0, 2), new Point2D.Double(1, -2), new Point2D.Double(1, -1),
			new Point2D.Double(1, 0), new Point2D.Double(1, 1), new Point2D.Double(1, 2), new Point2D.Double(2, -2),
			new Point2D.Double(2, -1), new Point2D.Double(2, 0), new Point2D.Double(2, 1), new Point2D.Double(2, 2));

	/**
	 * Constructor
	 * 
	 * @param agentMap
	 *            contains agent perception
	 * @param agentPoint
	 *            point to explore from
	 */
	public AgentExplorer(Map<Point2D.Double, Character> agentMap, Point2D.Double agentPoint) {
		this.agentMap = agentMap;
		this.agentPoint = agentPoint;
	}

	/**
	 * Returns a point that will be able to explore more information in agent
	 * environment
	 * 
	 * @param haveKey
	 *            agent has key, can pass doors
	 * @param haveAxe
	 *            agent has axe, can pass trees
	 * @param haveRaft
	 *            agent has raft, can pass waters
	 * @return agentPoint, if no point found, else there is unknown points
	 */
	public Point2D.Double getFreeTile(boolean haveKey, boolean haveAxe, boolean haveRaft) {
		// Begin generating points based on current location and spiral out
		int x = 0;
		int y = 0;
		int tmpX = 0;
		int tmpY = -1;
		int maxX = WorldModel.MAX_MAP_X;
		int maxY = WorldModel.MAX_MAP_Y;
		int maxTile = WorldModel.MAX_MAP_X * WorldModel.MAX_MAP_Y;
		int agentX = (int) agentPoint.getX();
		int agentY = (int) agentPoint.getY();

		for (int tileCount = 0; tileCount <= maxTile;) {
			if ((x == maxX) || (y == maxY))
				break;
			// Update inspection point by offset to agent start point
			Point2D.Double newPoint = new Point2D.Double(x + agentX, y + agentY);
			if (agentMap.get(newPoint) != null) {
				tileCount++;
				char tileCheck = agentMap.get(newPoint);
				if (WorldModel.tilePassWithItem(tileCheck, haveKey, haveAxe, 0, haveRaft, false)) {
					if (unseenPoint(newPoint)) {
						AgentReach ff = new AgentReach(agentMap, agentPoint, newPoint);
						if (ff.reach(haveKey, haveAxe, 0, haveRaft, false)) {
							if (!newPoint.equals(agentPoint))
								return newPoint;
						}
					}
				}

			}
			// Update tmpX, tmpY if checking straight line
			if ((x == y) || ((x < 0) && (x == -y)) || ((x > 0) && (x == 1 - y))) {
				int tmp = tmpX;
				tmpX = -tmpY;
				tmpY = tmp;
			}
			x += tmpX;
			y += tmpY;
		}
		return agentPoint;
	}

	/**
	 * Returns a point that will be able to explore more information in agent
	 * environment (WATER ONLY)
	 * 
	 * @return agentPoint, if no point found, else there is unknown points
	 */
	public Point2D.Double getWaterTile() {
		// Begin generating points based on current location and spiral
		int x = 0;
		int y = 0;
		int tmpX = 0;
		int tmpY = -1;
		int maxX = WorldModel.MAX_MAP_X;
		int maxY = WorldModel.MAX_MAP_Y;
		int maxTile = WorldModel.MAX_MAP_X * WorldModel.MAX_MAP_Y;
		int agentX = (int) agentPoint.getX();
		int agentY = (int) agentPoint.getY();
		for (int tileCount = 0; tileCount <= maxTile;) {
			if ((x == maxX) || (y == maxY))
				break;
			// Update inspection point by offset to agent start point
			Point2D.Double newPoint = new Point2D.Double(x + agentX, y + agentY);
			if (agentMap.get(newPoint) != null) {
				tileCount++;
				if (!newPoint.equals(agentPoint)) {
					char tileCheck = agentMap.get(newPoint);
					if (tileCheck == WorldModel.OBSTACLE_WATER) {
						if (unseenPoint(newPoint)) {
							AgentReach ff = new AgentReach(agentMap, agentPoint, newPoint);
							if (ff.waterReach()) {
								return newPoint;
							}
						}
					}
				}

			}
			if ((x == y) || ((x < 0) && (x == -y)) || ((x > 0) && (x == 1 - y))) {
				int tmp = tmpX;
				tmpX = -tmpY;
				tmpY = tmp;
			}

			x += tmpX;
			y += tmpY;
		}
		return agentPoint;
	}

	/**
	 * Checks 24 surrounding points of the point passed in, if it is unseen
	 * agent is curious and will want to look
	 * 
	 * @param point
	 *            the centre of point to check surrounding
	 * @return true if any of the 24 surrounding points is unseen, false
	 *         otherwise
	 */
	private boolean unseenPoint(Point2D.Double middlePoint) {
		for (Object o : explorePoint) { // For every 24 points
			Point2D.Double offset = (Point2D.Double) o;
			Point2D.Double viewPoint = new Point2D.Double(middlePoint.getX() + offset.getX(),
					middlePoint.getY() + offset.getY());
			if (agentMap.get(viewPoint) != null) {
				char viewPointTile = agentMap.get(viewPoint);
				// point is not completely visible (unknown to agent view)
				if (viewPointTile == WorldModel.OBSTACLE_UNSEEN)
					return true;
			}
		}
		return false;
	}
}
