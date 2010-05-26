package rescuecore2.misc.geometry.spatialindex;

/**
   Interface for things that can go into a spatial index.
*/
public interface Indexable {
    /**
       Get the bounding region of this object.
       @return The bounding region.
    */
    Region getBoundingRegion();
}