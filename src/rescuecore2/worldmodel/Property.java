package rescuecore2.worldmodel;

/**
   Interface for the properties that make up an entity.
 */
public interface Property {
    /**
       Get the name of this property. The name is a unique identifier.
       @return The name of this property.
     */
    public String getName();

    /**
       Does this property have a defined value?
       @return True if a value has been set for this property, false otherwise.
     */
    public boolean isDefined();
}