package maps.convert.osm2gml;

import java.util.List;

/**
 * A temporary intersection during conversion.
 */
public class TemporaryIntersection extends TemporaryObject {

    /**
     * Constructs a temporary intersection with the specified edges.
     *
     * @param edges the edges
     */
    public TemporaryIntersection(List<DirectedEdge> edges) {
        super(edges);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TemporaryObject copyWithEdges(List<DirectedEdge> edges) {
        return new TemporaryIntersection(edges);
    }
}
