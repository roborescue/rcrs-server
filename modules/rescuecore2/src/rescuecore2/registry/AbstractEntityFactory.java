package rescuecore2.registry;

import java.util.EnumSet;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;

/**
   An abstract entity factory with helper methods for defining URNs with enums.
   @param <T> An enum type that defines the URNs this factory knows about.
 */
public abstract class AbstractEntityFactory<T extends Enum<T>> implements EntityFactory {
    private Class<T> clazz;
    private Method fromString;

    /**
       Constructor for AbstractEntityFactory.
       @param clazz The class of enum this factory uses.
    */
    protected AbstractEntityFactory(Class<T> clazz) {
        this.clazz = clazz;
        try {
            fromString = clazz.getDeclaredMethod("fromString", String.class);
        }
        catch (NoSuchMethodException e) {
            fromString = null;
        }
    }

    @Override
    public String[] getKnownEntityURNs() {
        EnumSet<T> set = getKnownEntityURNsEnum();
        String[] result = new String[set.size()];
        int i = 0;
        for (T next : set) {
            result[i++] = next.toString();
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Entity makeEntity(String urn, EntityID id) {
        T t = null;
        if (fromString != null) {
            try {
                t = (T)fromString.invoke(null, urn);
            }
            catch (IllegalAccessException e) {
                t = null;
            }
            catch (InvocationTargetException e) {
                t = null;
            }
        }
        if (t == null) {
            t = Enum.valueOf(clazz, urn);
        }
        return makeEntity(t, id);
    }

    /**
       Get an EnumSet containing known entity URNs. Default implementation returns EnumSet.allOf(T).
       @return An EnumSet containing known entity URNs.
     */
    protected EnumSet<T> getKnownEntityURNsEnum() {
        return EnumSet.allOf(clazz);
    }

    /**
       Create a new Entity.
       @param urn The enum urn of the entity to create.
       @param id The id of the new entity.
       @return A new Entity of the correct type.
       @throws IllegalArgumentException If the urn is not recognised.
     */
    protected abstract Entity makeEntity(T urn, EntityID id);
}
