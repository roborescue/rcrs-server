package rescuecore2.standard.entities;

import static rescuecore2.misc.EncodingTools.readInt32;
import static rescuecore2.misc.EncodingTools.writeInt32;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import rescuecore2.worldmodel.Property;
import rescuecore2.worldmodel.AbstractProperty;
import rescuecore2.worldmodel.EntityID;

/**
   A property that defines a list of Edges.
 */
public class EdgeListProperty extends AbstractProperty {
    private List<Edge> edges;

    /**
       Construct a new EdgeListProperty with no defined value.
       @param urn The urn of this property.
    */
    public EdgeListProperty(String urn) {
        super(urn);
        edges = new ArrayList<Edge>();
    }

    /**
       Construct a new EdgeListProperty with no defined value.
       @param urn The urn of this property.
    */
    public EdgeListProperty(Enum<?> urn) {
        super(urn);
        edges = new ArrayList<Edge>();
    }

    /**
       Construct a new EdgeListProperty with no defined value.
       @param urn The urn of this property.
       @param edges The edge list.
    */
    public EdgeListProperty(String urn, List<Edge> edges) {
        super(urn, true);
        this.edges = new ArrayList<Edge>(edges);
    }

    /**
       Construct a new EdgeListProperty with no defined value.
       @param urn The urn of this property.
       @param edges The edge list.
    */
    public EdgeListProperty(Enum<?> urn, List<Edge> edges) {
        super(urn, true);
        this.edges = new ArrayList<Edge>(edges);
    }

    /**
       EdgeListProperty copy constructor.
       @param other The EdgeListProperty to copy.
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
       Set the list of edges. Future calls to {@link #isDefined()} will return true.
       @param newEdges The new edge list.
    */
    public void setEdges(List<Edge> newEdges) {
        edges.clear();
        edges.addAll(newEdges);
        setDefined();
    }

    /**
       Add an edge to the list.
       @param edge The edge to add.
     */
    public void addEdge(Edge edge) {
        edges.add(edge);
        setDefined();
    }

    /**
       Remove all edges from this list but keep it defined.
     */
    public void clearEdges() {
        edges.clear();
    }

    @Override
    public void takeValue(Property p) {
        if (p instanceof EdgeListProperty) {
            EdgeListProperty e = (EdgeListProperty)p;
            if (e.isDefined()) {
                setEdges(e.getValue());
            }
            else {
                undefine();
            }
        }
        else {
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
            }
            else {
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

    /*
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(getURN());
        if (isDefined()) {
            result.append(" = {");
            for (Iterator<Edge> it = ids.iterator(); it.hasNext();) {
                result.append(it.next());
                if (it.hasNext()) {
                    result.append(", ");
                }
            }
            result.append("}");
        }
        else {
            result.append(" (undefined)");
        }
        return result.toString();
    }
    */

    @Override
    public EdgeListProperty copy() {
        return new EdgeListProperty(this);
    }
}
