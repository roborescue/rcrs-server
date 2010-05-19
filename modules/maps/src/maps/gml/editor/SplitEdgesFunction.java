package maps.gml.editor;

import java.awt.Window;
import java.awt.Dialog;
import java.awt.BorderLayout;
import javax.swing.JDialog;
import javax.swing.JProgressBar;

import java.util.Queue;
import java.util.LinkedList;
import java.util.Collection;

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
public class SplitEdgesFunction extends AbstractFunction {
    private static final double THRESHOLD = 0.001;

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
        // Go through all edges and split any that cover nearby nodes
        final JDialog dialog = new JDialog((Window)editor.getViewer().getTopLevelAncestor(), "Splitting edges", Dialog.ModalityType.APPLICATION_MODAL);
        final Queue<GMLEdge> remaining = new LinkedList<GMLEdge>(editor.getMap().getEdges());
        final JProgressBar progress = new JProgressBar(0, remaining.size());
        progress.setStringPainted(true);
        dialog.getContentPane().add(progress, BorderLayout.CENTER);
        Thread t = new Thread() {
                @Override
                public void run() {
                    int count = 0;
                    int total = remaining.size();
                    int inspected = 0;
                    while (!remaining.isEmpty()) {
                        GMLEdge next = remaining.remove();
                        Line2D line = GMLTools.toLine(next);
                        // Look for nodes that are close to the line
                        for (GMLNode node : editor.getMap().getNodes()) {
                            if (node == next.getStart() || node == next.getEnd()) {
                                continue;
                            }
                            Point2D p = GMLTools.toPoint(node);
                            Point2D closest = GeometryTools2D.getClosestPointOnSegment(line, p);
                            if (GeometryTools2D.getDistance(p, closest) < THRESHOLD) {
                                // Split the edge
                                Collection<GMLEdge> newEdges = editor.getMap().splitEdge(next, node);
                                remaining.addAll(newEdges);
                                editor.getMap().removeEdge(next);
                                total += newEdges.size();
                                progress.setMaximum(total);
                                ++count;
                                break;
                            }
                        }
                        ++inspected;
                        progress.setValue(inspected);
                        progress.setString(inspected + " / " + total);
                    }
                    if (count != 0) {
                        editor.setChanged();
                        editor.getViewer().repaint();
                    }
                    Logger.debug("Split " + count + " edges");
                    dialog.setVisible(false);
                    dialog.dispose();
                }
            };
        t.start();
        dialog.pack();
        dialog.setVisible(true);
    }
}