package maps.gml.editor;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.Color;
import java.awt.Point;
import java.awt.Insets;

import javax.swing.undo.AbstractUndoableEdit;

import maps.gml.view.FilledShapeDecorator;
import maps.gml.GMLRoad;
import maps.gml.GMLBuilding;
import maps.gml.GMLSpace;
import maps.gml.GMLShape;
import maps.gml.GMLCoordinates;

/**
   A tool for deleting shapes.
*/
public class DeleteShapeTool extends AbstractTool {
    private static final Color HIGHLIGHT_COLOUR = Color.BLUE;

    private Listener listener;
    private FilledShapeDecorator highlight;

    private GMLShape shape;

    /**
       Construct a DeleteShapeTool.
       @param editor The editor instance.
    */
    public DeleteShapeTool(GMLEditor editor) {
        super(editor);
        listener = new Listener();
        highlight = new FilledShapeDecorator(HIGHLIGHT_COLOUR, HIGHLIGHT_COLOUR, HIGHLIGHT_COLOUR);
    }

    @Override
    public String getName() {
        return "Delete shape";
    }

    @Override
    public void activate() {
        editor.getViewer().addMouseListener(listener);
        editor.getViewer().addMouseMotionListener(listener);
        shape = null;
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

    private void highlightShape(GMLShape newShape) {
        if (shape == newShape) {
            return;
        }
        if (shape != null) {
            if (shape instanceof GMLBuilding) {
                editor.getViewer().clearBuildingDecorator((GMLBuilding)shape);
            }
            if (shape instanceof GMLRoad) {
                editor.getViewer().clearRoadDecorator((GMLRoad)shape);
            }
            if (shape instanceof GMLSpace) {
                editor.getViewer().clearSpaceDecorator((GMLSpace)shape);
            }
        }
        shape = newShape;
        if (shape != null) {
            if (shape instanceof GMLBuilding) {
                editor.getViewer().setBuildingDecorator(highlight, (GMLBuilding)shape);
            }
            if (shape instanceof GMLRoad) {
                editor.getViewer().setRoadDecorator(highlight, (GMLRoad)shape);
            }
            if (shape instanceof GMLSpace) {
                editor.getViewer().setSpaceDecorator(highlight, (GMLSpace)shape);
            }
        }
        editor.getViewer().repaint();
    }

    private class Listener implements MouseListener, MouseMotionListener {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (shape != null && e.getButton() == MouseEvent.BUTTON1) {
                editor.getMap().remove(shape);
                editor.getViewer().repaint();
                editor.setChanged();
                editor.addEdit(new DeleteShapeEdit(shape));
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            Point p = fixEventPoint(e.getPoint());
            GMLCoordinates c = editor.snap(editor.getViewer().getCoordinatesAtPoint(p.x, p.y));
            highlightShape(editor.getMap().findShapeUnder(c.getX(), c.getY()));
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

    private class DeleteShapeEdit extends AbstractUndoableEdit {
        private GMLShape shape;

        public DeleteShapeEdit(GMLShape shape) {
            this.shape = shape;
        }

        @Override
        public void undo() {
            super.undo();
            editor.getMap().add(shape);
            editor.getViewer().repaint();
        }

        @Override
        public void redo() {
            super.redo();
            editor.getMap().remove(shape);
            editor.getViewer().repaint();
        }
    }
}