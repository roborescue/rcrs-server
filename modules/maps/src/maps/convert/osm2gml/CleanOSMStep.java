package maps.convert.osm2gml;

import maps.osm.OSMMap;
import maps.osm.OSMNode;
import maps.osm.OSMRoad;
import maps.osm.OSMBuilding;
import maps.osm.OSMWay;
import maps.convert.ConvertStep;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.Collections;

import rescuecore2.misc.geometry.Line2D;
//import rescuecore2.log.Logger;

/**
   This step cleans the OpenStreetMap data by removing duplicate nodes and way, fixing degenerate ways, and fixing building edge orderings.
*/
public class CleanOSMStep extends ConvertStep {
    private TemporaryMap map;

    /**
       Construct a CleanOSMStep.
       @param map The TemporaryMap to clean.
    */
    public CleanOSMStep(TemporaryMap map) {
        this.map = map;
    }

    @Override
    public String getDescription() {
        return "Cleaning OpenStreetMap data";
    }

    @Override
    protected void step() {
        OSMMap osm = map.getOSMMap();
        setProgressLimit(osm.getNodes().size() + (osm.getRoads().size() + osm.getBuildings().size()) * 2 + osm.getBuildings().size());
        setStatus("Looking for duplicate nodes");
        int nodes = fixNodes();
        setStatus("Fixing degenerate ways");
        int fixed = fixDegenerateWays(osm.getRoads());
        fixed += fixDegenerateWays(osm.getBuildings());
        setStatus("Looking for duplicate ways");
        int ways = fixDuplicateWays(osm.getRoads());
        ways += fixDuplicateWays(osm.getBuildings());
        setStatus("Fixing building direction");
        int b = fixBuildingDirection(osm.getBuildings());
        setStatus("Removed " + nodes + " duplicate nodes and " + ways + " duplicate ways, fixed " + fixed + " degenerate ways, fixed " + b + " clockwise buildings");
    }

    private int fixNodes() {
        OSMMap osm = map.getOSMMap();
        int count = 0;
        double threshold = ConvertTools.nearbyThreshold(osm, map.getNearbyThreshold());
        Set<OSMNode> removed = new HashSet<OSMNode>();
        for (OSMNode next : osm.getNodes()) {
            if (removed.contains(next)) {
                bumpProgress();
                continue;
            }
            for (OSMNode test : osm.getNodes()) {
                if (next == test) {
                    continue;
                }
                if (removed.contains(test)) {
                    continue;
                }
                if (nearby(next, test, threshold)) {
                    // Remove the test node and replace all references to it with 'next'
                    osm.replaceNode(test, next);
                    removed.add(test);
                    //                    Logger.debug("Removed duplicate node " + test.getID());
                    ++count;
                }
            }
            bumpProgress();
        }
        return count;
    }

    private int fixDegenerateWays(Collection<? extends OSMWay> ways) {
        int count = 0;
        for (OSMWay way : ways) {
            // Check that no nodes are listed multiple times in sequence
            List<Long> ids = new ArrayList<Long>(way.getNodeIDs());
            Iterator<Long> it = ids.iterator();
            if (!it.hasNext()) {
                // Empty way. Remove it.
                remove(way);
                ++count;
                continue;
            }
            long last = it.next();
            boolean fixed = false;
            while (it.hasNext()) {
                long next = it.next();
                if (next == last) {
                    // Duplicate node
                    it.remove();
                    //                    Logger.debug("Removed node " + next + " from way " + way.getID());
                    fixed = true;
                }
                last = next;
            }
            if (fixed) {
                way.setNodeIDs(ids);
                ++count;
            }
            bumpProgress();
        }
        return count;
    }

    private int fixDuplicateWays(Collection<? extends OSMWay> ways) {
        int count = 0;
        Set<OSMWay> removed = new HashSet<OSMWay>();
        for (OSMWay next : ways) {
            if (removed.contains(next)) {
                bumpProgress();
                continue;
            }
            // Look at all other roads and see if any are subpaths of this road
            for (OSMWay test : ways) {
                if (next == test) {
                    continue;
                }
                if (removed.contains(test)) {
                    continue;
                }
                List<Long> testIDs = test.getNodeIDs();
                if (isSubList(testIDs, next.getNodeIDs())) {
                    remove(test);
                    removed.add(test);
                    ++count;
                    //                    Logger.debug("Removed way " + test.getID());
                }
                else {
                    Collections.reverse(testIDs);
                    if (isSubList(testIDs, next.getNodeIDs())) {
                        remove(test);
                        removed.add(test);
                        ++count;
                        //                        Logger.debug("Removed way " + test.getID());
                    }
                }
            }
            bumpProgress();
        }
        return count;
    }

    /**
       Make sure all buildings have their nodes listed in clockwise order.
    */
    private int fixBuildingDirection(Collection<OSMBuilding> buildings) {
        OSMMap osm = map.getOSMMap();
        // Sum the angles of all right-hand turns
        // If the total is +360 then order is clockwise, -360 means counterclockwise.
        int count = 0;
        for (OSMBuilding building : buildings) {
            //            Logger.debug("Building " + building + " angle sum: " + ConvertTools.getAnglesSum(building, osm));
            if (ConvertTools.isClockwise(building, osm)) {
                // Reverse the order
                List<Long> ids = building.getNodeIDs();
                Collections.reverse(ids);
                building.setNodeIDs(ids);
                ++count;
            }
            bumpProgress();
        }
        return count;
    }

    private boolean nearby(OSMNode first, OSMNode second, double threshold) {
        double dx = first.getLongitude() - second.getLongitude();
        double dy = first.getLatitude() - second.getLatitude();
        return (dx >= -threshold
                && dx <= threshold
                && dy >= -threshold
                && dy <= threshold);
    }

    private boolean isSubList(List<Long> first, List<Long> second) {
        return Collections.indexOfSubList(second, first) != -1;
    }

    private void remove(OSMWay way) {
        OSMMap osm = map.getOSMMap();
        if (way instanceof OSMRoad) {
            osm.removeRoad((OSMRoad)way);
        }
        else if (way instanceof OSMBuilding) {
            osm.removeBuilding((OSMBuilding)way);
        }
        else {
            throw new IllegalArgumentException("Don't know how to handle this type of OSMWay: " + way.getClass().getName());
        }
    }

    private Line2D makeLine(long first, long second) {
        OSMMap osm = map.getOSMMap();
        OSMNode n1 = osm.getNode(first);
        OSMNode n2 = osm.getNode(second);
        return new Line2D(n1.getLongitude(), n1.getLatitude(), n2.getLongitude() - n1.getLongitude(), n2.getLatitude() - n1.getLatitude());
    }
}
