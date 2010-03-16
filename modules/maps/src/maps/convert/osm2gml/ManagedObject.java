package maps.convert.osm2gml;

/**
   A managed map object.
*/
public abstract class ManagedObject {
    private long id;

    /**
       Construct a managed object.
       @param id The id of the object.
     */
    protected ManagedObject(long id) {
        this.id = id;
    }

    /**
       Get this object's ID.
       @return The object ID.
    */
    public long getID() {
        return id;
    }

    @Override
    public int hashCode() {
        return (int)id;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ManagedObject) {
            return this.id == ((ManagedObject)o).id;
        }
        return false;
    }
}
