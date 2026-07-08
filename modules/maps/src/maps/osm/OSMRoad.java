package maps.osm;

import lombok.Getter;

import java.util.List;

/**
   An OpenStreetMap road.
 */
public class OSMRoad extends OSMWay {

    @Getter private final OSMRoadType type;
    @Getter private final int lanes;

    /**
     * Construct an OSMRoad.
     * @param id    The ID of the road.
     * @param ids   The IDs of the apex nodes of the road.
     * @param type  The type of the road.
     * @param lanes The number of lanes of the road.
     */
    public OSMRoad(final Long id, final List<Long> ids, final OSMRoadType type, final int lanes) {
        super(id, ids);
        this.type  = type;
        this.lanes = lanes;
    }

}
