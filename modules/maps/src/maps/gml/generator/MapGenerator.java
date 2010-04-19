package maps.gml.generator;

import maps.gml.GMLMap;

/**
   Top-level interface for map generation strategies.
 */
public interface MapGenerator {
    /**
       Generate a map.
       @param map The map object to populate.
    */
    void populate(GMLMap map);
}