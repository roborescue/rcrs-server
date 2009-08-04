package rescuecore2.worldmodel;

/**
   Entity types must have a name and an ID number.
 */
public interface EntityType {
    /**
       Get the numeric ID for this entity type.
       @return The numeric ID.
     */
    int getID();

    /**
       Get the name of this entity type.
       @return The name of this entity type.
     */
    String getName();
}