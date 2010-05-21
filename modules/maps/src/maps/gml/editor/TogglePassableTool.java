package maps.gml.editor;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.Color;
import java.awt.Point;
import java.awt.Insets;

import javax.swing.undo.AbstractUndoableEdit;

import java.util.Collection;
import java.util.Iterator;

import maps.gml.GMLEdge;
import maps.gml.GMLShape;
import maps.gml.GMLCoordinates;
import maps.gml.view.EdgeDecorator;
import maps.gml.view.LineEdgeDecorator;

/**
   A tool for toggling the passable flag on edges.
*/
public class TogglePassableTool extends AbstractTool {
    private static final Color HIGHLIGHT_COLOUR = Color.BLUE;

    private Listener listener;
    private EdgeDecorator highlight;
    private GMLEdge selected;

    /**
       Construct a TogglePassableTool.
       @param editor The editor instance.
    */
    public TogglePassableTool(GMLEditor editor) {
        super(editor);
        highlight = new LineEdgeDecorator(HIGHLIGHT_COLOUR);
        listener = new Listener();
    }

    @Override
    public String getName() {
        return "Toggle passable";
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
        editor.getViewer().clearAllEdgeDecorators();
        editor.getViewer().repaint();
    }

    private void highlight(GMLEdge edge) {
        if (selected == edge) {
            return;
        }
        if (selected != null) {
            editor.getViewer().clearEdgeDecorator(selected);
        }
        selected = edge;
        if (selected != null) {
            editor.getViewer().setEdgeDecorator(highlight, selected);
        }
        editor.getViewer().repaint();
    }

    private void toggle() {
        boolean isPassable = !selected.isPassable();
        setPassable(selected, isPassable);
        editor.addEdit(new ToggleEdit(selected, isPassable));
    }

    private void setPassable(GMLEdge edge, boolean passable) {
        edge.setPassable(passable);
        Collection<GMLShape> attached = editor.getMap().getAttachedShapes(edge);
        Iterator<GMLShape> it = attached.iterator();
        GMLShape first = it.next();
        GMLShape second = it.next();
        if (passable) {
            first.setNeighbour(edge, second.getID());
            second.setNeighbour(edge, first.getID());
        }
        else {
            first.setNeighbour(edge, null);
            second.setNeighbour(edge, null);
        }
        editor.setChanged();
        editor.getViewer().repaint();
    }

    private class Listener implements MouseListener, MouseMotionListener {
        @Override
        public void mouseMoved(MouseEvent e) {
            Point p = fixEventPoint(e.getPoint());
            GMLCoordinates c = editor.getViewer().getCoordinatesAtPoint(p.x, p.y);
            GMLEdge edge = editor.getMap().findNearestEdge(c.getX(), c.getY());
            if (editor.getMap().getAttachedShapes(edge).size() == 2) {
                highlight(edge);
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (selected == null) {
                return;
            }
            if (e.getButton() == MouseEvent.BUTTON1) {
                toggle();
                highlight(null);
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

    private class ToggleEdit extends AbstractUndoableEdit {
        private GMLEdge edge;
        private boolean newPassable;

        public ToggleEdit(GMLEdge edge, boolean newPassable) {
            this.edge = edge;
            this.newPassable = newPassable;
        }

        @Override
        public void undo() {
            super.undo();
            setPassable(edge, !newPassable);
            editor.getViewer().repaint();
        }

        @Override
        public void redo() {
            super.redo();
            setPassable(edge, newPassable);
            editor.getViewer().repaint();
        }
    }
}