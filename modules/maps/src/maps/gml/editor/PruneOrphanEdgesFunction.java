package maps.gml.editor;

import java.awt.Window;
import java.awt.Dialog;
import java.awt.BorderLayout;
import javax.swing.JDialog;
import javax.swing.JProgressBar;
import javax.swing.undo.AbstractUndoableEdit;

import java.util.HashSet;
import java.util.Collection;

import maps.gml.GMLEdge;

import rescuecore2.log.Logger;

/**
   A function for pruning edges that are not attached to any shapes.
*/
public class PruneOrphanEdgesFunction extends AbstractFunction {
    /**
       Construct a PruneOrphanEdgesFunction.
       @param editor The editor instance.
    */
    public PruneOrphanEdgesFunction(GMLEditor editor) {
        super(editor);
    }

    @Override
    public String getName() {
        return "Prune orphaned edges";
    }

    @Override
    public void execute() {
        // Go through all edges and remove any that are not attached to shapes.
        final JDialog dialog = new JDialog((Window)editor.getViewer().getTopLevelAncestor(), "Pruning orphaned edges", Dialog.ModalityType.APPLICATION_MODAL);
        final Collection<GMLEdge> remaining = new HashSet<GMLEdge>(editor.getMap().getEdges());
        final Collection<GMLEdge> deleted = new HashSet<GMLEdge>();
        final JProgressBar progress = new JProgressBar(0, remaining.size());
        progress.setStringPainted(true);
        dialog.getContentPane().add(progress, BorderLayout.CENTER);
        Thread t = new Thread() {
                @Override
                public void run() {
                    for (GMLEdge next : remaining) {
                        if (editor.getMap().getAttachedShapes(next).isEmpty()) {
                            editor.getMap().removeEdge(next);
                            deleted.add(next);
                        }
                        progress.setValue(progress.getValue() + 1);
                    }
                    if (!deleted.isEmpty()) {
                        editor.setChanged();
                        editor.getViewer().repaint();
                    }
                    Logger.debug("Removed " + deleted.size() + " edges");
                    editor.addEdit(new DeleteEdgesEdit(deleted));
                    dialog.setVisible(false);
                    dialog.dispose();
                }
            };
        t.start();
        dialog.pack();
        dialog.setVisible(true);
    }

    private class DeleteEdgesEdit extends AbstractUndoableEdit {
        private Collection<GMLEdge> edges;

        public DeleteEdgesEdit(Collection<GMLEdge> edges) {
            this.edges = edges;
        }

        @Override
        public void undo() {
            super.undo();
            editor.getMap().add(edges);
            editor.getViewer().repaint();
        }

        @Override
        public void redo() {
            super.redo();
            editor.getMap().remove(edges);
            editor.getViewer().repaint();
        }
    }
}