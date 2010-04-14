package maps.gml.editor;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.Color;
import java.awt.Point;
import java.awt.Insets;

import javax.swing.undo.AbstractUndoableEdit;

import maps.gml.view.NodeDecorator;
import maps.gml.view.EdgeDecorator;
import maps.gml.view.SquareNodeDecorator;
import maps.gml.view.LineEdgeDecorator;
import maps.gml.GMLNode;
import maps.gml.GMLEdge;
import maps.gml.GMLCoordinates;

/**
   A tool for creating edges.
*/
public class CreateEdgeTool extends AbstractTool {
    private static final Color HIGHLIGHT_COLOUR = Color.BLUE;
    private static final int HIGHLIGHT_SIZE = 6;

    private Listener listener;
    private NodeDecorator nodeHighlight;
    private EdgeDecorator edgeHighlight;

    private GMLNode hover;
    private GMLNode start;
    private GMLNode end;
    private GMLEdge edge;

    /**
       Construct a CreateEdgeTool.
       @param editor The editor instance.
    */
    public CreateEdgeTool(GMLEditor editor) {
        super(editor);
        listener = new Listener();
        nodeHighlight = new SquareNodeDecorator(HIGHLIGHT_COLOUR, HIGHLIGHT_SIZE);
        edgeHighlight = new LineEdgeDecorator(HIGHLIGHT_COLOUR);
    }

    @Override
    public String getName() {
        return "Create edge";
    }

    @Override
    public void activate() {
        editor.getViewer().addMouseListener(listener);
        editor.getViewer().addMouseMotionListener(listener);
        hover = null;
        start = null;
        end = null;
        edge = null;
    }

    @Override
    public void deactivate() {
        editor.getViewer().removeMouseListener(listener);
        editor.getViewer().removeMouseMotionListener(listener);
        editor.getViewer().clearAllNodeDecorators();
        editor.getViewer().clearAllEdgeDecorators();
    }

    private void createTempEdge(GMLNode startNode) {
        start = startNode;
        end = startNode;
        edge = editor.getMap().createEdge(start, end);
        if (hover != null) {
            editor.getViewer().clearNodeDecorator(hover);
            hover = null;
        }
        editor.getViewer().setNodeDecorator(nodeHighlight, start);
        editor.getViewer().setNodeDecorator(nodeHighlight, end);
        editor.getViewer().setEdgeDecorator(edgeHighlight, edge);
        editor.getViewer().repaint();
    }

    private void moveTempEdge(GMLNode newEnd) {
        if (end == newEnd) {
            return;
        }
        editor.getViewer().clearNodeDecorator(end);
        end = newEnd;
        edge.setEnd(newEnd);
        editor.getViewer().setNodeDecorator(nodeHighlight, end);
        editor.getViewer().repaint();
    }

    private void highlightNode(GMLNode node) {
        if (hover == node) {
            return;
        }
        if (hover != null) {
            editor.getViewer().clearNodeDecorator(hover);
        }
        hover = node;
        if (hover != null) {
            editor.getViewer().setNodeDecorator(nodeHighlight, hover);
        }
        editor.getViewer().repaint();
    }

    private class Listener implements MouseListener, MouseMotionListener {
        @Override
        public void mousePressed(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                Point p = fixEventPoint(e.getPoint());
                GMLCoordinates c = editor.getViewer().getCoordinatesAtPoint(p.x, p.y);
                GMLNode node = editor.getMap().findNearestNode(c.getX(), c.getY());
                createTempEdge(node);
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                if (start != null) {
                    editor.setChanged();
                    editor.addEdit(new CreateEdgeEdit(edge));
                    editor.getViewer().clearNodeDecorator(start);
                    editor.getViewer().clearNodeDecorator(end);
                    editor.getViewer().clearEdgeDecorator(edge);
                    editor.getViewer().repaint();
                    start = null;
                    end = null;
                    edge = null;
                }
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (start != null) {
                Point p = fixEventPoint(e.getPoint());
                GMLCoordinates c = editor.getViewer().getCoordinatesAtPoint(p.x, p.y);
                GMLNode node = editor.getMap().findNearestNode(c.getX(), c.getY());
                moveTempEdge(node);
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            Point p = fixEventPoint(e.getPoint());
            GMLCoordinates c = editor.snap(editor.getViewer().getCoordinatesAtPoint(p.x, p.y));
            GMLNode node = editor.getMap().findNearestNode(c.getX(), c.getY());
            highlightNode(node);
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

    private class CreateEdgeEdit extends AbstractUndoableEdit {
        private GMLEdge edge;

        public CreateEdgeEdit(GMLEdge edge) {
            this.edge = edge;
        }

        @Override
        public void undo() {
            super.undo();
            editor.getMap().removeEdge(edge);
            editor.getViewer().repaint();
        }

        @Override
        public void redo() {
            super.redo();
            editor.getMap().addEdge(edge);
            editor.getViewer().repaint();
        }
    }
}