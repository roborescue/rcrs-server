package maps.convert.osm2gml;

import java.util.List;
import java.awt.geom.Area;

import rescuecore2.misc.geometry.Point2D;

/**
   Interface for OSM object shapes.
*/
public interface OSMShape {
    /**
       Get the vertices of this shape in clockwise order.
       @return The vertices of this shape.
    */
    List<Point2D> getVertices();

    /**
       Get the area covered by this shape.
       @return The area of this shape.
    */
    Area getArea();
}
