package maps.convert.osm2gml.debug;

import rescuecore2.misc.geometry.Line2D;

/**
 * An object that can be represented as a line segment.
 */
public interface Lineal {

    /**
     * Get the line segment that represent object.
     * @return The line segment.
     */
    Line2D getLine();
}
