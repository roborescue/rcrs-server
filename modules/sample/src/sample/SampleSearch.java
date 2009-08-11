package sample;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.Node;
import rescuecore2.standard.entities.Building;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

/**
   A sample search class.
 */
public final class SampleSearch {
    private boolean ignoreBlockedRoads;
    private StandardWorldModel world;

    /**
       Construct a new SampleSearch.
       @param world The world model to search.
       @param ignoreBlockedRoads Whether searches should treat blocked roads as passable or not. If true then blocked roads will be ignored, i.e. not included in the path. If false then blocked roads will be included in the path.
     */
    public SampleSearch(StandardWorldModel world, boolean ignoreBlockedRoads) {
        this.world = world;
        this.ignoreBlockedRoads = ignoreBlockedRoads;
    }

    /**
       Set whether searches should treat blocked roads as passable or not. If true then blocked roads will be ignored, i.e. not included in the path. If false then blocked roads will be included in the path.
       @param b The new ignoreBlockedRoads parameter.
     */
    public void setIgnoreBlockedRoads(boolean b) {
        ignoreBlockedRoads = b;
    }

    /**
       Do a breadth first search from one location to the closest (in terms of number of nodes) of a set of goals.
       @param start The location we start at.
       @param goals The set of possible goals.
       @return The path from start to one of the goals, or null if no path can be found.
    */
    public List<EntityID> breadthFirstSearch(StandardEntity start, StandardEntity... goals) {
        return breadthFirstSearch(start, Arrays.asList(goals));
    }

    /**
       Do a breadth first search from one location to the closest (in terms of number of nodes) of a set of goals.
       @param start The location we start at.
       @param goals The set of possible goals.
       @return The path from start to one of the goals, or null if no path can be found.
    */
    public List<EntityID> breadthFirstSearch(StandardEntity start, Collection<? extends StandardEntity> goals) {
        List<StandardEntity> open = new LinkedList<StandardEntity>();
        Map<StandardEntity, StandardEntity> ancestors = new HashMap<StandardEntity, StandardEntity>();
        open.add(start);
        StandardEntity next = null;
        boolean found = false;
        //        System.out.println("Planning path from " + start + " to " + goals);
        ancestors.put(start, null);
        do {
            next = open.remove(0);
            //            System.out.println("Next: " + next);
            Collection<StandardEntity> neighbours = findNeighbours(next);
            //            System.out.println("Neighbours: " + neighbours);
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
            //            System.out.println("No path found");
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
       Get the neighbours of an entity.
       @param e The entity to look up.
       @return All neighbours of that entity.
    */
    public Collection<StandardEntity> findNeighbours(StandardEntity e) {
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
                    int blocked = r.countBlockedLanes();
                    // Assume symmetric road
                    int lanes = r.getLinesToHead();
                    if (blocked == lanes) {
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
            if (next.getID().equals(e.getID())) {
                return true;
            }
        }
        return false;
    }
}