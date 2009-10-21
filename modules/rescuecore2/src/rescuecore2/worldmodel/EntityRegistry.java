package rescuecore2.worldmodel;

import java.util.Map;
import java.util.HashMap;

/**
   A class for managing the different types of entities and their associated factories.
 */
public final class EntityRegistry {
    private static Map<String, EntityFactory> factories;

    // Static class; private constructor.
    private EntityRegistry() {}

    static {
        factories = new HashMap<String, EntityFactory>();
    }

    /**
       Register an entity factory.
       @param factory The entity factory to register.
     */
    public static void register(EntityFactory factory) {
        for (String  urn : factory.getKnownEntityURNs()) {
            register(urn, factory);
        }
    }

    /**
       Register an entity type and assign an EntityFactory for constructing instances of this type.
       @param urn The urn to register.
       @param factory The factory that is responsible for constructing instances of this type.
     */
    public static void register(String urn, EntityFactory factory) {
        synchronized (factories) {
            EntityFactory old = factories.get(urn);
            if (old != null && old != factory) {
                System.out.println("WARNING: Entity " + urn + " is being clobbered by " + factory + ". Old factory: " + old);
            }
            factories.put(urn, factory);
        }
    }

    /**
       Deregister an entity urn.
       @param urn The entity urn to deregister.
     */
    public static void deregister(String urn) {
        synchronized (factories) {
            factories.remove(urn);
        }
    }

    /**
       Create an entity from a urn. If the urn is not recognised then return null. This method will delegate to the {@link #register(EntityFactory) previously registered} EntityFactory.
       @param urn The urn of the entity type to create.
       @param id The EntityID of the Entity that will be created.
       @return A new Entity object, or null if the urn is not recognised.
     */
    public static Entity createEntity(String urn, EntityID id) {
        EntityFactory factory;
        synchronized (factories) {
            factory = factories.get(urn);
        }
        if (factory == null) {
            System.out.println("Entity " + urn + " not recognised.");
            return null;
        }
        return factory.makeEntity(urn, id);
    }
}