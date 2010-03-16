package maps.convert.osm2gml;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.Collections;

import java.awt.Color;

import maps.convert.ConvertStep;

/**
   This class merges adjacent shapes of the same type.
*/
public class MergeShapesStep extends ConvertStep {
    private TemporaryMap map;

    /**
       Construct a MergeShapesStep.
       @param map The TemporaryMap to use.
    */
    public MergeShapesStep(TemporaryMap map) {
        this.map = map;
    }

    @Override
    public String getDescription() {
        return "Merging adjacent shapes";
    }

    @Override
    protected void step() {
        debug.setBackground(ConvertTools.getAllDebugShapes(map));
        Collection<TemporaryBuilding> buildings = map.getBuildings();
        Collection<TemporaryRoad> roads = map.getRoads();
        setProgressLimit(buildings.size() + roads.size());
        // Merge any buildings with the same ID that got split earlier.
        int buildingCount = 0;
        int roadCount = 0;
        for (TemporaryBuilding next : buildings) {
            if (tryToMerge(next)) {
                ++buildingCount;
            }
            bumpProgress();
        }
        // Try merging adjacent roads
        for (TemporaryRoad next : roads) {
            if (tryToMerge(next)) {
                ++roadCount;
            }
            bumpProgress();
        }
        setStatus("Merged " + buildingCount + " building shapes and " + roadCount + " road shapes");
    }

    private boolean tryToMerge(TemporaryBuilding b) {
        if (!map.getBuildings().contains(b)) {
            return false;
        }
        Collection<TemporaryBuilding> others = map.getBuildings();
        for (TemporaryBuilding other : others) {
            if (b == other) {
                continue;
            }
            if (other.getBuildingID() == b.getBuildingID()) {
                List<DirectedEdge> boundary = mergeShapes(b, other);
                if (boundary == null) {
                    continue;
                }
                TemporaryBuilding newBuilding = new TemporaryBuilding(boundary, b.getBuildingID());
                map.addBuilding(newBuilding);
                map.removeBuilding(b);
                map.removeBuilding(other);
                debug.show("Merged buildings", new TemporaryObjectInfo(b, "First", Color.BLACK, Color.GREEN),
                           new TemporaryObjectInfo(other, "Second", Color.BLACK, Color.WHITE),
                           new TemporaryObjectInfo(newBuilding, "New building", Color.BLUE, null));
                return true;
            }
        }
        return false;
    }

    private boolean tryToMerge(TemporaryRoad r) {
        if (!map.getRoads().contains(r)) {
            return false;
        }
        Collection<TemporaryRoad> others = map.getRoads();
        for (TemporaryRoad other : others) {
            if (r == other) {
                continue;
            }
            List<DirectedEdge> boundary = mergeShapes(r, other);
            if (boundary == null) {
                continue;
            }
            // Check for convexity
            if (ConvertTools.isConvex(boundary)) {
                TemporaryRoad newRoad = new TemporaryRoad(boundary);
                map.addRoad(newRoad);
                map.removeRoad(r);
                map.removeRoad(other);
                debug.show("Merged roads", new TemporaryObjectInfo(r, "First", Color.BLACK, Color.GREEN),
                           new TemporaryObjectInfo(other, "Second", Color.BLACK, Color.WHITE),
                           new TemporaryObjectInfo(newRoad, "New road", Color.BLUE, null));
                return true;
            }
        }
        return false;
    }

    private List<DirectedEdge> mergeShapes(TemporaryObject first, TemporaryObject second) {
        Map<Edge, DirectedEdge> edges1 = new HashMap<Edge, DirectedEdge>();
        Map<Edge, DirectedEdge> edges2 = new HashMap<Edge, DirectedEdge>();
        for (DirectedEdge e : first.getEdges()) {
            edges1.put(e.getEdge(), e);
        }
        for (DirectedEdge e : second.getEdges()) {
            edges2.put(e.getEdge(), e);
        }
        if (Collections.disjoint(edges1.keySet(), edges2.keySet())) {
            return null;
        }
        Set<DirectedEdge> boundary = new HashSet<DirectedEdge>();
        for (Map.Entry<Edge, DirectedEdge> next : edges1.entrySet()) {
            if (!edges2.containsKey(next.getKey())) {
                boundary.add(next.getValue());
            }
        }
        for (Map.Entry<Edge, DirectedEdge> next : edges2.entrySet()) {
            if (!edges1.containsKey(next.getKey())) {
                boundary.add(next.getValue());
            }
        }
        // Walk the boundary
        DirectedEdge start = boundary.iterator().next();
        List<DirectedEdge> result = new ArrayList<DirectedEdge>();
        result.add(start);
        while (!boundary.isEmpty()) {
            start = findNextEdge(start, boundary);
            boundary.remove(start);
            result.add(start);
        }
        return result;
    }

    private DirectedEdge findNextEdge(DirectedEdge from, Set<DirectedEdge> candidates) {
        Node n = from.getEndNode();
        for (DirectedEdge next : candidates) {
            if (next.getStartNode().equals(n)) {
                return next;
            }
        }
        throw new IllegalArgumentException("No candidate edge starting from " + n);
    }
}
