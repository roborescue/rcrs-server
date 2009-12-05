package rescuecore2.misc.java;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
   A set of useful functions related to the Java language.
 */
public final class JavaTools {
    private static final Log LOG = LogFactory.getLog(JavaTools.class);

    private JavaTools() {}

    /**
       Instantiate a class by reflection. This method will not throw exceptions but will return null on error.
       @param className The class name to instantiate.
       @param outputClass The class that specifies the type that should be returned. This will usually be a superclass of the given class name.
       @param <T> The desired return type.
       @return A new instance of the given class name cast as the output class, or null if the class cannot be instantiated.
     */
    @SuppressWarnings("unchecked")
    public static <T> T instantiate(String className, Class<T> outputClass) {
        try {
            Class<T> clazz = (Class<T>)Class.forName(className);
            return clazz.newInstance();
        }
        catch (ClassNotFoundException e) {
            LOG.info("Could not find class " + className,  e);
        }
        catch (IllegalAccessException e) {
            LOG.info("Could not instantiate class " + className, e);
        }
        catch (InstantiationException e) {
            LOG.info("Could not instantiate class " + className, e);
        }
        return null;
    }

    /**
       Instantiate a factory class by reflection. Essentially the same as {@link #instantiate(String, Class)} except that it will look for a static field called INSTANCE that contains an instance of the right class. If this doesn't exist (or in inaccessable or the wrong type) then a constructor will be used.
       @param classname The class name to instantiate.
       @param outputClass The class that specifies the type that should be returned. This will usually be a superclass of the given class name.
       @param <T> The desired return type.
       @return The content of the INSTANCE field if it exists; a new instance of the given class name cast as the output class, or null if the class cannot be instantiated.
    */
    public static <T> T instantiateFactory(String classname, Class<T> outputClass) {
        Class<?> clazz;
        try {
            clazz = Class.forName(classname);
        }
        catch (ClassNotFoundException e) {
            LOG.info("Could not find class " + classname, e);
            return null;
        }
        // Is there a singleton instance called INSTANCE?
        try {
            Field field = clazz.getField("INSTANCE");
            if (Modifier.isStatic(field.getModifiers())) {
                try {
                    Object o = field.get(null);
                    if (o != null) {
                        return outputClass.cast(o);
                    }
                }
                catch (IllegalAccessException e) {
                    LOG.info("Could not access INSTANCE field in class " + classname + ": trying constructor.");
                }
                catch (ClassCastException e) {
                    LOG.info("Could not cast INSTANCE field to " + outputClass + " in class " + classname + ": trying constructor.");
                }
            }
        }
        catch (NoSuchFieldException e) {
            LOG.info("No INSTANCE field in class " + classname, e);
            // No singleton instance. Try instantiating it.
        }
        try {
            return outputClass.cast(clazz.newInstance());
        }
        catch (IllegalAccessException e) {
            LOG.info("Could not instantiate class " + classname, e);
        }
        catch (InstantiationException e) {
            LOG.info("Could not instantiate class " + classname, e);
        }
        return null;
    }
}