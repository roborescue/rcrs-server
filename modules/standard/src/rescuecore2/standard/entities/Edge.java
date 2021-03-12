package rescuecore2.standard.entities;

import org.json.JSONObject;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Line2D;

/**
 * An edge is a line segment with an optional neighbouring entity. Edges without
 * neighbours are impassable, edges with neighbours are passable.
 */
public class Edge {

  private Point2D  start;
  private Point2D  end;
  private Line2D   line;
  private EntityID neighbour;


  /**
   * Construct an impassable Edge.
   *
   * @param startX
   *          The X coordinate of the first endpoint.
   * @param startY
   *          The Y coordinate of the first endpoint.
   * @param endX
   *          The X coordinate of the second endpoint.
   * @param endY
   *          The Y coordinate of the second endpoint.
   */
  public Edge( int startX, int startY, int endX, int endY ) {
    this( new Point2D( startX, startY ), new Point2D( endX, endY ), null );
  }


  /**
   * Construct an impassable Edge.
   *
   * @param start
   *          The first endpoint coordinates.
   * @param end
   *          The second endpoint coordinates.
   */
  public Edge( Point2D start, Point2D end ) {
    this( start, end, null );
  }


  /**
   * Construct an Edge. If the neighbour is null then this edge is impassable;
   * if it is non-null then this edge is passable.
   *
   * @param startX
   *          The X coordinate of the first endpoint.
   * @param startY
   *          The Y coordinate of the first endpoint.
   * @param endX
   *          The X coordinate of the second endpoint.
   * @param endY
   *          The Y coordinate of the second endpoint.
   * @param neighbour
   *          The ID of the neighbour on the other side of this edge. This may
   *          be null to indicate an impassable edge.
   */
  public Edge( int startX, int startY, int endX, int endY, EntityID neighbour ) {
    this( new Point2D( startX, startY ), new Point2D( endX, endY ), neighbour );
  }


  /**
   * Construct an Edge. If the neighbour is null then this edge is impassable;
   * if it is non-null then this edge is passable.
   *
   * @param start
   *          The first endpoint coordinates.
   * @param end
   *          The second endpoint coordinates.
   * @param neighbour
   *          The ID of the neighbour on the other side of this edge. This may
   *          be null to indicate an impassable edge.
   */
  public Edge( Point2D start, Point2D end, EntityID neighbour ) {
    this.start = start;
    this.end = end;
    this.neighbour = neighbour;
    line = new Line2D( start, end );
  }


  /**
   * Get the X coordinate of the first endpoint.
   *
   * @return The X coordinate of the first endpoint.
   */
  public int getStartX() {
    return (int) start.getX();
  }


  /**
   * Get the Y coordinate of the first endpoint.
   *
   * @return The Y coordinate of the first endpoint.
   */
  public int getStartY() {
    return (int) start.getY();
  }


  /**
   * Get the X coordinate of the second endpoint.
   *
   * @return The X coordinate of the second endpoint.
   */
  public int getEndX() {
    return (int) end.getX();
  }


  /**
   * Get the Y coordinate of the second endpoint.
   *
   * @return The Y coordinate of the second endpoint.
   */
  public int getEndY() {
    return (int) end.getY();
  }


  /**
   * Get the start point.
   *
   * @return The start point.
   */
  public Point2D getStart() {
    return start;
  }


  /**
   * Get the end point.
   *
   * @return The end point.
   */
  public Point2D getEnd() {
    return end;
  }


  /**
   * Get the ID of the neighbour.
   *
   * @return The ID of the neighbour or null if this edge is impassable.
   */
  public EntityID getNeighbour() {
    return neighbour;
  }


  /**
   * Find out if this edge is passable or not.
   *
   * @return True iff the neighbour is non-null.
   */
  public boolean isPassable() {
    return neighbour != null;
  }


  /**
   * Get a line representing this edge.
   *
   * @return A Line2D representing this edge.
   */
  public Line2D getLine() {
    return line;
  }


  @Override
  public String toString() {
    return "Edge from " + start + " to " + end + " ("
        + ( neighbour == null ? "impassable" : "neighbour: " + neighbour )
        + ")";
  }


  public JSONObject toJson() {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put( "StartX", this.getStartX() );
    jsonObject.put( "StartY", this.getStartY() );
    jsonObject.put( "EndX", this.getEndX() );
    jsonObject.put( "EndY", this.getEndY() );

    return jsonObject;
  }
}
