package maps.gml;

import java.util.List;

/**
   An open space in GML space.
*/
public class GMLSpace extends GMLShape {
    /**
       Construct a GMLSpace.
       @param id The ID of the space.
    */
    public GMLSpace(int id) {
        super(id);
    }

    /**
       Construct a GMLSpace.
       @param id The ID of the space.
       @param edges The edges of the space.
    */
    public GMLSpace(int id, List<GMLDirectedEdge> edges) {
        super(id, edges);
    }

    /**
       Construct a GMLSpace.
       @param id The ID of the space.
       @param edges The edges of the space.
       @param neighbours The neighbours of each edge.
    */
    public GMLSpace(int id, List<GMLDirectedEdge> edges, List<Integer> neighbours) {
        super(id, edges, neighbours);
    }
}
