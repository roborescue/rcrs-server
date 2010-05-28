package maps.gml.editor;

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
        final Collection<GMLEdge> remaining = new HashSet<GMLEdge>(editor.getMap().getEdges());
        final Collection<GMLEdge> deleted = new HashSet<GMLEdge>();
        for (GMLEdge next : remaining) {
            if (editor.getMap().getAttachedShapes(next).isEmpty()) {
                editor.getMap().removeEdge(next);
                deleted.add(next);
            }
        }
        if (!deleted.isEmpty()) {
            editor.setChanged();
            editor.getViewer().repaint();
        }
        Logger.debug("Removed " + deleted.size() + " edges");
        editor.addEdit(new DeleteEdgesEdit(deleted));
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