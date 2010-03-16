package rescuecore2.misc.collections;

import java.lang.reflect.Array;

/**
   Useful array tools.
*/
public final class ArrayTools {
    private ArrayTools() {}

    /**
       Convert an array of objects to a string.
       @param array The array to convert.
       @return A stringified version of the array.
    */
    public static String convertArrayToString(Object[] array) {
        StringBuilder result = new StringBuilder();
        result.append("[");
        for (int i = 0; i < array.length; ++i) {
            result.append(array[i]);
            if (i < array.length - 1) {
                result.append(", ");
            }
        }
        result.append("]");
        return result.toString();
    }

    /**
       Convert an array object to a string. This will handle arrays of primitives as well as of Objects.
       @param array The array object to convert.
       @return A stringified version of the array.
    */
    public static String convertArrayObjectToString(Object array) {
        StringBuilder result = new StringBuilder();
        result.append("[");
        int length = Array.getLength(array);
        for (int i = 0; i < length; ++i) {
            result.append(Array.get(array, i));
            if (i < length - 1) {
                result.append(", ");
            }
        }
        result.append("]");
        return result.toString();
    }
}
