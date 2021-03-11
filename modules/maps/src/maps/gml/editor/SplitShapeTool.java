package maps.gml.editor;

import java.awt.Color;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;

import maps.gml.GMLBuilding;
import maps.gml.GMLCoordinates;
import maps.gml.GMLEdge;
import maps.gml.GMLNode;
import maps.gml.GMLRoad;
import maps.gml.GMLShape;
import maps.gml.GMLSpace;
import maps.gml.view.LineOverlay;
import maps.gml.view.NodeDecorator;
import maps.gml.view.SquareNodeDecorator;
import rescuecore2.log.Logger;
import rescuecore2.misc.Pair;
import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Point2D;

/**
 * A tool for creating edges.
 */
public class SplitShapeTool extends AbstractTool {

  private static final Color  HIGHLIGHT_COLOUR = Color.BLUE;
  private static final int    HIGHLIGHT_SIZE   = 6;

  private static final double THRESHOLD        = 0.001;

  private Listener            listener;
  private NodeDecorator       nodeHighlight;
  private LineOverlay         overlay;

  private GMLNode             hover;
  private GMLNode             start;
  private GMLNode             end;
  // private GMLEdge edge;


  /**
   * Construct a CreateEdgeTool.
   *
   * @param editor
   *          The editor instance.
   */
  public SplitShapeTool( GMLEditor editor ) {
    super( editor );
    listener = new Listener();
    nodeHighlight = new SquareNodeDecorator( HIGHLIGHT_COLOUR, HIGHLIGHT_SIZE );
    overlay = new LineOverlay( HIGHLIGHT_COLOUR, true );
  }


  @Override
  public String getName() {
    return "Split shape";
  }


  @Override
  public void activate() {
    editor.getViewer().addMouseListener( listener );
    editor.getViewer().addMouseMotionListener( listener );
    editor.getViewer().addOverlay( overlay );
    hover = null;
    start = null;
    end = null;
    // edge = null;
  }


  @Override
  public void deactivate() {
    editor.getViewer().removeMouseListener( listener );
    editor.getViewer().removeMouseMotionListener( listener );
    editor.getViewer().clearAllNodeDecorators();
    editor.getViewer().removeOverlay( overlay );
    editor.getViewer().repaint();
  }


  private void setHover( GMLNode node ) {
    if ( hover == node ) {
      return;
    }
    if ( hover != null ) {
      editor.getViewer().clearNodeDecorator( hover );
    }
    hover = node;
    if ( hover != null ) {
      editor.getViewer().setNodeDecorator( nodeHighlight, hover );
    }
    editor.getViewer().repaint();
  }


  private void setStart( GMLNode node ) {
    if ( start == node ) {
      return;
    }
    if ( start != null ) {
      editor.getViewer().clearNodeDecorator( start );
    }
    start = node;
    if ( start != null ) {
      editor.getViewer().setNodeDecorator( nodeHighlight, start );
    }
    editor.getViewer().repaint();
  }


  private void setEnd( GMLNode node ) {
    if ( start == node || end == node ) {
      return;
    }
    if ( end != null ) {
      editor.getViewer().clearNodeDecorator( end );
    }
    end = node;
    if ( end != null ) {
      editor.getViewer().setNodeDecorator( nodeHighlight, end );
    }
    editor.getViewer().repaint();
  }


  private class Listener implements MouseListener, MouseMotionListener {

    @Override
    public void mousePressed( MouseEvent e ) {
      if ( e.getButton() == MouseEvent.BUTTON1 ) {
        Point p = fixEventPoint( e.getPoint() );
        GMLCoordinates c = editor.getViewer().getCoordinatesAtPoint( p.x, p.y );
        GMLNode node = editor.getMap().findNearestNode( c.getX(), c.getY() );
        overlay.setStart( new Point2D( node.getX(), node.getY() ) );
        setStart( node );
        setHover( null );
      }
    }


    @Override
    public void mouseReleased( MouseEvent e ) {
      if ( e.getButton() == MouseEvent.BUTTON1 ) {
        if ( start != null && end != null ) {
          SplitShapeEdit edit = splitByEdge();
          editor.setChanged();
          if ( edit != null ) {
            editor.addEdit( edit );
          }
          editor.getViewer().clearAllNodeDecorators();
          overlay.setStart( null );
          overlay.setEnd( null );
          editor.getViewer().repaint();
          start = null;
          end = null;
          hover = null;
        }
      }
    }


    private SplitShapeEdit splitByEdge() {
      Collection<GMLShape> add = new ArrayList<GMLShape>();
      Collection<GMLShape> delete = new ArrayList<GMLShape>();

      GMLEdge edge = editor.getMap().createEdge( start, end );
      Collection<GMLEdge> startEdges = editor.getMap()
          .getAttachedEdges( start );
      Collection<GMLEdge> endEdges = editor.getMap().getAttachedEdges( end );
      Collection<GMLShape> startShapes = new HashSet<GMLShape>();
      Collection<GMLShape> endShapes = new HashSet<GMLShape>();
      for ( GMLEdge next : startEdges ) {
        startShapes.addAll( editor.getMap().getAttachedShapes( next ) );
      }
      for ( GMLEdge next : endEdges ) {
        endShapes.addAll( editor.getMap().getAttachedShapes( next ) );
      }
      for ( GMLShape shape : startShapes ) {
        if ( endShapes.contains( shape ) ) {
          Pair<GMLShape, GMLShape> split = splitShape( shape, edge );
          if ( split != null ) {
            add.add( split.first() );
            add.add( split.second() );
            delete.add( shape );
          }
        }
      }
      if ( !add.isEmpty() ) {
        edge.setPassable( true );
        return new SplitShapeEdit( edge, add, delete );
      } else {
        editor.getMap().remove( edge );
        return null;
      }
    }


    private Pair<GMLShape, GMLShape> splitShape( GMLShape shape,
        GMLEdge edge ) {
      List<GMLNode> nodes1 = new ArrayList<GMLNode>();
      List<GMLNode> nodes2 = new ArrayList<GMLNode>();
      boolean first = true;
      for ( GMLNode n : shape.getUnderlyingNodes() ) {
        if ( n == edge.getStart() || n == edge.getEnd() ) {
          first = !first;
          nodes1.add( n );
          nodes2.add( n );
        } else if ( first ) {
          nodes1.add( n );
        } else {
          nodes2.add( n );
        }
      }
      if ( nodes1.size() <= 2 || nodes2.size() <= 2 ) {
        return null;
      }

      // Check if we really split an interior edge
      double oldArea = area( shape.getUnderlyingNodes() );
      double area1 = area( nodes1 );
      double area2 = area( nodes2 );
      if ( area1 + area2 > oldArea + THRESHOLD ) {
        return null;
      }

      GMLShape s1 = null;
      GMLShape s2 = null;
      if ( shape instanceof GMLBuilding ) {
        GMLBuilding b = (GMLBuilding) shape;
        GMLBuilding b1 = editor.getMap().createBuildingFromNodes( nodes1 );
        GMLBuilding b2 = editor.getMap().createBuildingFromNodes( nodes2 );
        b1.setCode( b.getCode() );
        b2.setCode( b.getCode() );
        b1.setFloors( b.getFloors() );
        b2.setFloors( b.getFloors() );
        b1.setImportance( b.getImportance() );
        b2.setImportance( b.getImportance() );
        b1.setCapacity( b.getCapacity() );
        b2.setCapacity( b.getCapacity() );
        s1 = b1;
        s2 = b2;
      } else if ( shape instanceof GMLRoad ) {
        // GMLBuilding b = (GMLBuilding) shape;
        s1 = editor.getMap().createRoadFromNodes( nodes1 );
        s2 = editor.getMap().createRoadFromNodes( nodes2 );
      } else if ( shape instanceof GMLSpace ) {
        // GMLBuilding b = (GMLBuilding) shape;
        s1 = editor.getMap().createSpaceFromNodes( nodes1 );
        s2 = editor.getMap().createSpaceFromNodes( nodes2 );
      } else {
        throw new IllegalArgumentException(
            "Shape is not a building, road or space" );
      }
      editor.getMap().remove( shape );
      return new Pair<GMLShape, GMLShape>( s1, s2 );
    }


    private double area( List<GMLNode> nodes ) {
      List<Point2D> vertices = new ArrayList<Point2D>();
      for ( GMLNode n : nodes ) {
        vertices.add( new Point2D( n.getX(), n.getY() ) );
      }
      return GeometryTools2D.computeArea( vertices );
    }


    @Override
    public void mouseDragged( MouseEvent e ) {
      if ( start != null ) {
        Point p = fixEventPoint( e.getPoint() );
        GMLCoordinates c = editor.getViewer().getCoordinatesAtPoint( p.x, p.y );
        GMLNode node = editor.getMap().findNearestNode( c.getX(), c.getY() );
        overlay.setEnd( new Point2D( node.getX(), node.getY() ) );
        setEnd( node );
      }
    }


    @Override
    public void mouseMoved( MouseEvent e ) {
      Point p = fixEventPoint( e.getPoint() );
      GMLCoordinates c = editor
          .snap( editor.getViewer().getCoordinatesAtPoint( p.x, p.y ) );
      GMLNode node = editor.getMap().findNearestNode( c.getX(), c.getY() );
      setHover( node );
    }


    @Override
    public void mouseClicked( MouseEvent e ) {
    }


    @Override
    public void mouseEntered( MouseEvent e ) {
    }


    @Override
    public void mouseExited( MouseEvent e ) {
    }


    private Point fixEventPoint( Point p ) {
      Insets insets = editor.getViewer().getInsets();
      return new Point( p.x - insets.left, p.y - insets.top );
    }
  }

  private class SplitShapeEdit extends AbstractUndoableEdit {

    private Collection<GMLShape> add;
    private Collection<GMLShape> remove;
    private GMLEdge              edge;


    public SplitShapeEdit( GMLEdge edge, Collection<GMLShape> add, Collection<GMLShape> remove ) {
      this.edge = edge;
      this.add = add;
      this.remove = remove;
    }


    @Override
    public void undo() {
      super.undo();
      editor.getMap().removeEdge( edge );
      editor.getMap().remove( add );
      editor.getMap().add( remove );
      editor.getViewer().repaint();
    }


    @Override
    public void redo() {
      super.redo();
      editor.getMap().addEdge( edge );
      for ( GMLShape r : remove ) {
        Logger.debug( "remove: " + r.toString() );
      }
      for ( GMLShape r : add ) {
        Logger.debug( "add: " + r.toString() );
      }
      editor.getMap().remove( remove );
      editor.getMap().add( add );
      editor.getViewer().repaint();
    }
  }
}