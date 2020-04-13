package traffic3.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import rescuecore2.log.Logger;
import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Vector2D;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.Road;
import traffic3.manager.TrafficManager;
import traffic3.simulator.PathElement;
import traffic3.simulator.TrafficConstants;

/**
 * A TrafficAgent is a mobile object in the world.
 */
public class TrafficAgent {

	/**
	 * This class is used to compute and cache wall related information.
	 *
	 * @author goebelbe
	 *
	 */
	private static class WallInfo {
		private Line2D wall;
		private TrafficArea area;
		private double distance;
		private Point2D closest;
		private Point2D origin;
		private Line2D line;
		private Vector2D vector;

		/**
		 * Create a new WallInfo object from a Line2D in a TrafficArea.
		 *
		 * @param wall
		 *            The wall to cache.
		 * @param area
		 *            The area this wall belongs to.
		 */
		public WallInfo(Line2D wall, TrafficArea area) {
			this.wall = wall;
			this.area = area;
			this.distance = -1;
			this.closest = null;
			this.origin = null;
		}

		/**
		 * Get the shortest distance from the agent's position. The distance may
		 * not be accurate if the wall can't affect the agent in this microstep.
		 *
		 * @return The distance to the agent.
		 */
		public double getDistance() {
			return this.distance;
		}

		/**
		 * Recompute the distance to the agent and the closest point on the
		 * line.
		 *
		 * @param from
		 *            The position of the agent.
		 */
		public void computeClostestPoint(Point2D from) {
			if (from.equals(origin) && distance >= 0 && closest != null) {
				return;
			}
			origin = from;
			closest = GeometryTools2D.getClosestPointOnSegment(wall, origin);
			line = new Line2D(origin, closest);
			vector = line.getDirection();
			distance = vector.getLength();
		}

		/**
		 * Get the clostest point to the agent on the wall.
		 *
		 * @return The closest point.
		 */
		public Point2D getClosestPoint() {
			return closest;
		}

		/**
		 * Decrease the distance from the wall by an amount.
		 *
		 * @param d
		 *            The amount by which to decrease the distance.
		 */
		public void decreaseDistance(double d) {
			distance -= d;
		}

		/**
		 * Get the wall this WallInfo represents.
		 *
		 * @return The wall.
		 */
		public Line2D getWall() {
			return wall;
		}

		/**
		 * Get the line from the agent to the closest point on the wall.
		 *
		 * @return Line2D to wall.
		 */
		public Line2D getLine() {
			return line;
		}

		/**
		 * Get the vector from the agent to the closest point on the wall.
		 *
		 * @return Vector2D to wall.
		 */
		public Vector2D getVector() {
			return vector;
		}

		/**
		 * Get the are the wall lies in.
		 *
		 * @return The area of this wall.
		 */
		public TrafficArea getArea() {
			return area;
		}
	}

	private static final int D = 2;

	private static final int DEFAULT_POSITION_HISTORY_FREQUENCY = 60;

	private static final double NEARBY_THRESHOLD_SQUARED = 1000000;

	// Force towards destination
	private final double[] destinationForce = new double[D];

	// Force away from agents
	private final double[] agentsForce = new double[D];

	// Force away from walls
	private final double[] wallsForce = new double[D];

	// Location
	private final double[] location = new double[D];

	// Velocity
	private final double[] velocity = new double[D];

	// Force
	private final double[] force = new double[D];

	// List of blocking lines near the agent.
	private List<WallInfo> blockingLines;

	private double radius;
	private double velocityLimit;

	// The point this agent wants to reach.
	private Point2D finalDestination;

	// The path this agent wants to take.
	private Queue<PathElement> path;

	// The current (possibly intermediate) destination.
	private PathElement currentPathElement;
	private Point2D currentDestination;

	// The area the agent is currently in.
	private TrafficArea currentArea;

	private List<Point2D> positionHistory;
	private double totalDistance;
	private boolean savePositionHistory;
	private int positionHistoryFrequency;
	private int historyCount;

	private Human human;
	private TrafficManager manager;

	private boolean mobile;
	private boolean colocated;
	private boolean verbose;

	private TrafficArea startPosition;

	/**
	 * Construct a TrafficAgent.
	 *
	 * @param human
	 *            The Human wrapped by this object.
	 * @param manager
	 *            The traffic manager.
	 * @param radius
	 *            The radius of this agent in mm.
	 * @param velocityLimit
	 *            The velicity limit.
	 */
	public TrafficAgent(Human human, TrafficManager manager, double radius, double velocityLimit) {
		this.human = human;
		this.manager = manager;
		this.radius = radius;
		this.velocityLimit = velocityLimit;
		path = new LinkedList<PathElement>();
		positionHistory = new ArrayList<Point2D>();
		savePositionHistory = true;
		historyCount = 0;
		positionHistoryFrequency = DEFAULT_POSITION_HISTORY_FREQUENCY;
		mobile = true;
		blockingLines = new ArrayList<WallInfo>();
	}

	/**
	 * Get the Human wrapped by this object.
	 *
	 * @return The wrapped Human.
	 */
	public Human getHuman() {
		return human;
	}

	/**
	 * Get the maximum velocity of this agent.
	 *
	 * @return The maximum velocity.
	 */
	public double getMaxVelocity() {
		return velocityLimit;
	}

	/**
	 * Set the maximum velocity of this agent.
	 *
	 * @param vLimit
	 *            The new maximum velocity.
	 */
	public void setMaxVelocity(double vLimit) {
		velocityLimit = vLimit;
	}

	/**
	 * Get the TrafficArea the agent is currently in.
	 *
	 * @return The current TrafficArea.
	 */
	public TrafficArea getArea() {
		return currentArea;
	}

	/**
	 * Get the position history.
	 *
	 * @return The position history.
	 */
	public List<Point2D> getPositionHistory() {
		return Collections.unmodifiableList(positionHistory);
	}

	/**
	 * Get the distance travelled so far.
	 *
	 * @return The distance travelled.
	 */
	public double getTravelDistance() {
		return totalDistance;
	}

	/**
	 * Clear the position history and distance travelled.
	 */
	public void clearPositionHistory() {
		positionHistory.clear();
		historyCount = 0;
		totalDistance = 0;
	}

	/**
	 * Set the frequency of position history records. One record will be created
	 * every nth microstep.
	 *
	 * @param n
	 *            The new frequency.
	 */
	public void setPositionHistoryFrequency(int n) {
		positionHistoryFrequency = n;
	}

	/**
	 * Enable or disable position history recording.
	 *
	 * @param b
	 *            True to enable position history recording, false otherwise.
	 */
	public void setPositionHistoryEnabled(boolean b) {
		savePositionHistory = b;
	}

	/**
	 * Get the X coordinate of this agent.
	 *
	 * @return The X coordinate.
	 */
	public double getX() {
		return location[0];
	}

	/**
	 * Get the Y coordinate of this agent.
	 *
	 * @return The Y coordinate.
	 */
	public double getY() {
		return location[1];
	}

	/**
	 * Get the total X force on this agent.
	 *
	 * @return The total X force in N.
	 */
	public double getFX() {
		return force[0];
	}

	/**
	 * Get the total Y force on this agent.
	 *
	 * @return The total Y force in N.
	 */
	public double getFY() {
		return force[1];
	}

	/**
	 * Get the X velocity of this agent.
	 *
	 * @return The X velocity in mm/s.
	 */
	public double getVX() {
		return velocity[0];
	}

	/**
	 * Get the Y velocity of this agent.
	 *
	 * @return The Y velocity in mm/s.
	 */
	public double getVY() {
		return velocity[1];
	}

	/**
	 * Set the radius of this agent.
	 *
	 * @param r
	 *            The new radius in mm.
	 */
	public void setRadius(double r) {
		this.radius = r;
	}

	/**
	 * Get the radius of this agent.
	 *
	 * @return The radius in mm.
	 */
	public double getRadius() {
		return this.radius;
	}

	/**
	 * Set the path this agent wants to take.
	 *
	 * @param steps
	 *            The new path.
	 */
	public void setPath(List<PathElement> steps) {
		if (steps == null || steps.isEmpty()) {
			clearPath();
			return;
		}
		path.clear();
		path.addAll(steps);
		finalDestination = steps.get(steps.size() - 1).getGoal();
		currentDestination = null;
		currentPathElement = null;
		// Logger.debug(this + " destination set: " + path);
		// Logger.debug(this + " final destination set: " + finalDestination);
	}

	/**
	 * Clear the path.
	 */
	public void clearPath() {
		finalDestination = null;
		currentDestination = null;
		currentPathElement = null;
		path.clear();
	}

	/**
	 * Get the final destination.
	 *
	 * @return The final destination
	 */
	public Point2D getFinalDestination() {
		return finalDestination;
	}

	/**
	 * Get the current (possibly intermediate) destination.
	 *
	 * @return The current destination.
	 */
	public Point2D getCurrentDestination() {
		return currentDestination;
	}

	/**
	 * Get the current (possibly intermediate) path element.
	 *
	 * @return The current path element.
	 */
	public PathElement getCurrentElement() {
		return currentPathElement;
	}

	/**
	 * Get the current path.
	 *
	 * @return The path.
	 */
	public List<PathElement> getPath() {
		return Collections.unmodifiableList((List<PathElement>) path);
	}

	/**
	 * Set the location of this agent. This method will also update the position
	 * history (if enabled).
	 *
	 * @param x
	 *            location x
	 * @param y
	 *            location y
	 */
	public void setLocation(double x, double y) {
		if (currentArea == null || !currentArea.contains(x, y)) {
			if (currentArea != null) {
				currentArea.removeAgent(this);
			}
			TrafficArea newArea = manager.findArea(x, y);

			if (newArea == null) {
				Logger.warn(getHuman() + "moved outside area: " + this);
				return;
			}

			currentArea = newArea;
			findBlockingLines();
			currentArea.addAgent(this);
		}
		// Check current destination
		if (currentPathElement != null) {
			// If we just crossed the target edge then clear the current path
			// element
			if (currentDestination == currentPathElement.getGoal() && currentDestination != finalDestination) {
				// Did we cross the edge?
				if (currentPathElement.getEdgeLine() != null && crossedLine(location[0], location[1], x, y, currentPathElement.getEdgeLine())) {
					currentPathElement = null;
				}
				// Are we close enough to the goal point?
				else {
					double dx = x - currentDestination.getX();
					double dy = y - currentDestination.getY();
					double distanceSquared = dx * dx + dy * dy;
					if (distanceSquared < NEARBY_THRESHOLD_SQUARED) {
						currentPathElement = null;
					}
				}
			}
		}
		// Save position history
		if (savePositionHistory) {
			if (historyCount % positionHistoryFrequency == 0) {
				positionHistory.add(new Point2D(x, y));
			}
			historyCount++;

			// Update distance travelled
			double dx = x - location[0];
			double dy = y - location[1];
			totalDistance += Math.hypot(dx, dy);
		}
		location[0] = x;
		location[1] = y;
	}

	private boolean haveThisAreaInPath(TrafficArea newArea) {
		for (PathElement path : getPath()) {
			if (path.getAreaID().equals(newArea.getArea().getID()))
				return true;
		}
		return false;
	}

	/**
	 * Perform any pre-timestep activities required.
	 */
	public void beginTimestep() {
		findBlockingLines();
		if (insideBlockade()) {
			Logger.debug(this + " inside blockade");
			setMobile(false);
		}
		startPosition = currentArea;
	}

	/**
	 * Execute a microstep.
	 *
	 * @param dt
	 *            The amount of time to simulate in ms.
	 */
	public void step(double dt) {
		if (mobile) {
			updateWalls(dt);
			updateGoals();
			computeForces(dt);
			updatePosition(dt);
		}
	}

	/**
	 * Perform any post-timestep activities required.
	 */
	public void endTimestep() {
		handleOutOfActionCivilianMoves();

	}

	private void handleOutOfActionCivilianMoves() {
		if (!(getHuman() instanceof Civilian))
			return;
		if (currentArea.getArea().equals(startPosition.getArea()))
			return;
		if (!(currentArea.getArea() instanceof Building))
			return;
		if (getPath().isEmpty())
			return;
		if (haveThisAreaInPath(currentArea))
			return;

		Logger.warn(getHuman() + " moved to unplaned building(" + currentArea + ") " + this);
		TrafficArea newDest = getBestRoadNeighbor(currentArea, new HashSet<TrafficArea>());
		if (newDest == null) {
			Logger.warn(currentArea + " dosen't connected to any Road!");
			return;
		}
		setLocation(newDest.getArea().getX(), newDest.getArea().getY());

	}

	private TrafficArea getBestRoadNeighbor(TrafficArea area, HashSet<TrafficArea> checked) {
		checked.add(area);
		if (area.getArea() instanceof Road)
			return area;
		for (TrafficArea neighbor : manager.getNeighbours(area)) {
			if (neighbor.getArea() instanceof Road)
				return neighbor;
		}
		for (TrafficArea neighbor : manager.getNeighbours(area)) {
			if (checked.contains(neighbor))
				continue;
			TrafficArea result = getBestRoadNeighbor(neighbor, checked);
			if (result != null)
				return result;
		}
		return null;
	}

	/**
	 * Set whether this agent is mobile or not.
	 *
	 * @param m
	 *            True if this agent is mobile, false otherwise.
	 */
	public void setMobile(boolean m) {
		mobile = m;
	}

	/**
	 * Find out if this agent is mobile.
	 *
	 * @return True if this agent is mobile.
	 */
	public boolean isMobile() {
		return mobile;
	}

	/**
	 * Turn verbose logging on or off.
	 *
	 * @param b
	 *            True for piles of debugging output, false for smaller piles.
	 */
	public void setVerbose(boolean b) {
		verbose = b;
		Logger.debug(this + " is now " + (verbose ? "" : "not ") + "verbose");
	}

	private void updateGoals() {
		if (currentPathElement == null) {
			if (path.isEmpty()) {
				currentDestination = finalDestination;
				currentPathElement = null;
			} else {
				currentPathElement = path.remove();
				if (verbose) {
					Logger.debug(this + " updated path: " + path);
				}
			}
		}
		// Head for the best point in the current path element
		if (currentPathElement != null) {
			// Assume we're heading for the target edge.
			currentDestination = currentPathElement.getGoal();
			Point2D current = new Point2D(location[0], location[1]);
			Vector2D vectorToEdge = currentDestination.minus(current).normalised();
			if (verbose) {
				Logger.debug(this + " finding goal point");
				Logger.debug(this + " current path element: " + currentPathElement);
				Logger.debug(this + " current position: " + current);
				Logger.debug(this + " edge goal: " + currentDestination);
			}
			for (Point2D next : currentPathElement.getWaypoints()) {
				if (verbose) {
					Logger.debug(this + " next possible goal: " + next);
				}
				if (next != currentPathElement.getGoal()) {
					Vector2D vectorToNext = next.minus(current).normalised();
					double dot = vectorToNext.dot(vectorToEdge);
					if (dot < 0 || dot > 1) {
						if (verbose) {
							Logger.debug("Dot product of " + vectorToNext + " and " + vectorToEdge + " is " + dot);
							Logger.debug(this + " next point is " + (dot < 0 ? "backwards" : "too distant") + "; ignoring");
						}
						continue;
					}
				}

				if (hasLos(current, next, currentArea)) {
					currentDestination = next;
					if (verbose) {
						Logger.debug(this + " has line-of-sight to " + next);
					}
					break;
				}
			}
		}
	}

	private void computeForces(double dt) {
		colocated = false;
		computeAgentsForce(agentsForce);
		if (!colocated) {
			computeDestinationForce(destinationForce);
			computeWallsForce(wallsForce, dt);
		}

		force[0] = destinationForce[0] + agentsForce[0] + wallsForce[0];
		force[1] = destinationForce[1] + agentsForce[1] + wallsForce[1];

		if (Double.isNaN(force[0]) || Double.isNaN(force[1])) {
			Logger.warn("Force is NaN!");
			force[0] = 0;
			force[1] = 0;
		}
	}

	private void updatePosition(double dt) {
		double newVX = velocity[0] + dt * force[0];
		double newVY = velocity[1] + dt * force[1];
		double v = Math.hypot(newVX, newVY);
		if (v > this.velocityLimit) {
			// System.err.println("velocity exceeded velocityLimit");
			v /= this.velocityLimit;
			newVX /= v;
			newVY /= v;
		}

		double x = location[0] + dt * newVX;
		double y = location[1] + dt * newVY;

		if (verbose) {
			Logger.debug("Updating position for " + this);
			Logger.debug("Current position   : " + location[0] + ", " + location[1]);
			Logger.debug("Current velocity   : " + velocity[0] + ", " + velocity[1]);
			Logger.debug("Destination forces : " + destinationForce[0] + ", " + destinationForce[1]);
			Logger.debug("Agent forces       : " + agentsForce[0] + ", " + agentsForce[1]);
			Logger.debug("Wall forces        : " + wallsForce[0] + ", " + wallsForce[1]);
			Logger.debug("Total forces       : " + force[0] + ", " + force[1]);
			Logger.debug("New position       : " + x + ", " + y);
			Logger.debug("New velocity       : " + newVX + ", " + newVY);
		}
		if (crossedWall(location[0], location[1], x, y)) {
			velocity[0] = 0;
			velocity[1] = 0;
			return;
		}
		velocity[0] = newVX;
		velocity[1] = newVY;
		if (newVX != 0 || newVY != 0) {
			double dist = v * dt;
			for (WallInfo wall : blockingLines) {
				wall.decreaseDistance(dist);
			}
			setLocation(x, y);
		}
	}

	private boolean hasLos(WallInfo target, List<WallInfo> blocking) {
		Line2D line = target.getLine();

		for (WallInfo wall : blocking) {
			if (wall == target) {
				break;
			}

			Line2D next = wall.getWall();
			if (target.getClosestPoint().equals(next.getOrigin()) || target.getClosestPoint().equals(next.getEndPoint())) {
				continue;
			}

			// Here we test if the wall can intersect the line to the target
			// in front of the target. This is the case if the following holds:
			// |v_target| < |v_wall|/cos(alpha)
			// using dot(v_t, v_w) = cos(alpha) * |v_t| * |v_w|
			// we get dot(v_t, v_w) < |v_w|^2
			//
			// This is strictly true only if the line to the wall and the wall
			// are orthogonal, but in our case the angle between those can never
			// be
			// acute (because they intersect at the closest point), so we never
			// prune real intersections here.
			double dotp = line.getDirection().dot(wall.getVector());
			if (dotp < wall.getDistance() * wall.getDistance()) {
				continue;
			}

			if (GeometryTools2D.getSegmentIntersectionPoint(line, next) != null) {
				return false;
			}
		}
		return true;
	}

	private boolean hasLos(Point2D source, Point2D target, TrafficArea area) {
		Line2D line = new Line2D(source, target);
		double dist = line.getDirection().getLength();

		for (WallInfo wall : blockingLines) {
			if (wall.getDistance() > dist || wall.getArea() != area) {
				break;
			}

			Line2D next = wall.getWall();
			if (GeometryTools2D.getSegmentIntersectionPoint(line, next) != null) {
				return false;
			}
		}
		return true;
	}

	private boolean insideBlockade() {
		if (currentArea == null) {
			return false;
		}
		for (TrafficBlockade block : currentArea.getBlockades()) {
			if (block.contains(location[0], location[1])) {
				return true;
			}
		}
		return false;
	}

	private boolean crossedLine(double oldX, double oldY, double newX, double newY, Line2D line) {
		Line2D moved = new Line2D(oldX, oldY, newX - oldX, newY - oldY);
		return (GeometryTools2D.getSegmentIntersectionPoint(moved, line) != null);
		/*
		 * Vector2D normal = line.getDirection().getNormal().normalised();
		 * double dot1 = new Vector2D(oldX - line.getOrigin().getX(), oldY -
		 * line.getOrigin().getY()).normalised().dot(normal); double dot2 = new
		 * Vector2D(newX - line.getOrigin().getX(), newY -
		 * line.getOrigin().getY()).normalised().dot(normal); return (((dot1 < 0
		 * && dot2 > 0) || (dot1 > 0 && dot2 < 0)) && GeometryTools2D
		 * .getSegmentIntersectionPoint(moved, line) != null);
		 */
	}

	private boolean crossedWall(double oldX, double oldY, double newX, double newY) {
		Line2D moved = new Line2D(oldX, oldY, newX - oldX, newY - oldY);
		double dist = moved.getDirection().getLength();
		for (WallInfo wall : blockingLines) {
			if (wall.getDistance() >= dist) {
				break;
			}
			Line2D test = wall.getWall();
			if (GeometryTools2D.getSegmentIntersectionPoint(moved, test) != null) {
				// if (crossedLine(oldX, oldY, newX, newY, test)) {
				/*
				 * Logger.warn(this + " crossed wall");
				 * Logger.warn("Old location: " + oldX + ", " + oldY);
				 * Logger.warn("New location: " + newX + ", " + newY);
				 * Logger.warn("Movement line: " + moved);
				 * Logger.warn("Wall         : " + test);
				 * Logger.warn("Crossed at " +
				 * GeometryTools2D.getSegmentIntersectionPoint(moved, test));
				 */
				return true;
			}
		}
		return false;
	}

	private void findBlockingLines() {
		blockingLines.clear();
		if (currentArea != null) {
			for (Line2D line : currentArea.getAllBlockingLines()) {
				blockingLines.add(new WallInfo(line, currentArea));
			}
			for (TrafficArea neighbour : manager.getNeighbours(currentArea)) {
				for (Line2D line : neighbour.getAllBlockingLines()) {
					blockingLines.add(new WallInfo(line, neighbour));
				}
			}
		}
	}

	private void updateWalls(double dt) {
		Point2D position = new Point2D(location[0], location[1]);
		double crossingCutoff = dt * this.velocityLimit;
		double forceCutoff = TrafficConstants.getWallDistanceCutoff();
		double cutoff = Math.max(forceCutoff, crossingCutoff);
		// double dist;

		for (WallInfo wall : blockingLines) {
			if (wall.getDistance() > cutoff) {
				continue;
			}
			wall.computeClostestPoint(position);
		}

		// Hand coded, in-sito insertion sort is much faster than
		// Collection.sort() for lists of this size.
		for (int i = 1; i < blockingLines.size(); i++) {
			WallInfo info = blockingLines.get(i);
			for (int j = i; j >= 0; j--) {
				if (j == 0) {
					blockingLines.remove(i);
					blockingLines.add(0, info);
				} else if (blockingLines.get(j - 1).getDistance() < info.getDistance()) {
					if (j == i) {
						break;
					}
					blockingLines.remove(i);
					blockingLines.add(j, info);
					break;
				}
			}
		}
	}

	private void computeDestinationForce(double[] result) {
		double destx = 0;
		double desty = 0;
		if (currentDestination != null) {
			double dx = currentDestination.getX() - location[0];
			double dy = currentDestination.getY() - location[1];
			double dist = Math.hypot(dx, dy);
			if (dist == 0) {
				dx = 0;
				dy = 0;
			} else {
				dx /= dist;
				dy /= dist;
			}
			final double ddd = 0.001;
			if (currentDestination == finalDestination) {
				dx = Math.min(velocityLimit, ddd * dist) * dx;
				dy = Math.min(velocityLimit, ddd * dist) * dy;
			} else {
				dx = this.velocityLimit * dx;
				dy = this.velocityLimit * dy;
			}

			final double sss2 = 0.0002;
			destx = sss2 * (dx - velocity[0]);
			desty = sss2 * (dy - velocity[1]);
		} else {
			final double sss = 0.0001;
			destx = sss * (-velocity[0]);
			desty = sss * (-velocity[1]);
		}
		result[0] = destx;
		result[1] = desty;
		if (Double.isNaN(destx)) {
			Logger.error("Destination force x is NaN");
			result[0] = 0;
		}
		if (Double.isNaN(desty)) {
			Logger.error("Destination force y is NaN");
			result[1] = 0;
		}
		if (verbose) {
			Logger.debug("Destination force: " + result[0] + ", " + result[1]);
		}
	}

	private void computeAgentsForce(double[] result) {
		result[0] = 0;
		result[1] = 0;
		if (currentArea == null) {
			return;
		}

		double xSum = 0;
		double ySum = 0;

		double cutoff = TrafficConstants.getAgentDistanceCutoff();
		double a = TrafficConstants.getAgentForceCoefficientA();
		double b = TrafficConstants.getAgentForceCoefficientB();
		double k = TrafficConstants.getAgentForceCoefficientK();
		double forceLimit = TrafficConstants.getAgentForceLimit();

		Collection<TrafficAgent> nearby = manager.getNearbyAgents(this);
		for (TrafficAgent agent : nearby) {
			if (!agent.isMobile()) {
				continue;
			}
			double dx = agent.getX() - location[0];
			double dy = agent.getY() - location[1];

			if (Math.abs(dx) > cutoff) {
				continue;
			}
			if (Math.abs(dy) > cutoff) {
				continue;
			}

			double totalRadius = radius + agent.getRadius();
			double distanceSquared = dx * dx + dy * dy;

			if (distanceSquared == 0) {
				xSum = TrafficConstants.getColocatedAgentNudge();
				ySum = TrafficConstants.getColocatedAgentNudge();
				colocated = true;
				Logger.debug(this + " is co-located with " + agent);
				break;
			}
			double distance = Math.sqrt(distanceSquared);
			double dxN = dx / distance;
			double dyN = dy / distance;
			double negativeSeparation = totalRadius - distance;
			double tmp = -a * Math.exp(negativeSeparation * b);
			if (Double.isInfinite(tmp)) {
				Logger.warn("calculateAgentsForce(): A result of exp is infinite: exp(" + (negativeSeparation * b) + ")");
			} else {
				xSum += tmp * dxN;
				ySum += tmp * dyN;
			}
			if (negativeSeparation > 0) {
				// Agents overlap
				xSum += -k * negativeSeparation * dxN;
				ySum += -k * negativeSeparation * dyN;
			}
		}

		double forceSum = Math.hypot(xSum, ySum);
		if (forceSum > forceLimit) {
			forceSum /= forceLimit;
			xSum /= forceSum;
			ySum /= forceSum;
		}
		if (Double.isNaN(xSum)) {
			Logger.warn("computeAgentsForce: Sum of X force is NaN");
			xSum = 0;
		}
		if (Double.isNaN(ySum)) {
			Logger.warn("computeAgentsForce: Sum of Y force is NaN");
			ySum = 0;
		}
		result[0] = xSum;
		result[1] = ySum;
	}

	private void computeWallsForce(double[] result, double dt) {
		double xSum = 0;
		double ySum = 0;
		if (currentArea != null) {
			double r = getRadius();
			double dist;
			double cutoff = TrafficConstants.getWallDistanceCutoff();
			// double a = TrafficConstants.getWallForceCoefficientA();
			double b = TrafficConstants.getWallForceCoefficientB();
			Point2D position = new Point2D(location[0], location[1]);
			if (verbose) {
				Logger.debug("Computing wall forces for " + this);
				Logger.debug("Position: " + position);
			}

			for (WallInfo wall : blockingLines) {
				if (wall.getDistance() > cutoff) {
					break;
				}
				Line2D line = wall.getWall();
				dist = wall.getDistance();
				Point2D closest = wall.getClosestPoint();

				if (verbose) {
					Logger.debug("Next wall: " + line);
				}
				// Point2D closest =
				// GeometryTools2D.getClosestPointOnSegment(line, position);
				if (verbose) {
					Logger.debug("Closest point: " + closest);
				}
				// dist = GeometryTools2D.getDistance(closest, position);
				// if (dist > cutoff) {
				// if (verbose) {
				// Logger.debug("Distance to wall: " + dist +
				// " greater than cutoff " + cutoff);
				// }
				// continue;
				// }
				if (!hasLos(wall, blockingLines)) {
					// No line-of-sight to closest point
					if (verbose) {
						Logger.debug("No line of sight");
					}
					continue;
				}

				boolean endPoint = false;
				if (closest == line.getOrigin() || closest == line.getEndPoint()) {
					endPoint = true;
				}
				// Two forces apply:
				// If the agent is moving towards this wall then apply a force
				// to bring the agent to a stop. This force applies when the
				// distance is less than the agent radius.
				// Also apply a force that decreases exponentially with distance
				// no matter what the agent is doing.
				double currentVX = velocity[0];
				double currentVY = velocity[1];
				double currentFX = destinationForce[0] + agentsForce[0];
				double currentFY = destinationForce[1] + agentsForce[1];
				double expectedVX = currentVX + dt * currentFX;
				double expectedVY = currentVY + dt * currentFY;
				Vector2D expectedVelocity = new Vector2D(expectedVX, expectedVY);
				Vector2D wallForceVector = wall.getVector().scale(-1.0 / dist);
				double radii = dist / r;
				// Compute the stopping force
				// Magnitude is the multiple of wallForceVector required to
				// bring the agent to a stop.
				double magnitude = -expectedVelocity.dot(wallForceVector);
				if (magnitude < 0 || radii >= 1) {
					magnitude = 0;
					// Agent is moving away or far enough away - no stopping
					// force required.
				} else if (radii < 1) {
					double d = Math.exp(-(radii - 1) * b);
					if (d < 1) {
						d = 0;
					}
					magnitude *= d;
					if (endPoint) {
						// Endpoints are counted twice so halve the magnitude
						magnitude /= 2;
					}
				}
				Vector2D stopForce = wallForceVector.scale(magnitude / dt);
				// Compute the repulsion force
				// Decreases exponentially with distance in terms of agent
				// radii.
				// double factor = a * Math.min(1, Math.exp(-(radii - 1) * b));
				// Vector2D repulsionForce = wallForceVector.scale(factor / dt);
				xSum += stopForce.getX();
				ySum += stopForce.getY();
				// xSum += repulsionForce.getX();
				// ySum += repulsionForce.getY();
				if (verbose) {
					Logger.debug("Distance to wall : " + dist);
					Logger.debug("Distance to wall : " + radii + " radii");
					Logger.debug("Current velocity : " + currentVX + ", " + currentVY);
					Logger.debug("Current force    : " + currentFX + ", " + currentFY);
					Logger.debug("Expected velocity: " + expectedVelocity);
					Logger.debug("Wall force       : " + wallForceVector);
					Logger.debug("Magnitude        : " + magnitude);
					Logger.debug("Stop force       : " + stopForce);
					// Logger.debug("Factor           : " + factor + " (e^" +
					// (-(dist / r) * b) + ")");
					// Logger.debug("Repulsion force  : " + repulsionForce);
				}
			}
		}
		if (Double.isNaN(xSum) || Double.isNaN(ySum)) {
			xSum = 0;
			ySum = 0;
		}
		if (verbose) {
			Logger.debug("Total wall force: " + xSum + ", " + ySum);
		}

		result[0] = xSum;
		result[1] = ySum;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("TrafficAgent[");
		sb.append("id:").append(human.getID()).append(";");
		sb.append("x:").append((int) getX()).append(";");
		sb.append("y:").append((int) getY()).append(";");
		sb.append("]");
		return sb.toString();
	}

	/**
	 * Get a long version of the toString method.
	 *
	 * @return A long description of this agent.
	 */
	public String toLongString() {
		StringBuffer sb = new StringBuffer("TrafficAgent[");
		sb.append("id: ").append(human.getID()).append(";");
		sb.append(" x: ").append(location[0]).append(";");
		sb.append(" y: ").append(location[1]).append(";");
		sb.append(" current area: ").append(currentArea).append(";");
		sb.append(" current destination: ").append(currentDestination).append(";");
		sb.append(" final destination: ").append(finalDestination).append(";");
		sb.append("]");
		return sb.toString();
	}

	@Override
	public int hashCode() {
		return human.getID().hashCode();
	}
}
