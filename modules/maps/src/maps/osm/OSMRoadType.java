package maps.osm;

import lombok.Getter;

import java.util.Optional;

public enum OSMRoadType {

    MOTORWAY      ("motorway"      , 18.00),
    MOTORWAY_LINK ("motorway_link" ,  6.00),
    TRUNK         ("trunk"         , 12.00),
    TRUNK_LINK    ("trunk_link"    ,  6.00),
    PRIMARY       ("primary"       ,  9.00),
    PRIMARY_LINK  ("primary_link"  ,  5.00),
    SECONDARY     ("secondary"     ,  7.00),
    SECONDARY_LINK("secondary_link",  5.00),
    TERTIARY      ("tertiary"      ,  5.50),
    UNCLASSIFIED  ("unclassified"  ,  4.50),
    ROAD          ("road"          ,  4.50),
    RESIDENTIAL   ("residential"   ,  4.50),
    LIVING_STREET ("living_street" ,  3.50),
    SERVICE       ("service"       ,  3.50),
    TRACK         ("track"         ,  2.75),
    SERVICES      ("services"      ,  7.00),
    PEDESTRIAN    ("pedestrian"    ,  3.00);

    @Getter private final String tagValue;
    @Getter private final double widthMeters;

    OSMRoadType(final String tagValue, final double widthMeters) {
        this.tagValue = tagValue;
        this.widthMeters = widthMeters;
    }

    public static Optional<OSMRoadType> fromTagValue(final String tagValue) {
        for (final OSMRoadType type : values()) {
            if (type.tagValue.equals(tagValue)) return Optional.of(type);
        }
        return Optional.empty();
    }

}
