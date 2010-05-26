package rescuecore2.misc.geometry.spatialindex;

/**
   Useful tools for R* tree building.
*/
public final class Tools {
    private static final double EPSILON = 0.000000001;

    private Tools() {}

    /**
       Find out if two numbers are equal (within epsilon).
       @param d1 The first number.
       @param d2 The second number.
       @return True if d1 and d2 are within epsilon of each other.
    */
    public static boolean equal(double d1, double d2) {
        return ((d1 > d2 - EPSILON) && (d1 < d2 + EPSILON));
    }
}