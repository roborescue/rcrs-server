package maps.osm;

import java.util.List;

/**
   A building in OSM space.
*/
public class OSMBuilding extends OSMWay {

    // The value of the "building" tag that explicitly denotes a non-building
    private static final String NON_BUILDING_VALUE = "no";

    /**
       Construct an OSMBuilding.
       @param id The ID of the building.
       @param ids The IDs of the apex nodes of the building.
    */
    public OSMBuilding(Long id, List<Long> ids) {
        super(id, ids);
    }

    /**
     * Returns {@code true} if the specified value of the {@code building}
     * tag denotes a building. Every non-blank value is accepted except
     * {@code "no"}, which is compared ignoring case.
     *
     * @param tagValue the value of the {@code building} tag, may be {@code null}
     * @return {@code true} if the value denotes a building
     */
    public static boolean isBuildingTagValue(String tagValue) {
        // An absent or blank tag does not denote a building
        if (tagValue == null || tagValue.isBlank()) return false;

        return !NON_BUILDING_VALUE.equalsIgnoreCase(tagValue.trim());
    }

    @Override
    public String toString() {
        return "OSMBuilding: id " + getId();
    }
}
