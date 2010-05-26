package rescuecore2.misc.geometry.spatialindex;

import static rescuecore2.misc.geometry.spatialindex.Tools.equal;

import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.GeometryTools2D;

/**
   A point region.
*/
public class PointRegion implements Region {
    private double x;
    private double y;
    private Point2D point;

    /**
       Construct a point region.
       @param x The X coordinate.
       @param y The Y coordinate.
    */
    public PointRegion(double x, double y) {
        this.x = x;
        this.y = y;
        point = null;
    }

    /**
       Get the X coordinate.
       @return The X coordinate.
    */
    public double getX() {
        return x;
    }

    /**
       Get the Y coordinate.
       @return The Y coordinate.
    */
    public double getY() {
        return y;
    }

    /**
       Get a Point2D representing this region.
       @return A Point2D representation of the region.
    */
    public Point2D getPoint() {
        if (point == null) {
            point = new Point2D(x, y);
        }
        return point;
    }

    @Override
    public double getXMin() {
        return x;
    }

    @Override
    public double getYMin() {
        return y;
    }

    @Override
    public double getXMax() {
        return x;
    }

    @Override
    public double getYMax() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof PointRegion) {
            PointRegion p = (PointRegion)o;
            return equal(x, p.x) && equal(y, p.y);
        }
        return false;
    }

    @Override
    public int hashCode() {
        Double d = x + y;
        return d.hashCode();
    }

    @Override
    public String toString() {
        return "Point region: " + x + ", " + y;
    }

    @Override
    public boolean intersects(Region r) {
        if (r instanceof RectangleRegion) {
            RectangleRegion rect = (RectangleRegion)r;
            return x >= rect.getXMin() && x <= rect.getXMax() && y >= rect.getYMin() && y <= rect.getYMax();
        }
        else if (r instanceof LineRegion) {
            LineRegion l = (LineRegion)r;
            return GeometryTools2D.contains(l.getLine(), getPoint());
        }
        else if (r instanceof PointRegion) {
            return this.equals(r);
        }
        else {
            return false;
        }
    }

    @Override
    public boolean contains(Region r) {
        return false;
    }

    /*
    @Override
    public boolean touches(Region r) {
        if (r instanceof RectangleRegion) {
            RectangleRegion rect = (RectangleRegion)r;
            return (equal(this.x, rect.getXMin())
                    || equal(this.x, rect.getXMax())
                    || equal(this.y, rect.getYMin())
                    || equal(this.y, rect.getYMax()));
        }
        else if (r instanceof PointRegion) {
            PointRegion p = (PointRegion)r;
            return (equal(this.x, p.x)
                    || equal(this.y, p.y));
        }
        else if (r instanceof NullRegion) {
            return false;
        }
        else {
            throw new IllegalArgumentException("Cannot check for touch with " + r);
        }
    }
    */
}

