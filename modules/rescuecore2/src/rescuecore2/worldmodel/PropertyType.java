package rescuecore2.worldmodel;

/**
   Property types must have a name and an ID number.
 */
public interface PropertyType {
    /**
       Get the numeric ID for this property type.
       @return The numeric ID.
     */
    int getID();

    /**
       Get the name of this property type.
       @return The name of this property type.
     */
    String getName();
}