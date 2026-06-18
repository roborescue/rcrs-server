package maps.convert.osm2gml;

import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Vector2D;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility class for decomposing a simple polygon into triangles using the Ear Clipping algorithm.
 */
public class PolygonTriangular {

    private PolygonTriangular() {
        throw new IllegalCallerException("This is a utility class and cannot be instantiated");
    }

    /**
     * Decompose a given polygon into a set of triangles.
     *
     * @param originalVertices The vertices of the polygon to triangulate.
     *                         The first and last vertices may be identical.
     * @return A list of triangles, where each triangle is a list of exactly 3 {@link Point2D} objects.
     */
    public static List<List<Point2D>> triangulate(final List<Point2D> originalVertices) {
        final List<List<Point2D>> triangles = new ArrayList<>();

        // Return an empty list if the polygon has fewer than 3 vertices.
        if (originalVertices == null || originalVertices.size() < 3) return triangles;

        // Copy the vertices to avoid modifying the original list,
        // and remove the closing duplicate vertex if present.
        final List<Point2D> vertices = new ArrayList<>(originalVertices);
        if (vertices.getFirst().equals(vertices.getLast())) vertices.removeLast();

        // If the polygon is already a single triangle, return it directly.
        if (vertices.size() == 3) {
            triangles.add(vertices);
            return triangles;
        }

        final boolean isCCW = GeometryTools2D.isCounterClockwise(vertices);
        while (3 < vertices.size()) {
            boolean isEarFound = false;
            final int n = vertices.size();
            for (int i = 0; i < n; i++) {
                final Point2D pPrev = vertices.get((i - 1 + n) % n);
                final Point2D pCurr = vertices.get(i);
                final Point2D pNext = vertices.get((i + 1) % n);

                // If an ear is found, clip it and restart the search.
                if (isEar(pPrev, pCurr, pNext, vertices, isCCW)) {
                    triangles.add(List.of(pPrev, pCurr, pNext));
                    vertices.remove(i);
                    isEarFound = true;
                    break;
                }
            }

            if (!isEarFound) break;
        }

        // Add the remaining 3 vertices as the final triangle.
        if (vertices.size() == 3) triangles.add(vertices);

        return triangles;
    }

    // Determines whether three consecutive vertices from an "ear" of the polygon.
    private static boolean isEar(
            final Point2D p1, final Point2D p2, final Point2D p3, List<Point2D> polygon, boolean isCCW) {

        final Vector2D v1    = p2.minus(p1);
        final Vector2D v2    = p3.minus(p2);
        final double cross   = v1.cross(v2);
        if (isCCW && cross <= 0) {
            return false;
        }
        if (!isCCW && 0 <= cross) {
            return false;
        }

        for (final Point2D pt : polygon) {
            if (pt.equals(p1) || pt.equals(p2) || pt.equals(p3)) continue;
            if (GeometryTools2D.isPointInTriangle(pt, p1, p2, p3)) {
                return false;
            }
        }

        return true;
    }

}
