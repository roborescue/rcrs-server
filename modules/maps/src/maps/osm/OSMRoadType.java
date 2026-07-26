package maps.osm;

import lombok.Getter;

import java.util.Optional;

public enum OSMRoadType {

    MOTORWAY      ("motorway"      , 3.50, 1.50, 18.00),
    MOTORWAY_LINK ("motorway_link" , 3.50, 1.50,  6.00),
    TRUNK         ("trunk"         , 3.00, 1.00, 12.00),
    TRUNK_LINK    ("trunk_link"    , 3.00, 1.00,  6.00),
    PRIMARY       ("primary"       , 3.00, 0.75,  9.00),
    PRIMARY_LINK  ("primary_link"  , 3.00, 0.50,  5.00),
    SECONDARY     ("secondary"     , 2.75, 0.50,  7.00),
    SECONDARY_LINK("secondary_link", 2.75, 0.50,  5.00),
    TERTIARY      ("tertiary"      , 2.75, 0.50,  5.50),
    UNCLASSIFIED  ("unclassified"  , 2.50, 0.50,  4.50),
    ROAD          ("road"          , 2.50, 0.50,  4.50),
    RESIDENTIAL   ("residential"   , 2.50, 0.50,  4.50),
    LIVING_STREET ("living_street" , 2.50, 0.25,  3.50),
    SERVICE       ("service"       , 2.50, 0.25,  3.50),
    TRACK         ("track"         , 2.50, 0.25,  2.75),
    SERVICES      ("services"      , 2.50, 0.25,  7.00),
    PEDESTRIAN    ("pedestrian"    , 2.50, 0.25,  3.00);

    @Getter private final String tagValue;
    @Getter private final double laneWidth;
    @Getter private final double shoulderWidth;
    @Getter private final double defaultWidth;

    OSMRoadType(final String tagValue, final double laneWidth, final double shoulderWidth, final double defaultWidth) {
        this.tagValue = tagValue;
        this.laneWidth = laneWidth;
        this.shoulderWidth = shoulderWidth;
        this.defaultWidth = defaultWidth;
    }

    public static Optional<OSMRoadType> fromTagValue(final String tagValue) {
        for (final OSMRoadType type : values()) {
            if (type.tagValue.equals(tagValue)) return Optional.of(type);
        }
        return Optional.empty();
    }

}
