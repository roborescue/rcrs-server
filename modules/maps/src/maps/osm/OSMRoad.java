package maps.osm;

import java.util.List;

/**
   An OpenStreetMap road.
 */
public class OSMRoad extends OSMWay {
    /**
       Construct an OSMRoad.
       @param id The ID of the road.
       @param ids The IDs of the apex nodes of the road.
     */
    public OSMRoad(Long id, List<Long> ids) {
        super(id, ids);
    }
}
