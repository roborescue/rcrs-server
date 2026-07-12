package maps.convert.osm2gml.debug;

import rescuecore2.misc.geometry.Point2D;

import java.util.List;

/**
 * An object that can be represented as a polygon.
 */
public interface Polygonal {

    /**
     * Get the vertices of this polygon.
     * @return The list of vertices.
     */
    List<Point2D> getVertices();
}
