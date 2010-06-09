package maps.gml.editor;

import java.util.Collection;
import java.util.List;
import java.util.Iterator;

import maps.gml.GMLEdge;
import maps.gml.GMLDirectedEdge;
import maps.gml.GMLShape;
import maps.gml.GMLRoad;

import rescuecore2.log.Logger;

/**
   A function for computing passable edges.
*/
public class ComputePassableEdgesFunction extends ProgressFunction {
    /**
       Construct a ComputePassableEdgesFunction.
       @param editor The editor instance.
    */
    public ComputePassableEdgesFunction(GMLEditor editor) {
        super(editor);
    }

    @Override
    public String getName() {
        return "Compute passable edges";
    }

    @Override
    protected String getTitle() {
        return "Finding neighbours";
    }

    @Override
    protected void executeImpl() {
        final Collection<GMLEdge> edges = editor.getMap().getEdges();
        setProgressLimit(edges.size());
        int passable = 0;
        int impassable = 0;
        for (GMLEdge next : edges) {
            Collection<GMLShape> shapes = editor.getMap().getAttachedShapes(next);
            if (shapes.size() == 2) {
                Iterator<GMLShape> it = shapes.iterator();
                GMLShape first = it.next();
                GMLShape second = it.next();
                if (first instanceof GMLRoad || second instanceof GMLRoad
                        || next.isPassable()) {
                    next.setPassable(true);
                    GMLDirectedEdge firstEdge = findDirectedEdge(first.getEdges(), next);
                    GMLDirectedEdge secondEdge = findDirectedEdge(second.getEdges(), next);
                    first.setNeighbour(firstEdge, second.getID());
                    second.setNeighbour(secondEdge, first.getID());
                    ++passable;
                }
                else {
                    makeImpassable(next, shapes);
                    ++impassable;
                }
            }
            else {
                makeImpassable(next, shapes);
                ++impassable;
            }
            bumpProgress();
        }
        editor.setChanged();
        editor.getViewer().repaint();
        Logger.debug("Made " + passable + " edges passable and " + impassable + " impassable");
    }

    private void makeImpassable(GMLEdge edge, Collection<GMLShape> attached) {
        edge.setPassable(false);
        for (GMLShape shape : attached) {
            shape.setNeighbour(edge, null);
        }
    }

    private GMLDirectedEdge findDirectedEdge(List<GMLDirectedEdge> possible, GMLEdge target) {
        for (GMLDirectedEdge next : possible) {
            if (next.getEdge() == target) {
                return next;
            }
        }
        return null;
    }
}