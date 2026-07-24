package maps.convert.osm2gml;

import java.util.List;

/**
 * A temporary road during conversion.
 */
public class TemporaryRoad extends TemporaryObject {

    /**
     * Constructs a temporary road with the specified edges.
     *
     * @param edges the edges
     */
    public TemporaryRoad(List<DirectedEdge> edges) {
        super(edges);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TemporaryObject copyWithEdges(List<DirectedEdge> edges) {
        return new TemporaryRoad(edges);
    }
}
