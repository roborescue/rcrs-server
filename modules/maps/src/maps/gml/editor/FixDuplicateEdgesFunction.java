package maps.gml.editor;

import java.awt.Window;
import java.awt.Dialog;
import java.awt.BorderLayout;
import javax.swing.JDialog;
import javax.swing.JProgressBar;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import maps.gml.GMLEdge;

import rescuecore2.log.Logger;

/**
   A function for fixing duplicate edges.
*/
public class FixDuplicateEdgesFunction extends AbstractFunction {
    /**
       Construct a FixDuplicateEdgesFunction.
       @param editor The editor instance.
    */
    public FixDuplicateEdgesFunction(GMLEditor editor) {
        super(editor);
    }

    @Override
    public String getName() {
        return "Fix duplicate edges";
    }

    @Override
    public void execute() {
        // Go through all edges and replace any duplicates
        final JDialog dialog = new JDialog((Window)editor.getViewer().getTopLevelAncestor(), "Fixing duplicate edges", Dialog.ModalityType.APPLICATION_MODAL);
        final Set<GMLEdge> remaining = new HashSet<GMLEdge>(editor.getMap().getEdges());
        final JProgressBar progress = new JProgressBar(0, remaining.size());
        progress.setStringPainted(true);
        dialog.getContentPane().add(progress, BorderLayout.CENTER);
        Thread t = new Thread() {
                @Override
                public void run() {
                    int count = 0;
                    while (!remaining.isEmpty()) {
                        GMLEdge next = remaining.iterator().next();
                        remaining.remove(next);
                        // Look at other edges for a duplicate
                        Iterator<GMLEdge> it = remaining.iterator();
                        while (it.hasNext()) {
                            GMLEdge test = it.next();
                            if ((test.getStart() == next.getStart() || test.getStart() == next.getEnd())
                                && (test.getEnd() == next.getStart() || test.getEnd() == next.getEnd())) {
                                // Duplicate found
                                editor.getMap().replaceEdge(test, next);
                                editor.getMap().removeEdge(test);
                                it.remove();
                                ++count;
                                progress.setValue(progress.getValue() + 1);
                            }
                        }
                        progress.setValue(progress.getValue() + 1);
                    }
                    if (count != 0) {
                        editor.setChanged();
                        editor.getViewer().repaint();
                    }
                    Logger.debug("Removed " + count + " duplicate edges");
                    dialog.setVisible(false);
                    dialog.dispose();
                }
            };
        t.start();
        dialog.pack();
        dialog.setVisible(true);
    }
}