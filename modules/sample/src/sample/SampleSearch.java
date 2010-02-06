package sample;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Building;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
   A sample search class.
 */
public final class SampleSearch {
    private static final Log LOG = LogFactory.getLog(SampleSearch.class);

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
        ancestors.put(start, null);
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
        //        LOG.debug("Building path");
        //        LOG.debug("Goal found: " + current);
        do {
            path.add(0, current.getID());
            current = ancestors.get(current);
            //            LOG.debug("Parent node: " + current);
            if (current == null) {
                throw new RuntimeException("Found a node with no ancestor! Something is broken.");
            }
        } while (current != start);
        //        LOG.debug("Final path: " + path);
        return path;
    }

    /**
       Get the neighbours of an entity.
       @param e The entity to look up.
       @return All neighbours of that entity.
    */
    public Collection<StandardEntity> findNeighbours(StandardEntity e) {
        Collection<StandardEntity> result = new ArrayList<StandardEntity>();
        if (e instanceof Area) {
            Area a = (Area)e;
            for (EntityID next : a.getNeighbours()) {
                result.add(world.getEntity(next));
            }
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