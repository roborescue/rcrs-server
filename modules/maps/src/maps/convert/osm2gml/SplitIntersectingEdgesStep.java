package maps.convert.osm2gml;

import java.util.*;
import java.util.List;

import maps.convert.osm2gml.debug.*;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.GeometryTools2D;

import maps.convert.ConvertStep;

/**
 * This step splits any edges that intersect.
 */
public class SplitIntersectingEdgesStep extends ConvertStep {
    // Tolerance used when comparing t-parameters in diagonal-line containment checks.
    private static final double T_PARAMETER_TOLERANCE = 1e-8;

    private final TemporaryMap map;
    private int splitCount;

    private final Set<Node> createdNodes;
    private final Set<Edge> createdEdges;

    /**
     * Construct a {@code SplitIntersectingEdgesStep}.
     * @param map The {@code TemporaryMap} to use.
     */
    public SplitIntersectingEdgesStep(final TemporaryMap map) {
        this.map = map;
        this.splitCount = 0;

        this.createdNodes = new HashSet<>();
        this.createdEdges = new HashSet<>();
    }

    @Override
    public String getDescription() {
        return "Splitting intersecting edges";
    }

    @Override
    protected void step() {
        int inspectedCount = 0;

        // Build the spatial grid once upfront.
        SpatialGrid<Edge> grid = createEdgeGrid();

        // Initialize the work queue with all edges.
        // inQueue tracks membership to avoid redundant re-enqueuing.
        Queue<Edge> workQueue = new ArrayDeque<>(map.getAllEdges());
        Set<Edge> inQueue = new HashSet<>(workQueue);

        setProgressLimit(workQueue.size());

        while (!workQueue.isEmpty()) {
            Edge target = workQueue.poll();
            inQueue.remove(target);
            inspectedCount++;

            // Skip edges that were removed from the map by a previous split.
            if (!map.containsEdge(target)) {
                bumpProgress();
                continue;
            }

            final Set<Edge> newEdges = processEdge(target, grid);

            // Re-enqueue only the newly created edges and their spatial neighbors.
            // These are the only edges whose intersection status may have changed.
            for (Edge newEdge : newEdges) {
                enqueueIfAbsent(newEdge, workQueue, inQueue);
                for (Edge neighbor : grid.getNearbyItems(newEdge)) {
                    if (map.containsEdge(neighbor)) {
                        enqueueIfAbsent(neighbor, workQueue, inQueue);
                    }
                }
            }

            // Expand the progress limit to reflect newly created work.
            setProgressLimit(getProgressLimit() + newEdges.size());
            bumpProgress();
        }

        setStatus("Inspected " + inspectedCount + " edges and split " + splitCount + " times");
        visualizeResults();
    }

    private SpatialGrid<Edge> createEdgeGrid() {
        Collection<Edge> edges = map.getAllEdges();
        final double averageLength = edges.stream()
                .map(Edge::getLine)
                .mapToDouble(Line2D::getLength)
                .average().orElseThrow();
        final double cellSize = Math.max(averageLength, ConvertTools.sizeOf1Metre(map.getOSMMap()));
        SpatialGrid<Edge> grid = new SpatialGrid<>(map.getBounds(), cellSize);
        map.getAllEdges().forEach(grid::add);
        return grid;
    }

    // Enqueue the edge only if it is not already in the queue.
    private void enqueueIfAbsent(Edge edge, Queue<Edge> queue, Set<Edge> inQueue) {
        if (inQueue.add(edge)) {
            queue.add(edge);
        }
    }


    // Process one edge: attempt to split it against all nearby candidates.
    // Returns the set of edges newly created by any splits.
    private Set<Edge> processEdge(Edge target, SpatialGrid<Edge> grid) {
        Set<Edge> created = new HashSet<>();

        // Keep retrying until a full candidate scan completes without any splits.
        // Each split changes the map topology, so the candidate list must be rescanned.
        while (true) {
            if (!map.containsEdge(target)) break;
            boolean splitThisIteration = false;
            for (Edge candidate : grid.getNearbyItems(target)) {
                if (candidate.equals(target)) continue;
                if (!map.containsEdge(candidate)) continue;
                if (!map.containsEdge(target)) break;

                final Set<Edge> newFromSplit = trySplit(target, candidate, grid);
                if (!newFromSplit.isEmpty()) {
                    created.addAll(newFromSplit);
                    splitThisIteration = true;
                    break; // Restart after topology change
                }
            }

            if (!splitThisIteration) break;
        }

        return created;
    }

    // Attempt to split target and candidate at their intersection.
    // Updates the grid in place and returns any newly created edges.
    private Set<Edge> trySplit(Edge target, Edge candidate, SpatialGrid<Edge> grid) {
        if (GeometryTools2D.parallel(target.getLine(), candidate.getLine())) {
            return processParallelLines(target, candidate, grid);
        } else {
            return checkForIntersection(target, candidate, grid);
        }
    }

    // Processes overlapping parallel edges.
    // Returns the newly created edges if either edge was split.
    private Set<Edge> processParallelLines(Edge first, Edge second, SpatialGrid<Edge> grid) {
        final double e1Length  = first.getLine().getDirection().getLength();
        final double e2Length  = second.getLine().getDirection().getLength();
        final Edge shorterEdge = e1Length < e2Length ? first : second;
        final Edge longerEdge  = e1Length < e2Length ? second : first;

        final boolean isShorterStartEqualsLongerStart = shorterEdge.getStart().equals(longerEdge.getStart());
        final boolean isShorterStartEqualsLongerEnd   = shorterEdge.getStart().equals(longerEdge.getEnd());
        final boolean isShorterEndEqualsLongerStart   = shorterEdge.getEnd().equals(longerEdge.getStart());
        final boolean isShorterEndEqualsLongerEnd     = shorterEdge.getEnd().equals(longerEdge.getEnd());

        final boolean isStartShared = isShorterStartEqualsLongerStart || isShorterStartEqualsLongerEnd;
        final boolean isEndShared   = isShorterEndEqualsLongerStart   || isShorterEndEqualsLongerEnd;
        final Point2D shorterStart  = shorterEdge.getStart().getPoint();
        final Point2D shorterEnd    = shorterEdge.getEnd().getPoint();
        final boolean isStartInside = !isStartShared && containsRobust(longerEdge.getLine(), shorterStart);
        final boolean isEndInside   = !isEndShared   && containsRobust(longerEdge.getLine(), shorterEnd);

        if (isStartInside && isEndInside) {
            return processInternalEdge(shorterEdge, longerEdge, grid);
        }
        if (isStartShared && isEndInside) {
            return processCoincidentNode(shorterEdge, longerEdge, shorterEdge.getStart(), grid);
        }
        if (isEndShared && isStartInside) {
            return processCoincidentNode(shorterEdge, longerEdge, shorterEdge.getEnd(), grid);
        }
        if (isStartInside || isEndInside) {
            return processOverlap(shorterEdge, longerEdge, grid);
        }
        return Collections.emptySet();
    }

    // Returns the newly created edges if either edge was split,
    // or an empty set otherwise.
    private Set<Edge> checkForIntersection(Edge first, Edge second, SpatialGrid<Edge> grid) {
        final Point2D intersection = resolveIntersectionPoint(first, second);
        if (intersection == null) return Collections.emptySet();

        final Node node = map.getNode(intersection);
        final boolean splitFirst = !node.equals(first.getStart()) && !node.equals(first.getEnd());
        final boolean splitSecond = !node.equals(second.getStart()) && !node.equals(second.getEnd());

        Set<Edge> created = new HashSet<>();
        if (splitFirst) created.addAll(splitAndRegister(first, grid, node));
        if (splitSecond) created.addAll(splitAndRegister(second, grid, node));
        return created;
    }

    // Resolve the intersection point between two segments, falling back to the
    // infinite-line intersection when an endpoint lies near the other line.
    // Returns null if no valid intersection lies within both segments.
    private Point2D resolveIntersectionPoint(Edge first, Edge second) {
        Line2D l1 = first.getLine();
        Line2D l2 = second.getLine();
        Point2D segmentIntersection = GeometryTools2D.getSegmentIntersectionPoint(l1, l2);
        if (segmentIntersection != null) return segmentIntersection;

        Point2D lineIntersection = Objects.requireNonNull(GeometryTools2D.getIntersectionPoint(l1, l2));
        if (isNearEndpoint(first, lineIntersection)) {
            final double d = second.getLine().getIntersection(first.getLine());
            return (d < 0 || 1 < d) ? null : lineIntersection;
        }
        if (isNearEndpoint(second, lineIntersection)) {
            final double d = first.getLine().getIntersection(second.getLine());
            return (d < 0 || 1 < d) ? null : lineIntersection;
        }
        return null;
    }

    // Returns true if the point lies near either endpoint of the edge.
    private boolean isNearEndpoint(Edge edge, Point2D point) {
        return map.isNear(point, edge.getStart().getPoint()) ||
               map.isNear(point, edge.getEnd().getPoint());
    }

    // Returns whether the specified point lies on the given line segment.
    private boolean containsRobust(Line2D line, Point2D point) {
        if (GeometryTools2D.nearlyZero(line.getDirection().getX()) ||
            GeometryTools2D.nearlyZero(line.getDirection().getY())) {
            return GeometryTools2D.contains(line, point);
        }

        final double offsetX = point.getX() - line.getOrigin().getX();
        final double offsetY = point.getY() - line.getOrigin().getY();
        final double tx = offsetX / line.getDirection().getX();
        final double ty = offsetY / line.getDirection().getY();

        // Out of segment bounds.
        if (tx < 0 || 1 < tx || ty < 0 || 1 < ty) return false;

        return Math.abs(tx - ty) <= T_PARAMETER_TOLERANCE;
    }

    // Splits the longer edge into chunks using the endpoints of the internal shorter edge.
    // Returns the newly created edges.
    private Set<Edge> processInternalEdge(Edge shorterEdge, Edge longerEdge, SpatialGrid<Edge> grid) {
        final double t1 = GeometryTools2D.positionOnLine(longerEdge.getLine(), shorterEdge.getLine().getOrigin());
        final double t2 = GeometryTools2D.positionOnLine(longerEdge.getLine(), shorterEdge.getLine().getEndPoint());

        Node cutPoint1 = (t1 < t2) ? shorterEdge.getStart() : shorterEdge.getEnd();
        Node cutPoint2 = (t1 < t2) ? shorterEdge.getEnd() : shorterEdge.getStart();

        // Check validity to prevent zero-length edges.
        Node longerEdgeStart = longerEdge.getStart();
        Node longerEdgeEnd = longerEdge.getEnd();
        final boolean isFirstValid = !cutPoint1.equals(longerEdgeStart)  && !cutPoint1.equals(longerEdgeEnd);
        final boolean isSecondValid = !cutPoint2.equals(longerEdgeStart) && !cutPoint2.equals(longerEdgeEnd);

        if (isFirstValid && isSecondValid) {
            return splitAndRegister(longerEdge, grid, cutPoint1, cutPoint2);
        }
        if (isFirstValid) {
            return splitAndRegister(longerEdge, grid, cutPoint1);
        }
        if (isSecondValid) {
            return splitAndRegister(longerEdge, grid, cutPoint2);
        }
        return Collections.emptySet();
    }

    // Splits the longer edge at the non-shared node of the shorter edge.
    // Returns the newly created edges.
    private Set<Edge> processCoincidentNode(
            Edge shorterEdge, Edge longerEdge, Node sharedNode, SpatialGrid<Edge> grid) {

        Node cutPoint = sharedNode.equals(shorterEdge.getStart()) ? shorterEdge.getEnd() : shorterEdge.getStart();
        if (cutPoint.equals(longerEdge.getStart()) || cutPoint.equals(longerEdge.getEnd())) {
            return Collections.emptySet();
        }
        return splitAndRegister(longerEdge, grid, cutPoint);
    }

    // Splits both edges at their overlap boundary.
    // Returns the newly created edges.
    private Set<Edge> processOverlap(Edge shorterEdge, Edge longerEdge, SpatialGrid<Edge> grid) {
        Line2D shorterLine = shorterEdge.getLine();
        Line2D longerLine = longerEdge.getLine();
        Node shorterStart = shorterEdge.getStart();
        Node shorterEnd = shorterEdge.getEnd();
        Node longerStart = longerEdge.getStart();
        Node longerEnd = longerEdge.getEnd();
        Node shorterCutPoint = GeometryTools2D.contains(shorterLine, longerLine.getOrigin()) ? longerStart : longerEnd;
        Node longerCutPoint = GeometryTools2D.contains(longerLine, shorterLine.getOrigin()) ? shorterStart : shorterEnd;

        Set<Edge> created = new HashSet<>();
        if (!shorterCutPoint.equals(shorterStart) && !shorterCutPoint.equals(shorterEnd)) {
            created.addAll(splitAndRegister(shorterEdge, grid, shorterCutPoint));
        }
        if (!longerCutPoint.equals(longerStart) && !longerCutPoint.equals(longerEnd)) {
            created.addAll(splitAndRegister(longerEdge, grid, longerCutPoint));
        }
        return created;
    }

    // Splits an edge at the given nodes, updates the grid,
    // and returns the newly created edges.
    private Set<Edge> splitAndRegister(Edge edge, SpatialGrid<Edge> grid, Node... splitNodes) {
        List<Edge> created = map.splitEdge(edge, splitNodes);
        grid.remove(edge);
        created.forEach(grid::add);

        createdEdges.remove(edge);
        createdEdges.addAll(created);
        Collections.addAll(createdNodes, splitNodes);
        splitCount += splitNodes.length;
        return new HashSet<>(created);
    }

    private void visualizeResults() {
        StepVisualizer.create(debug)
                .title("Split Intersecting Edges")
                .layer(PointLayer.of(createdNodes)
                        .name("Created Node")
                        .color(DebugPalette.MOSS_STROKE))
                .layer(LineLayer.of(createdEdges)
                        .name("Created Edge")
                        .color(DebugPalette.MOSS_STROKE))
                .backgroundLayer(LineLayer.of(map.getAllEdges())
                        .color(DebugPalette.SLATE_STROKE))
                .show();
    }

}
