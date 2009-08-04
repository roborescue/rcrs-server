package rescuecore2.misc;

/**
   A set of useful functions related to the Java language.
 */
public final class JavaTools {
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
            System.err.println("Could not find class " + className + ": " + e);
        }
        catch (IllegalAccessException e) {
            System.err.println("Could not instantiate class " + className + ": " + e);
        }
        catch (InstantiationException e) {
            System.err.println("Could not instantiate class " + className + ": " + e);
        }
        return null;
    }
}