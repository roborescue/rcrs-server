package rescuecore2.worldmodel;

/**
   Basic PropertyType implementation.
 */
public class SimplePropertyType implements PropertyType {
    private int id;
    private String name;

    /**
       Construct a SimplePropertyType with an ID and a name.
       @param id The ID of this property type.
       @param name The name of this property type.
     */
    public SimplePropertyType(int id, String name) {
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
        if (o instanceof SimplePropertyType) {
            return ((SimplePropertyType)o).id == this.id;
        }
        return false;
    }
}