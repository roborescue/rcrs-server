package maps.gml.editor;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.Color;
import java.awt.Point;
import java.awt.Insets;

import java.util.Collection;
import java.util.HashSet;

import maps.gml.view.NodeDecorator;
import maps.gml.view.SquareNodeDecorator;
import maps.gml.view.EdgeDecorator;
import maps.gml.view.LineEdgeDecorator;
import maps.gml.GMLNode;
import maps.gml.GMLEdge;
import maps.gml.GMLCoordinates;
import maps.gml.GMLObject;

import javax.swing.undo.AbstractUndoableEdit;

/**
   A tool for deleting nodes.
*/
public class DeleteNodeTool extends AbstractTool {
    private static final Color HIGHLIGHT_COLOUR = Color.BLUE;
    private static final int HIGHLIGHT_SIZE = 6;

    private Listener listener;
    private NodeDecorator nodeHighlight;
    private EdgeDecorator edgeHighlight;
    private GMLNode selected;
    private Collection<GMLEdge> attachedEdges;

    /**
       Construct a DeleteNodeTool.
       @param editor The editor instance.
    */
    public DeleteNodeTool(GMLEditor editor) {
        super(editor);
        listener = new Listener();
        nodeHighlight = new SquareNodeDecorator(HIGHLIGHT_COLOUR, HIGHLIGHT_SIZE);
        edgeHighlight = new LineEdgeDecorator(HIGHLIGHT_COLOUR);
        selected = null;
        attachedEdges = new HashSet<GMLEdge>();
    }

    @Override
    public String getName() {
        return "Delete node";
    }

    @Override
    public void activate() {
        editor.getViewer().addMouseListener(listener);
        editor.getViewer().addMouseMotionListener(listener);
        selected = null;
        attachedEdges.clear();
    }

    @Override
    public void deactivate() {
        editor.getViewer().removeMouseListener(listener);
        editor.getViewer().removeMouseMotionListener(listener);
        editor.getViewer().clearAllNodeDecorators();
        editor.getViewer().clearAllEdgeDecorators();
        editor.getViewer().repaint();
    }

    private void highlightNode(GMLNode node) {
        if (selected == node) {
            return;
        }
        if (selected != null) {
            editor.getViewer().clearNodeDecorator(selected);
            editor.getViewer().clearEdgeDecorator(attachedEdges);
        }
        selected = node;
        attachedEdges.clear();
        if (selected != null) {
            attachedEdges.addAll(editor.getMap().getAttachedEdges(selected));
            editor.getViewer().setNodeDecorator(nodeHighlight, selected);
            editor.getViewer().setEdgeDecorator(edgeHighlight, attachedEdges);
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
        public void mouseClicked(MouseEvent e) {
            if (selected == null) {
                return;
            }
            if (e.getButton() == MouseEvent.BUTTON1) {
                Collection<GMLObject> deleted = editor.getMap().removeNode(selected);
                editor.getViewer().repaint();
                editor.setChanged();
                editor.addEdit(new DeleteNodeEdit(selected, deleted));
            }
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

    private class DeleteNodeEdit extends AbstractUndoableEdit {
        private GMLNode node;
        private Collection<GMLObject> deletedObjects;

        public DeleteNodeEdit(GMLNode node, Collection<GMLObject> deletedObjects) {
            this.node = node;
            this.deletedObjects = deletedObjects;
        }

        @Override
        public void undo() {
            super.undo();
            editor.getMap().addNode(node);
            editor.getMap().add(deletedObjects);
            editor.getViewer().repaint();
        }

        @Override
        public void redo() {
            super.redo();
            editor.getMap().removeNode(node);
            editor.getMap().remove(deletedObjects);
            editor.getViewer().repaint();
        }
    }
}