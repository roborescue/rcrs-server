package maps.osm;

/**
   Abstract base class for OpenStreetMap objects.
*/
public abstract class OSMObject {
    private long id;

    /**
       Construct an OSMObject.
       @param id The ID of the object.
    */
    public OSMObject(long id) {
        this.id = id;
    }

    /**
       Get the ID of this object.
       @return The ID of the object.
    */
    public long getID() {
        return id;
    }
}
