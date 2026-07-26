package maps.convert.osm2gml;

import java.awt.geom.Rectangle2D;

public interface SpatialIndexable {

    /**
     * Returns the bounds of this object.
     *
     * @return the bounds
     */
    Rectangle2D getBounds();
}
