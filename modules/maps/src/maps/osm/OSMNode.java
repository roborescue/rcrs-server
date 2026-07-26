package maps.osm;

import lombok.Getter;
import maps.convert.osm2gml.debug.Puntal;
import rescuecore2.misc.geometry.Point2D;

/**
 * An OpenStreetMap node.
 */
public class OSMNode extends OSMObject implements Puntal {
    @Getter private final Point2D point;

    /**
     * Construct an OSMNode.
     * @param id        The ID of the node.
     * @param latitude  The latitude of the node.
     * @param longitude The longitude of the node.
     */
    public OSMNode(final long id, final double latitude, final double longitude) {
        super(id);
        this.point = new Point2D(longitude, latitude);
    }

    /**
     * Get the longitude of this node in degrees.
     * @return The longitude in degrees.
     */
    public double getLongitude() {
        return point.getX();
    }

    /**
     * Get the latitude of this node in degrees.
     * @return The latitude in degrees.
     */
    public double getLatitude() {
        return point.getY();
    }

    @Override
    public String toString() {
        return "OSMNode#" + getId() + " (point=" + point + ")";
    }
}
