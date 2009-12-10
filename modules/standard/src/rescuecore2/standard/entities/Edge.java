package rescuecore2.standard.entities;

import rescuecore2.worldmodel.EntityID;
import java.awt.Point;

/**
   An edge is a line segment with an optional neighbouring entity. Edges without neighbours are impassable, edges with neighbours are passable.
 */
public class Edge {
    private int startX;
    private int startY;
    private int endX;
    private int endY;
    private EntityID neighbour;

    /**
       Constuct an impassable Edge.
       @param startX The X coordinate of the first endpoint.
       @param startY The Y coordinate of the first endpoint.
       @param endX The X coordinate of the second endpoint.
       @param endY The Y coordinate of the second endpoint.
     */
    public Edge(int startX, int startY, int endX, int endY) {
        this(startX, startY, endX, endY, null);
    }

    /**
       Constuct an Edge. If the neighbour is null then this edge is impassable; if it is non-null then this edge is passable.
       @param startX The X coordinate of the first endpoint.
       @param startY The Y coordinate of the first endpoint.
       @param endX The X coordinate of the second endpoint.
       @param endY The Y coordinate of the second endpoint.
       @param neighbour The ID of the neighbour on the other side of this edge. This may be null to indicate an impassable edge.
     */
    public Edge(int startX, int startY, int endX, int endY, EntityID neighbour) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.neighbour = neighbour;
    }

    /**
       Get the X coordinate of the first endpoint.
       @return The X coordinate of the first endpoint.
    */
    public int getStartX() {
        return startX;
    }

    /**
       Get the Y coordinate of the first endpoint.
       @return The Y coordinate of the first endpoint.
    */
    public int getStartY() {
        return startY;
    }

    /**
       Get the X coordinate of the second endpoint.
       @return The X coordinate of the second endpoint.
    */
    public int getEndX() {
        return endX;
    }

    /**
       Get the Y coordinate of the second endpoint.
       @return The Y coordinate of the second endpoint.
    */
    public int getEndY() {
        return endY;
    }

    /**
       Get the ID of the neighbour.
       @return The ID of the neighbour or null if this edge is impassable.
    */
    public EntityID getNeighbour() {
        return neighbour;
    }

    /**
       Find out if this edge is passable or not.
       @return True iff the neighbour is non-null.
    */
    public boolean isPassable() {
        return neighbour != null;
    }

    /**
       Get the start point.
       @return The start point.
    */
    public Point getStartPoint() {
        return new Point(startX, startY);
    }

    /**
       Get the end point.
       @return The end point.
    */
    public Point getEndPoint() {
        return new Point(endX, endY);
    }

    @Override
    public String toString() {
        return "Edge from " + startX + ", " + startY + " to " + endX + ", " + endY + " (" + (neighbour == null ? "impassable" : "neighbour: " + neighbour) + ")";
    }
}