package rescuecore2.misc.geometry.spatialindex;

import static rescuecore2.misc.geometry.spatialindex.Tools.equal;

import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.GeometryTools2D;

/**
   A line region.
*/
public class LineRegion implements Region {
    private double x1;
    private double y1;
    private double x2;
    private double y2;
    private Line2D line;

    /**
       Construct a line region.
       @param x1 The first X coordinate.
       @param y1 The first Y coordinate.
       @param x2 The second X coordinate.
       @param y2 The second Y coordinate.
    */
    public LineRegion(double x1, double y1, double x2, double y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        line = null;
    }

    /**
       Get the first X coordinate.
       @return The first X coordinate.
    */
    public double getX1() {
        return x1;
    }

    /**
       Get the first Y coordinate.
       @return The first Y coordinate.
    */
    public double getY1() {
        return y1;
    }

    /**
       Get the second X coordinate.
       @return The second X coordinate.
    */
    public double getX2() {
        return x2;
    }

    /**
       Get the second Y coordinate.
       @return The second Y coordinate.
    */
    public double getY2() {
        return y2;
    }

    /**
       Get a Line2D representing this region.
       @return A Line2D representation of the region.
    */
    public Line2D getLine() {
        if (line == null) {
            line = new Line2D(new Point2D(x1, y1), new Point2D(x2, y2));
        }
        return line;
    }

    @Override
    public double getXMin() {
        return Math.min(x1, x2);
    }

    @Override
    public double getYMin() {
        return Math.min(y1, y2);
    }

    @Override
    public double getXMax() {
        return Math.max(x1, x2);
    }

    @Override
    public double getYMax() {
        return Math.max(y1, y2);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof LineRegion) {
            LineRegion l = (LineRegion)o;
            return equal(x1, l.x1) && equal(y1, l.y1) && equal(x2, l.x2) && equal(y2, l.y2);
        }
        return false;
    }

    @Override
    public int hashCode() {
        Double d = x1 + x2 + y1 + y2;
        return d.hashCode();
    }

    @Override
    public String toString() {
        return "Line region: " + x1 + ", " + y1 + " -> " + x2 + ", " + y2;
    }

    @Override
    public boolean intersects(Region r) {
        if (r instanceof RectangleRegion) {
            RectangleRegion rect = (RectangleRegion)r;
            return GeometryTools2D.clipToRectangle(getLine(), rect.getXMin(), rect.getYMin(), rect.getXMax(), rect.getYMax()) != null;
        }
        else if (r instanceof LineRegion) {
            LineRegion l = (LineRegion)r;
            return GeometryTools2D.getSegmentIntersectionPoint(getLine(), l.getLine()) != null;
        }
        else if (r instanceof PointRegion) {
            PointRegion p = (PointRegion)r;
            return GeometryTools2D.contains(getLine(), p.getPoint());
        }
        else {
            return false;
        }
    }

    @Override
    public boolean contains(Region r) {
        if (r instanceof LineRegion) {
            LineRegion l = (LineRegion)r;
            double first = GeometryTools2D.positionOnLine(getLine(), l.getLine().getOrigin());
            double second = GeometryTools2D.positionOnLine(getLine(), l.getLine().getEndPoint());
            return first >= 0 && first <= 1 && second >= 0 && second <= 1;
        }
        else if (r instanceof PointRegion) {
            PointRegion p = (PointRegion)r;
            double d = GeometryTools2D.positionOnLine(getLine(), p.getPoint());
            return d >= 0 && d <= 1;
        }
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

