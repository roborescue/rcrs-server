package maps.convert.osm2gml;

import lombok.Getter;

import java.util.List;

/**
 * A temporary building during conversion.
 */
public class TemporaryBuilding extends TemporaryObject {
    @Getter
    private final long osmId;

    /**
     * Constructs a temporary building with the specified edges and OSM ID.
     * @param edges the edges
     * @param osmId the OSM ID
     */
    public TemporaryBuilding(List<DirectedEdge> edges, long osmId) {
        super(edges);
        this.osmId = osmId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TemporaryObject copyWithEdges(List<DirectedEdge> edges) {
        return new TemporaryBuilding(edges, osmId);
    }

    @Override
    public String toString() {
        return super.toString() + "(osmId=" + osmId + ")";
    }
}
