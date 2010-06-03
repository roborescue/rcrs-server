package rescuecore2.misc.geometry;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
   A bunch of useful 2D geometry tools: finding line intersections, closest points and so on.
 */
public final class GeometryTools2D {
    /**
       The threshold for equality testing in geometric operations. Lines will be considered parallel if the D factor is less than this; points will be considered equivalent if their position difference is less than this and so on.
    */
    public static final double THRESHOLD = 0.0000000000001;

    private GeometryTools2D() {}

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
        return nearlyZero(d);
    }

    /**
       Find out if a point is on a line.
       @param line The line to test.
       @param point The point to test.
       @return true iff the point is on the line (within tolerance).
     */
    public static boolean contains(Line2D line, Point2D point) {
        if (nearlyZero(line.getDirection().getX())) {
            // Line is parallel to the Y axis so just check that the X coordinate is correct and the Y coordinate is within bounds
            double d = point.getX() - line.getOrigin().getX();
            double y = point.getY();
            double yMin = Math.min(line.getOrigin().getY(), line.getEndPoint().getY());
            double yMax = Math.max(line.getOrigin().getY(), line.getEndPoint().getY());
            return nearlyZero(d) && y >= yMin && y <= yMax;
        }
        if (nearlyZero(line.getDirection().getY())) {
            // Line is parallel to the X axis so just check that the Y coordinate is correct and the X coordinate is within bounds
            double d = point.getY() - line.getOrigin().getY();
            double x = point.getX();
            double xMin = Math.min(line.getOrigin().getX(), line.getEndPoint().getX());
            double xMax = Math.max(line.getOrigin().getX(), line.getEndPoint().getX());
            return nearlyZero(d) && x >= xMin && x <= xMax;
        }
        double tx = (point.getX() - line.getOrigin().getX()) / line.getDirection().getX();
        double ty = (point.getY() - line.getOrigin().getY()) / line.getDirection().getY();
        if (tx < 0 || tx > 1 || ty < 0 || ty > 1) {
            return false;
        }
        double d = tx - ty;
        return nearlyZero(d);
    }

    /**
       Find out how far a point is along a line.
       @param line The line to test.
       @param point The point to test.
       @return The t value of the point along the line, or NaN if the point is not on the line.
     */
    public static double positionOnLine(Line2D line, Point2D point) {
        if (nearlyZero(line.getDirection().getX())) {
            // Line is parallel to the Y axis so just solve for Y
            double d = (point.getY() - line.getOrigin().getY()) / line.getDirection().getY();
            return d;
        }
        if (nearlyZero(line.getDirection().getY())) {
            // Line is parallel to the X axis so just solve for X
            double d = (point.getX() - line.getOrigin().getX()) / line.getDirection().getX();
            return d;
        }
        // Solve for both X and Y
        double tx = (point.getX() - line.getOrigin().getX()) / line.getDirection().getX();
        double ty = (point.getY() - line.getOrigin().getY()) / line.getDirection().getY();
        double d = tx - ty;
        if (nearlyZero(d)) {
            return tx;
        }
        else {
            return Double.NaN;
        }
    }

    /**
       Compute the angle between two lines in a clockwise direction.
       @param first The first line.
       @param second The second line.
       @return The angle in degrees measured in a clockwise direction.
    */
    public static double getRightAngleBetweenLines(Line2D first, Line2D second) {
        return getRightAngleBetweenVectors(first.getDirection(), second.getDirection());
    }

    /**
       Compute the angle between two lines in a counter-clockwise direction.
       @param first The first line.
       @param second The second line.
       @return The angle in degrees measured in a counter-clockwise direction.
    */
    public static double getLeftAngleBetweenLines(Line2D first, Line2D second) {
        return getLeftAngleBetweenVectors(first.getDirection(), second.getDirection());
    }

    /**
       Compute the angle between two vectors in degrees.
       @param first The first vector.
       @param second The second vector.
       @return The angle in degrees. This will be between 0 and 180.
    */
    public static double getAngleBetweenVectors(Vector2D first, Vector2D second) {
        Vector2D v1 = first.normalised();
        Vector2D v2 = second.normalised();
        double cos = v1.dot(v2);
        if (cos > 1) {
            cos = 1;
        }
        double angle = Math.toDegrees(Math.acos(cos));
        return angle;
    }

    /**
       Compute the angle between two vectors in a clockwise direction.
       @param first The first vector.
       @param second The second vector.
       @return The angle in degrees measured in a clockwise direction.
    */
    public static double getRightAngleBetweenVectors(Vector2D first, Vector2D second) {
        double angle = getAngleBetweenVectors(first, second);
        // Now find out if we're turning left or right
        if (isRightTurn(first, second)) {
            return angle;
        }
        else {
            // It's a left turn
            // CHECKSTYLE:OFF:MagicNumber
            return 360.0 - angle;
            // CHECKSTYLE:ON:MagicNumber
        }
    }

    /**
       Compute the angle between two vectors in a counter-clockwise direction.
       @param first The first vector.
       @param second The second vector.
       @return The angle in degrees measured in a counter-clockwise direction.
    */
    public static double getLeftAngleBetweenVectors(Vector2D first, Vector2D second) {
        double angle = getAngleBetweenVectors(first, second);
        // Now find out if we're turning left or right
        if (isRightTurn(first, second)) {
            // CHECKSTYLE:OFF:MagicNumber
            return 360.0 - angle;
            // CHECKSTYLE:ON:MagicNumber
        }
        else {
            return angle;
        }
    }

    /**
       Find out if we turn right from one line to another.
       @param first The first line.
       @param second The second line.
       @return True if the second line is a right turn from the first, false otherwise.
    */
    public static boolean isRightTurn(Line2D first, Line2D second) {
        return isRightTurn(first.getDirection(), second.getDirection());
    }

    /**
       Find out if we turn right from one vector to another.
       @param first The first vector.
       @param second The second vector.
       @return True if the second vector is a right turn from the first, false otherwise.
    */
    public static boolean isRightTurn(Vector2D first, Vector2D second) {
        double t = (first.getX() * second.getY()) - (first.getY() * second.getX());
        return t < 0;
    }

    /**
       Find out if a number is near enough to zero.
       @param d The number to test.
       @return true iff the number is nearly zero.
    */
    public static boolean nearlyZero(double d) {
        return d > -THRESHOLD && d < THRESHOLD;
    }

    /**
       Compute the area of a simple polygon.
       @param vertices The vertices of the polygon.
       @return The area of the polygon.
    */
    public static double computeArea(List<Point2D> vertices) {
        return Math.abs(computeAreaUnsigned(vertices));
    }

    /**
       Compute the centroid of a simple polygon.
       @param vertices The vertices of the polygon.
       @return The centroid.
    */
    public static Point2D computeCentroid(List<Point2D> vertices) {
        double area = computeAreaUnsigned(vertices);
        Iterator<Point2D> it = vertices.iterator();
        Point2D last = it.next();
        Point2D first = last;
        double xSum = 0;
        double ySum = 0;
        while (it.hasNext()) {
            Point2D next = it.next();
            double lastX = last.getX();
            double lastY = last.getY();
            double nextX = next.getX();
            double nextY = next.getY();
            xSum += (lastX + nextX) * ((lastX * nextY) - (nextX * lastY));
            ySum += (lastY + nextY) * ((lastX * nextY) - (nextX * lastY));
            last = next;
        }
        double lastX = last.getX();
        double lastY = last.getY();
        double nextX = first.getX();
        double nextY = first.getY();
        xSum += (lastX + nextX) * ((lastX * nextY) - (nextX * lastY));
        ySum += (lastY + nextY) * ((lastX * nextY) - (nextX * lastY));
        // CHECKSTYLE:OFF:MagicNumber
        xSum /= 6.0 * area;
        ySum /= 6.0 * area;
        // CHECKSTYLE:ON:MagicNumber
        return new Point2D(xSum, ySum);
    }

    /**
       Convert a vertex array to a list of Point2D objects.
       @param vertices The vertices in x, y order.
       @return A list of Point2D objects.
    */
    public static List<Point2D> vertexArrayToPoints(int[] vertices) {
        List<Point2D> result = new ArrayList<Point2D>();
        for (int i = 0; i < vertices.length; i += 2) {
            result.add(new Point2D(vertices[i], vertices[i + 1]));
        }
        return result;
    }

    /**
       Convert a vertex array to a list of Point2D objects.
       @param vertices The vertices in x, y order.
       @return A list of Point2D objects.
    */
    public static List<Point2D> vertexArrayToPoints(double[] vertices) {
        List<Point2D> result = new ArrayList<Point2D>();
        for (int i = 0; i < vertices.length; i += 2) {
            result.add(new Point2D(vertices[i], vertices[i + 1]));
        }
        return result;
    }

    /**
       Convert a list of Point2D objects to a list of lines connecting adjacent points. The shape will not be automatically closed.
       @param points The points to connect.
       @return A list of Line2D objects.
    */
    public static List<Line2D> pointsToLines(List<Point2D> points) {
        return pointsToLines(points, false);
    }

    /**
       Convert a list of Point2D objects to a list of lines connecting adjacent points.
       @param points The points to connect.
       @param close Whether to close the shape or not.
       @return A list of Line2D objects.
    */
    public static List<Line2D> pointsToLines(List<Point2D> points, boolean close) {
        List<Line2D> result = new ArrayList<Line2D>();
        Iterator<Point2D> it = points.iterator();
        Point2D first = it.next();
        Point2D prev = first;
        while (it.hasNext()) {
            Point2D next = it.next();
            result.add(new Line2D(prev, next));
            prev = next;
        }
        if (close && !prev.equals(first)) {
            result.add(new Line2D(prev, first));
        }
        return result;
    }

    /**
       Find the closest point on a line.
       @param line The line to check.
       @param point The point to check against.
       @return The point on the line that is closest to the reference point.
    */
    public static Point2D getClosestPoint(Line2D line, Point2D point) {
        Point2D p1 = line.getOrigin();
        Point2D p2 = line.getEndPoint();
        double u = (((point.getX() - p1.getX()) * (p2.getX() - p1.getX())) + ((point.getY() - p1.getY()) * (p2.getY() - p1.getY()))) / (line.getDirection().getLength() * line.getDirection().getLength());
        return line.getPoint(u);
    }

    /**
       Find the closest point on a line.
       @param line The line to check.
       @param point The point to check against.
       @return The point on the line that is closest to the reference point.
    */
    public static Point2D getClosestPointOnSegment(Line2D line, Point2D point) {
        Point2D p1 = line.getOrigin();
        Point2D p2 = line.getEndPoint();
        double u = (((point.getX() - p1.getX()) * (p2.getX() - p1.getX())) + ((point.getY() - p1.getY()) * (p2.getY() - p1.getY()))) / (line.getDirection().getLength() * line.getDirection().getLength());
        if (u <= 0) {
            return p1;
        }
        if (u >= 1) {
            return p2;
        }
        return line.getPoint(u);
    }

    /**
       Compute the distance between two points.
       @param p1 The first point.
       @param p2 The second point.
       @return The distance between the two points.
    */
    public static double getDistance(Point2D p1, Point2D p2) {
        return Math.hypot(p1.getX() - p2.getX(), p1.getY() - p2.getY());
    }

    /**
       Clip a line to a rectangle.
       @param line The line to clip.
       @param xMin The lower X coordinate of the rectangle.
       @param yMin The lower Y coordinate of the rectangle.
       @param xMax The upper X coordinate of the rectangle.
       @param yMax The upper Y coordinate of the rectangle.
       @return A clipped line, or null if the line is outside the rectangle.
    */
    public static Line2D clipToRectangle(Line2D line, double xMin, double yMin, double xMax, double yMax) {
        // Liang-Barsky line clipping algorithm
        double x1 = line.getOrigin().getX();
        double y1 = line.getOrigin().getY();
        double x2 = line.getEndPoint().getX();
        double y2 = line.getEndPoint().getY();
        double tL = (xMin - x1) / (x2 - x1);
        double tR = (xMax - x1) / (x2 - x1);
        double tT = (yMax - y1) / (y2 - y1);
        double tB = (yMin - y1) / (y2 - y1);
        double tMin = 0;
        double tMax = 1;
        if ((x1 < xMin && x2 < xMin)
            || (x1 > xMax && x2 > xMax)
            || (y1 < yMin && y2 < yMin)
            || (y1 > yMax && y2 > yMax)) {
            return null;
        }
        // Left
        if (tL > tMin && tL < tMax) {
            if (x1 < x2) {
                // Entering left edge
                tMin = tL;
            }
            else {
                tMax = tL;
            }
        }
        // Right
        if (tR > tMin && tR < tMax) {
            if (x1 > x2) {
                // Entering right edge
                tMin = tR;
            }
            else {
                tMax = tR;
            }
        }
        // Top
        if (tT > tMin && tT < tMax) {
            if (y1 > y2) {
                // Entering top edge
                tMin = tT;
            }
            else {
                tMax = tT;
            }
        }
        // Left
        if (tB > tMin && tB < tMax) {
            if (y1 < y2) {
                // Entering bottom edge
                tMin = tB;
            }
            else {
                tMax = tB;
            }
        }
        if (tMin > tMax) {
            return null;
        }
        return new Line2D(line.getPoint(tMin), line.getPoint(tMax));
    }

    private static double computeAreaUnsigned(List<Point2D> vertices) {
        Iterator<Point2D> it = vertices.iterator();
        Point2D last = it.next();
        Point2D first = last;
        double sum = 0;
        while (it.hasNext()) {
            Point2D next = it.next();
            double lastX = last.getX();
            double lastY = last.getY();
            double nextX = next.getX();
            double nextY = next.getY();
            sum += (lastX * nextY) - (nextX * lastY);
            last = next;
        }
        double lastX = last.getX();
        double lastY = last.getY();
        double nextX = first.getX();
        double nextY = first.getY();
        sum += (lastX * nextY) - (nextX * lastY);
        sum /= 2.0;
        return sum;
    }
}
