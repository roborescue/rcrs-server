package rescuecore2.standard.entities;

import java.awt.Polygon;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import rescuecore2.misc.Pair;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.EntityListener;
import rescuecore2.worldmodel.Property;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.properties.EntityRefListProperty;
import rescuecore2.worldmodel.properties.IntProperty;

/**
 * The Area object.
 */
public abstract class Area extends StandardEntity {

  private IntProperty x;
  private IntProperty y;
  private EdgeListProperty edges;
  private EntityRefListProperty blockades;

  private Shape shape;
  private int[] apexList;
  private List<EntityID> neighbours;

  /**
   * Construct a subclass of Area with entirely undefined property values.
   *
   * @param id The ID of this entity.
   */
  protected Area(EntityID id) {
    super(id);
    this.x = new IntProperty(StandardPropertyURN.X);
    this.y = new IntProperty(StandardPropertyURN.Y);
    this.edges = new EdgeListProperty(StandardPropertyURN.EDGES);
    this.blockades = new EntityRefListProperty(StandardPropertyURN.BLOCKADES);
    registerProperties(this.x, this.y, this.edges, this.blockades);
    this.shape = null;
    this.apexList = null;
    this.neighbours = null;
    addEntityListener(new EdgesListener());
  }

  /**
   * Area copy constructor.
   *
   * @param other The Area to copy.
   */
  protected Area(Area other) {
    super(other);
    this.x = new IntProperty(other.x);
    this.y = new IntProperty(other.y);
    this.edges = new EdgeListProperty(other.edges);
    this.blockades = new EntityRefListProperty(other.blockades);
    registerProperties(this.x, this.y, this.edges, this.blockades);
    this.shape = null;
    this.apexList = null;
    this.neighbours = null;
    addEntityListener(new EdgesListener());
  }

  @Override
  public Pair<Integer, Integer> getLocation(WorldModel<? extends StandardEntity> world) {
    return new Pair<Integer, Integer>(this.x.getValue(), this.y.getValue());
  }

  @Override
  public Property<?> getProperty(String urn) {
    StandardPropertyURN type;
    try {
      type = StandardPropertyURN.fromString(urn);
    } catch (IllegalArgumentException e) {
      return super.getProperty(urn);
    }
    switch (type) {
      case X:
        return this.x;
      case Y:
        return this.y;
      case EDGES:
        return this.edges;
      case BLOCKADES:
        return this.blockades;
      default:
        return super.getProperty(urn);
    }
  }

  /**
   * Get the X property.
   *
   * @return The X property.
   */
  public IntProperty getXProperty() {
    return this.x;
  }

  /**
   * Get the X coordinate.
   *
   * @return The X coordinate.
   */
  public int getX() {
    return this.x.getValue();
  }

  /**
   * Set the X coordinate.
   *
   * @param x The new X coordinate.
   */
  public void setX(int x) {
    this.x.setValue(x);
  }

  /**
   * Find out if the X property has been defined.
   *
   * @return True if the X property has been defined, false otherwise.
   */
  public boolean isXDefined() {
    return this.x.isDefined();
  }

  /**
   * Undefine the X property.
   */
  public void undefineX() {
    this.x.undefine();
  }

  /**
   * Get the Y property.
   *
   * @return The Y property.
   */
  public IntProperty getYProperty() {
    return this.y;
  }

  /**
   * Get the Y coordinate.
   *
   * @return The Y coordinate.
   */
  public int getY() {
    return this.y.getValue();
  }

  /**
   * Set the Y coordinate.
   *
   * @param y The new y coordinate.
   */
  public void setY(int y) {
    this.y.setValue(y);
  }

  /**
   * Find out if the Y property has been defined.
   *
   * @return True if the Y property has been defined, false otherwise.
   */
  public boolean isYDefined() {
    return this.y.isDefined();
  }

  /**
   * Undefine the Y property.
   */
  public void undefineY() {
    this.y.undefine();
  }

  /**
   * Get the edges property.
   *
   * @return The edges property.
   */
  public EdgeListProperty getEdgesProperty() {
    return this.edges;
  }

  /**
   * Get the edges of this area.
   *
   * @return The edges.
   */
  public List<Edge> getEdges() {
    return this.edges.getValue();
  }

  /**
   * Set the edges.
   *
   * @param edges The new edges.
   */
  public void setEdges(List<Edge> edges) {
    this.edges.setEdges(edges);
  }

  /**
   * Find out if the edges property has been defined.
   *
   * @return True if the edges property has been defined, false otherwise.
   */
  public boolean isEdgesDefined() {
    return this.edges.isDefined();
  }

  /**
   * Undefine the edges property.
   */
  public void undefineEdges() {
    this.edges.undefine();
  }

  /**
   * Get the blockades property.
   *
   * @return The blockades property.
   */
  public EntityRefListProperty getBlockadesProperty() {
    return this.blockades;
  }

  /**
   * Get the blockades in this area.
   *
   * @return The blockades.
   */
  public List<EntityID> getBlockades() {
    return this.blockades.getValue();
  }

  /**
   * Set the blockades in this area.
   *
   * @param blockades The new blockades.
   */
  public void setBlockades(List<EntityID> blockades) {
    this.blockades.setValue(blockades);
  }

  /**
   * Find out if the blockades property has been defined.
   *
   * @return True if the blockades property has been defined, false otherwise.
   */
  public boolean isBlockadesDefined() {
    return this.blockades.isDefined();
  }

  /**
   * Undefine the blockades property.
   */
  public void undefineBlockades() {
    this.blockades.undefine();
  }

  /**
   * Get the neighbours of this area.
   *
   * @return The neighbours.
   */
  public List<EntityID> getNeighbours() {
    if (this.neighbours == null) {
      this.neighbours = new ArrayList<EntityID>();
      for (Edge next : edges.getValue()) {
        if (next.isPassable()) {
          this.neighbours.add(next.getNeighbour());
        }
      }
    }
    return this.neighbours;
  }

  /**
   * Get the edge that crosses to a particular neighbour.
   *
   * @param neighbour The neighbour ID.
   * @return The edge that crosses to the given neighbour, or null if no edges
   *         border that neighbour.
   */
  public Edge getEdgeTo(EntityID neighbour) {
    for (Edge next : getEdges()) {
      if (neighbour.equals(next.getNeighbour())) {
        return next;
      }
    }
    return null;
  }

  /**
   * Get the list of apexes for this area.
   *
   * @return The list of apexes.
   */
  public int[] getApexList() {
    if (this.apexList == null) {
      List<Edge> e = getEdges();
      if (e == null) {
        System.out.println("NULL");
      }
      this.apexList = new int[e.size() * 2];
      int i = 0;
      for (Edge next : e) {
        this.apexList[i++] = next.getStartX();
        this.apexList[i++] = next.getStartY();
      }
    }
    return this.apexList;
  }

  /**
   * Get this area as a Java Shape object.
   *
   * @return A Shape describing this area.
   */
  public Shape getShape() {
    if (this.shape == null) {
      int[] apexes = getApexList();
      int count = apexes.length / 2;
      int[] xs = new int[count];
      int[] ys = new int[count];
      for (int i = 0; i < count; ++i) {
        xs[i] = apexes[i * 2];
        ys[i] = apexes[i * 2 + 1];
      }
      this.shape = new Polygon(xs, ys, count);
    }
    return this.shape;
  }

  private class EdgesListener implements EntityListener {

    @Override
    public void propertyChanged(Entity e, Property<?> p, Object oldValue, Object newValue) {
      if (p == edges) {
        shape = null;
        apexList = null;
        neighbours = null;
      }
    }
  }

  @Override
  public JSONObject toJson() {
    JSONObject jsonObject = super.toJson();
    jsonObject.put(StandardPropertyURN.APEXES.toString(), this.isEdgesDefined() ? this.getApexList() : JSONObject.NULL);

    return jsonObject;
  }
}