package rescuecore2.worldmodel;

import java.util.Map;
import java.util.HashMap;

/**
   A class for managing the different types of properties and their associated factories.
 */
public final class PropertyRegistry {
    private static Map<String, PropertyFactory> factories;

    // Static class; private constructor.
    private PropertyRegistry() {}

    static {
        factories = new HashMap<String, PropertyFactory>();
    }

    /**
       Register a property factory.
       @param factory The property factory to register.
     */
    public static void register(PropertyFactory factory) {
        for (String  urn : factory.getKnownPropertyURNs()) {
            register(urn, factory);
        }
    }

    /**
       Register a property type and assign a PropertyFactory for constructing instances of this type.
       @param urn The urn to register.
       @param factory The factory that is responsible for constructing instances of this type.
     */
    public static void register(String urn, PropertyFactory factory) {
        synchronized (factories) {
            PropertyFactory old = factories.get(urn);
            if (old != null && old != factory) {
                System.out.println("WARNING: Property " + urn + " is being clobbered by " + factory + ". Old factory: " + old);
            }
            factories.put(urn, factory);
        }
    }

    /**
       Deregister a property urn.
       @param urn The property urn to deregister.
     */
    public static void deregister(String urn) {
        synchronized (factories) {
            factories.remove(urn);
        }
    }

    /**
       Create a property from a urn. If the urn is not recognised then return null. This method will delegate to the {@link #register(PropertyFactory) previously registered} PropertyFactory.
       @param urn The urn of the property type to create.
       @return A new Property object, or null if the urn is not recognised.
     */
    public static Property createProperty(String urn) {
        PropertyFactory factory;
        synchronized (factories) {
            factory = factories.get(urn);
        }
        if (factory == null) {
            System.out.println("Property " + urn + " not recognised.");
            return null;
        }
        return factory.makeProperty(urn);
    }
}