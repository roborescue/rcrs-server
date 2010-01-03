package rescuecore2.standard.misc;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.misc.Pair;

import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.Node;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.messages.AKMove;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/**
   A class for encapsulating the actual movement path an agent took during a timestep.
 */
public abstract class AgentPath {
    /**
       Compute the path an agent took.
       @param human The agent.
       @param startPosition The agent's position at the start of the timestep.
       @param startPositionExtra The agent's positionExtra at the start of the timestep.
       @param move The move command the agent issued.
       @param world The world model.
       @return The computed Path, or null if the agent didn't move.
     */
    public static AgentPath computePath(Human human, EntityID startPosition, int startPositionExtra, AKMove move, StandardWorldModel world) {
        if (human == null) {
            throw new IllegalArgumentException("Agent must not be null");
        }
        if (!human.isPositionDefined() || !human.isPositionExtraDefined()) {
            throw new IllegalArgumentException("Agent has an undefined position");
        }
        if (move == null) {
            throw new IllegalArgumentException("Move command must not be null");
        }
        List<EntityID> path = move.getPath();
        Iterator<EntityID> it = path.iterator();
        EntityID position = human.getPosition();
        int positionExtra = human.getPositionExtra();
        if (position.equals(startPosition) && positionExtra == startPositionExtra) {
            // Didn't move
            return null;
        }
        if (position.equals(startPosition) && Math.abs(positionExtra - startPositionExtra) == 1) {
            // Didn't really move - traffic simulator sometimes adjusts positionExtra by 1.
            return null;
        }
        EntityID previous = it.next();
        // Find the agent along the path
        while (!previous.equals(startPosition) && it.hasNext()) {
            previous = it.next();
        }
        if (!it.hasNext()) {
            // Didn't find the agent along the requested path. Let's assume the agent is in the right place.
            //            System.out.println(agentID + ": original position not found on path");
            it = path.iterator();
            previous = startPosition;
        }
        CompositePath result = new CompositePath();
        boolean success = false;
        // Walk along the move command until we find the agent's current position, updating the result as we go.
        while (it.hasNext()) {
            EntityID next = it.next();
            // Create the appropriate path element
            StandardEntity from = world.getEntity(previous);
            StandardEntity to = world.getEntity(next);
            if (from instanceof Road && to instanceof Node) {
                Road road = (Road)from;
                Node node = (Node)to;
                boolean toHead = node.getID().equals(road.getHead());
                if (previous.equals(startPosition)) {
                    // At origin: must be a PartialRoadPath
                    result.addPath(new PartialRoadPath(road, startPositionExtra, toHead, false, world));
                }
                else {
                    // RoadPath
                    result.addPath(new RoadPath(road, toHead, false, world));
                }
            }
            else if (from instanceof Node && to instanceof Road) {
                Road road = (Road)to;
                Node node = (Node)from;
                boolean fromHead = node.getID().equals(road.getHead());
                if (next.equals(position)) {
                    // At destination: must be a PartialRoadPath
                    result.addPath(new PartialRoadPath(road, positionExtra, fromHead, true, world));
                }
                else {
                    // RoadPath
                    result.addPath(new RoadPath(road, !fromHead, true, world));
                }
            }
            else if (from instanceof Node && to instanceof Building) {
                result.addPath(new BuildingEntryPath((Building)to, (Node)from, world));
            }
            else if (from instanceof Building && to instanceof Node) {
                result.addPath(new BuildingExitPath((Building)from, (Node)to, world));
            }
            previous = next;
            if (next.equals(position)) {
                success = true;
                break;
            }
        }
        if (!success) {
            // We never found the agent along the move command. Probably the command was invalid. In any case, we can't determine the agent's true path.
            return null;
        }
        return result;
    }

    /**
       Get the coordinates of a point along this path.
       @param d The distance along the path in the range [0..1].
       @return The coordinates of the point along the path.
     */
    public abstract Pair<Integer, Integer> getPointOnPath(double d);

    /**
       Get the length of this path.
       @return The length of the path.
    */
    public abstract double getLength();

    private static class CompositePath extends AgentPath {
        private List<AgentPath> parts;

        CompositePath() {
            parts = new ArrayList<AgentPath>();
        }

        void addPath(AgentPath p) {
            parts.add(p);
        }

        @Override
        public double getLength() {
            double d = 0;
            for (AgentPath next : parts) {
                d += next.getLength();
            }
            return d;
        }

        @Override
        public Pair<Integer, Integer> getPointOnPath(double d) {
            //            System.out.println("Finding point along composite path: " + d);
            double length = getLength();
            double point = d * length;
            //            System.out.println("Length = " + length + ", length along path = " + point);
            // Find the right part
            AgentPath result = null;
            for (AgentPath next : parts) {
                double nextLength = next.getLength();
                //                System.out.println("Next part: " + next);
                //                System.out.println("Length: " + nextLength);
                if (nextLength > point) {
                    result = next;
                    break;
                }
                point -= nextLength;
                //                System.out.println("Length remaining: " + point);
            }
            //            System.out.println("Result: " + result);
            if (result == null) {
                // Fell off the end, probably because of numerical issues
                return parts.get(parts.size() - 1).getPointOnPath(1.0);
            }
            double p = point / result.getLength();
            return result.getPointOnPath(p);
        }

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();
            for (Iterator<AgentPath> it = parts.iterator(); it.hasNext();) {
                result.append(it.next());
                if (it.hasNext()) {
                    result.append(", ");
                }
            }
            return result.toString();
        }
    }

    private abstract static class AbstractPath extends AgentPath {
        private Pair<Integer, Integer> start;
        private Pair<Integer, Integer> end;
        private double length;
        private String description;

        protected void setStart(Pair<Integer, Integer> s) {
            start = s;
        }

        protected void setEnd(Pair<Integer, Integer> e) {
            end = e;
        }

        protected void setLength(double l) {
            length = l;
        }

        protected void computeLength() {
            int dx = end.first() - start.first();
            int dy = end.second() - start.second();
            length = Math.hypot(dx, dy);
        }

        protected void setDescription(String d) {
            description = d;
        }

        @Override
        public double getLength() {
            return length;
        }

        @Override
        public Pair<Integer, Integer> getPointOnPath(double d) {
            double dx = end.first() - start.first();
            double dy = end.second() - start.second();
            int x = start.first() + (int)(d * dx);
            int y = start.second() + (int)(d * dy);
            return new Pair<Integer, Integer>(x, y);
        }

        @Override
        public String toString() {
            return description;
        }
    }

    private static class PartialRoadPath extends AbstractPath {
        PartialRoadPath(Road road, int positionExtra, boolean toOrFromHead, boolean toPositionExtra, StandardWorldModel world) {
            Node head = (Node)road.getHead(world);
            Node tail = (Node)road.getTail(world);
            if (toPositionExtra) {
                setStart(toOrFromHead ? head.getLocation(world) : tail.getLocation(world));
                setEnd(findPositionExtra(road, positionExtra, world));
                setDescription("From node " + (toOrFromHead ? head.getID() : tail.getID()) + " to road " + road.getID() + " position " + positionExtra);
            }
            else {
                setStart(findPositionExtra(road, positionExtra, world));
                setEnd(toOrFromHead ? head.getLocation(world) : tail.getLocation(world));
                setDescription("From road " + road.getID() + " position " + positionExtra + " towards node " + (toOrFromHead ? head.getID() : tail.getID()));
            }
            setLength(toOrFromHead ? positionExtra : road.getLength() - positionExtra);
        }

        private Pair<Integer, Integer> findPositionExtra(Road road, double positionExtra, StandardWorldModel world) {
            double d = positionExtra / road.getLength();
            Pair<Integer, Integer> start = road.getHead(world).getLocation(world);
            Pair<Integer, Integer> end = road.getTail(world).getLocation(world);
            double dx = end.first() - start.first();
            double dy = end.second() - start.second();
            int x = start.first() + (int)(d * dx);
            int y = start.second() + (int)(d * dy);
            return new Pair<Integer, Integer>(x, y);
        }
    }

    private static class RoadPath extends AbstractPath {
        RoadPath(Road road, boolean toHead, boolean intoRoad, StandardWorldModel world) {
            Node head = (Node)road.getHead(world);
            Node tail = (Node)road.getTail(world);
            Node from = toHead ? tail : head;
            Node to = toHead ? head : tail;
            Pair<Integer, Integer> fromLocation = from.getLocation(world);
            Pair<Integer, Integer> toLocation = to.getLocation(world);
            int x = (fromLocation.first() + toLocation.first()) / 2;
            int y = (fromLocation.second() + toLocation.second()) / 2;
            if (intoRoad) {
                setStart(from.getLocation(world));
                setEnd(new Pair<Integer, Integer>(x, y));
            }
            else {
                setStart(new Pair<Integer, Integer>(x, y));
                setEnd(to.getLocation(world));
            }
            computeLength();
            setDescription((intoRoad ? "Into" : "Out of") + " road " + road.getID() + " from " + (toHead ? tail.getID() : head.getID()) + " towards " + (toHead ? head.getID() : tail.getID()));
        }
    }

    private static class BuildingEntryPath extends AbstractPath {
        BuildingEntryPath(Building b, Node entrance, StandardWorldModel world) {
            setStart(entrance.getLocation(world));
            setEnd(b.getLocation(world));
            computeLength();
            setDescription("Into building " + b.getID() + " from node " + entrance.getID());
        }
    }

    private static class BuildingExitPath extends AbstractPath {
        BuildingExitPath(Building b, Node entrance, StandardWorldModel world) {
            setStart(b.getLocation(world));
            setEnd(entrance.getLocation(world));
            computeLength();
            setDescription("Out of building " + b.getID() + " to node " + entrance.getID());
        }
    }
}