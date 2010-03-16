package maps.convert.osm2gml;

import java.util.List;

/**
   A temporary intersection during conversion.
*/
public class TemporaryIntersection extends TemporaryObject {
    /**
       Construct a new TemporaryIntersection.
       @param edges The edges of the intersection in counter-clockwise order.
    */
    public TemporaryIntersection(List<DirectedEdge> edges) {
        super(edges);
    }
}
