package maps.gml.editor;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.Color;
import java.awt.Point;
import java.awt.Insets;

import javax.swing.undo.AbstractUndoableEdit;

import java.util.List;
import java.util.ArrayList;

import maps.gml.view.NodeDecorator;
import maps.gml.view.EdgeDecorator;
import maps.gml.view.SquareNodeDecorator;
import maps.gml.view.LineEdgeDecorator;
import maps.gml.GMLNode;
import maps.gml.GMLEdge;
import maps.gml.GMLSpace;
import maps.gml.GMLCoordinates;

/**
   A tool for creating spaces.
*/
public class CreateSpaceTool extends AbstractTool {
    private static final Color HOVER_COLOUR = Color.BLUE;
    private static final Color SELECTED_COLOUR = Color.GREEN;

    private Listener listener;
    private EdgeDecorator hoverHighlight;
    private EdgeDecorator selectedHighlight;

    private List<GMLEdge> edges;
    private List<GMLNode> nodes;
    private GMLNode startNode;
    private GMLNode currentNode;
    private GMLEdge hover;

    /**
       Construct a CreateSpaceTool.
       @param editor The editor instance.
    */
    public CreateSpaceTool(GMLEditor editor) {
        super(editor);
        listener = new Listener();
        hoverHighlight = new LineEdgeDecorator(HOVER_COLOUR);
        selectedHighlight = new LineEdgeDecorator(SELECTED_COLOUR);
        edges = new ArrayList<GMLEdge>();
        nodes = new ArrayList<GMLNode>();
    }

    @Override
    public String getName() {
        return "Create space";
    }

    @Override
    public void activate() {
        editor.getViewer().addMouseListener(listener);
        editor.getViewer().addMouseMotionListener(listener);
        edges.clear();
    }

    @Override
    public void deactivate() {
        editor.getViewer().removeMouseListener(listener);
        editor.getViewer().removeMouseMotionListener(listener);
        editor.getViewer().clearAllEdgeDecorators();
    }

    private void addEdge(GMLEdge edge) {
        edges.add(edge);
        editor.getViewer().setEdgeDecorator(selectedHighlight, edge);
        editor.getViewer().repaint();
        if (edges.size() == 1) {
            startNode = edge.getStart();
            currentNode = edge.getEnd();
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
        }
        else if (edges.size() > 2) {
            // Update end node
            currentNode = currentNode.equals(edge.getStart()) ? edge.getEnd() : edge.getStart();
            if (currentNode.equals(startNode)) {
                // We're done
                GMLSpace space = editor.getMap().createSpaceFromNodes(nodes);
                editor.addEdit(new CreateSpaceEdit(space));
                editor.setChanged();
                nodes.clear();
                edges.clear();
                editor.getViewer().clearAllEdgeDecorators();
                editor.getViewer().repaint();
            }
            else {
                nodes.add(currentNode);
            }
        }
        editor.getViewer().repaint();
    }

    private void hover(GMLEdge edge) {
        if (hover == edge) {
            return;
        }
        if (hover != null) {
            editor.getViewer().clearEdgeDecorator(hover);
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
            GMLEdge edge = editor.getMap().findNearestEdge(c.getX(), c.getY());
            if (edges.contains(edge)) {
                return;
            }
            if (edges.isEmpty()) {
                hover(edge);
            }
            else if (edges.size() == 1) {
                if (edge.getStart().equals(startNode)
                    || edge.getStart().equals(currentNode)
                    || edge.getEnd().equals(startNode)
                    || edge.getEnd().equals(currentNode)) {
                    hover(edge);
                }
            }
            else if (edge.getStart().equals(currentNode)
                     || edge.getEnd().equals(currentNode)) {
                hover(edge);
            }
            else {
                hover(null);
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

    private class CreateSpaceEdit extends AbstractUndoableEdit {
        private GMLSpace space;

        public CreateSpaceEdit(GMLSpace space) {
            this.space = space;
        }

        @Override
        public void undo() {
            super.undo();
            editor.getMap().removeSpace(space);
            editor.getViewer().repaint();
        }

        @Override
        public void redo() {
            super.redo();
            editor.getMap().addSpace(space);
            editor.getViewer().repaint();
        }
    }
}