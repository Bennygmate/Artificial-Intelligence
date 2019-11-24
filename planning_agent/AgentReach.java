import java.util.*;
import java.awt.geom.Point2D;

/**
 * Representation of AgentReach class Performs reachable tests from startPoint
 * to endPoint Idea founded from Flood/Seed Fill algorithm
 * 
 * @author  bennygmate
 */
public class AgentReach {
	private final Point2D.Double startPoint;
	private final Point2D.Double endPoint;
	private final Map<Point2D.Double, Character> agentMap;

	/**
	 * Constructor
	 * 
	 * @param agentMap
	 *            contains agent perception
	 * @param startPoint
	 *            point to start checking
	 * @param endPoint
	 *            point connected to startPoint
	 */
	public AgentReach(Map<Point2D.Double, Character> agentMap, Point2D.Double startPoint, Point2D.Double endPoint) {
		this.agentMap = agentMap;
		this.startPoint = startPoint;
		this.endPoint = endPoint;
	}

	/**
	 * Performs test for whether agent can reach the end location depending on
	 * agent's items
	 * 
	 * @param haveKey
	 *            whether Agent holds key
	 * @param haveAxe
	 *            whether Agent holds axe
	 * @param numDyna
	 *            amount of dynamite agent holds
	 * @param haveRaft
	 *            whether Agent holds raft
	 * @param usingRaft
	 *            whether Agent is using raft
	 * @return true if goal point is reachable from start point, false otherwise
	 */

	public boolean reach(boolean haveKey, boolean haveAxe, int numDyna, boolean haveRaft, boolean usingRaft) {
		Queue<Point2D.Double> openQ = new ArrayDeque<>();
		openQ.add(startPoint);
		Set<Point2D.Double> reachableTiles = new HashSet<>();
		while (!openQ.isEmpty()) {
			Point2D.Double firstPoint = openQ.remove();
			if (firstPoint.equals(endPoint))
				return true;
			reachableTiles.add(firstPoint); // Mark as Seen
			// Add west, east, north, south tiles, like a plus sign
			for (int p = 0; p < 4; p++) {
				int nextX = (int) firstPoint.getX();
				int nextY = (int) firstPoint.getY();
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
				char nextTile = agentMap.get(nextPoint);
				if (!WorldModel.tilePassWithItem(nextTile, haveKey, haveAxe, numDyna, haveRaft, usingRaft)) {
					continue; // Tile can't be passed
				}
				if (!reachableTiles.contains(nextPoint))
					openQ.add(nextPoint);
			}
		}
		return false;
	}

	/**
	 * Performs test for whether agent can reach the end location (ONLY FOR
	 * WATER)
	 * 
	 * @return true if goal point is reachable from start point, false otherwise
	 */
	public boolean waterReach() {
		Queue<Point2D.Double> openQ = new ArrayDeque<>();
		openQ.add(startPoint);
		Set<Point2D.Double> reachableTiles = new HashSet<>();
		while (!openQ.isEmpty()) {
			Point2D.Double firstPoint = openQ.remove();
			if (firstPoint.equals(endPoint))
				return true;
			reachableTiles.add(firstPoint); // Mark as Seen
			// Add west, east, north, south tiles, like a plus sign
			for (int p = 0; p < 4; p++) {
				int nextX = (int) firstPoint.getX();
				int nextY = (int) firstPoint.getY();
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
				char nextTile = agentMap.get(nextPoint);
				if (!WorldModel.tilePassWater(nextTile)) {
					continue; // Tile can't be passed
				}
				if (!reachableTiles.contains(nextPoint))
					openQ.add(nextPoint);
			}
		}
		return false;
	}
}

