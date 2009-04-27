package rescuecore2.worldmodel;

/**
   A type-safe ID class for entity types. Entity types are really just integers.
 */
public final class EntityType {
    private final int id;
    private final String name;

    /**
       Construct a new EntityType object.
       @param id The numeric ID to use.
       @param name The name of this entity type.
     */
    public EntityType(int id, String name) {
	this.id = id;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
	if (o instanceof EntityType) {
	    return this.id == ((EntityType)o).id;
	}
	return false;
    }

    @Override
    public int hashCode() {
	return id;
    }

    /**
       Get the numeric ID for this entity type.
       @return The numeric ID.
     */
    public int getID() {
	return id;
    }

    /**
       Get the name of this entity type.
       @return The name of this entity type.
     */
    public String getName() {
        return name;
    }
}