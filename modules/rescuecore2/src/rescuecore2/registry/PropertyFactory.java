package rescuecore2.registry;

import rescuecore2.worldmodel.Property;

/**
   A factory for vending Properties.
 */
public interface PropertyFactory {
    /**
       Create a new Property.
       @param urn The urn of the property to create.
       @return A new Property of the correct type.
       @throws IllegalArgumentException If the urn is not recognised.
     */
    Property makeProperty(String urn);

    /**
       Get all property urns understood by this factory.
       @return All property urns.
    */
    String[] getKnownPropertyURNs();
}
