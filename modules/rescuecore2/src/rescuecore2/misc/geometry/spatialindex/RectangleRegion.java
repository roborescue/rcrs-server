package rescuecore2.misc.geometry.spatialindex;

import static rescuecore2.misc.geometry.spatialindex.Tools.equal;

import rescuecore2.misc.geometry.GeometryTools2D;

/**
   A rectangular region.
*/
public class RectangleRegion implements Region {
    private double xMin;
    private double xMax;
    private double yMin;
    private double yMax;

    /**
       Construct a rectangular region.
       @param xMin The lower X coordinate.
       @param yMin The lower Y coordinate.
       @param xMax The upper X coordinate.
       @param yMax The upper Y coordinate.
    */
    public RectangleRegion(double xMin, double yMin, double xMax, double yMax) {
        this.xMin = xMin;
        this.yMin = yMin;
        this.xMax = xMax;
        this.yMax = yMax;
        if (xMin > xMax) {
            this.xMin = xMax;
            this.xMax = xMin;
        }
        if (yMin > yMax) {
            this.yMin = yMax;
            this.yMax = yMin;
        }
    }

    @Override
    public double getXMin() {
        return xMin;
    }

    @Override
    public double getYMin() {
        return yMin;
    }

    @Override
    public double getXMax() {
        return xMax;
    }

    @Override
    public double getYMax() {
        return yMax;
    }

    /**
       Calculate the overlap with another RectangleRegion.
       @param other The other region.
       @return The overlapping area.
    */
    public double getOverlapArea(RectangleRegion other) {
        if (!this.intersects(other)) {
            return 0;
        }
        double overlapXMin = Math.max(this.xMin, other.xMin);
        double overlapXMax = Math.min(this.xMax, other.xMax);
        double overlapYMin = Math.max(this.yMin, other.yMin);
        double overlapYMax = Math.min(this.yMax, other.yMax);
        return (overlapXMax - overlapXMin) * (overlapYMax - overlapYMin);
    }

    /**
       Get the area of this rectangle.
       @return The area.
    */
    public double getArea() {
        return (xMax - xMin) * (yMax - yMin);
    }

    @Override
    public String toString() {
        return "Rectangle region: " + xMin + ", " + yMin + " -> " + xMax + ", " + yMax;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof RectangleRegion) {
            RectangleRegion r = (RectangleRegion)o;
            return equal(xMin, r.xMin) && equal(xMax, r.xMax) && equal(yMin, r.yMin) && equal(yMax, r.yMax);
        }
        return false;
    }

    @Override
    public int hashCode() {
        Double d = xMin;
        return d.hashCode();
    }

    @Override
    public boolean intersects(Region r) {
        if (r instanceof RectangleRegion) {
            RectangleRegion rect = (RectangleRegion)r;
            if (rect.xMax < this.xMin
                || rect.xMin > this.xMax
                || rect.yMax < this.yMin
                || rect.yMin > this.yMax) {
                return false;
            }
            return true;
        }
        else if (r instanceof LineRegion) {
            LineRegion l = (LineRegion)r;
            return GeometryTools2D.clipToRectangle(l.getLine(), getXMin(), getYMin(), getXMax(), getYMax()) != null;
        }
        else if (r instanceof PointRegion) {
            PointRegion p = (PointRegion)r;
            if (p.getX() < xMin
                || p.getX() > xMax
                || p.getY() < yMin
                || p.getY() > yMax) {
                return false;
            }
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public boolean contains(Region r) {
        if (r instanceof RectangleRegion) {
            RectangleRegion rect = (RectangleRegion)r;
            return (rect.xMin >= this.xMin
                    && rect.xMax <= this.xMax
                    && rect.yMin >= this.yMin
                    && rect.yMax <= this.yMax);
        }
        else if (r instanceof LineRegion) {
            LineRegion l = (LineRegion)r;
            return (l.getXMin() >= this.xMin
                    && l.getXMax() <= this.xMax
                    && l.getYMin() >= this.yMin
                    && l.getYMax() <= this.yMax);
        }
        else if (r instanceof PointRegion) {
            PointRegion p = (PointRegion)r;
            return (p.getX() >= this.xMin
                    && p.getX() <= this.xMax
                    && p.getY() >= this.yMin
                    && p.getY() <= this.yMax);
        }
        else {
            return false;
        }
    }

    /*
    @Override
    public boolean touches(Region r) {
        if (r instanceof RectangleRegion) {
            RectangleRegion rect = (RectangleRegion)r;
            return (equal(this.xMin, rect.xMin)
                    || equal(this.xMax, rect.xMax)
                    || equal(this.yMin, rect.yMin)
                    || equal(this.yMax, rect.yMax));
        }
        else if (r instanceof PointRegion) {
            PointRegion p = (PointRegion)r;
            return (equal(this.xMin, p.getX())
                    || equal(this.xMax, p.getX())
                    || equal(this.yMin, p.getY())
                    || equal(this.yMax, p.getY()));
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

