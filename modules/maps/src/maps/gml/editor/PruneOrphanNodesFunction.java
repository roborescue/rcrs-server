package maps.gml.editor;

import javax.swing.undo.AbstractUndoableEdit;

import java.util.HashSet;
import java.util.Collection;

import maps.gml.GMLNode;

import rescuecore2.log.Logger;

/**
   A function for pruning nodes that are not attached to any edges.
*/
public class PruneOrphanNodesFunction extends AbstractFunction {
    /**
       Construct a PruneOrphanNodesFunction.
       @param editor The editor instance.
    */
    public PruneOrphanNodesFunction(GMLEditor editor) {
        super(editor);
    }

    @Override
    public String getName() {
        return "Prune orphaned nodes";
    }

    @Override
    public void execute() {
        // Go through all nodes and remove any that are not attached to edges.
        final Collection<GMLNode> remaining = new HashSet<GMLNode>(editor.getMap().getNodes());
        final Collection<GMLNode> deleted = new HashSet<GMLNode>();
        for (GMLNode next : remaining) {
            if (editor.getMap().getAttachedEdges(next).isEmpty()) {
                editor.getMap().removeNode(next);
                deleted.add(next);
            }
        }
        if (!deleted.isEmpty()) {
            editor.setChanged();
            editor.getViewer().repaint();
        }
        Logger.debug("Removed " + deleted.size() + " nodes");
        editor.addEdit(new DeleteNodesEdit(deleted));
    }

    private class DeleteNodesEdit extends AbstractUndoableEdit {
        private Collection<GMLNode> nodes;

        public DeleteNodesEdit(Collection<GMLNode> nodes) {
            this.nodes = nodes;
        }

        @Override
        public void undo() {
            super.undo();
            editor.getMap().add(nodes);
            editor.getViewer().repaint();
        }

        @Override
        public void redo() {
            super.redo();
            editor.getMap().remove(nodes);
            editor.getViewer().repaint();
        }
    }
}