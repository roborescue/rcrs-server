package maps.gml.editor;

import java.util.Set;
import java.util.HashSet;

import maps.gml.GMLShape;
import maps.gml.GMLEdge;

/**
   A function for fixing the lists of attached shapes.
*/
public class FixAttachedObjectsFunction extends ProgressFunction {
    /**
       Construct a FixAttachedObjectsFunction.
       @param editor The editor instance.
    */
    public FixAttachedObjectsFunction(GMLEditor editor) {
        super(editor);
    }

    @Override
    public String getName() {
        return "Fix attached objects";
    }

    @Override
    protected String getTitle() {
        return "Fixing attached objects";
    }

    @Override
    protected void executeImpl() {
        // Remove and re-add all edges and shapes.
        final Set<GMLShape> shapes = new HashSet<GMLShape>();
        final Set<GMLEdge> edges = new HashSet<GMLEdge>();
        synchronized (editor.getMap()) {
            shapes.addAll(editor.getMap().getAllShapes());
            edges.addAll(editor.getMap().getEdges());
        }
        setProgressLimit(shapes.size() + edges.size());
        synchronized (editor.getMap()) {
            editor.getMap().removeAllEdges();
        }
        for (GMLEdge next : edges) {
            synchronized (editor.getMap()) {
                editor.getMap().add(next);
            }
            bumpProgress();
        }
        for (GMLShape next : shapes) {
            synchronized (editor.getMap()) {
                editor.getMap().add(next);
            }
            bumpProgress();
        }
        editor.setChanged();
        editor.getViewer().repaint();
    }
}