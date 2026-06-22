package maps.convert.osm2gml;

import java.util.*;

/**
 * This step detects self-intersecting building and splits them into valid polygons.
 *
 * <p>
 * Self-intersections can occur when nearby OSM nodes are snapped to the same position
 * during {@code TemporaryMap} node registration, causing a single node to appear more than once
 * in a building's edge list. This step resolves such cases by splitting the building
 * at the duplicated node into two separate buildings.
 *
 * <p>
 * This step should run immediately after {@code MakeTempObjectsStep}, before any geometric
 * processing that assumes valid (non-self-intersecting) polygons.
 */
public class SplitSelfIntersectingBuildingsStep extends BaseModificationStep {

    public SplitSelfIntersectingBuildingsStep(final TemporaryMap map) {
        super(map);
    }

    @Override
    public String getDescription() {
        return "Splitting self-intersecting buildings";
    }

    @Override
    protected void step() {
        final List<TemporaryBuilding> before  = new ArrayList<>(map.getBuildings());
        final List<TemporaryBuilding> removed = new ArrayList<>();
        final List<TemporaryBuilding> added   = new ArrayList<>();

        setProgressLimit(before.size());

        // Process each building; use a queue to handle recursive splits.
        final Queue<TemporaryBuilding> queue = new ArrayDeque<>(before);
        while (!queue.isEmpty()) {
            final TemporaryBuilding building = queue.poll();

            final Node duplicateNode = findDuplicateNode(building);
            if (duplicateNode == null) {
                // No self-intersection found; building is valid.
                bumpProgress();
                continue;
            }

            // Split the building at the duplicate node into two buildings.
            final List<TemporaryBuilding> splitResult = splitAtNode(building, duplicateNode);
            if (splitResult.isEmpty()) {
                // Split failed; leave the building as-is.
                bumpProgress();
                continue;
            }

            // Mark the original building for removal and enqueue the splits for further checking.
            removed.add(building);
            map.removeBuilding(building);
            for (final TemporaryBuilding split : splitResult) {
                map.addBuilding(split);
                added.add(split);
                queue.add(split);
            }

            bumpProgress();
        }

        if (!removed.isEmpty()) {
            map.resynchronizeStateFromObjects();
        }

        setStatus("Split " + removed.size() + " self-intersecting buildings into " + added.size() + " .");
        visualizeDifference(removed, added, "Split Self-Intersection Buildings");
    }

    // Find the first node that appears more than once in the building's edge list
    private Node findDuplicateNode(final TemporaryBuilding building) {
        final Map<Node, Integer> seen = new LinkedHashMap<>();
        for (final DirectedEdge edge : building.getEdges()) {
            final Node start = edge.getStartNode();
            final int count = seen.getOrDefault(start, 0) + 1;
            if (1 < count) {
                return start;
            }
            seen.put(start, count);
        }
        return null;
    }

    // Split a building's edge list at a duplicate node into two separate buildings.
    private List<TemporaryBuilding> splitAtNode(
            final TemporaryBuilding building, final Node pivot) {
        final List<DirectedEdge> edges = building.getEdges();

        // Collect indices where the pivot node appears as a start node.
        final List<Integer> pivotIndices = new ArrayList<>();
        for (int i = 0; i < edges.size(); i++) {
            if (edges.get(i).getStartNode().equals(pivot)) {
                pivotIndices.add(i);
            }
        }

        // Require exactly two occurrences to perform a clean split.
        if (pivotIndices.size() != 2) {
            return Collections.emptyList();
        }

        final int first  = pivotIndices.get(0);
        final int second = pivotIndices.get(1);

        // First polygon: edges from first occurrence to second occurrence, closed by pivot.
        final List<DirectedEdge> path1 = new ArrayList<>(edges.subList(first, second));
        final DirectedEdge closeEdge1 = map.getDirectedEdge(edges.get(second).getStartNode(), pivot);


        path1.add(map.getDirectedEdge(edges.get(second).getStartNode(), pivot));

        // Second polygon: edges from second occurrence to end, then from start to first
        // occurrence, close by pivot.
        final List<DirectedEdge> path2 = new ArrayList<>(edges.subList(second, edges.size()));
        path2.addAll(edges.subList(0, first));
        final DirectedEdge closeEdge2 = map.getDirectedEdge(edges.get(first).getStartNode(), pivot);

        // Guard: closing edge would be zero-length; splitting is not possible.
        if (closeEdge1 == null || closeEdge2 == null) {
            return Collections.emptyList();
        }

        // Guard: discard degenerate polygons with fewer than 3 edges.
        if (path1.size() < 3 || path2.size() < 3) {
            return Collections.emptyList();
        }

        final TemporaryBuilding building1 = new TemporaryBuilding(path1, building.getBuildingID());
        final TemporaryBuilding building2 = new TemporaryBuilding(path2, building.getBuildingID());

        return List.of(building1, building2);
    }
}
