package maps.gml;

import java.util.List;

/**
   A building in GML space.
*/
public class GMLBuilding extends GMLShape {
    /**
       Construct a GMLBuilding.
       @param id The ID of the building.
    */
    public GMLBuilding(int id) {
        super(id);
    }

    /**
       Construct a GMLBuilding.
       @param id The ID of the building.
       @param edges The edges of the building.
    */
    public GMLBuilding(int id, List<GMLDirectedEdge> edges) {
        super(id, edges);
    }

    /**
       Construct a GMLBuilding.
       @param id The ID of the building.
       @param edges The edges of the building.
       @param neighbours The neighbours of each edge.
    */
    public GMLBuilding(int id, List<GMLDirectedEdge> edges, List<Integer> neighbours) {
        super(id, edges, neighbours);
    }

    @Override
    public String toString() {
        return "GMLBuilding " + getID();
    }
}
