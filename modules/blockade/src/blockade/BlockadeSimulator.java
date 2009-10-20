package blockade;

import rescuecore2.components.AbstractSimulator;
import rescuecore2.messages.control.Commands;
import rescuecore2.messages.control.Update;
import rescuecore2.messages.control.SKUpdate;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.Node;
import rescuecore2.misc.collections.LazyMap;
import rescuecore2.misc.Pair;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;

/**
   A simple blockade simulator.
 */
public class BlockadeSimulator extends AbstractSimulator<StandardEntity> {
    private static final int RUBBLE_DIVISOR = 20;
    private static final double NEARBY_THRESHOLD = 5000;

    private Set<Road> changed;
    private Map<EntityID, Set<Road>> nearbyRoads;

    @Override
    public String getName() {
        return "Basic blockade simulator";
    }

    @Override
    protected StandardWorldModel createWorldModel() {
        return new StandardWorldModel();
    }

    @Override
    protected void postConnect() {
        nearbyRoads = new LazyMap<EntityID, Set<Road>>() {
            public Set<Road> createValue() {
                return new HashSet<Road>();
            }
        };
        System.out.println("Computing roads near buildings...");
        for (Entity next : model) {
            if (next instanceof Building) {
                Building b = (Building)next;
                for (Entity road : model) {
                    if (road instanceof Road) {
                        Road r = (Road)road;
                        if (isNear(r, b)) {
                            nearbyRoads.get(b.getID()).add(r);
                        }
                    }
                }
            }
        }
        System.out.println("Done");
        changed = new HashSet<Road>();
    }

    @Override
    protected void handleCommands(Commands c) {
        send(new SKUpdate(simulatorID, c.getTime(), changed));
    }

    @Override
    protected void handleUpdate(Update u) {
        super.handleUpdate(u);
        changed.clear();
        for (Entity next : u.getUpdatedEntities()) {
            if (next instanceof Building) {
                Building b = (Building)next;
                if (b.isBrokennessDefined()) {
                    // Brokenness has changed. Add some blockedness to nearby roads
                    //                    System.out.println(b + " is broken. Updating nearby roads");
                    for (Road r : nearbyRoads.get(b.getID())) {
                        int width = r.getWidth();
                        int block = r.getBlock();
                        int increase = calculateBlock(b);
                        //                        System.out.println("Increasing block of " + r + " by " + increase);
                        block += increase;
                        if (block > width) {
                            block = width;
                        }
                        //                        System.out.println("New block: " + block);
                        r.setBlock(block);
                        changed.add(r);
                    }
                }
            }
        }
    }

    private int calculateBlock(Building b) {
        // CHECKSTYLE:OFF:MagicNumber 100 is OK here as brokenness is a value out of 100.
        long rubble = b.getBrokenness() * b.getGroundArea() * b.getFloors() / 100L;
        // CHECKSTYLE:ON:MagicNumber
        return (int)(rubble / RUBBLE_DIVISOR);
    }

    private boolean isNear(Road r, Building b) {
        //        System.out.println("Is " + r + " near " + b + "?");
        return isNear((Node)r.getHead(model), b) || isNear((Node)r.getTail(model), b);
    }

    private boolean isNear(Node n, Building b) {
        //        System.out.println("Is " + n + " near " + b + "?");
        if (b.getEntrances().contains(n.getID())) {
            //            System.out.println("Building entrance: is nearby");
            return true;
        }
        Pair<Integer, Integer> node = n.getLocation(model);
        //        System.out.println("Node location: " + node.first() + ", " + node.second());
        for (Pair<Integer, Integer> apex : b.getApexesAsList()) {
            double d = Math.hypot(apex.first() - node.first(), apex.second() - node.second());
            //            System.out.println("Apex location: " + apex.first() + ", " + apex.second());
            //            System.out.println("Distance: " + d);
            if (d < NEARBY_THRESHOLD) {
                //                System.out.println("Nearby");
                return true;
            }
        }
        //        System.out.println("Not nearby");
        return false;
    }
}