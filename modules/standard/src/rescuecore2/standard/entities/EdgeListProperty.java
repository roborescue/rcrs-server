package rescuecore2.standard.entities;

import static rescuecore2.misc.EncodingTools.readInt32;
import static rescuecore2.misc.EncodingTools.writeInt32;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rescuecore2.URN;
import rescuecore2.messages.protobuf.RCRSProto.EdgeListProto;
import rescuecore2.messages.protobuf.RCRSProto.EdgeProto;
import rescuecore2.messages.protobuf.RCRSProto.PropertyProto;
import rescuecore2.worldmodel.AbstractProperty;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Property;

/**
 * A property that defines a list of Edges.
 */
public class EdgeListProperty extends AbstractProperty {

  private List<Edge> edges;

  /**
   * Construct a new EdgeListProperty with no defined value.
   *
   * @param urn The urn of this property.
   */
  public EdgeListProperty(int urn) {
    super(urn);
    edges = new ArrayList<Edge>();
  }

  /**
   * Construct a new EdgeListProperty with no defined value.
   *
   * @param urn The urn of this property.
   */
  public EdgeListProperty(URN urn) {
    super(urn);
    edges = new ArrayList<Edge>();
  }

  /**
   * Construct a new EdgeListProperty with no defined value.
   *
   * @param urn   The urn of this property.
   * @param edges The edge list.
   */
  public EdgeListProperty(int urn, List<Edge> edges) {
    super(urn, true);
    this.edges = new ArrayList<Edge>(edges);
  }

  /**
   * Construct a new EdgeListProperty with no defined value.
   *
   * @param urn   The urn of this property.
   * @param edges The edge list.
   */
  public EdgeListProperty(URN urn, List<Edge> edges) {
    super(urn, true);
    this.edges = new ArrayList<Edge>(edges);
  }

  /**
   * EdgeListProperty copy constructor.
   *
   * @param other The EdgeListProperty to copy.
   */
  public EdgeListProperty(EdgeListProperty other) {
    super(other);
    this.edges = new ArrayList<Edge>(other.edges);
  }

  @Override
  public List<Edge> getValue() {
    if (!isDefined()) {
      return null;
    }
    return Collections.unmodifiableList(edges);
  }

  /**
   * Set the list of edges. Future calls to {@link #isDefined()} will return true.
   *
   * @param newEdges The new edge list.
   */
  public void setEdges(List<Edge> newEdges) {
    edges.clear();
    edges.addAll(newEdges);
    setDefined();
  }

  /**
   * Add an edge to the list.
   *
   * @param edge The edge to add.
   */
  public void addEdge(Edge edge) {
    edges.add(edge);
    setDefined();
  }

  /**
   * Remove all edges from this list but keep it defined.
   */
  public void clearEdges() {
    edges.clear();
  }

  @Override
  public void takeValue(Property p) {
    if (p instanceof EdgeListProperty) {
      EdgeListProperty e = (EdgeListProperty) p;
      if (e.isDefined()) {
        setEdges(e.getValue());
      } else {
        undefine();
      }
    } else {
      throw new IllegalArgumentException(this + " cannot take value from " + p);
    }
  }

  @Override
  public void write(OutputStream out) throws IOException {
    writeInt32(edges.size(), out);
    for (Edge next : edges) {
      writeInt32(next.getStartX(), out);
      writeInt32(next.getStartY(), out);
      writeInt32(next.getEndX(), out);
      writeInt32(next.getEndY(), out);
      if (next.isPassable()) {
        writeInt32(next.getNeighbour().getValue(), out);
      } else {
        writeInt32(0, out);
      }
    }
  }

  @Override
  public void read(InputStream in) throws IOException {
    int count = readInt32(in);
    edges.clear();
    for (int i = 0; i < count; ++i) {
      int startX = readInt32(in);
      int startY = readInt32(in);
      int endX = readInt32(in);
      int endY = readInt32(in);
      EntityID neighbour = null;
      int id = readInt32(in);
      if (id != 0) {
        neighbour = new EntityID(id);
      }
      edges.add(new Edge(startX, startY, endX, endY, neighbour));
    }
    setDefined();
  }

  @Override
  public EdgeListProperty copy() {
    return new EdgeListProperty(this);
  }

  @Override
  public PropertyProto toPropertyProto() {
    PropertyProto.Builder builder = basePropertyProto();
    if (isDefined()) {
      EdgeListProto.Builder edgeListbuilder = EdgeListProto.newBuilder();
      for (Edge next : edges) {
        int neighbour = 0;
        if (next.isPassable()) {
          neighbour = next.getNeighbour().getValue();
        }
        edgeListbuilder.addEdges(EdgeProto.newBuilder().setStartX(next.getStartX()).setStartY(next.getStartY())
            .setEndX(next.getEndX()).setEndY(next.getEndY()).setNeighbour(neighbour));
      }
      builder.setEdgeList(edgeListbuilder);
    }
    return builder.build();
  }

  @Override
  public void fromPropertyProto(PropertyProto proto) {
    if (!proto.getDefined())
      return;
    edges.clear();
    List<EdgeProto> edgesProto = proto.getEdgeList().getEdgesList();
    for (EdgeProto edgeProto : edgesProto) {
      int startX = edgeProto.getStartX();
      int startY = edgeProto.getStartY();
      int endX = edgeProto.getEndX();
      int endY = edgeProto.getEndY();
      EntityID neighbour = null;
      int id = edgeProto.getNeighbour();
      if (id != 0) {
        neighbour = new EntityID(id);
      }
      edges.add(new Edge(startX, startY, endX, endY, neighbour));

    }
    setDefined();
  }
}