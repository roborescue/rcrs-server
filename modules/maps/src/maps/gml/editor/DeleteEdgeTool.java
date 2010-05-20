package maps.gml.editor;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.Color;
import java.awt.Point;
import java.awt.Insets;

import javax.swing.undo.AbstractUndoableEdit;

import maps.gml.view.EdgeDecorator;
import maps.gml.view.LineEdgeDecorator;
import maps.gml.GMLEdge;
import maps.gml.GMLCoordinates;
import maps.gml.GMLObject;

import java.util.Collection;

/**
   A tool for deleting edges.
*/
public class DeleteEdgeTool extends AbstractTool {
    private static final Color HIGHLIGHT_COLOUR = Color.BLUE;

    private Listener listener;
    private EdgeDecorator edgeHighlight;

    private GMLEdge edge;

    /**
       Construct a DeleteEdgeTool.
       @param editor The editor instance.
    */
    public DeleteEdgeTool(GMLEditor editor) {
        super(editor);
        listener = new Listener();
        edgeHighlight = new LineEdgeDecorator(HIGHLIGHT_COLOUR);
    }

    @Override
    public String getName() {
        return "Delete edge";
    }

    @Override
    public void activate() {
        editor.getViewer().addMouseListener(listener);
        editor.getViewer().addMouseMotionListener(listener);
        edge = null;
    }

    @Override
    public void deactivate() {
        editor.getViewer().removeMouseListener(listener);
        editor.getViewer().removeMouseMotionListener(listener);
        editor.getViewer().clearAllEdgeDecorators();
        editor.getViewer().repaint();
    }

    private void highlightEdge(GMLEdge newEdge) {
        if (edge == newEdge) {
            return;
        }
        if (edge != null) {
            editor.getViewer().clearEdgeDecorator(edge);
        }
        edge = newEdge;
        if (edge != null) {
            editor.getViewer().setEdgeDecorator(edgeHighlight, edge);
        }
        editor.getViewer().repaint();
    }

    private class Listener implements MouseListener, MouseMotionListener {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                Collection<GMLObject> deleted = editor.getMap().removeEdge(edge);
                editor.getViewer().repaint();
                editor.setChanged();
                editor.addEdit(new DeleteEdgeEdit(edge, deleted));
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            Point p = fixEventPoint(e.getPoint());
            GMLCoordinates c = editor.snap(editor.getViewer().getCoordinatesAtPoint(p.x, p.y));
            highlightEdge(editor.getMap().findNearestEdge(c.getX(), c.getY()));
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

    private class DeleteEdgeEdit extends AbstractUndoableEdit {
        private GMLEdge edge;
        private Collection<GMLObject> deleted;

        public DeleteEdgeEdit(GMLEdge edge, Collection<GMLObject> deleted) {
            this.edge = edge;
            this.deleted = deleted;
        }

        @Override
        public void undo() {
            super.undo();
            editor.getMap().addEdge(edge);
            editor.getMap().add(deleted);
            editor.getViewer().repaint();
        }

        @Override
        public void redo() {
            super.redo();
            editor.getMap().removeEdge(edge);
            editor.getViewer().repaint();
        }
    }
}