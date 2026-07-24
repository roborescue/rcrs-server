package maps.convert.osm2gml.debug;

import rescuecore2.misc.geometry.Point2D;

import java.util.List;

/**
 * An object that can be represented as a polygon.
 */
public interface Polygonal {

    /**
     * Returns the vertices of this object as a closed polygon.
     *
     * @return the vertices
     */
    List<Point2D> getVertices();
}
