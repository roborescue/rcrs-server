package maps.convert.osm2gml;

import java.awt.geom.Area;

import maps.convert.osm2gml.debug.Polygonal;

/**
 * Interface for OSM object shapes.
 */
public interface OSMShape extends Polygonal {

    /**
     * Get the area covered by this shape.
     * @return The area of this shape.
     */
    Area getArea();
}
