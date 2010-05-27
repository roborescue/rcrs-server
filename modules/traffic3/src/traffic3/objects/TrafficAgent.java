package traffic3.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import traffic3.manager.TrafficManager;
import traffic3.simulator.TrafficConstants;
import traffic3.simulator.PathElement;

import rescuecore2.standard.entities.Human;

import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.GeometryTools2D;

import rescuecore2.log.Logger;

/**
 * A TrafficAgent is a mobile object in the world.
 */
public class TrafficAgent {
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
    private Collection<Line2D> blockingLines;

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

    /**
       Construct a TrafficAgent.
       @param human The Human wrapped by this object.
       @param manager The traffic manager.
       @param radius The radius of this agent in mm.
       @param velocityLimit The velicity limit.
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
        blockingLines = new HashSet<Line2D>();
    }

    /**
       Get the Human wrapped by this object.
       @return The wrapped Human.
    */
    public Human getHuman() {
        return human;
    }

    /**
       Get the maximum velocity of this agent.
       @return The maximum velocity.
    */
    public double getMaxVelocity() {
        return velocityLimit;
    }

    /**
       Set the maximum velocity of this agent.
       @param vLimit The new maximum velocity.
    */
    public void setMaxVelocity(double vLimit) {
        velocityLimit = vLimit;
    }

    /**
       Get the TrafficArea the agent is currently in.
       @return The current TrafficArea.
    */
    public TrafficArea getArea() {
        return currentArea;
    }

    /**
       Get the position history.
       @return The position history.
    */
    public List<Point2D> getPositionHistory() {
        return Collections.unmodifiableList(positionHistory);
    }

    /**
       Get the distance travelled so far.
       @return The distance travelled.
    */
    public double getTravelDistance() {
        return totalDistance;
    }

    /**
       Clear the position history and distance travelled.
    */
    public void clearPositionHistory() {
        positionHistory.clear();
        historyCount = 0;
        totalDistance = 0;
    }

    /**
       Set the frequency of position history records. One record will be created every nth microstep.
       @param n The new frequency.
    */
    public void setPositionHistoryFrequency(int n) {
        positionHistoryFrequency = n;
    }

    /**
       Enable or disable position history recording.
       @param b True to enable position history recording, false otherwise.
    */
    public void setPositionHistoryEnabled(boolean b) {
        savePositionHistory = b;
    }

    /**
       Get the X coordinate of this agent.
       @return The X coordinate.
    */
    public double getX() {
        return location[0];
    }

    /**
       Get the Y coordinate of this agent.
       @return The Y coordinate.
    */
    public double getY() {
        return location[1];
    }

    /**
       Get the total X force on this agent.
       @return The total X force in N.
    */
    public double getFX() {
        return force[0];
    }

    /**
       Get the total Y force on this agent.
       @return The total Y force in N.
    */
    public double getFY() {
        return force[1];
    }

    /**
       Get the X velocity of this agent.
       @return The X velocity in mm/s.
    */
    public double getVX() {
        return velocity[0];
    }

    /**
       Get the Y velocity of this agent.
       @return The Y velocity in mm/s.
    */
    public double getVY() {
        return velocity[1];
    }

    /**
       Set the radius of this agent.
       @param r The new radius in mm.
    */
    public void setRadius(double r) {
        this.radius = r;
    }

    /**
       Get the radius of this agent.
       @return The radius in mm.
    */
    public double getRadius() {
        return this.radius;
    }

    /**
       Set the path this agent wants to take.
       @param steps The new path.
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
        Logger.debug(this + " destination set: " + path);
        Logger.debug(this + " final destination set: " + finalDestination);
    }

    /**
       Clear the path.
    */
    public void clearPath() {
        finalDestination = null;
        currentDestination = null;
        currentPathElement = null;
        path.clear();
    }

    /**
       Get the final destination.
       @return The final destination
    */
    public Point2D getFinalDestination() {
        return finalDestination;
    }

    /**
       Get the current (possibly intermediate) destination.
       @return The current destination.
    */
    public Point2D getCurrentDestination() {
        return currentDestination;
    }

    /**
       Get the current (possibly intermediate) path element.
       @return The current path element.
    */
    public PathElement getCurrentElement() {
        return currentPathElement;
    }

    /**
       Get the current path.
       @return The path.
    */
    public List<PathElement> getPath() {
        return Collections.unmodifiableList((List<PathElement>)path);
    }

    /**
       Set the location of this agent. This method will also update the position history (if enabled).
       @param x location x
       @param y location y
    */
    public void setLocation(double x, double y) {
        if (currentArea == null || !currentArea.contains(x, y)) {
            if (currentArea != null) {
                currentArea.removeAgent(this);
            }
            TrafficArea newArea = manager.findArea(x, y);
            if (newArea == null) {
                Logger.warn("Agent moved outside area: " + this);
                return;
            }
            currentArea = newArea;
            findBlockingLines();
            currentArea.addAgent(this);
        }
        // Check current destination
        if (currentPathElement != null) {
            double dx = currentPathElement.getGoal().getX() - location[0];
            double dy = currentPathElement.getGoal().getY() - location[1];
            double dSquared = (dx * dx) + (dy * dy);
            if (dSquared < NEARBY_THRESHOLD_SQUARED) {
                Logger.debug("Near target: location = " + location[0] + ", " + location[1] + ", target = " + currentDestination + ", distance squared = " + dSquared + ", threshold = " + NEARBY_THRESHOLD_SQUARED);
                currentPathElement = null;
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

    /**
       Perform any pre-timestep activities required.
    */
    public void beginTimestep() {
        findBlockingLines();
        if (insideBlockade()) {
            setMobile(false);
        }
    }

    /**
       Execute a microstep.
       @param dt The amount of time to simulate in ms.
    */
    public void step(double dt) {
        if (mobile) {
            updateGoals();
            computeForces();
            updatePosition(dt);
        }
    }

    /**
       Perform any post-timestep activities required.
    */
    public void endTimestep() {
    }

    /**
       Set whether this agent is mobile or not.
       @param m True if this agent is mobile, false otherwise.
    */
    public void setMobile(boolean m) {
        mobile = m;
    }

    /**
       Find out if this agent is mobile.
       @return True if this agent is mobile.
    */
    public boolean isMobile() {
        return mobile;
    }

    private void updateGoals() {
        if (currentPathElement == null) {
            if (path.isEmpty()) {
                currentDestination = finalDestination;
                currentPathElement = null;
            }
            else {
                currentPathElement = path.remove();
                Logger.debug(this + " updated path: " + path);
            }
        }
        // Head for the best point in the current path element
        if (currentPathElement != null) {
            currentDestination = null;
            Point2D current = new Point2D(location[0], location[1]);
            for (Point2D next : currentPathElement.getWaypoints()) {
                if (hasLos(current, next, currentArea.getAllBlockingLines(), null)) {
                    currentDestination = next;
                    break;
                }
            }
            if (currentDestination == null) {
                currentDestination = currentPathElement.getGoal();
            }
            Logger.debug(this + " current destination: " + currentDestination);
        }
    }

    private void computeForces() {
        computeDestinationForce(destinationForce);
        computeAgentsForce(agentsForce);
        computeWallsForce(wallsForce);

        force[0] = destinationForce[0] + agentsForce[0] + wallsForce[0];
        force[1] = destinationForce[1] + agentsForce[1] + wallsForce[1];

        if (Double.isNaN(force[0]) || Double.isNaN(force[1])) {
            Logger.warn("Force is NaN!");
            force[0] = 0;
            force[1] = 0;
        }
    }

    private void updatePosition(double dt) {
        double x = location[0] + dt * velocity[0];
        double y = location[1] + dt * velocity[1];
        double newVX = velocity[0] + dt * force[0];
        double newVY = velocity[1] + dt * force[1];
        double v = Math.hypot(newVX, newVY);
        if (v > this.velocityLimit) {
            //System.err.println("velocity exceeded velocityLimit");
            v /= this.velocityLimit;
            newVX /= v;
            newVY /= v;
        }
        if (crossedWall(location[0], location[1], x, y)) {
            Logger.warn(this + " crossed a wall");
            return;
        }
        velocity[0] = newVX;
        velocity[1] = newVY;
        setLocation(x, y);
    }

    private boolean hasLos(Point2D source, Point2D target, Collection<Line2D> blocking, Line2D ignore) {
        Line2D line = new Line2D(source, target);
        for (Line2D next : blocking) {
            if (next == ignore) {
                continue;
            }
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

    private boolean crossedWall(double oldX, double oldY, double newX, double newY) {
        Line2D moved = new Line2D(oldX, oldY, newX - oldX, newY - oldY);
        for (Line2D test : blockingLines) {
            if (GeometryTools2D.getSegmentIntersectionPoint(moved, test) != null) {
                return true;
            }
        }
        return false;
    }

    private void findBlockingLines() {
        blockingLines.clear();
        if (currentArea != null) {
            blockingLines.addAll(currentArea.getAllBlockingLines());
            for (TrafficArea neighbour : manager.getNeighbours(currentArea)) {
                blockingLines.addAll(neighbour.getAllBlockingLines());
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
            }
            else {
                dx /= dist;
                dy /= dist;
            }
            final double ddd = 0.001;
            if (currentDestination == finalDestination) {
                dx = Math.min(velocityLimit, ddd * dist) * dx;
                dy = Math.min(velocityLimit, ddd * dist) * dy;
            }
            else {
                dx = this.velocityLimit * dx;
                dy = this.velocityLimit * dy;
            }

            final double sss2 = 0.0002;
            destx = sss2 * (dx - velocity[0]);
            desty = sss2 * (dy - velocity[1]);
        }
        else {
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
                xSum += TrafficConstants.getColocatedAgentNudge();
                ySum += TrafficConstants.getColocatedAgentNudge();
                continue;
            }
            double distance = Math.sqrt(distanceSquared);
            double dxN = dx / distance;
            double dyN = dy / distance;
            double negativeSeparation = totalRadius - distance;
            double tmp = -a * Math.exp(negativeSeparation * b);
            if (Double.isInfinite(tmp)) {
                Logger.warn("calculateAgentsForce(): A result of exp is infinite: exp(" + (negativeSeparation * b) + ")");
            }
            else {
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

    private void computeWallsForce(double[] result) {
        double xSum = 0;
        double ySum = 0;
        if (currentArea != null) {
            double r = getRadius();
            double dx;
            double dy;
            double dist;
            double cutoff = TrafficConstants.getWallDistanceCutoff();
            double a = TrafficConstants.getWallForceCoefficientA();
            double b = TrafficConstants.getWallForceCoefficientB();
            double k = TrafficConstants.getWallForceCoefficientK();
            Point2D position = new Point2D(location[0], location[1]);
            for (Line2D line : blockingLines) {
                Point2D closest = GeometryTools2D.getClosestPointOnSegment(line, position);
                if (closest == line.getOrigin() || closest == line.getEndPoint()) {
                    // Closest point is an endpoint - we're outside the influence of this line.
                    continue;
                }
                if (!hasLos(position, closest, blockingLines, line)) {
                    // No line-of-sight to closest point
                    continue;
                }
                Point2D p1 = line.getOrigin();
                Point2D p2 = line.getEndPoint();
                double p1p2X = p2.getX() - p1.getX();
                double p1p2Y = p2.getY() - p1.getY();
                double p1pX =  location[0] - p1.getX();
                double p1pY =  location[1] - p1.getY();
                double lineLength = line.getDirection().getLength();
                if (lineLength == 0) {
                    continue;
                }
                double d = (p1p2X * p1pX + p1p2Y * p1pY) / lineLength;
                if (d < 0) {
                    dist = GeometryTools2D.getDistance(p1, position) - r;
                    dx = (location[0] - p1.getX()) / dist / 2;
                    dy = (location[1] - p1.getY()) / dist / 2;
                }
                else if (lineLength < d) {
                    dist = GeometryTools2D.getDistance(p2, position) - r;
                    dx = (location[0] - p2.getX()) / dist / 2;
                    dy = (location[1] - p2.getY()) / dist / 2;
                }
                else {
                    double p1p2NX = p1p2X / lineLength;
                    double p1p2NY = p1p2Y / lineLength;
                    dx = -d * p1p2NX + p1pX;
                    dy = -d * p1p2NY + p1pY;
                    dist = Math.sqrt(dx * dx + dy * dy) - r;
                    dx /= dist;
                    dy /= dist;
                    if (Double.isNaN(dist)) {
                        Logger.warn("computeWallsForce: NaN: Math.sqrt(" + (dx * dx + dy * dy) + "): " + dx + "," + dy + ": " + lineLength);
                    }
                }
                if (dist > cutoff) {
                    continue;
                }
                if (dist < 0) {
                    //System.out.println("mark@");
                    xSum += k * (dist) * dx;
                    ySum += k * (dist) * dy;
                }
                else {
                    double tmp = a * Math.exp(-(dist) * b);
                    if (Double.isInfinite(tmp)) {
                        Logger.warn("calculateWallForce(): A result of exp is infinite: exp(" + (-dist * b) + ")");
                    }
                    else if (Double.isNaN(tmp)) {
                        Logger.warn("calculateWallForce(): A result of exp is NaN: exp(" + (-(dist) * b) + ")");
                    }
                    else {
                        xSum += tmp * dx;
                        ySum += tmp * dy;
                    }
                }
            }
        }
        if (Double.isNaN(xSum) || Double.isNaN(ySum)) {
            xSum = 0;
            ySum = 0;
        }

        result[0] = xSum;
        result[1] = ySum;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("TrafficAgent[");
        sb.append("id:").append(human.getID()).append(";");
        sb.append("x:").append((int)getX()).append(";");
        sb.append("y:").append((int)getY()).append(";");
        sb.append("]");
        return sb.toString();
    }

    /**
       Get a long version of the toString method.
       @return A long description of this agent.
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
}
