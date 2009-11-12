package rescuecore2.standard.view;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Queue;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Iterator;

import rescuecore2.misc.Pair;
import rescuecore2.config.Config;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.Node;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.messages.AKMove;

/**
   A view layer that animates human movements.
 */
public class AnimatedHumanLayer extends HumanLayer {
    private Map<EntityID, AKMove> moveCommands;
    private Map<EntityID, Pair<EntityID, Integer>> agentLastPositions;
    private Set<EntityID> humanIDs;

    private Map<EntityID, Queue<Pair<Integer, Integer>>> frames;

    private int frameCount;

    private final Object frameLock = new Object();

    /**
       Construct an animated human view layer.
       @param frameCount The number of animation frames to compute.
    */
    public AnimatedHumanLayer(int frameCount) {
        this.frameCount = frameCount;
        moveCommands = new HashMap<EntityID, AKMove>();
        agentLastPositions = new HashMap<EntityID, Pair<EntityID, Integer>>();
        humanIDs = new HashSet<EntityID>();
        frames = new HashMap<EntityID, Queue<Pair<Integer, Integer>>>();
    }

    @Override
    public void initialise(Config config) {
        super.initialise(config);
    }

    @Override
    public String getName() {
        return "Humans (animated)";
    }

    /**
       Increase the frame number.
    */
    public void nextFrame() {
        synchronized (frameLock) {
            for (Queue<Pair<Integer, Integer>> next : frames.values()) {
                if (next.size() > 1) {
                    next.remove();
                }
            }
        }
    }

    @Override
    protected Pair<Integer, Integer> getLocation(Human h) {
        synchronized (frameLock) {
            Queue<Pair<Integer, Integer>> agentFrames = frames.get(h.getID());
            if (agentFrames != null && !agentFrames.isEmpty()) {
                return agentFrames.peek();
            }
        }
        return h.getLocation(world);
    }

    @Override
    protected void viewObject(Object o) {
        super.viewObject(o);
        if (o instanceof AKMove) {
            AKMove move = (AKMove)o;
            moveCommands.put(move.getAgentID(), move);
        }
        if (o instanceof Human) {
            humanIDs.add(((Human)o).getID());
        }
    }

    @Override
    protected void preView() {
        super.preView();
        moveCommands.clear();
        humanIDs.clear();
    }

    @Override
    protected void postView() {
        super.postView();
        // Compute animation
        double step = 1.0 / (frameCount - 1.0);
        for (EntityID next : humanIDs) {
            //            System.out.println("Computing frames for " + next);
            Queue<Pair<Integer, Integer>> result = new LinkedList<Pair<Integer, Integer>>();
            Human human = (Human)world.getEntity(next);
            if (human == null) {
                continue;
            }
            Pair<EntityID, Integer> start = agentLastPositions.get(next);
            agentLastPositions.put(next, new Pair<EntityID, Integer>(human.getPosition(), human.isPositionExtraDefined() ? human.getPositionExtra() : 0));
            //            System.out.println("Last position: " + start);
            //            System.out.println("Current position: " + agentLastPositions.get(next));
            if (start == null) {
                continue;
            }
            Path path = computePath(human, start.first(), start.second());
            if (path == null) {
                continue;
            }
            for (int i = 0; i < frameCount; ++i) {
                Pair<Integer, Integer> nextPoint = path.getPointOnPath(i * step);
                //                System.out.println("Frame " + i + " position " + nextPoint);
                result.add(nextPoint);
            }
            synchronized (frameLock) {
                frames.put(next, result);
            }
        }
    }

    private Path computePath(Human human, EntityID lastPosition, int lastPositionExtra) {
        EntityID agentID = human.getID();
        AKMove move = moveCommands.get(agentID);
        if (move == null) {
            //            System.out.println("No move command for " + agentID);
            return null;
        }
        List<EntityID> path = move.getPath();
        Iterator<EntityID> it = path.iterator();
        EntityID position = human.getPosition();
        int positionExtra = human.getPositionExtra();
        if (position.equals(lastPosition)) {
            // Didn't move (maybe positionExtra changed but we won't bother animating that)
            //            System.out.println(agentID + " didn't move");
            return null;
        }
        EntityID previous = it.next();
        // Find the agent along the path
        while (!previous.equals(lastPosition) && it.hasNext()) {
            previous = it.next();
        }
        if (!it.hasNext()) {
            // Didn't find the agent along the requested path. Let's assume the agent is in the right place.
            //            System.out.println(agentID + ": original position not found on path");
            it = path.iterator();
            previous = lastPosition;
        }
        CompositePath result = new CompositePath();
        while (it.hasNext()) {
            EntityID next = it.next();
            // Create the appropriate path element
            StandardEntity from = world.getEntity(previous);
            StandardEntity to = world.getEntity(next);
            if (from instanceof Road && to instanceof Node) {
                Road road = (Road)from;
                Node node = (Node)to;
                boolean toHead = node.getID().equals(road.getHead());
                if (previous.equals(lastPosition)) {
                    // At origin: must be a PartialRoadPath
                    result.addPath(new PartialRoadPath(road, lastPositionExtra, toHead, false));
                }
                else {
                    // RoadPath
                    result.addPath(new RoadPath(road, toHead, false));
                }
            }
            else if (from instanceof Node && to instanceof Road) {
                Road road = (Road)to;
                Node node = (Node)from;
                boolean fromHead = node.getID().equals(road.getHead());
                if (next.equals(position)) {
                    // At destination: must be a PartialRoadPath
                    result.addPath(new PartialRoadPath(road, positionExtra, fromHead, true));
                }
                else {
                    // RoadPath
                    result.addPath(new RoadPath(road, !fromHead, true));
                }
            }
            else if (from instanceof Node && to instanceof Building) {
                result.addPath(new BuildingEntryPath((Building)to, (Node)from));
            }
            else if (from instanceof Building && to instanceof Node) {
                result.addPath(new BuildingExitPath((Building)from, (Node)to));
            }
            previous = next;
            //            System.out.println("Next path element");
            //            System.out.println("From    : " + from);
            //            System.out.println("To      : " + to);
            //            System.out.println("New path: " + result);
            if (next.equals(position)) {
                break;
            }
        }
        return result;
    }

    private interface Path {
        Pair<Integer, Integer> getPointOnPath(double d);
        double getLength();
    }

    private static class CompositePath implements Path {
        private List<Path> parts;

        CompositePath() {
            parts = new ArrayList<Path>();
        }

        void addPath(Path p) {
            parts.add(p);
        }

        @Override
        public double getLength() {
            double d = 0;
            for (Path next : parts) {
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
            Path result = null;
            for (Path next : parts) {
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
            for (Iterator<Path> it = parts.iterator(); it.hasNext();) {
                result.append(it.next());
                if (it.hasNext()) {
                    result.append(", ");
                }
            }
            return result.toString();
        }
    }

    private abstract class AbstractPath implements Path {
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

    private class PartialRoadPath extends AbstractPath {
        PartialRoadPath(Road road, int positionExtra, boolean toOrFromHead, boolean toPositionExtra) {
            Node head = (Node)road.getHead(world);
            Node tail = (Node)road.getTail(world);
            if (toPositionExtra) {
                setStart(toOrFromHead ? head.getLocation(world) : tail.getLocation(world));
                setEnd(findPositionExtra(road, positionExtra));
                setDescription("From node " + (toOrFromHead ? head.getID() : tail.getID()) + " to road " + road.getID() + " position " + positionExtra);
            }
            else {
                setStart(findPositionExtra(road, positionExtra));
                setEnd(toOrFromHead ? head.getLocation(world) : tail.getLocation(world));
                setDescription("From road " + road.getID() + " position " + positionExtra + " towards node " + (toOrFromHead ? head.getID() : tail.getID()));
            }
            setLength(toOrFromHead ? positionExtra : road.getLength() - positionExtra);
        }

        private Pair<Integer, Integer> findPositionExtra(Road road, double positionExtra) {
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

    private class RoadPath extends AbstractPath {
        RoadPath(Road road, boolean toHead, boolean intoRoad) {
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

    private class BuildingEntryPath extends AbstractPath {
        BuildingEntryPath(Building b, Node entrance) {
            setStart(entrance.getLocation(world));
            setEnd(b.getLocation(world));
            computeLength();
            setDescription("Into building " + b.getID() + " from node " + entrance.getID());
        }
    }

    private class BuildingExitPath extends AbstractPath {
        BuildingExitPath(Building b, Node entrance) {
            setStart(b.getLocation(world));
            setEnd(entrance.getLocation(world));
            computeLength();
            setDescription("Out of building " + b.getID() + " to node " + entrance.getID());
        }
    }
}