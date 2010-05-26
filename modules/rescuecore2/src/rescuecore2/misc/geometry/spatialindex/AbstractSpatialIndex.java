package rescuecore2.misc.geometry.spatialindex;

import java.util.Collection;

/**
   Abstract base class for spatial index implementations.
*/
public abstract class AbstractSpatialIndex implements SpatialIndex {
    @Override
    public Collection<Indexable> getItemsInRegion(double xMin, double yMin, double xMax, double yMax) {
        return getItemsInRegion(new RectangleRegion(xMin, yMin, xMax, yMax));
    }
}