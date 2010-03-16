package maps.convert.osm2gml;

import java.util.List;

/**
   A temporary road during conversion.
*/
public class TemporaryRoad extends TemporaryObject {
    /**
       Construct a new TemporaryRoad.
       @param edges The edges of the road in counter-clockwise order.
    */
    public TemporaryRoad(List<DirectedEdge> edges) {
        super(edges);
    }
}
