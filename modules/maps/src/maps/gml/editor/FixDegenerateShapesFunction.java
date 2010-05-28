package maps.gml.editor;

import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import maps.gml.GMLShape;
import maps.gml.GMLNode;
import maps.gml.GMLEdge;
import maps.gml.GMLDirectedEdge;

import rescuecore2.log.Logger;

/**
   A function for fixing degenerate shapes.
*/
public class FixDegenerateShapesFunction extends ProgressFunction {
    /**
       Construct a FixDegenerateShapesFunction.
       @param editor The editor instance.
    */
    public FixDegenerateShapesFunction(GMLEditor editor) {
        super(editor);
    }

    @Override
    public String getName() {
        return "Fix degenerate shapes";
    }

    @Override
    protected String getTitle() {
        return "Fixing degenerate shapes";
    }

    @Override
    protected void executeImpl() {
        // Go through all shapes and remove any that have two or fewer edges.
        Set<GMLShape> shapes = new HashSet<GMLShape>();
        Set<GMLEdge> edges = new HashSet<GMLEdge>();
        synchronized (editor.getMap()) {
            shapes.addAll(editor.getMap().getAllShapes());
            edges.addAll(editor.getMap().getEdges());
        }
        setProgressLimit(shapes.size() + edges.size());
        int shapeCount = 0;
        int spurCount = 0;
        int edgeCount = 0;
        int outlineCount = 0;
        for (GMLShape next : shapes) {
            synchronized (editor.getMap()) {
                removeDuplicateEdges(next);
                if (checkForDegenerateShape(next)) {
                    ++shapeCount;
                }
                else {
                    if (checkForSpurs(next)) {
                        ++spurCount;
                    }
                    if (!checkOutline(next)) {
                        ++outlineCount;
                        editor.getMap().remove(next);
                    }
                }
            }
            bumpProgress();
        }
        for (GMLEdge next : edges) {
            synchronized (editor.getMap()) {
                if (checkForDegenerateEdge(next)) {
                    ++edgeCount;
                }
            }
            bumpProgress();
        }
        Logger.debug("Removed " + shapeCount + " degenerate shapes and " + edgeCount + " edges");
        Logger.debug("Removed " + outlineCount + " shapes with broken outlines");
        Logger.debug("Fixed " + spurCount + " spurs");
        editor.setChanged();
        editor.getViewer().repaint();
    }

    private void removeDuplicateEdges(GMLShape shape) {
        List<GMLDirectedEdge> result = new ArrayList<GMLDirectedEdge>(shape.getEdges());
        Set<GMLEdge> seen = new HashSet<GMLEdge>();
        /*
        Logger.debug("Checking for duplicate edges in " + shape);
        Logger.debug("Edges:");
        for (GMLDirectedEdge next : result) {
            Logger.debug("  " + next);
        }
        */
        for (Iterator<GMLDirectedEdge> it = result.iterator(); it.hasNext();) {
            GMLDirectedEdge dEdge = it.next();
            GMLEdge edge = dEdge.getEdge();
            if (seen.contains(edge)) {
                //                Logger.debug("Duplicate found: " + dEdge);
                it.remove();
            }
            seen.add(edge);
        }
        /*
        Logger.debug("Resulting edges:");
        for (GMLDirectedEdge next : result) {
            Logger.debug("  " + next);
        }
        */
        editor.getMap().remove(shape);
        shape.reorderEdges(result);
        // Update attached edges by removing and re-adding
        editor.getMap().add(shape);
    }

    private boolean checkForSpurs(GMLShape shape) {
        boolean spur = false;
        List<GMLDirectedEdge> good = new ArrayList<GMLDirectedEdge>(shape.getEdges().size());
        /*
        Logger.debug("Checking for spurs in " + shape);
        Logger.debug("Edges:");
        for (GMLDirectedEdge next : shape.getEdges()) {
            Logger.debug("  " + next);
        }
        */
        for (GMLDirectedEdge dEdge : shape.getEdges()) {
            // This edge is good if both its nodes are part of other edges.
            GMLNode start = dEdge.getStartNode();
            GMLNode end = dEdge.getEndNode();
            if (isFound(start, shape, dEdge) && isFound(end, shape, dEdge)) {
                good.add(dEdge);
            }
            else {
                //                Logger.debug("Found spur edge: " + dEdge);
                spur = true;
            }
        }
        if (spur) {
            editor.getMap().remove(shape);
            shape.reorderEdges(good);
            // Update attached edges by removing and re-adding
            editor.getMap().add(shape);
        }
        return spur;
    }

    private boolean checkOutline(GMLShape shape) {
        List<GMLDirectedEdge> edges = shape.getEdges();
        List<GMLDirectedEdge> result = new ArrayList<GMLDirectedEdge>(edges.size());
        Set<GMLEdge> seen = new HashSet<GMLEdge>();
        GMLDirectedEdge dEdge = edges.get(0);
        GMLNode start = dEdge.getStartNode();
        GMLNode current = dEdge.getEndNode();
        result.add(dEdge);
        seen.add(dEdge.getEdge());
        /*
        Logger.debug("Checking outline of " + shape);
        Logger.debug("Edges:");
        for (GMLDirectedEdge next : edges) {
            Logger.debug("  " + next);
        }
        Logger.debug("First edge: " + dEdge);
        Logger.debug("Start node: " + start);
        */
        while (current != start) {
            //            Logger.debug("Current node: " + current);
            GMLDirectedEdge next = findNextEdge(current, edges, seen);
            //            Logger.debug("Next edge: " + next);
            if (next == null) {
                //                Logger.debug("No next edge found!");
                return false;
            }
            current = next.getEndNode();
            seen.add(next.getEdge());
            result.add(next);
        }
        /*
        Logger.debug("Finished checking outline");
        Logger.debug("New edges:");
        for (GMLDirectedEdge next : result) {
            Logger.debug("  " + next);
        }
        */
        editor.getMap().remove(shape);
        shape.reorderEdges(result);
        // Update attached edges by removing and re-adding
        editor.getMap().add(shape);
        return true;
    }

    private boolean checkForDegenerateShape(GMLShape shape) {
        // CHECKSTYLE:OFF:MagicNumber
        if (shape.getEdges().size() < 3) {
            // CHECKSTYLE:ON:MagicNumber
            editor.getMap().remove(shape);
            return true;
        }
        return false;
    }

    private boolean checkForDegenerateEdge(GMLEdge edge) {
        if (edge.getStart().equals(edge.getEnd())) {
            // Remove this edge from all attached shapes
            Collection<GMLShape> attached = new HashSet<GMLShape>(editor.getMap().getAttachedShapes(edge));
            for (GMLShape shape : attached) {
                editor.getMap().remove(shape);
                shape.removeEdge(edge);
                editor.getMap().add(shape);
            }
            editor.getMap().remove(edge);
            return true;
        }
        return false;
    }

    private boolean isFound(GMLNode node, GMLShape shape, GMLDirectedEdge ignore) {
        for (GMLDirectedEdge edge : shape.getEdges()) {
            if (edge == ignore) {
                continue;
            }
            if (node.equals(edge.getStartNode()) || node.equals(edge.getEndNode())) {
                return true;
            }
        }
        return false;
    }

    private GMLDirectedEdge findNextEdge(GMLNode start, Collection<GMLDirectedEdge> possible, Set<GMLEdge> seen) {
        for (GMLDirectedEdge next : possible) {
            if (next.getStartNode() == start && !seen.contains(next.getEdge())) {
                return next;
            }
        }
        // No edges found. Try reversing them.
        for (GMLDirectedEdge next : possible) {
            if (next.getEndNode() == start && !seen.contains(next.getEdge())) {
                //                Logger.debug("Reversed edge " + next);
                next.reverse();
                return next;
            }
        }
        // Nothing found.
        return null;
    }
}