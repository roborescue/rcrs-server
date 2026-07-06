package maps.convert.osm2gml;

import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Point2D;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This step modify the map so that all shapes are traversable from their centroid.
 */
public class EnsureTraversabilityStep extends BaseModificationStep {
    private final double threshold;

    /**
     * Constructs a new {@link EnsureTraversabilityStep}.
     *
     * @param map The {@link TemporaryMap} to be modified.
     */
    public EnsureTraversabilityStep(final TemporaryMap map) {
        super(map);
        threshold = ConvertTools.sizeOfMeters(map.getOSMMap(), 1);
    }

    @Override
    public String getDescription() {
        return "Ensure shapes are traversable";
    }

    @Override
    protected void step() {
        final Collection<TemporaryObject> allObjects = map.getAllObjects();

        final List<TemporaryObject> shapesToRemove = new ArrayList<>();
        final List<TemporaryObject> shapesToAdd    = new ArrayList<>();

        setProgressLimit(allObjects.size());

        for (final TemporaryObject object : allObjects) {
            final List<Point2D> vertices = object.getVertices();
            final List<Line2D> impassableLines = getImpassableLines(object);

            // Check if the original polygon is already fully traversable.
            if (isTraversable(vertices, impassableLines)) {
                bumpProgress();
                continue;
            }

            // Decompose the non-traversable polygon into a set of convex triangles.
            final List<List<Point2D>> pieces = PolygonTriangular.triangulate(vertices);

            boolean mergedAnything = true;
            while (mergedAnything && 1 < pieces.size()) {
                mergedAnything = false;

                // Iteratively test all pairs pieces to find mergeable adjacent polygons.
                for (int i = 0; i < pieces.size(); i++) {
                    for (int j = i + 1; j < pieces.size(); j++) {
                        final List<Point2D> p1 = pieces.get(i);
                        final List<Point2D> p2 = pieces.get(j);

                        // Attempt to merge the two pieces if they share exactly one edge.
                        final List<Point2D> merged = tryMerge(p1, p2);
                        if (merged == null) continue;

                        // Keep the merged shape and discard the original two if the new shape is traversable.
                        if (isTraversable(merged, impassableLines)) {
                            pieces.remove(j);
                            pieces.remove(i);
                            pieces.add(merged);
                            mergedAnything = true;
                            break;
                        }
                    }
                    if (mergedAnything) break;
                }
            }

            // Replace the original object with the newly formed traversable sub-shapes.
            if (1 < pieces.size()) {
                shapesToRemove.add(object);
                for (final List<Point2D> pieceVertices : pieces) {
                    final TemporaryObject newObject = createTemporaryObjectFromVertices(object, pieceVertices);
                    shapesToAdd.add(newObject);
                }
            }
            bumpProgress();
        }

        shapesToRemove.forEach(map::removeTemporaryObject);
        shapesToAdd.forEach(map::addTemporaryObject);
        map.resynchronizeStateFromObjects();

        setStatus("Split " + shapesToRemove.size() + " objects into " + shapesToAdd.size() + " traversable sub-shapes");
        visualizeDifference(shapesToRemove, shapesToAdd, "Ensure Traversability (Split Polygons)");
    }

    // Extracts the boundary lines that are shared with other objects and cannot be crossed.
    private List<Line2D> getImpassableLines(final TemporaryObject object) {
        return object.getEdges().stream()
                .filter(edge -> 1 < map.getAttachedObjects(edge.getEdge()).size())
                .map(DirectedEdge::getLine)
                .toList();
    }

    // Checks if a polygon is traversable from its centroid to its passable edges.
    private boolean isTraversable(final List<Point2D> vertices, final List<Line2D> impassableLines) {

        // Triangles are always convex and thus fully traversable.
        if (vertices.size() < 4) return true;

        // Check the traversal from the centroid to each edge.
        final Point2D centroid = GeometryTools2D.computeCentroid(vertices);
        for (int i = 0; i < vertices.size(); i++) {
            final Point2D p1 = vertices.get(i);
            final Point2D p2 = vertices.get((i + 1) % vertices.size());
            final Line2D line = new Line2D(p1, p2);

            // Skip edges that are already defines as impassable walls.
            if (impassableLines.contains(line)) continue;

            final Point2D edgeCenter = line.getMidpoint();
            final Line2D traversalLine =  new Line2D(centroid, edgeCenter);

            // The shape is not traversable if the path crosses an impassable line.
            if (intersectsAny(traversalLine, impassableLines)) return false;
        }

        return true;
    }

    // Attempts to merge two polygons into one if they share exactly one oppositely directed edge.
    private List<Point2D> tryMerge(final List<Point2D> p1, final List<Point2D> p2) {
        for (int i = 0; i < p1.size(); i++) {
            final Point2D a1 = p1.get(i);
            final Point2D b1 = p1.get((i + 1) % p1.size());

            for (int j = 0; j < p2.size(); j++) {
                final Point2D a2 = p2.get(j);
                final Point2D b2 = p2.get((j + 1) % p2.size());

                // Check if the edges are identical but in opposite directions.
                if (a1.equals(b2) && b1.equals(a2)) {
                    final List<Point2D> merged = new ArrayList<>();

                    for (int k = 0; k < p1.size(); k++) {
                        final Point2D p = p1.get((i + 1 + k) % p1.size());
                        merged.add(p);

                        if (p.equals(a1)) {
                            for (int l = 1; l < p2.size() - 1; l++) {
                                merged.add(p2.get((j + 1 + l) % p2.size()));
                            }
                        }
                    }
                    return merged;
                }
            }
        }
        return null;
    }

    // Reconstructs a TemporaryObjects from a point list by reusing existing nodes or
    // creating new internal edges.
    private TemporaryObject createTemporaryObjectFromVertices(
            final TemporaryObject original, final List<Point2D> vertices) {
        if (vertices.size() < 3) return null;

        final List<Node> pieceNodes = vertices.stream().map(map::getNode).toList();
        final List<DirectedEdge> pieceEdges = new ArrayList<>();
        for (int i = 0; i < vertices.size(); i++) {
            final Node start = pieceNodes.get(i);
            final Node end   = pieceNodes.get((i + 1) % pieceNodes.size());
            final DirectedEdge edge = map.getDirectedEdge(start, end);
            pieceEdges.add(edge);
        }

        return switch (original) {
            case TemporaryRoad         ignored  -> new TemporaryRoad(pieceEdges);
            case TemporaryIntersection ignored  -> new TemporaryIntersection(pieceEdges);
            case TemporaryBuilding     building -> new TemporaryBuilding(pieceEdges, building.getOsmId());
            default -> throw new IllegalStateException("Unsupported TemporaryObject type: "
                    + original.getClass().getSimpleName());
        };
    }

    // Check if a given line intersects with any line in a collection.
    private boolean intersectsAny(final Line2D line, final Collection<Line2D> others) {
        return others.stream().anyMatch(other -> crosses(line, other));
    }

    // Check if two line segments properly cross each other (excluding touching at endpoints).
    private boolean crosses(final Line2D line1, final Line2D line2) {
        // Compute intersection parameters along each line
        final double intersection1 = line1.getIntersection(line2);
        final double intersection2 = line2.getIntersection(line1);

        // If lines are parallel, they do not have a valid intersection.
        if (Double.isNaN(intersection1) || Double.isNaN(intersection2)) return false;

        // Define a small threshold to avoid counting endpoints as crossings.
        final boolean isInternal1 = threshold < intersection1 && intersection1 < (1.0 - threshold);
        final boolean isInternal2 = threshold < intersection2 && intersection2 < (1.0 - threshold);

        return isInternal1 && isInternal2;
    }

}
