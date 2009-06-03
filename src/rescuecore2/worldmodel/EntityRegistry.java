package rescuecore2.worldmodel;

import java.util.Map;
import java.util.HashMap;

/**
   A class for managing the different types of entities and their associated factories.
 */
public final class EntityRegistry {
    private static Map<Integer, EntityFactory> factories;

    // Static class; private constructor.
    private EntityRegistry() {}

    static {
        factories = new HashMap<Integer, EntityFactory>();
    }

    /**
       Register an entity factory.
       @param factory The entity factory to register.
     */
    public static void register(EntityFactory factory) {
        for (int i : factory.getKnownEntityTypeIDs()) {
            register(i, factory);
        }
    }

    /**
       Register an entity type and assign an EntityFactory for constructing instances of this type.
       @param id The entity type ID to register.
       @param factory The factory that is responsible for constructing instances of this type.
     */
    public static void register(int id, EntityFactory factory) {
        synchronized (factories) {
            factories.put(id, factory);
        }
    }

    /**
       Deregister an entity ID number.
       @param id The entity type ID to deregister.
     */
    public static void deregister(int id) {
        synchronized (factories) {
            factories.remove(id);
        }
    }

    /**
       Create an entity from a type ID. If the ID is not recognised then return null. This method will delegate to the {@link #register(int) previously registered} EntityFactory.
       @param type The type id of the entity type to create.
       @param id The EntityID of the Entity that will be created.
       @return A new Entity object, or null if the ID is not recognised.
     */
    public static Entity createEntity(int type, EntityID id) {
        EntityFactory factory;
        synchronized (factories) {
            factory = factories.get(type);
        }
        if (factory == null) {
            System.out.println("Entity id " + type + " not recognised.");
            return null;
        }
        return factory.makeEntity(type, id);
    }
}