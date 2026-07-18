package maps.convert.osm2gml;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;

import maps.convert.osm2gml.debug.LineLayer;
import maps.convert.osm2gml.debug.PointLayer;
import maps.convert.osm2gml.debug.StepVisualizer;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.GeometryTools2D;

import maps.convert.ConvertStep;

/**
 * This step splits any edges that intersect.
 */
public class SplitIntersectingEdgesStep extends ConvertStep {
    private final TemporaryMap map;
    private int splitCount;

    // Tolerance used when comparing t-parameters in diagonal-line containment checks.
    private static final double T_PARAMETER_TOLERANCE = 1e-8;

    private final Set<Node> createdNodes;
    private final Set<Edge> createdEdges;
    private final Set<Edge> intactEdges;

    /**
     * Construct a {@code SplitIntersectingEdgesStep}.
     * @param map The {@code TemporaryMap} to use.
     */
    public SplitIntersectingEdgesStep(final TemporaryMap map) {
        this.map = map;

        createdNodes = new HashSet<>();
        createdEdges = new HashSet<>();
        intactEdges = new HashSet<>();
    }

    @Override
    public String getDescription() {
        return "Splitting intersecting edges";
    }

    @Override
    protected void step() {
        splitCount = 0;
        int inspectedCount = 0;

        // Build the spatial grid once upfront.
        final Rectangle2D bounds = map.getBounds();
        double cellSize = (bounds.getWidth() + bounds.getHeight()) / 2.0 / 100.0;
        if (cellSize < 1e-9) cellSize = 1e-9;
        final SpatialGrid<Edge> grid = new SpatialGrid<>(bounds, cellSize);
        map.getAllEdges().forEach(grid::add);

        // Initialize the work queue with all edges.
        // inQueue tracks membership to avoid redundant re-enqueuing.
        final Queue<Edge> workQueue = new ArrayDeque<>(map.getAllEdges());
        final Set<Edge> inQueue = new HashSet<>(workQueue);

        intactEdges.addAll(map.getAllEdges());
        setProgressLimit(workQueue.size());

        while (!workQueue.isEmpty()) {
            final Edge target = workQueue.poll();
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
            for (final Edge newEdge : newEdges) {
                enqueueIfAbsent(newEdge, workQueue, inQueue);
                for (final Edge neighbor : grid.getNearbyItems(newEdge)) {
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
        visualizeResult();
    }

    // Enqueue the edge only if it is not already in the queue.
    private void enqueueIfAbsent(
            final Edge edge, final Queue<Edge> queue, final Set<Edge> inQueue) {
        if (inQueue.add(edge)) {
            queue.add(edge);
        }
    }


    // Process one edge: attempt to split it against all nearby candidates.
    // Returns the set of edges newly created by any splits.
    private Set<Edge> processEdge(final Edge target, final SpatialGrid<Edge> grid) {
        final Set<Edge> created = new HashSet<>();

        // Keep retrying until a full candidate scan completes without any splits.
        // Each split changes the map topology, so the candidate list must be rescanned.
        while (true) {
            if (!map.containsEdge(target)) break;
            boolean splitThisIteration = false;
            for (final Edge candidate : grid.getNearbyItems(target)) {
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
    private Set<Edge> trySplit(
            final Edge target, final Edge candidate, final SpatialGrid<Edge> grid) {
        final Line2D targetLine    = target.getLine();
        final Line2D candidateLine = candidate.getLine();

        if (GeometryTools2D.parallel(targetLine, candidateLine)) {
            return processParallelLines(target, candidate, grid);
        } else {
            return checkForIntersection(target, candidate, grid);
        }
    }

    // Returns the set of newly created edges if e1 or the longer edge was split, empty otherwise.
    private Set<Edge> processParallelLines(final Edge e1, final Edge e2, final SpatialGrid<Edge> grid) {
        final double e1Length  = e1.getLine().getDirection().getLength();
        final double e2Length  = e2.getLine().getDirection().getLength();
        final Edge shorterEdge = e1Length < e2Length ? e1 : e2;
        final Edge longerEdge  = e1Length < e2Length ? e2 : e1;

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

    // Returns the set of newly created edges of ether edge was split, empty otherwise.
    private Set<Edge> checkForIntersection(final Edge first, final Edge second, final SpatialGrid<Edge> grid) {
        Point2D intersection = resolveIntersectionPoint(first, second);
        if (intersection == null) return Collections.emptySet();

        final Node n = map.getNode(intersection);
        final boolean splitFirst = !n.equals(first.getStart()) && !n.equals(first.getEnd());
        final boolean splitSecond = !n.equals(second.getStart()) && !n.equals(second.getEnd());

        final Set<Edge> created = new HashSet<>();
        if (splitFirst) created.addAll(splitAndRegister(first, grid, n));
        if (splitSecond) created.addAll(splitAndRegister(second, grid, n));
        return created;
    }

    // Resolve the intersection point between two segments, falling back to the
    // infinite-line intersection when as endpoint lies near the other line.
    // Returns null if no valid intersection lies within both segments.
    private Point2D resolveIntersectionPoint(final Edge first, final Edge second) {
        final Line2D l1 = first.getLine();
        final Line2D l2 = second.getLine();
        final Point2D segmentIntersection = GeometryTools2D.getSegmentIntersectionPoint(l1, l2);
        if (segmentIntersection != null) return segmentIntersection;

        final Point2D lineIntersection = Objects.requireNonNull(GeometryTools2D.getIntersectionPoint(l1, l2));
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

    // Returns true if the lie near either endpoint of the edge.
    private boolean isNearEndpoint(final Edge edge, final Point2D point) {
        return map.isNear(point, edge.getStart().getPoint()) ||
               map.isNear(point, edge.getEnd().getPoint());
    }

    // Returns true if the point lies on the line segment, using a tolerance robust
    // enough for diagonal lines.
    private boolean containsRobust(final Line2D line, final Point2D point) {
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
    private Set<Edge> processInternalEdge(
            final Edge shorterEdge, final Edge longerEdge, final SpatialGrid<Edge> grid) {
        final double t1 = GeometryTools2D.positionOnLine(longerEdge.getLine(), shorterEdge.getLine().getOrigin());
        final double t2 = GeometryTools2D.positionOnLine(longerEdge.getLine(), shorterEdge.getLine().getEndPoint());

        final Node firstCutPoint  = (t1 < t2) ? shorterEdge.getStart() : shorterEdge.getEnd();
        final Node secondCutPoint = (t1 < t2) ? shorterEdge.getEnd()   : shorterEdge.getStart();

        // Check validity to prevent zero-length edges.
        final boolean isFirstValid =
                !firstCutPoint.equals(longerEdge.getStart())  && !firstCutPoint.equals(longerEdge.getEnd());
        final boolean isSecondValid =
                !secondCutPoint.equals(longerEdge.getStart()) && !secondCutPoint.equals(longerEdge.getEnd());

        if (isFirstValid && isSecondValid) {
            return splitAndRegister(longerEdge, grid, firstCutPoint, secondCutPoint);
        }
        if (isFirstValid) {
            return splitAndRegister(longerEdge, grid, firstCutPoint);
        }
        if (isSecondValid) {
            return splitAndRegister(longerEdge, grid, secondCutPoint);
        }
        return Collections.emptySet();
    }

    // Splits the longer edge at the non-shared node of the shorter edge.
    // Returns the newly created edges.
    private Set<Edge> processCoincidentNode(
            final Edge shorterEdge, final Edge longerEdge, final Node sharedNode, final SpatialGrid<Edge> grid) {
        final Node cutPoint = sharedNode.equals(shorterEdge.getStart()) ?
                shorterEdge.getEnd() : shorterEdge.getStart();

        if (cutPoint.equals(longerEdge.getStart()) || cutPoint.equals(longerEdge.getEnd())) {
            return Collections.emptySet();
        }
        return splitAndRegister(longerEdge, grid, cutPoint);
    }

    // Splits both edges at their overlap boundary.
    // Returns the newly created edges.
    private Set<Edge> processOverlap(
            final Edge shorterEdge, final Edge longerEdge, final SpatialGrid<Edge> grid) {
        final Node shorterCutPoint = GeometryTools2D.contains(
                shorterEdge.getLine(), longerEdge.getLine().getOrigin()) ?
                longerEdge.getStart() : longerEdge.getEnd();
        final Node longerCutPoint = GeometryTools2D.contains(
                longerEdge.getLine(), shorterEdge.getLine().getOrigin()) ?
                shorterEdge.getStart() : shorterEdge.getEnd();

        final Set<Edge> created = new HashSet<>();
        if (!shorterCutPoint.equals(shorterEdge.getStart()) && !shorterCutPoint.equals(shorterEdge.getEnd())) {
            created.addAll(splitAndRegister(shorterEdge, grid, shorterCutPoint));
        }
        if (!longerCutPoint.equals(longerEdge.getStart()) && !longerCutPoint.equals(longerEdge.getEnd())) {
            created.addAll(splitAndRegister(longerEdge, grid, longerCutPoint));
        }
        return created;
    }

    // Split an edge at the given nodes, update the grid, and return the new edges.
    private Set<Edge> splitAndRegister(
            final Edge edge, final SpatialGrid<Edge> grid, final Node... splitNodes) {
        grid.remove(edge);
        final List<Edge> created = map.splitEdge(edge, splitNodes);
        created.forEach(grid::add);

        createdEdges.remove(edge);
        createdEdges.addAll(created);
        Collections.addAll(createdNodes, splitNodes);
        intactEdges.remove(edge);
        splitCount += splitNodes.length;
        return new HashSet<>(created);
    }

    private void visualizeResult() {
        StepVisualizer.create(debug)
            .title("Split Intersecting Edges")
            .layer(PointLayer.of(createdNodes)
                             .name("Created Node")
                             .color(Color.GREEN))
            .layer(LineLayer.of(createdEdges)
                            .name("Created Edge")
                            .color(Color.GREEN))
            .layer(LineLayer.of(intactEdges)
                            .name("Intact Edge")
                            .color(Color.GRAY))
            .show();
    }

}
