package maps.gml.editor;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.Color;
import java.awt.Point;
import java.awt.Insets;

import maps.gml.view.NodeDecorator;
import maps.gml.view.SquareNodeDecorator;
import maps.gml.view.EdgeDecorator;
import maps.gml.view.LineEdgeDecorator;
import maps.gml.GMLNode;
import maps.gml.GMLEdge;
import maps.gml.GMLCoordinates;
import maps.gml.GMLTools;

import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.GeometryTools2D;

/**
   A tool for splitting edges.
*/
public class SplitEdgeTool extends AbstractTool {
    private static final Color EDGE_COLOUR = Color.BLUE;
    private static final Color NODE_COLOUR = Color.BLACK;
    private static final int NODE_SIZE = 6;

    private Listener listener;
    private NodeDecorator nodeHighlight;
    private EdgeDecorator edgeHighlight;
    private GMLNode node;
    private GMLEdge edge;

    /**
       Construct a SplitEdgeTool.
       @param editor The editor instance.
    */
    public SplitEdgeTool(GMLEditor editor) {
        super(editor);
        listener = new Listener();
        nodeHighlight = new SquareNodeDecorator(NODE_COLOUR, NODE_SIZE);
        edgeHighlight = new LineEdgeDecorator(EDGE_COLOUR);
        edge = null;
        node = null;
    }

    @Override
    public String getName() {
        return "Split edge";
    }

    @Override
    public void activate() {
        editor.getViewer().addMouseListener(listener);
        editor.getViewer().addMouseMotionListener(listener);
        node = null;
        edge = null;
    }

    @Override
    public void deactivate() {
        editor.getViewer().removeMouseListener(listener);
        editor.getViewer().removeMouseMotionListener(listener);
        editor.getViewer().clearAllNodeDecorators();
        editor.getViewer().clearAllEdgeDecorators();
        editor.getViewer().repaint();
        if (node != null) {
            editor.getMap().removeNode(node);
        }
    }

    private void update(GMLCoordinates c) {
        if (node == null) {
            node = editor.getMap().createNode(c);
            editor.getViewer().setNodeDecorator(nodeHighlight, node);
        }
        GMLEdge newEdge = editor.getMap().findNearestEdge(c.getX(), c.getY());
        if (newEdge != edge) {
            if (edge != null) {
                editor.getViewer().clearEdgeDecorator(edge);
            }
            edge = newEdge;
            editor.getViewer().setEdgeDecorator(edgeHighlight, edge);
        }
        // Snap the node coordinates to the edge
        Line2D line = GMLTools.toLine(edge);
        Point2D point = new Point2D(c.getX(), c.getY());
        Point2D closest = GeometryTools2D.getClosestPointOnSegment(line, point);
        c.setX(closest.getX());
        c.setY(closest.getY());
        node.setCoordinates(c);
        editor.getViewer().repaint();
    }

    private void split() {
        if (node == null || edge == null) {
            return;
        }
        editor.getMap().splitEdge(edge, node);
        editor.getMap().removeEdge(edge);
        editor.setChanged();
        editor.getViewer().clearAllNodeDecorators();
        editor.getViewer().clearAllEdgeDecorators();
        editor.getViewer().repaint();
        node = null;
        edge = null;
    }

    private class Listener implements MouseListener, MouseMotionListener {
        @Override
        public void mouseMoved(MouseEvent e) {
            Point p = fixEventPoint(e.getPoint());
            GMLCoordinates c = editor.snap(editor.getViewer().getCoordinatesAtPoint(p.x, p.y));
            update(c);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                split();
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
}