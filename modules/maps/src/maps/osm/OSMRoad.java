package maps.osm;

import lombok.Getter;

import java.util.List;

/**
 * An OpenStreetMap road.
 */
public class OSMRoad extends OSMWay {

    @Getter private final OSMRoadType type;
    @Getter private final int laneCount;

    /**
     * Construct an {@code OSMRoad}.
     * @param id        The ID of the road.
     * @param ids       The IDs of the apex nodes of the road.
     * @param type      The type of the road.
     * @param laneCount The number of lanes of the road.
     */
    public OSMRoad(final Long id, final List<Long> ids, final OSMRoadType type, final int laneCount) {
        super(id, ids);
        this.type      = type;
        this.laneCount = laneCount;
    }

    /**
     * Construct a copy of an {@code OSMRoad}.
     * @param other The {@code OSMRoad} to copy.
     */
    public OSMRoad(final OSMRoad other) {
        super(other.getId(), other.getNodeIDs());
        this.type  = other.type;
        this.laneCount = other.laneCount;
    }

}
