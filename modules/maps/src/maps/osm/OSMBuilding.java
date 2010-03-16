package maps.osm;

import java.util.List;

/**
   A building in OSM space.
*/
public class OSMBuilding extends OSMWay {
    /**
       Construct an OSMBuilding.
       @param id The ID of the building.
       @param ids The IDs of the apex nodes of the building.
    */
    public OSMBuilding(Long id, List<Long> ids) {
        super(id, ids);
    }

    @Override
    public String toString() {
        return "OSMBuilding: id " + getID();
    }
}
