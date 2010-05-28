package maps.gml.editor;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import maps.gml.GMLEdge;

import rescuecore2.log.Logger;

/**
   A function for fixing duplicate edges.
*/
public class FixDuplicateEdgesFunction extends ProgressFunction {
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
    protected String getTitle() {
        return "Fixing duplicate edges";
    }

    @Override
    protected void executeImpl() {
        // Go through all edges and replace any duplicates
        final Set<GMLEdge> remaining = new HashSet<GMLEdge>(editor.getMap().getEdges());
        setProgressLimit(remaining.size());
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
                    bumpProgress();
                }
            }
            bumpProgress();
        }
        if (count != 0) {
            editor.setChanged();
            editor.getViewer().repaint();
        }
        Logger.debug("Removed " + count + " duplicate edges");
    }
}