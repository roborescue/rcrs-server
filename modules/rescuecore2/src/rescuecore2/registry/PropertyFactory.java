package rescuecore2.registry;


import rescuecore2.worldmodel.Property;

/**
   A factory for vending Properties.
 */
public interface PropertyFactory extends Factory {
    /**
       Create a new Property.
       @param urn The urn of the property to create.
       @return A new Property of the correct type.
       @throws IllegalArgumentException If the urn is not recognised.
     */
    Property makeProperty(int urn);

}
