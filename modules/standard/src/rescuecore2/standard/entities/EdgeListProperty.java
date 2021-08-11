package rescuecore2.standard.entities;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import rescuecore2.worldmodel.AbstractProperty;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Property;

/**
 * A property that defines a list of Edges.
 */
public class EdgeListProperty extends AbstractProperty<List<Edge>> {

  public static final int EDGES = 0;

  private List<Edge>      edges;


  /**
   * Construct a new EdgeListProperty with no defined value.
   *
   * @param urn
   *          The urn of this property.
   */
  public EdgeListProperty( String urn ) {
    super( urn );
    this.edges = new ArrayList<Edge>();
  }


  /**
   * Construct a new EdgeListProperty with no defined value.
   *
   * @param urn
   *          The urn of this property.
   */
  public EdgeListProperty( Enum<?> urn ) {
    super( urn );
    this.edges = new ArrayList<Edge>();
  }


  /**
   * Construct a new EdgeListProperty with no defined value.
   *
   * @param urn
   *          The urn of this property.
   * @param edges
   *          The edge list.
   */
  public EdgeListProperty( String urn, List<Edge> edges ) {
    super( urn, true );
    this.edges = new ArrayList<Edge>( edges );
  }


  /**
   * Construct a new EdgeListProperty with no defined value.
   *
   * @param urn
   *          The urn of this property.
   * @param edges
   *          The edge list.
   */
  public EdgeListProperty( Enum<?> urn, List<Edge> edges ) {
    super( urn, true );
    this.edges = new ArrayList<Edge>( edges );
  }


  /**
   * EdgeListProperty copy constructor.
   *
   * @param other
   *          The EdgeListProperty to copy.
   */
  public EdgeListProperty( EdgeListProperty other ) {
    super( other );
    this.edges = new ArrayList<Edge>( other.edges );
  }


  @Override
  public List<Edge> getValue() {
    if ( !isDefined() ) {
      return null;
    }
    return Collections.unmodifiableList( this.edges );
  }


  /**
   * Set the list of edges. Future calls to {@link #isDefined()} will return
   * true.
   *
   * @param newEdges
   *          The new edge list.
   */
  public void setEdges( List<Edge> newEdges ) {
    this.edges.clear();
    this.edges.addAll( newEdges );
    this.setDefined();
  }


  /**
   * Add an edge to the list.
   *
   * @param edge
   *          The edge to add.
   */
  public void addEdge( Edge edge ) {
    this.edges.add( edge );
    this.setDefined();
  }


  /**
   * Remove all edges from this list but keep it defined.
   */
  public void clearEdges() {
    this.edges.clear();
  }


  @Override
  public void takeValue( Property<?> p ) {
    if ( p instanceof EdgeListProperty ) {
      EdgeListProperty e = (EdgeListProperty) p;
      if ( e.isDefined() ) {
        this.setEdges( e.getValue() );
      } else {
        this.undefine();
      }
    } else {
      throw new IllegalArgumentException(
          this + " cannot take value from " + p );
    }
  }


  @Override
  public EdgeListProperty copy() {
    return new EdgeListProperty( this );
  }


  @Override
  public void setFields( List<Object> fields ) {
    this.edges = this.convertToValue( fields );
    this.setDefined();
  }


  @Override
  public List<Object> getFields() {
    List<Object> fields = new ArrayList<Object>();

    int[][] edges = new int[this.edges.size()][5];
    for ( int i = 0; i < this.edges.size(); i++ ) {
      edges[i][0] = this.edges.get( i ).getStartX();
      edges[i][1] = this.edges.get( i ).getStartY();
      edges[i][2] = this.edges.get( i ).getEndX();
      edges[i][3] = this.edges.get( i ).getEndY();

      if ( this.edges.get( i ).getNeighbour() != null ) {
        edges[i][4] = this.edges.get( i ).getNeighbour().getValue();
      } else {
        edges[i][4] = -1;
      }
    }

    fields.add( EdgeListProperty.EDGES, edges );

    return fields;
  }


  @Override
  public List<Edge> convertToValue( List<Object> fields ) {
    List<Edge> values = new ArrayList<Edge>();

    int[][] edges = (int[][]) fields.get( EdgeListProperty.EDGES );
    for ( int i = 0; i < edges.length; i++ ) {
      Edge edge;
      if ( edges[i][4] == -1 ) {
        edge = new Edge( edges[i][0], edges[i][1], edges[i][2], edges[i][3] );
      } else {
        edge = new Edge( edges[i][0], edges[i][1], edges[i][2], edges[i][3],
            new EntityID( edges[i][4] ) );
      }

      values.add( edge );
    }
    return values;
  }
}