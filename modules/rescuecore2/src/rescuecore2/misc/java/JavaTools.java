package rescuecore2.misc.java;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import rescuecore2.log.Logger;

/**
 * A set of useful functions related to the Java language.
 */
public final class JavaTools {
  private JavaTools() {
  }

  /**
   * Instantiate a class by reflection. This method will not throw exceptions but
   * will return null on error.
   *
   * @param className   The class name to instantiate.
   * @param outputClass The class that specifies the type that should be returned.
   *                    This will usually be a superclass of the given class name.
   * @param <T>         The desired return type.
   * @return A new instance of the given class name cast as the output class, or
   *         null if the class cannot be instantiated.
   */
  public static <T> T instantiate(String className, Class<T> outputClass) {
    try {
      Class<? extends T> clazz = Class.forName(className).asSubclass(outputClass);
      if (Modifier.isAbstract(clazz.getModifiers())) {
        return null;
      }
      return clazz.getDeclaredConstructor().newInstance();
    } catch (ClassNotFoundException e) {
      Logger.info("Could not find class " + className);
    } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
      Logger.info("Could not instantiate class " + className);
    }

    return null;
  }

  /**
   * Instantiate a factory class by reflection. Essentially the same as
   * {@link #instantiate(String, Class)} except that it will look for a static
   * field called INSTANCE that contains an instance of the right class. If this
   * doesn't exist (or in inaccessible or the wrong type) then a constructor will
   * be used.
   *
   * @param classname   The class name to instantiate.
   * @param outputClass The class that specifies the type that should be returned.
   *                    This will usually be a superclass of the given class name.
   * @param <T>         The desired return type.
   * @return The content of the INSTANCE field if it exists; a new instance of the
   *         given class name cast as the output class, or null if the class
   *         cannot be instantiated.
   */
  public static <T> T instantiateFactory(String classname, Class<T> outputClass) {
    Class<? extends T> clazz;
    try {
      clazz = Class.forName(classname).asSubclass(outputClass);
    } catch (ClassNotFoundException e) {
      Logger.info("Could not find class " + classname);
      return null;
    } catch (ClassCastException e) {
      Logger.info(classname + " is not a subclass of " + outputClass.getName(), e);
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
        } catch (IllegalAccessException e) {
          Logger.info("Could not access INSTANCE field in class " + classname + ": trying constructor.");
        } catch (ClassCastException e) {
          Logger.info(
              "Could not cast INSTANCE field to " + outputClass + " in class " + classname + ": trying constructor.");
        }
      }
    } catch (NoSuchFieldException e) {
      Logger.info("No INSTANCE field in class " + classname);
      // No singleton instance. Try instantiating it.
    }
    try {
      return clazz.getDeclaredConstructor().newInstance();
    } catch (IllegalAccessException e) {
      Logger.info("Could not instantiate class " + classname);
    } catch (InstantiationException | NoSuchMethodException | InvocationTargetException e) {
      Logger.info("Could not instantiate class " + classname);
    }
    return null;
  }
}