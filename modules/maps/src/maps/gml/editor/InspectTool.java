package maps.gml.editor;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.Point;
import java.awt.Insets;
import java.awt.Color;

import maps.gml.GMLNode;
import maps.gml.GMLEdge;
import maps.gml.GMLRoad;
import maps.gml.GMLBuilding;
import maps.gml.GMLSpace;
import maps.gml.GMLShape;
import maps.gml.GMLObject;
import maps.gml.GMLCoordinates;
import maps.gml.view.FilledShapeDecorator;
import maps.gml.view.NodeDecorator;
import maps.gml.view.EdgeDecorator;
import maps.gml.view.SquareNodeDecorator;
import maps.gml.view.LineEdgeDecorator;

import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.GeometryTools2D;

/**
   A tool for inspecting objects.
*/
public class InspectTool extends AbstractTool {
    /** Distance in pixels to consider an object "nearby". */
    private static final int NEARBY = 5;

    private static final Color HIGHLIGHT_COLOUR = Color.BLUE;
    private static final int NODE_SIZE = 5;

    private Listener listener;
    private NodeDecorator nodeHighlight;
    private EdgeDecorator edgeHighlight;
    private FilledShapeDecorator shapeHighlight;

    /**
       Construct an InspectTool.
       @param editor The editor instance.
    */
    public InspectTool(GMLEditor editor) {
        super(editor);
        listener = new Listener();
        nodeHighlight = new SquareNodeDecorator(HIGHLIGHT_COLOUR, NODE_SIZE);
        edgeHighlight = new LineEdgeDecorator(HIGHLIGHT_COLOUR);
        shapeHighlight = new FilledShapeDecorator(HIGHLIGHT_COLOUR, HIGHLIGHT_COLOUR, HIGHLIGHT_COLOUR);
    }

    @Override
    public String getName() {
        return "Inspect object";
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
        editor.getViewer().clearAllNodeDecorators();
        editor.getViewer().clearAllEdgeDecorators();
        editor.getViewer().clearAllBuildingDecorators();
        editor.getViewer().clearAllRoadDecorators();
        editor.getViewer().clearAllSpaceDecorators();
        editor.getViewer().repaint();
    }

    private void highlight(GMLObject object) {
        editor.getViewer().clearAllNodeDecorators();
        editor.getViewer().clearAllEdgeDecorators();
        editor.getViewer().clearAllBuildingDecorators();
        editor.getViewer().clearAllRoadDecorators();
        editor.getViewer().clearAllSpaceDecorators();
        if (object instanceof GMLNode) {
            editor.getViewer().setNodeDecorator(nodeHighlight, (GMLNode)object);
        }
        if (object instanceof GMLEdge) {
            editor.getViewer().setEdgeDecorator(edgeHighlight, (GMLEdge)object);
        }
        if (object instanceof GMLBuilding) {
            editor.getViewer().setBuildingDecorator(shapeHighlight, (GMLBuilding)object);
        }
        if (object instanceof GMLRoad) {
            editor.getViewer().setRoadDecorator(shapeHighlight, (GMLRoad)object);
        }
        if (object instanceof GMLSpace) {
            editor.getViewer().setSpaceDecorator(shapeHighlight, (GMLSpace)object);
        }
        editor.getViewer().repaint();
    }

    private boolean closeEnough(GMLNode node, Point p) {
        GMLCoordinates lowerLeft = editor.getViewer().getCoordinatesAtPoint(p.x - NEARBY, p.y + NEARBY);
        GMLCoordinates topRight = editor.getViewer().getCoordinatesAtPoint(p.x + NEARBY, p.y - NEARBY);
        return (node.getX() > lowerLeft.getX() && node.getX() < topRight.getX() && node.getY() > lowerLeft.getY() && node.getY() < topRight.getY());
    }

    private boolean closeEnough(GMLEdge edge, Point p) {
        Point start = editor.getViewer().getScreenCoordinates(edge.getStart().getCoordinates());
        Point end = editor.getViewer().getScreenCoordinates(edge.getEnd().getCoordinates());
        Point2D startPoint = new Point2D(start.x, start.y);
        Point2D endPoint = new Point2D(end.x, end.y);
        Line2D line = new Line2D(startPoint, endPoint);
        Point2D testPoint = new Point2D(p.x, p.y);
        Point2D closest = GeometryTools2D.getClosestPointOnSegment(line, testPoint);
        return GeometryTools2D.getDistance(testPoint, closest) < NEARBY;
    }

    private class Listener implements MouseListener, MouseMotionListener {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                Point p = fixEventPoint(e.getPoint());
                editor.getInspector().inspect(findNearbyObject(p));
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            Point p = fixEventPoint(e.getPoint());
            highlight(findNearbyObject(p));
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

        private GMLObject findNearbyObject(Point p) {
            GMLCoordinates c = editor.getViewer().getCoordinatesAtPoint(p.x, p.y);
            GMLNode node = editor.getMap().findNearestNode(c.getX(), c.getY());
            GMLEdge edge = editor.getMap().findNearestEdge(c.getX(), c.getY());
            GMLShape shape = editor.getMap().findShapeUnder(c.getX(), c.getY());
            // If the node is close enough inspect that
            // Otherwise, if the edge is close enough
            // Otherwise the shape
            if (node != null && closeEnough(node, p)) {
                return node;
            }
            else if (edge != null && closeEnough(edge, p)) {
                return edge;
            }
            else {
                return shape;
            }
        }

        private Point fixEventPoint(Point p) {
            Insets insets = editor.getViewer().getInsets();
            return new Point(p.x - insets.left, p.y - insets.top);
        }
    }
}