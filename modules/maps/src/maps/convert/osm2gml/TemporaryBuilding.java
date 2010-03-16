package maps.convert.osm2gml;

import java.util.List;

/**
   A temporary building during conversion.
*/
public class TemporaryBuilding extends TemporaryObject {
    private long id;

    /**
       Construct a new TemporaryBuilding.
       @param edges The edges of the building in counter-clockwise order.
       @param id The ID of the OSM building that generated this data.
    */
    public TemporaryBuilding(List<DirectedEdge> edges, long id) {
        super(edges);
        this.id = id;
    }

    /**
       Get the ID of the original OSM building.
       @return The OSM building ID.
    */
    public long getBuildingID() {
        return id;
    }
}
