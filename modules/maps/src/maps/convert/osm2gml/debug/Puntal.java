package maps.convert.osm2gml.debug;

import rescuecore2.misc.geometry.Point2D;

/**
 * An object that can be represented as a point.
 */
public interface Puntal {

    /**
     * Get the point that represents this object.
     * @return The point.
     */
    Point2D getPoint();
}
