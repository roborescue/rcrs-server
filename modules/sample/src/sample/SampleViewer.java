package sample;

import rescuecore2.messages.control.KVTimestep;
import rescuecore2.view.ViewComponent;
import rescuecore2.view.ViewListener;
import rescuecore2.view.RenderedObject;

import rescuecore2.standard.view.AnimatedWorldModelViewer;

import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import java.util.List;

import rescuecore2.standard.components.StandardViewer;

/**
   A simple viewer.
 */
public class SampleViewer extends StandardViewer {
    private static final int FONT_SIZE = 20;

    private ViewComponent viewer;
    private JLabel timeLabel;

    @Override
    protected void postConnect() {
        super.postConnect();
        JFrame frame = new JFrame("Viewer " + getViewerID() + " (" + model.getAllEntities().size() + " entities)");
        viewer = new AnimatedWorldModelViewer();
        viewer.initialise(config);
        viewer.view(model);
        // CHECKSTYLE:OFF:MagicNumber
        viewer.setPreferredSize(new Dimension(500, 500));
        // CHECKSTYLE:ON:MagicNumber
        timeLabel = new JLabel("Time: Not started", JLabel.CENTER);
        timeLabel.setBackground(Color.WHITE);
        timeLabel.setOpaque(true);
        timeLabel.setFont(timeLabel.getFont().deriveFont(Font.PLAIN, FONT_SIZE));
        frame.add(viewer, BorderLayout.CENTER);
        frame.add(timeLabel, BorderLayout.NORTH);
        frame.pack();
        frame.setVisible(true);

        viewer.addViewListener(new ViewListener() {
                @Override
                public void objectsClicked(ViewComponent view, List<RenderedObject> objects) {
                    for (RenderedObject next : objects) {
                        System.out.println(next.getObject());
                    }
                }

                @Override
                public void objectsRollover(ViewComponent view, List<RenderedObject> objects) {
                }
            });
    }

    @Override
    protected void handleTimestep(final KVTimestep t) {
        super.handleTimestep(t);
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    timeLabel.setText("Time: " + t.getTime());
                    viewer.view(model, t.getCommands());
                    viewer.repaint();
                }
            });
    }

    @Override
    public String toString() {
        return "Sample viewer";
    }
}
