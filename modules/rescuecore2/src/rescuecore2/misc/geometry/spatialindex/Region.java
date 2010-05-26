package rescuecore2.misc.geometry.spatialindex;

/**
   An indexable region.
*/
public interface Region {
    /**
       Find out if this region intersects another region.
       @param r The other region.
       @return True if this region intersects the other.
    */
    boolean intersects(Region r);

    /**
       Find out if this region fully contains another region.
       @param r The other region.
       @return True if this region contains the other.
    */
    boolean contains(Region r);

    /**
       Find out if this region touches another region.
       @param r The other region.
       @return True if this region touches the other.
    */
    //    boolean touches(Region r);

    /**
       Get the lower X coordinate.
       @return The lower X coordinate.
    */
    double getXMin();

    /**
       Get the lower Y coordinate.
       @return The lower Y coordinate.
    */
    double getYMin();

    /**
       Get the upper X coordinate.
       @return The upper X coordinate.
    */
    double getXMax();

    /**
       Get the upper Y coordinate.
       @return The upper Y coordinate.
    */
    double getYMax();
}