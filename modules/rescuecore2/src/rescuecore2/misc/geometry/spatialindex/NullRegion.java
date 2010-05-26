package rescuecore2.misc.geometry.spatialindex;

/**
   An empty region.
*/
public class NullRegion implements Region {
    /**
       Construct an empty region.
    */
    public NullRegion() {
    }

    @Override
    public String toString() {
        return "Empty region";
    }

    @Override
    public boolean intersects(Region r) {
        return false;
    }

    @Override
    public boolean contains(Region r) {
        return false;
    }

    /*
    @Override
    public boolean touches(Region r) {
        return false;
    }
    */

    @Override
    public double getXMin() {
        return Double.NaN;
    }

    @Override
    public double getYMin() {
        return Double.NaN;
    }

    @Override
    public double getXMax() {
        return Double.NaN;
    }

    @Override
    public double getYMax() {
        return Double.NaN;
    }
}

