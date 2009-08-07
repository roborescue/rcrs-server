package rescuecore2.worldmodel;

/**
   Basic EntityType implementation.
 */
public class SimpleEntityType implements EntityType {
    private int id;
    private String name;

    /**
       Construct a SimpleEntityType with an ID and a name.
       @param id The ID of this entity type.
       @param name The name of this entity type.
     */
    public SimpleEntityType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SimpleEntityType) {
            return ((SimpleEntityType)o).id == this.id;
        }
        return false;
    }
}