package rescuecore2.registry;

import java.util.EnumSet;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import rescuecore2.worldmodel.Property;

/**
   An abstract property factory with helper methods for defining URNs with enums.
   @param <T> An enum type that defines the URNs this factory knows about.
 */
public abstract class AbstractPropertyFactory<T extends Enum<T>> implements PropertyFactory {
    private Class<T> clazz;
    private Method fromString;

    /**
       Constructor for AbstractPropertyFactory.
       @param clazz The class of enum this factory uses.
    */
    protected AbstractPropertyFactory(Class<T> clazz) {
        this.clazz = clazz;
        try {
            fromString = clazz.getDeclaredMethod("fromString", String.class);
        }
        catch (NoSuchMethodException e) {
            fromString = null;
        }
    }

    @Override
    public String[] getKnownPropertyURNs() {
        EnumSet<T> set = getKnownPropertyURNsEnum();
        String[] result = new String[set.size()];
        int i = 0;
        for (T next : set) {
            result[i++] = next.toString();
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Property makeProperty(String urn) {
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
        return makeProperty(t);
    }

    /**
       Get an EnumSet containing known property URNs. Default implementation returns EnumSet.allOf(T).
       @return An EnumSet containing known property URNs.
     */
    protected EnumSet<T> getKnownPropertyURNsEnum() {
        return EnumSet.allOf(clazz);
    }

    /**
       Create a new Property.
       @param urn The enum urn of the property to create.
       @return A new Property of the correct type.
       @throws IllegalArgumentException If the urn is not recognised.
     */
    protected abstract Property makeProperty(T urn);
}
