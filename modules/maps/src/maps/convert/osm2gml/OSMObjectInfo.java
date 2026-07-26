package maps.convert.osm2gml;

import java.awt.geom.Area;
import java.util.List;

import maps.convert.osm2gml.debug.Polygonal;

/**
 * Information about an OSM object.
 */
public interface OSMObjectInfo extends Polygonal {

    /**
     * Returns the area of this object.
     *
     * @return the area of this object
     */
    Area getArea();

    /**
     * Creates a temporary object with the specified edges.
     *
     * @param edges the edges
     * @return the temporary object
     */
    TemporaryObject createTemporaryObject(List<DirectedEdge> edges);
}
