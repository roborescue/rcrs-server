package rescuecore2.misc.geometry.spatialindex;

import java.util.Collection;

/**
   Top-level interface for spatial index implementations.
*/
public interface SpatialIndex {
    /**
       Add an item to the index.
       @param i The item to add.
    */
    void insert(Indexable i);

    /**
       Get all indexable objects in a region.
       @param xMin The minimum X value.
       @param yMin The minimum Y value.
       @param xMax The maximum X value.
       @param yMax The maximum Y value.
       @return All Indexable objects in the region.
    */
    Collection<Indexable> getItemsInRegion(double xMin, double yMin, double xMax, double yMax);

    /**
       Get all indexable objects in a region.
       @param region The region to check.
       @return All Indexable objects in the region.
    */
    Collection<Indexable> getItemsInRegion(Region region);
}