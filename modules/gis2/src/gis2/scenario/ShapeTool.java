package gis2.scenario;

import java.awt.Color;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import maps.gml.GMLBuilding;
import maps.gml.GMLCoordinates;
import maps.gml.GMLRoad;
import maps.gml.GMLShape;
import maps.gml.GMLSpace;
import maps.gml.view.FilledShapeDecorator;

/**
 * Abstract base class for tools that operate on GML shapes.
 */
public abstract class ShapeTool extends AbstractTool {
  private static final Color HIGHLIGHT_COLOUR = new Color(0, 0, 255, 128);

  private Listener listener;
  private FilledShapeDecorator highlight;

  private GMLShape highlightShape;

  /**
   * Construct a ShapeTool.
   *
   * @param editor The editor instance.
   */
  public ShapeTool(ScenarioEditor editor) {
    super(editor);
    listener = new Listener();
    highlight = new FilledShapeDecorator(HIGHLIGHT_COLOUR, HIGHLIGHT_COLOUR, HIGHLIGHT_COLOUR);
  }

  @Override
  public void activate() {
    editor.getViewer().addMouseListener(listener);
    editor.getViewer().addMouseMotionListener(listener);
    highlightShape = null;
  }

  @Override
  public void deactivate() {
    editor.getViewer().removeMouseListener(listener);
    editor.getViewer().removeMouseMotionListener(listener);
    editor.getViewer().clearAllBuildingDecorators();
    editor.getViewer().clearAllRoadDecorators();
    editor.getViewer().clearAllSpaceDecorators();
    editor.getViewer().repaint();
  }

  /**
   * Handle a click on a shape.
   *
   * @param shape The shape that was clicked.
   */
  protected abstract void processClick(GMLShape shape);

  /**
   * Find out if a shape should be highlighted or not. Only highlighted shapes can
   * be clicked.
   *
   * @param shape The shape to check.
   * @return True if the shape should be highlighted, false otherwise.
   */
  protected abstract boolean shouldHighlight(GMLShape shape);

  private void highlight(GMLShape newShape) {
    if (!shouldHighlight(newShape)) {
      return;
    }
    if (highlightShape == newShape) {
      return;
    }
    if (highlightShape != null) {
      if (highlightShape instanceof GMLBuilding) {
        editor.getViewer().clearBuildingDecorator((GMLBuilding) highlightShape);
      }
      if (highlightShape instanceof GMLRoad) {
        editor.getViewer().clearRoadDecorator((GMLRoad) highlightShape);
      }
      if (highlightShape instanceof GMLSpace) {
        editor.getViewer().clearSpaceDecorator((GMLSpace) highlightShape);
      }
    }
    highlightShape = newShape;
    if (highlightShape != null) {
      if (highlightShape instanceof GMLBuilding) {
        editor.getViewer().setBuildingDecorator(highlight, (GMLBuilding) highlightShape);
      }
      if (highlightShape instanceof GMLRoad) {
        editor.getViewer().setRoadDecorator(highlight, (GMLRoad) highlightShape);
      }
      if (highlightShape instanceof GMLSpace) {
        editor.getViewer().setSpaceDecorator(highlight, (GMLSpace) highlightShape);
      }
    }
    editor.getViewer().repaint();
  }

  private class Listener implements MouseListener, MouseMotionListener {
    @Override
    public void mouseClicked(MouseEvent e) {
      if (highlightShape != null && e.getButton() == MouseEvent.BUTTON1) {
        processClick(highlightShape);
      }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
      Point p = fixEventPoint(e.getPoint());
      GMLCoordinates c = editor.getViewer().getCoordinatesAtPoint(p.x, p.y);
      highlight(editor.getMap().findShapeUnder(c.getX(), c.getY()));
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    private Point fixEventPoint(Point p) {
      Insets insets = editor.getViewer().getInsets();
      return new Point(p.x - insets.left, p.y - insets.top);
    }
  }
}