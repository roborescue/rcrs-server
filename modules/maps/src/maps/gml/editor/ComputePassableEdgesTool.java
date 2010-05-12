package maps.gml.editor;

import java.awt.Window;
import java.awt.Dialog;
import java.awt.BorderLayout;
import javax.swing.JDialog;
import javax.swing.JProgressBar;

import java.util.Collection;
import java.util.List;
import java.util.Iterator;

import maps.gml.GMLEdge;
import maps.gml.GMLDirectedEdge;
import maps.gml.GMLShape;

import rescuecore2.log.Logger;

/**
   A tool for computing passable edges.
*/
public class ComputePassableEdgesTool extends AbstractTool {
    /**
       Construct a ComputePassableEdgesTool.
       @param editor The editor instance.
    */
    public ComputePassableEdgesTool(GMLEditor editor) {
        super(editor);
    }

    @Override
    public String getName() {
        return "Compute passable edges";
    }

    @Override
    public void activate() {
        final JDialog dialog = new JDialog((Window)editor.getViewer().getTopLevelAncestor(), "Finding neighbours", Dialog.ModalityType.APPLICATION_MODAL);
        final Collection<GMLEdge> edges = editor.getMap().getEdges();
        final JProgressBar progress = new JProgressBar(0, edges.size());
        progress.setStringPainted(true);
        dialog.getContentPane().add(progress, BorderLayout.CENTER);
        Thread t = new Thread() {
                @Override
                public void run() {
                    int count = 0;
                    for (GMLEdge next : edges) {
                        Collection<GMLShape> shapes = editor.getMap().getAttachedShapes(next);
                        if (shapes.size() == 2) {
                            next.setPassable(true);
                            Iterator<GMLShape> it = shapes.iterator();
                            GMLShape first = it.next();
                            GMLShape second = it.next();
                            GMLDirectedEdge firstEdge = findDirectedEdge(first.getEdges(), next);
                            GMLDirectedEdge secondEdge = findDirectedEdge(second.getEdges(), next);
                            first.setNeighbour(firstEdge, second.getID());
                            second.setNeighbour(secondEdge, first.getID());
                            ++count;
                        }
                        progress.setValue(progress.getValue() + 1);
                    }
                    editor.setChanged();
                    editor.getViewer().repaint();
                    Logger.debug("Made " + count + " edges passable");
                    dialog.setVisible(false);
                    dialog.dispose();
                }
            };
        t.start();
        dialog.pack();
        dialog.setVisible(true);
    }

    @Override
    public void deactivate() {
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