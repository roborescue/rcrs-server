package maps.gml;

import java.util.List;

/**
   A road in GML space.
*/
public class GMLRoad extends GMLShape {
    /**
       Construct a GMLRoad.
       @param id The ID of the road.
    */
    public GMLRoad(int id) {
        super(id);
    }

    /**
       Construct a GMLRoad.
       @param id The ID of the road.
       @param edges The edges of the road.
    */
    public GMLRoad(int id, List<GMLDirectedEdge> edges) {
        super(id, edges);
    }

    /**
       Construct a GMLRoad.
       @param id The ID of the road.
       @param edges The edges of the road.
       @param neighbours The neighbours of each edge.
    */
    public GMLRoad(int id, List<GMLDirectedEdge> edges, List<Integer> neighbours) {
        super(id, edges, neighbours);
    }

    @Override
    public String toString() {
        return "GMLRoad " + getID();
    }
}
