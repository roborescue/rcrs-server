package maps.gml.editor;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.Color;
import java.awt.Point;
import java.awt.Insets;

import javax.swing.undo.UndoableEdit;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import maps.gml.view.EdgeDecorator;
import maps.gml.view.LineEdgeDecorator;
import maps.gml.GMLNode;
import maps.gml.GMLEdge;
import maps.gml.GMLCoordinates;

/**
   A tool for creating shapes.
*/
public abstract class CreateShapeTool extends AbstractTool {
    private static final Color HOVER_COLOUR = Color.BLUE;
    private static final Color SELECTED_COLOUR = Color.GREEN;
    private static final Color POSSIBLE_COLOUR = Color.WHITE;

    private Listener listener;
    private EdgeDecorator hoverHighlight;
    private EdgeDecorator selectedHighlight;
    private EdgeDecorator possibleHighlight;

    private List<GMLEdge> edges;
    private List<GMLNode> nodes;
    private Set<GMLEdge> possible;
    private GMLNode startNode;
    private GMLNode currentNode;
    private GMLEdge hover;

    /**
       Construct a CreateShapeTool.
       @param editor The editor instance.
    */
    protected CreateShapeTool(GMLEditor editor) {
        super(editor);
        listener = new Listener();
        hoverHighlight = new LineEdgeDecorator(HOVER_COLOUR);
        selectedHighlight = new LineEdgeDecorator(SELECTED_COLOUR);
        possibleHighlight = new LineEdgeDecorator(POSSIBLE_COLOUR);
        edges = new ArrayList<GMLEdge>();
        nodes = new ArrayList<GMLNode>();
        possible = new HashSet<GMLEdge>();
    }

    @Override
    public void activate() {
        editor.getViewer().addMouseListener(listener);
        editor.getViewer().addMouseMotionListener(listener);
        clearData();
    }

    @Override
    public void deactivate() {
        editor.getViewer().removeMouseListener(listener);
        editor.getViewer().removeMouseMotionListener(listener);
        editor.getViewer().clearAllEdgeDecorators();
        editor.getViewer().repaint();
        clearData();
    }

    /**
       Perform whatever shape creation tasks are needed once the shape has been closed.
       @param shapeNodes The nodes of the shape.
       @return An UndoableEdit for the change.
    */
    protected abstract UndoableEdit finished(List<GMLNode> shapeNodes);

    private void addEdge(GMLEdge edge) {
        editor.getViewer().clearEdgeDecorator(possible);
        edges.add(edge);
        possible.clear();
        editor.getViewer().setEdgeDecorator(selectedHighlight, edge);
        editor.getViewer().repaint();
        if (edges.size() == 1) {
            startNode = edge.getStart();
            currentNode = edge.getEnd();
            possible.addAll(editor.getMap().getAttachedEdges(startNode));
            possible.addAll(editor.getMap().getAttachedEdges(currentNode));
        }
        else if (edges.size() == 2) {
            // Find the shared node
            GMLEdge first = edges.get(0);
            GMLEdge second = edges.get(1);
            GMLNode shared;
            if (first.getStart().equals(second.getStart()) || first.getStart().equals(second.getEnd())) {
                startNode = first.getEnd();
                shared = first.getStart();
            }
            else {
                startNode = first.getStart();
                shared = first.getEnd();
            }
            currentNode = shared.equals(second.getStart()) ? second.getEnd() : second.getStart();
            nodes.add(startNode);
            nodes.add(shared);
            nodes.add(currentNode);
            possible.addAll(editor.getMap().getAttachedEdges(currentNode));
        }
        else if (edges.size() > 2) {
            // Update end node
            currentNode = currentNode.equals(edge.getStart()) ? edge.getEnd() : edge.getStart();
            if (currentNode.equals(startNode)) {
                // We're done
                editor.addEdit(finished(nodes));
                editor.setChanged();
                clearData();
                editor.getViewer().clearAllEdgeDecorators();
                editor.getViewer().repaint();
            }
            else {
                nodes.add(currentNode);
                possible.addAll(editor.getMap().getAttachedEdges(currentNode));
            }
        }
        possible.removeAll(edges);
        editor.getViewer().setEdgeDecorator(possibleHighlight, possible);
        if (possible.size() == 1) {
            addEdge(possible.iterator().next());
        }
        editor.getViewer().repaint();
    }

    private void clearData() {
        nodes.clear();
        edges.clear();
        possible.clear();
        startNode = null;
        currentNode = null;
        hover = null;
    }

    private void hover(GMLEdge edge) {
        if (hover == edge) {
            return;
        }
        if (hover != null) {
            editor.getViewer().clearEdgeDecorator(hover);
            if (possible.contains(hover)) {
                editor.getViewer().setEdgeDecorator(possibleHighlight, hover);
            }
        }
        hover = edge;
        if (hover != null) {
            editor.getViewer().setEdgeDecorator(hoverHighlight, hover);
        }
        editor.getViewer().repaint();
    }

    private class Listener implements MouseListener, MouseMotionListener {
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
        public void mouseMoved(MouseEvent e) {
            Point p = fixEventPoint(e.getPoint());
            GMLCoordinates c = editor.snap(editor.getViewer().getCoordinatesAtPoint(p.x, p.y));
            if (edges.isEmpty()) {
                hover(editor.getMap().findNearestEdge(c.getX(), c.getY()));
            }
            else {
                hover(editor.getMap().findNearestEdge(c.getX(), c.getY(), possible));
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (hover != null && e.getButton() == MouseEvent.BUTTON1) {
                GMLEdge edge = hover;
                hover(null);
                addEdge(edge);
            }
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