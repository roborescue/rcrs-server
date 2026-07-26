package maps.osm;

import lombok.Getter;

/**
 * Abstract base class for OpenStreetMap objects.
 */
public abstract class OSMObject {
    @Getter private final long id;

    /**
     * Construct an OSMObject.
     * @param id The ID of the object.
     */
    public OSMObject(long id) {
        this.id = id;
    }
}
