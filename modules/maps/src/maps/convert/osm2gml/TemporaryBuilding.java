package maps.convert.osm2gml;

import lombok.Getter;

import java.util.List;

/**
   A temporary building during conversion.
*/
public class TemporaryBuilding extends TemporaryObject {
    @Getter
    private final long osmId;

    /**
       Construct a new TemporaryBuilding.
       @param edges The edges of the building in counter-clockwise order.
       @param osmId The ID of the OSM building that generated this data.
    */
    public TemporaryBuilding(List<DirectedEdge> edges, long osmId) {
        super(edges);
        this.osmId = osmId;
    }

    @Override
    public String toString() {
        return super.toString() + "(osmId=" + osmId + ")";
    }
}
