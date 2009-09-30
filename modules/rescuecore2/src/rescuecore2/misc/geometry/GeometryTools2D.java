package rescuecore2.misc.geometry;

/**
   A bunch of useful 2D geometry tools: finding line intersections, closest points and so on.
 */
public class GeometryTools2D {
    public static final double THRESHOLD = 0.0000000000001;

    /**
       Find the intersection point of two lines.
       @param l1 The first line.
       @param l2 The second line.
       @return The intersection point of the two lines, or null if the lines are parallel.
     */
    public static Point2D getIntersectionPoint(Line2D l1, Line2D l2) {
        double t1 = l1.getIntersection(l2);
        double t2 = l2.getIntersection(l1);
        if (Double.isNaN(t1) || Double.isNaN(t2)) {
            return null;
        }
        return l1.getPoint(t1);
    }

    /**
       Find the intersection point of two line segments.
       @param l1 The first line segment.
       @param l2 The second line segment.
       @return The intersection point, or null if the line segments are parallel or do not intersect.
     */
    public static Point2D getSegmentIntersectionPoint(Line2D l1, Line2D l2) {
        double t1 = l1.getIntersection(l2);
        double t2 = l2.getIntersection(l1);
        if (Double.isNaN(t1) || Double.isNaN(t2) || t1 < 0 || t1 > 1 || t2 < 0 || t2 > 1) {
            return null;
        }
        return l1.getPoint(t1);
    }

    /**
       Are two lines parallel?
       @param l1 The first line.
       @param l2 The second line.
       @return true iff the lines are parallel (within tolerance). Direction does not matter; lines pointing in opposite directions are still parallel.
     */
    public static boolean parallel(Line2D l1, Line2D l2) {
        double d = (l1.getDirection().getX() * l2.getDirection().getY()) - (l1.getDirection().getY() * l2.getDirection().getX());
        return (d > -THRESHOLD) && (d < THRESHOLD);
    }

    /**
       Find out if a point is on a line.
       @param line The line to test.
       @param point The point to test.
       @return true iff the point is on the line (within tolerance).
     */
    public static boolean contains(Line2D line, Point2D point) {
        double tx = (point.getX() - line.getOrigin().getX()) / line.getDirection().getX();
        double ty = (point.getY() - line.getOrigin().getY()) / line.getDirection().getY();
        if (line.getDirection().getX() > -THRESHOLD && line.getDirection().getX() < THRESHOLD) {
            // Line is parallel to the Y axis so tx can be anything as long as the point is the right x value
            double d = point.getX() - line.getOrigin().getX();
            if (d > -THRESHOLD && d < THRESHOLD) {
                // Close enough
                tx = ty;
            }
            else {
                tx = Double.NaN; // This will make all comparisons false
            }
        }
        if (line.getDirection().getY() > -THRESHOLD && line.getDirection().getY() < THRESHOLD) {
            // Line is parallel to the X axis so ty can be anything as long as the point is the right y value
            double d = point.getY() - line.getOrigin().getY();
            if (d > -THRESHOLD && d < THRESHOLD) {
                // Close enough
                ty = tx;
            }
            else {
                ty = Double.NaN; // This will make all comparisons false
            }
        }
        if (tx < 0 || tx > 1 || ty < 0 || ty > 1) {
            return false;
        }
        double d = tx - ty;
        return (d > -THRESHOLD) && (d < THRESHOLD);
    }

    /**
       Find out how far a point is along a line.
       @param line The line to test.
       @param point The point to test.
       @return The t value of the point along the line, or NaN if the point is not on the line.
     */
    public static double positionOnLine(Line2D line, Point2D point) {
        double tx = (point.getX() - line.getOrigin().getX()) / line.getDirection().getX();
        double ty = (point.getY() - line.getOrigin().getY()) / line.getDirection().getY();
        double d = tx - ty;
        if ((d > -THRESHOLD) && (d < THRESHOLD)) {
            return tx;
        }
        else {
            return Double.NaN;
        }
    }
}