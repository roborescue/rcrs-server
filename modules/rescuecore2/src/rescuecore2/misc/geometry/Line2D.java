package rescuecore2.misc.geometry;

import static rescuecore2.misc.geometry.GeometryTools2D.nearlyZero;

/**
   A line segment in 2D space. Lines are immutable.
 */
public class Line2D {
    private Point2D origin;
    private Point2D end;
    private Vector2D direction;

    /**
       Create a new line segment.
       @param origin The origin of the line.
       @param direction The direction of the line.
     */
    public Line2D(Point2D origin, Vector2D direction) {
        this.origin = origin;
        this.direction = direction;
        this.end = origin.plus(direction);
    }

    /**
       Create a new line segment.
       @param origin The origin of the line.
       @param end The end of the line.
     */
    public Line2D(Point2D origin, Point2D end) {
        this.origin = origin;
        this.end = end;
        this.direction = end.minus(origin);
    }

    /**
       Create a new line segment.
       @param x The x coordinate of the origin.
       @param y The y coordinate of the origin.
       @param dx The x component of the direction.
       @param dy The y component of the direction.
     */
    public Line2D(double x, double y, double dx, double dy) {
        this(new Point2D(x, y), new Vector2D(dx, dy));
    }

    /**
       Get a point along this line.
       @param t The distance along the direction vector to create the point.
       @return A new Point2D.
     */
    public Point2D getPoint(double t) {
        return origin.translate(t * direction.getX(), t * direction.getY());
    }

    /**
       Get the origin of this line segment.
       @return The origin.
    */
    public Point2D getOrigin() {
        return origin;
    }

    /**
       Get the endpoint of this line segment.
       @return The endpoint.
    */
    public Point2D getEndPoint() {
        return end;
    }

    /**
       Get the direction of this line segment.
       @return The direction vector.
    */
    public Vector2D getDirection() {
        return direction;
    }

    @Override
    public String toString() {
        return "Line from " + origin + " towards " + end + " (direction = " + direction + ")";
    }

    /**
       Find out how far along this line the intersection point with another line is.
       @param other The other line.
       @return How far along this line (in terms of this line's direction vector) the intersection point is, or NaN if the lines are parallel.
    */
    public double getIntersection(Line2D other) {
        double bxax = direction.getX();
        double dycy = other.direction.getY();
        double byay = direction.getY();
        double dxcx = other.direction.getX();
        double cxax = other.origin.getX() - origin.getX();
        double cyay = other.origin.getY() - origin.getY();
        double d = (bxax * dycy) - (byay * dxcx);
        double t = (cxax * dycy) - (cyay * dxcx);
        if (nearlyZero(d)) {
            // d is close to zero: lines are parallel so no intersection
            return Double.NaN;
        }
        return t / d;
    }
}
