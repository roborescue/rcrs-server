package maps.gml.editor;

import java.awt.Window;
import java.awt.Dialog;
import java.awt.BorderLayout;
import javax.swing.JDialog;
import javax.swing.JProgressBar;

import java.util.Set;
import java.util.HashSet;
import java.util.Collection;

import maps.gml.GMLShape;
import maps.gml.GMLEdge;

import rescuecore2.log.Logger;

/**
   A function for fixing degenerate shapes.
*/
public class FixDegenerateShapesFunction extends AbstractFunction {
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
    public void execute() {
        // Go through all shapes and remove any that have two or fewer edges.
        final JDialog dialog = new JDialog((Window)editor.getViewer().getTopLevelAncestor(), "Fixing degenerate shapes", Dialog.ModalityType.APPLICATION_MODAL);
        final Set<GMLShape> shapes = new HashSet<GMLShape>();
        final Set<GMLEdge> edges = new HashSet<GMLEdge>();
        synchronized (editor.getMap()) {
            shapes.addAll(editor.getMap().getAllShapes());
            edges.addAll(editor.getMap().getEdges());
        }
        final JProgressBar progress = new JProgressBar(0, shapes.size() + edges.size());
        progress.setStringPainted(true);
        dialog.getContentPane().add(progress, BorderLayout.CENTER);
        Thread t = new Thread() {
                @Override
                public void run() {
                    int shapeCount = 0;
                    int edgeCount = 0;
                    for (GMLShape next : shapes) {
                        synchronized (editor.getMap()) {
                            // CHECKSTYLE:OFF:MagicNumber
                            if (next.getEdges().size() < 3) {
                                // CHECKSTYLE:ON:MagicNumber
                                editor.getMap().remove(next);
                                ++shapeCount;
                            }
                        }
                        progress.setValue(progress.getValue() + 1);
                    }
                    for (GMLEdge next : edges) {
                        synchronized (editor.getMap()) {
                            if (next.getStart().equals(next.getEnd())) {
                                // Remove this edge from all attached shapes
                                Collection<GMLShape> attached = new HashSet<GMLShape>(editor.getMap().getAttachedShapes(next));
                                for (GMLShape shape : attached) {
                                    editor.getMap().remove(shape);
                                    shape.removeEdge(next);
                                    editor.getMap().add(shape);
                                }
                                editor.getMap().remove(next);
                                ++edgeCount;
                            }
                        }
                    }
                    if (shapeCount != 0 || edgeCount != 0) {
                        editor.setChanged();
                        editor.getViewer().repaint();
                    }
                    Logger.debug("Removed " + shapeCount + " degenerate shapes and " + edgeCount + " edges");
                    dialog.setVisible(false);
                    dialog.dispose();
                }
            };
        t.start();
        dialog.pack();
        dialog.setVisible(true);
    }
}