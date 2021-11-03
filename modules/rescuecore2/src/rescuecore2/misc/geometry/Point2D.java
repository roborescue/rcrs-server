package rescuecore2.misc.geometry;

import rescuecore2.misc.geometry.spatialindex.Indexable;
import rescuecore2.misc.geometry.spatialindex.PointRegion;
import rescuecore2.misc.geometry.spatialindex.Region;

/**
 * A point in 2D space. Points are immutable.
 */
public class Point2D implements Indexable {
  private double x;
  private double y;
  private PointRegion region;

  /**
   * Create a new Point2D.
   *
   * @param x The x coordinate.
   * @param y The y coordinate.
   */
  public Point2D(double x, double y) {
    this.x = x;
    this.y = y;
    region = null;
  }

  /**
   * Get the X coordinate.
   *
   * @return The X coordinate.
   */
  public double getX() {
    return x;
  }

  /**
   * Get the Y coordinate.
   *
   * @return The Y coordinate.
   */
  public double getY() {
    return y;
  }

  /**
   * Create a new Point2D that is a translation of this point.
   *
   * @param dx The x translation.
   * @param dy The y translation.
   * @return A new Point2D.
   */
  public Point2D translate(double dx, double dy) {
    return new Point2D(x + dx, y + dy);
  }

  /**
   * Create a vector by subtracting a point from this point.
   *
   * @param p The Point2D to subtract from this one.
   * @return A new Vector2D that represents the vector from the other point to
   *         this one.
   */
  public Vector2D minus(Point2D p) {
    return new Vector2D(this.x - p.x, this.y - p.y);
  }

  /**
   * Create a Point2D by adding a vector to this point.
   *
   * @param v The Vector2D to add.
   * @return A new Point2D.
   */
  public Point2D plus(Vector2D v) {
    return new Point2D(this.x + v.getX(), this.y + v.getY());
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Point2D)) {
      return false;
    }
    return this.x == ((Point2D) o).x && this.y == ((Point2D) o).y;
  }

  @Override
  public int hashCode() {
    return Double.valueOf(x).hashCode() ^ Double.valueOf(y).hashCode();
  }

  @Override
  public String toString() {
    return x + " , " + y;
  }

  @Override
  public Region getBoundingRegion() {
    if (region == null) {
      region = new PointRegion(x, y);
    }
    return region;
  }
}