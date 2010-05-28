package maps.gml.editor;

import javax.swing.JOptionPane;

import java.util.Queue;
import java.util.LinkedList;
import java.util.Collection;
import java.util.HashSet;

import maps.gml.GMLNode;
import maps.gml.GMLEdge;
import maps.gml.GMLTools;

import rescuecore2.log.Logger;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.GeometryTools2D;

/**
   A function for splitting edges that cover nearby nodes.
*/
public class SplitEdgesFunction extends ProgressFunction {
    private static final double DEFAULT_THRESHOLD = 0.001;

    private double threshold;

    /**
       Construct a SplitEdgesFunction.
       @param editor The editor instance.
    */
    public SplitEdgesFunction(GMLEditor editor) {
        super(editor);
    }

    @Override
    public String getName() {
        return "Split edges";
    }

    @Override
    public void execute() {
        String s = JOptionPane.showInputDialog(editor.getViewer(), "Enter the desired distance threshold (in m)", DEFAULT_THRESHOLD);
        if (s == null) {
            return;
        }
        threshold = Double.parseDouble(s);
        super.execute();
    }

    @Override
    protected String getTitle() {
        return "Splitting edges";
    }

    @Override
    protected void executeImpl() {
        // Go through all edges and split any that cover nearby nodes
        final Queue<GMLEdge> remaining = new LinkedList<GMLEdge>();
        final Collection<GMLNode> nodes = new HashSet<GMLNode>();
        synchronized (editor.getMap()) {
            remaining.addAll(editor.getMap().getEdges());
            nodes.addAll(editor.getMap().getNodes());
        }
        setProgressLimit(remaining.size());
        int count = 0;
        while (!remaining.isEmpty()) {
            GMLEdge next = remaining.remove();
            Line2D line = GMLTools.toLine(next);
            // Look for nodes that are close to the line
            for (GMLNode node : nodes) {
                if (node == next.getStart() || node == next.getEnd()) {
                    continue;
                }
                Point2D p = GMLTools.toPoint(node);
                Point2D closest = GeometryTools2D.getClosestPointOnSegment(line, p);
                if (GeometryTools2D.getDistance(p, closest) < threshold) {
                    // Split the edge
                    Collection<GMLEdge> newEdges;
                    synchronized (editor.getMap()) {
                        newEdges = editor.getMap().splitEdge(next, node);
                        editor.getMap().removeEdge(next);
                        newEdges.removeAll(editor.getMap().getEdges());
                    }
                    remaining.addAll(newEdges);
                    bumpMaxProgress(newEdges.size());
                    ++count;
                    break;
                }
            }
            bumpProgress();
        }
        if (count != 0) {
            editor.setChanged();
            editor.getViewer().repaint();
        }
        Logger.debug("Split " + count + " edges");
    }
}