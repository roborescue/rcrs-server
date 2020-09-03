package maps.gml;

import java.util.List;

public class GMLRefuge extends GMLBuilding {

  private int bedCapacity;
  private int refillCapacity;


  /**
   * Construct a GMLRefuge.
   *
   * @param id
   *          The ID of the Refuge.
   */
  public GMLRefuge( int id ) {
    super( id );
  }


  /**
   * Construct a GMLRefuge.
   *
   * @param id
   *          The ID of the Refuge.
   * @param edges
   *          The edges of the Refuge.
   */
  public GMLRefuge( int id, List<GMLDirectedEdge> edges ) {
    super( id, edges );
  }


  /**
   * Construct a GMLRefuge.
   *
   * @param id
   *          The ID of the Refuge.
   * @param edges
   *          The edges of the Refuge.
   * @param neighbours
   *          The neighbours of each edge.
   */
  public GMLRefuge( int id, List<GMLDirectedEdge> edges, List<Integer> neighbours ) {
    super( id, edges, neighbours );
  }


  @Override
  public String toString() {
    return "GMLRefuge " + getID();
  }


  /**
   * Set the bed capacity of this Refuge.
   *
   * @param newCapacity
   *          The new bed capacity of the Refuge.
   */
  public void setBedCapacity( int newCapacity ) {
    bedCapacity = newCapacity;
  }


  /**
   * Get the bed capacity of this Refuge.
   *
   * @return The the bed capacity of Refuge.
   */
  public int getBedCapacity() {
    return bedCapacity;
  }


  /**
   * Set the refill capacity of this Refuge.
   *
   * @param newCapacity
   *          The new refill capacity of the Refuge.
   */
  public void setRefillCapacity( int newCapacity ) {
    refillCapacity = newCapacity;
  }


  /**
   * Get the refill capacity of this Refuge.
   *
   * @return The the refill capacity of Refuge.
   */
  public int getRefillCapacity() {
    return refillCapacity;
  }

}
