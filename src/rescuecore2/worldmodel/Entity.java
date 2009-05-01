package rescuecore2.worldmodel;

import java.util.Set;

/**
   Interface for all objects that live in a WorldModel. Entities are made up of a fixed set of properties. The values of those properties may change but the set of properties may not.
 */
public interface Entity {
    /**
       Get the ID of this Entity.
       @return The ID.
    */
    EntityID getID();

    /**
       Get all the properties that this entity has.
       @return A set of Properties. This will never be null, but may be empty.
     */
    Set<Property> getProperties();

    /**
       Get a property by name.
       @param name The name to look up.
       @return The property with the given name or null if no such property exists.
    */
    Property getProperty(String name);
}