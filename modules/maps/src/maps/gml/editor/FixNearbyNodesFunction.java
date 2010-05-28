package maps.gml.editor;

import javax.swing.JOptionPane;

import java.util.Set;
import java.util.HashSet;

import maps.gml.GMLNode;

import rescuecore2.log.Logger;

/**
   A function for fixing nearby nodes.
*/
public class FixNearbyNodesFunction extends ProgressFunction {
    private static final double DEFAULT_TOLERANCE = 0.001;

    private double tolerance;

    /**
       Construct a FixNearbyNodesFunction.
       @param editor The editor instance.
    */
    public FixNearbyNodesFunction(GMLEditor editor) {
        super(editor);
    }

    @Override
    public String getName() {
        return "Fix nearby nodes";
    }

    @Override
    protected String getTitle() {
        return "Fixing nearby nodes";
    }

    @Override
    public void execute() {
        String s = JOptionPane.showInputDialog(editor.getViewer(), "Enter the desired tolerance (in m)", DEFAULT_TOLERANCE);
        if (s == null) {
            return;
        }
        tolerance = Double.parseDouble(s);
        super.execute();
    }

    @Override
    protected void executeImpl() {
        // Go through all nodes and replace any nearby ones.
        final Set<GMLNode> remaining = new HashSet<GMLNode>(editor.getMap().getNodes());
        setProgressLimit(remaining.size());
        int count = 0;
        while (!remaining.isEmpty()) {
            GMLNode next = remaining.iterator().next();
            remaining.remove(next);
            double x = next.getX();
            double y = next.getY();
            //                        Logger.debug("Next node: " + next);
            //                        Logger.debug("Finding nodes near " + x + ", " + y);
            bumpProgress();
            for (GMLNode replaced : editor.getMap().getNodesInRegion(x - tolerance, y - tolerance, x + tolerance, y + tolerance)) {
                if (replaced == next) {
                    continue;
                }
                //                            Logger.debug("Found " + replaced);
                editor.getMap().replaceNode(replaced, next);
                remaining.remove(replaced);
                editor.getMap().removeNode(replaced);
                ++count;
                bumpProgress();
            }
        }
        if (count != 0) {
            editor.setChanged();
            editor.getViewer().repaint();
        }
        Logger.debug("Removed " + count + " nodes");
    }
}