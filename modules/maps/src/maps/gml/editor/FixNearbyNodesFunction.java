package maps.gml.editor;

import java.awt.Window;
import java.awt.Dialog;
import java.awt.BorderLayout;
import javax.swing.JDialog;
import javax.swing.JProgressBar;
import javax.swing.JOptionPane;

import java.util.Set;
import java.util.HashSet;

import maps.gml.GMLNode;

import rescuecore2.log.Logger;

/**
   A function for fixing nearby nodes.
*/
public class FixNearbyNodesFunction extends AbstractFunction {
    private static final double DEFAULT_TOLERANCE = 0.001;

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
    public void execute() {
        String s = JOptionPane.showInputDialog(editor.getViewer(), "Enter the desired tolerance (in m)", DEFAULT_TOLERANCE);
        if (s == null) {
            return;
        }
        final double tolerance = Double.parseDouble(s);
        // Go through all nodes and replace any nearby ones.
        final JDialog dialog = new JDialog((Window)editor.getViewer().getTopLevelAncestor(), "Fixing nearby nodes", Dialog.ModalityType.APPLICATION_MODAL);
        final Set<GMLNode> remaining = new HashSet<GMLNode>(editor.getMap().getNodes());
        final JProgressBar progress = new JProgressBar(0, remaining.size());
        progress.setStringPainted(true);
        dialog.getContentPane().add(progress, BorderLayout.CENTER);
        Thread t = new Thread() {
                @Override
                public void run() {
                    int count = 0;
                    while (!remaining.isEmpty()) {
                        GMLNode next = remaining.iterator().next();
                        remaining.remove(next);
                        double x = next.getX();
                        double y = next.getY();
                        //                        Logger.debug("Next node: " + next);
                        //                        Logger.debug("Finding nodes near " + x + ", " + y);
                        progress.setValue(progress.getValue() + 1);
                        for (GMLNode replaced : editor.getMap().getNodesInRegion(x - tolerance, y - tolerance, x + tolerance, y + tolerance)) {
                            if (replaced == next) {
                                continue;
                            }
                            //                            Logger.debug("Found " + replaced);
                            editor.getMap().replaceNode(replaced, next);
                            remaining.remove(replaced);
                            editor.getMap().removeNode(replaced);
                            ++count;
                            progress.setValue(progress.getValue() + 1);
                        }
                    }
                    if (count != 0) {
                        editor.setChanged();
                        editor.getViewer().repaint();
                    }
                    Logger.debug("Removed " + count + " nodes");
                    dialog.setVisible(false);
                    dialog.dispose();
                }
            };
        t.start();
        dialog.pack();
        dialog.setVisible(true);
    }
}