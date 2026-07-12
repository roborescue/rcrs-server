package maps.osm;

import maps.convert.osm2gml.debug.Puntal;
import rescuecore2.misc.geometry.Point2D;

/**
   An OpenStreetMap node.
 */
public class OSMNode extends OSMObject implements Puntal {
    private double lat;
    private double lon;

    /**
       Construct an OSMNode.
       @param id The ID of the node.
       @param lat The latitude of the node.
       @param lon The longitude of the node.
     */
    public OSMNode(long id, double lat, double lon) {
        super(id);
        this.lat = lat;
        this.lon = lon;
    }

    /**
       Get the latitude of this node in degrees.
       @return The latitude in degrees.
     */
    public double getLatitude() {
        return lat;
    }

    /**
       Get the longitude of this node in degrees.
       @return The longitude in degrees.
     */
    public double getLongitude() {
        return lon;
    }

    @Override
    public String toString() {
        return "OSMNode (" + getID() + ") at lat " + lat + ", lon " + lon;
    }

    @Override
    public Point2D getPoint() {
        return new Point2D(lon, lat);
    }
}
