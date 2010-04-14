package maps.gml.editor;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.Point;
import java.awt.Insets;

import maps.gml.GMLNode;
import maps.gml.GMLCoordinates;

import rescuecore2.log.Logger;

/**
   A tool for inspecting objects.
*/
public class InspectTool extends AbstractTool {
    private Listener listener;

    /**
       Construct an InspectTool.
       @param editor The editor instance.
    */
    public InspectTool(GMLEditor editor) {
        super(editor);
        listener = new Listener();
    }

    @Override
    public String getName() {
        return "Inspect object";
    }

    @Override
    public void activate() {
        Logger.debug("InspectTool activated");
        editor.getViewer().addMouseListener(listener);
        editor.getViewer().addMouseMotionListener(listener);
    }

    @Override
    public void deactivate() {
        Logger.debug("InspectTool deactivated");
        editor.getViewer().removeMouseListener(listener);
        editor.getViewer().removeMouseMotionListener(listener);
    }

    private class Listener implements MouseListener, MouseMotionListener {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                Point p = fixEventPoint(e.getPoint());
                GMLCoordinates c = editor.getViewer().getCoordinatesAtPoint(p.x, p.y);
                GMLNode node = editor.getMap().findNearestNode(c.getX(), c.getY());
                editor.getInspector().inspect(node);
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
}