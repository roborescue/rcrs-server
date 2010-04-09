package maps.convert.osm2gml.buildings.row;

import java.util.Set;

import maps.gml.GMLShape;
import maps.gml.GMLDirectedEdge;
import maps.gml.GMLMap;

/**
   Interface for a building generator that works by row.
*/
public interface RowFiller {
    /**
       Generate buildings along an edge.
       @param edge The edge to populate.
       @param map The map.
       @return The set of new faces.
    */
    Set<GMLShape> fillRow(GMLDirectedEdge edge, GMLMap map);
}
