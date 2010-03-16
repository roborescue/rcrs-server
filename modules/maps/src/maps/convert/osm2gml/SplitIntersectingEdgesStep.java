package maps.convert.osm2gml;

import java.util.List;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.Set;
import java.util.HashSet;

import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.GeometryTools2D;
//import rescuecore2.log.Logger;

import maps.convert.ConvertStep;

/**
   This step splits any edges that intersect.
*/
public class SplitIntersectingEdgesStep extends ConvertStep {
    private TemporaryMap map;
    private int splitCount;
    private int inspectedCount;
    private Deque<Edge> toCheck;
    private Set<Edge> seen;

    /**
       Construct a SplitIntersectingEdgesStep.
       @param map The TemporaryMap to use.
    */
    public SplitIntersectingEdgesStep(TemporaryMap map) {
        this.map = map;
    }

    @Override
    public String getDescription() {
        return "Splitting intersecting edges";
    }

    @Override
    protected void step() {
        debug.setBackground(ConvertTools.getAllDebugShapes(map));
        toCheck = new ArrayDeque<Edge>(map.getAllEdges());
        seen = new HashSet<Edge>();
        setProgressLimit(toCheck.size());
        splitCount = 0;
        inspectedCount = 0;
        while (!toCheck.isEmpty()) {
            Edge next = toCheck.pop();
            check(next);
            ++inspectedCount;
            setProgressLimit(toCheck.size() + inspectedCount);
            bumpProgress();
        }
        setStatus("Inspected " + inspectedCount + " edges and split " + splitCount);
    }

    private void check(Edge e) {
        if (!map.getAllEdges().contains(e)) {
            //            Logger.debug("Skipped edge " + e);
            //            debug.show("Skipped edge", new EdgeShapeInfo(e, "Skipped edge", Color.BLUE, true, false));
            return;
        }
        if (seen.contains(e)) {
            return;
        }
        seen.add(e);
        Line2D l1 = e.getLine();
        Set<Edge> edges = new HashSet<Edge>(map.getAllEdges());
        for (Edge test : edges) {
            if (test.equals(e)) {
                continue;
            }
            Line2D l2 = test.getLine();
            if (GeometryTools2D.parallel(l1, l2)) {
                if (processParallelLines(e, test)) {
                    break;
                }
            }
            else {
                if (checkForIntersection(e, test)) {
                    break;
                }
            }
        }
    }

    /**
       @return True if e1 was split.
    */
    private boolean processParallelLines(Edge e1, Edge e2) {
        // Possible cases:
        // Shorter line entirely inside longer
        // Shorter line overlaps longer at longer start
        // Shorter line overlaps longer at longer end
        // Shorter line start point is same as longer start and end point is inside
        // Shorter line start point is same as longer end and end point is inside
        // Shorter line end point is same as longer start and start point is inside
        // Shorter line end point is same as longer end and start point is inside
        Edge shorterEdge = e1;
        Edge longerEdge = e2;
        if (e1.getLine().getDirection().getLength() > e2.getLine().getDirection().getLength()) {
            shorterEdge = e2;
            longerEdge = e1;
        }
        Line2D shorter = shorterEdge.getLine();
        Line2D longer = longerEdge.getLine();
        boolean shortStartLongStart = shorterEdge.getStart() == longerEdge.getStart();
        boolean shortStartLongEnd = shorterEdge.getStart() == longerEdge.getEnd();
        boolean shortEndLongStart = shorterEdge.getEnd() == longerEdge.getStart();
        boolean shortEndLongEnd = shorterEdge.getEnd() == longerEdge.getEnd();
        boolean startInside = !shortStartLongStart && !shortStartLongEnd && GeometryTools2D.contains(longer, shorter.getOrigin());
        boolean endInside = !shortEndLongStart && !shortEndLongEnd && GeometryTools2D.contains(longer, shorter.getEndPoint());
        /*
          if (startInside || endInside) {
          ++overlapCount;
          }
        */
        if (startInside && endInside) {
            processInternalEdge(shorterEdge, longerEdge);
            return longerEdge == e1;
        }
        else if (startInside && !endInside) {
            // Either full overlap or coincident end point
            if (shortEndLongStart) {
                processCoincidentNode(shorterEdge, longerEdge, shorterEdge.getEnd());
                return longerEdge == e1;
            }
            else if (shortEndLongEnd) {
                processCoincidentNode(shorterEdge, longerEdge, shorterEdge.getEnd());
                return longerEdge == e1;
            }
            else {
                // Full overlap
                processOverlap(shorterEdge, longerEdge);
                return true;
            }
        }
        else if (endInside && !startInside) {
            // Either full overlap or coincident end point
            if (shortStartLongStart) {
                processCoincidentNode(shorterEdge, longerEdge, shorterEdge.getStart());
                return longerEdge == e1;
            }
            else if (shortStartLongEnd) {
                processCoincidentNode(shorterEdge, longerEdge, shorterEdge.getStart());
                return longerEdge == e1;
            }
            else {
                // Full overlap
                processOverlap(shorterEdge, longerEdge);
                return true;
            }
        }
        return false;
    }

    /**
       @return true if first is split.
    */
    private boolean checkForIntersection(Edge first, Edge second) {
        Point2D intersection = GeometryTools2D.getSegmentIntersectionPoint(first.getLine(), second.getLine());
        //                    System.out.println(intersection);
        if (intersection == null) {
            // Maybe the intersection is within the map's "nearby" tolerance?
            intersection = GeometryTools2D.getIntersectionPoint(first.getLine(), second.getLine());
            /*
              debug.show("Split intersection",
              new ShapeDebugFrame.Line2DShapeInfo(first, "Line 1", Color.ORANGE, true, false),
              new ShapeDebugFrame.Line2DShapeInfo(second, "Line 2", Color.BLUE, true, false),
              new ShapeDebugFrame.Point2DShapeInfo(intersection, "Near-miss intersection", Color.BLACK, false),
              new ShapeDebugFrame.Line2DShapeInfo(firstObject.getLines(), "Object 1", Color.GREEN, false, false),
              new ShapeDebugFrame.Line2DShapeInfo(secondObject.getLines(), "Object 2", Color.GRAY, false, false)
              );
            */
            // Was this a near miss?
            if (map.isNear(intersection, first.getStart().getCoordinates()) || map.isNear(intersection, first.getEnd().getCoordinates())) {
                // Check that the intersection is actually somewhere on the second segment
                double d = second.getLine().getIntersection(first.getLine());
                if (d < 0 || d > 1) {
                    // Nope. Ignore it.
                    return false;
                }
            }
            else if (map.isNear(intersection, second.getStart().getCoordinates()) || map.isNear(intersection, second.getEnd().getCoordinates())) {
                // Check that the intersection is actually somewhere on the first line segment
                double d = first.getLine().getIntersection(second.getLine());
                if (d < 0 || d > 1) {
                    // Nope. Ignore it.
                    return false;
                }
            }
            else {
                // Not a near miss.
                return false;
            }
        }
        Node n = map.getNode(intersection);
        // Split the two edges into 4 (maybe)
        // Was the first edge split?
        boolean splitFirst = !n.equals(first.getStart()) && !n.equals(first.getEnd());
        boolean splitSecond = !n.equals(second.getStart()) && !n.equals(second.getEnd());
        Set<Edge> newEdges = new HashSet<Edge>();
        if (splitFirst) {
            List<Edge> e = map.splitEdge(first, n);
            toCheck.addAll(e);
            newEdges.addAll(e);
            ++splitCount;
            /*
            Logger.debug("Split edge " + first);
            debug.show("Split first line",
                       new ShapeDebugFrame.Line2DShapeInfo(first.getLine(), "Line 1", Color.ORANGE, true, false),
                       new EdgeShapeInfo(e, "New edges", Color.WHITE, false, true),
                       new ShapeDebugFrame.Point2DShapeInfo(intersection, "Intersection", Color.BLACK, true)
                       );
            */
        }
        if (splitSecond) {
            List<Edge> e = map.splitEdge(second, n);
            toCheck.addAll(e);
            newEdges.addAll(e);
            ++splitCount;
            /*
            Logger.debug("Split edge " + second);
            debug.show("Split second line",
                       new ShapeDebugFrame.Line2DShapeInfo(second.getLine(), "Line 2", Color.BLUE, true, false),
                       new EdgeShapeInfo(e, "New edges", Color.WHITE, false, true),
                       new ShapeDebugFrame.Point2DShapeInfo(intersection, "Intersection", Color.BLACK, true)
                       );
            */
        }
        //        if (splitFirst || splitSecond) {
            /*
            Logger.debug("First line: " + first + " -> " + first.getLine());
            Logger.debug("Second line: " + second + " -> " + second.getLine());
            Logger.debug("Intersection: " + intersection);
            Logger.debug("New edges");
            for (Edge next : newEdges) {
                Logger.debug("  " + next + " -> " + next.getLine());
            }
            */
            //            debug.show("Split intersection",
            //                       new ShapeDebugFrame.Line2DShapeInfo(first.getLine(), "Line 1", Color.ORANGE, true, false),
            //                       new ShapeDebugFrame.Line2DShapeInfo(second.getLine(), "Line 2", Color.BLUE, true, false),
            //                       new EdgeShapeInfo(newEdges, "New edges", Color.WHITE, false, true),
            //                       new ShapeDebugFrame.Point2DShapeInfo(intersection, "Intersection", Color.BLACK, true)
            //                       );
        //        }
        return splitFirst;
    }

    private void processInternalEdge(Edge shorter, Edge longer) {
        // Split longer into (up to) three chunks
        double t1 = GeometryTools2D.positionOnLine(longer.getLine(), shorter.getLine().getOrigin());
        double t2 = GeometryTools2D.positionOnLine(longer.getLine(), shorter.getLine().getEndPoint());
        Node first;
        Node second;
        if (t1 < t2) {
            first = shorter.getStart();
            second = shorter.getEnd();
        }
        else {
            first = shorter.getEnd();
            second = shorter.getStart();
        }
        toCheck.addAll(map.splitEdge(longer, first, second));
        ++splitCount;
    }

    private void processCoincidentNode(Edge shorter, Edge longer, Node coincidentPoint) {
        // Split the long edge at the non-coincident point
        Node cutPoint = coincidentPoint.equals(shorter.getStart()) ? shorter.getEnd() : shorter.getStart();
        toCheck.addAll(map.splitEdge(longer, cutPoint));
        ++splitCount;
    }

    private void processOverlap(Edge shorter, Edge longer) {
        Node shortSplit = GeometryTools2D.contains(shorter.getLine(), longer.getLine().getOrigin()) ? longer.getStart() : longer.getEnd();
        Node longSplit = GeometryTools2D.contains(longer.getLine(), shorter.getLine().getOrigin()) ? shorter.getStart() : shorter.getEnd();
        toCheck.addAll(map.splitEdge(shorter, shortSplit));
        toCheck.addAll(map.splitEdge(longer, longSplit));
        ++splitCount;
    }
}
