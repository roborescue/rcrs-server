package rescuecore2.sample;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.components.AbstractAgent;

import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.Node;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Human;

/**
   Abstract base class for sample agents.
 */
public abstract class AbstractSampleAgent extends AbstractAgent<StandardEntity> {
    private static final int MESH_SIZE = 10000;
    private static final int RANDOM_WALK_LENGTH = 50;

    /**
       The world model referenced as a StandardWorldModel. Note that this will reference the same object as {@link AbstractAgent#model}.
     */
    protected StandardWorldModel world;

    /**
       Variable for controlling whether path planning will ignore blocked roads or not. Setting this to false will cause blocked roads to be included in plans. The default is true.
     */
    protected boolean ignoreBlockedRoads;

    /**
       Construct an AbstractSampleAgent.
     */
    protected AbstractSampleAgent() {
        ignoreBlockedRoads = true;
    }

    @Override
    protected WorldModel<StandardEntity> createWorldModel() {
        world = new StandardWorldModel(MESH_SIZE);
        return world;
    }

    @Override
    protected void postConnect() {
        world.index();
    }

    /**
       Get the location of the entity controlled by this agent.
       @return The location of the entity controlled by this agent.
     */
    protected StandardEntity location() {
        Human me = (Human)me();
        return me.getPosition(world);
    }

    /**
       Do a breadth first search from one location to the closest (in terms of number of nodes) of a set of goals.
       @param start The location we start at.
       @param goals The set of possible goals.
       @return The path from start to one of the goals, or null if no path can be found.
    */
    protected List<EntityID> breadthFirstSearch(StandardEntity start, StandardEntity... goals) {
        return breadthFirstSearch(start, Arrays.asList(goals));
    }

    /**
       Do a breadth first search from one location to the closest (in terms of number of nodes) of a set of goals.
       @param start The location we start at.
       @param goals The set of possible goals.
       @return The path from start to one of the goals, or null if no path can be found.
    */
    protected List<EntityID> breadthFirstSearch(StandardEntity start, Collection<? extends StandardEntity> goals) {
        List<StandardEntity> open = new LinkedList<StandardEntity>();
        Map<StandardEntity, StandardEntity> ancestors = new HashMap<StandardEntity, StandardEntity>();
        open.add(start);
        StandardEntity next = null;
        boolean found = false;
        do {
            next = open.remove(0);
            Collection<StandardEntity> neighbours = findNeighbours(next);
            if (neighbours.isEmpty()) {
                continue;
            }
            for (StandardEntity neighbour : neighbours) {
                if (isGoal(neighbour, goals)) {
                    ancestors.put(neighbour, next);
                    next = neighbour;
                    found = true;
                    break;
                }
                else {
                    if (!ancestors.containsKey(neighbour) && !(neighbour instanceof Building)) {
                        open.add(neighbour);
                        ancestors.put(neighbour, next);
                    }
                }
            }
        } while (!found && !open.isEmpty());
        if (!found) {
            // No path
            return null;
        }
        // Walk back from goal to start
        StandardEntity current = next;
        List<EntityID> path = new LinkedList<EntityID>();
        do {
            path.add(0, current.getID());
            current = ancestors.get(current);
            if (current == null) {
                throw new RuntimeException("Found a node with no ancestor! Something is broken.");
            }
        } while (current != start);
        return path;
    }

    /**
       Construct a random walk starting from this agent's current location. Buildings will only be entered at the end of the walk.
       @return A random walk.
     */
    protected List<EntityID> randomWalk() {
        List<EntityID> result = new ArrayList<EntityID>(RANDOM_WALK_LENGTH);
        Set<StandardEntity> seen = new HashSet<StandardEntity>();
        StandardEntity current = location();
        for (int i = 0; i < RANDOM_WALK_LENGTH; ++i) {
            result.add(current.getID());
            seen.add(current);
            List<StandardEntity> neighbours = new ArrayList<StandardEntity>(findNeighbours(current));
            Collections.shuffle(neighbours);
            boolean found = false;
            for (StandardEntity next : neighbours) {
                if (seen.contains(next)) {
                    continue;
                }
                if (next instanceof Building && i < RANDOM_WALK_LENGTH - 1) {
                    continue;
                }
                current = next;
                found = true;
                break;
            }
            if (!found) {
                // We reached a dead-end.
                break;
            }
        }
        return result;
    }

    private Collection<StandardEntity> findNeighbours(StandardEntity e) {
        Collection<StandardEntity> result = new ArrayList<StandardEntity>();
        if (e instanceof Building) {
            for (EntityID next : ((Building)e).getEntrances()) {
                result.add(world.getEntity(next));
            }
        }
        if (e instanceof Node) {
            for (EntityID next : ((Node)e).getEdges()) {
                StandardEntity edge = world.getEntity(next);
                if (ignoreBlockedRoads && edge instanceof Road) {
                    // If it's blocked then ignore it
                    Road r = (Road)edge;
                    int lanes;
                    if (e.getID().equals(r.getHead())) {
                        lanes = r.getLinesToTail();
                    }
                    else {
                        lanes = r.getLinesToHead();
                    }
                    double totalLanes = r.getLinesToHead() + r.getLinesToTail();
                    double laneWidth = r.getWidth() / totalLanes;
                    // CHECKSTYLE:OFF:MagicNumber
                    int blockedLanes = (int)Math.floor((r.getBlock() / laneWidth / 2.0) + 0.5);
                    // CHECKSTYLE:ON:MagicNumber
                    if (lanes - blockedLanes == 0) {
                        continue;
                    }
                }
                result.add(edge);
            }
        }
        if (e instanceof Road) {
            Road r = (Road)e;
            result.add(r.getHead(world));
            result.add(r.getTail(world));
        }
        return result;
    }

    private boolean isGoal(StandardEntity e, Collection<? extends StandardEntity> test) {
        for (StandardEntity next : test) {
            if (next == e) {
                return true;
            }
        }
        return false;
    }

    /**
       A comparator that sorts entities by distance to a reference point.
     */
    protected class DistanceSorter implements Comparator<StandardEntity> {
        private StandardEntity reference;

        /**
           Create a DistanceSorter.
           @param reference The reference point to measure distances from.
         */
        public DistanceSorter(StandardEntity reference) {
            this.reference = reference;
        }

        @Override
        public int compare(StandardEntity a, StandardEntity b) {
            int d1 = world.getDistance(reference, a);
            int d2 = world.getDistance(reference, b);
            return d1 - d2;
        }
    }
}