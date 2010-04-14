package maps.gml.editor;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.Color;
import java.awt.Point;
import java.awt.Insets;

import maps.gml.view.NodeDecorator;
import maps.gml.view.SquareNodeDecorator;
import maps.gml.GMLNode;
import maps.gml.GMLCoordinates;

import javax.swing.undo.AbstractUndoableEdit;

/**
   A tool for moving nodes.
*/
public class MoveNodeTool extends AbstractTool {
    private static final Color HIGHLIGHT_COLOUR = Color.BLACK;
    private static final int HIGHLIGHT_SIZE = 6;

    private Listener listener;
    private NodeDecorator highlight;
    private GMLNode selected;
    private GMLCoordinates pressCoords;
    private GMLCoordinates originalCoords;

    /**
       Construct a MoveNodeTool.
       @param editor The editor instance.
    */
    public MoveNodeTool(GMLEditor editor) {
        super(editor);
        listener = new Listener();
        highlight = new SquareNodeDecorator(HIGHLIGHT_COLOUR, HIGHLIGHT_SIZE);
        selected = null;
    }

    @Override
    public String getName() {
        return "Move node";
    }

    @Override
    public void activate() {
        editor.getViewer().addMouseListener(listener);
        editor.getViewer().addMouseMotionListener(listener);
        selected = null;
    }

    @Override
    public void deactivate() {
        editor.getViewer().removeMouseListener(listener);
        editor.getViewer().removeMouseMotionListener(listener);
        editor.getViewer().clearAllNodeDecorators();
        editor.getViewer().repaint();
    }

    private void highlightNode(GMLNode node) {
        if (selected == node) {
            return;
        }
        if (selected != null) {
            editor.getViewer().clearNodeDecorator(selected);
        }
        selected = node;
        if (selected != null) {
            editor.getViewer().setNodeDecorator(highlight, selected);
        }
        editor.getViewer().repaint();
    }

    private class Listener implements MouseListener, MouseMotionListener {
        @Override
        public void mouseMoved(MouseEvent e) {
            Point p = fixEventPoint(e.getPoint());
            GMLCoordinates c = editor.getViewer().getCoordinatesAtPoint(p.x, p.y);
            GMLNode node = editor.getMap().findNearestNode(c.getX(), c.getY());
            highlightNode(node);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (selected == null) {
                return;
            }
            if (e.getButton() == MouseEvent.BUTTON1) {
                Point p = fixEventPoint(e.getPoint());
                pressCoords = editor.getViewer().getCoordinatesAtPoint(p.x, p.y);
                originalCoords = new GMLCoordinates(selected.getCoordinates());
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            pressCoords = null;
            editor.addEdit(new MoveNodeEdit(selected, originalCoords, new GMLCoordinates(selected.getCoordinates())));
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (selected == null) {
                return;
            }
            if (pressCoords == null) {
                return;
            }
            Point p = fixEventPoint(e.getPoint());
            GMLCoordinates dragCoords = editor.getViewer().getCoordinatesAtPoint(p.x, p.y);
            double dx = dragCoords.getX() - pressCoords.getX();
            double dy = dragCoords.getY() - pressCoords.getY();
            GMLCoordinates result = new GMLCoordinates(originalCoords.getX() + dx, originalCoords.getY() + dy);
            editor.snap(result);
            selected.setCoordinates(result);
            editor.setChanged();
            editor.getViewer().repaint();
        }

        @Override
        public void mouseClicked(MouseEvent e) {
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

    private class MoveNodeEdit extends AbstractUndoableEdit {
        private GMLNode node;
        private GMLCoordinates oldPosition;
        private GMLCoordinates newPosition;

        public MoveNodeEdit(GMLNode node, GMLCoordinates oldPosition, GMLCoordinates newPosition) {
            this.node = node;
            this.oldPosition = oldPosition;
            this.newPosition = newPosition;
        }

        @Override
        public void undo() {
            super.undo();
            node.setCoordinates(oldPosition);
            editor.getViewer().repaint();
        }

        @Override
        public void redo() {
            super.redo();
            node.setCoordinates(newPosition);
            editor.getViewer().repaint();
        }
    }
}