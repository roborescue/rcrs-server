package maps.gml.editor;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.Point;
import java.awt.Insets;

import javax.swing.undo.AbstractUndoableEdit;

import maps.gml.GMLNode;
import maps.gml.GMLCoordinates;

/**
   A tool for creating nodes.
*/
public class CreateNodeTool extends AbstractTool {
    private Listener listener;

    /**
       Construct a CreateNodeTool.
       @param editor The editor instance.
    */
    public CreateNodeTool(GMLEditor editor) {
        super(editor);
        listener = new Listener();
    }

    @Override
    public String getName() {
        return "Create node";
    }

    @Override
    public void activate() {
        editor.getViewer().addMouseListener(listener);
        editor.getViewer().addMouseMotionListener(listener);
    }

    @Override
    public void deactivate() {
        editor.getViewer().removeMouseListener(listener);
        editor.getViewer().removeMouseMotionListener(listener);
    }

    private class Listener implements MouseListener, MouseMotionListener {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                Point p = fixEventPoint(e.getPoint());
                GMLCoordinates c = editor.snap(editor.getViewer().getCoordinatesAtPoint(p.x, p.y));
                GMLNode node = editor.getMap().createNode(c);
                editor.setChanged();
                editor.addEdit(new CreateNodeEdit(node));
                editor.getViewer().repaint();
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
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

    private class CreateNodeEdit extends AbstractUndoableEdit {
        private GMLNode node;

        public CreateNodeEdit(GMLNode node) {
            this.node = node;
        }

        @Override
        public void undo() {
            super.undo();
            editor.getMap().removeNode(node);
            editor.getViewer().repaint();
        }

        @Override
        public void redo() {
            super.redo();
            editor.getMap().addNode(node);
            editor.getViewer().repaint();
        }
    }
}