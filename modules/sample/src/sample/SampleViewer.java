package sample;

import rescuecore2.components.AbstractViewer;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.messages.Command;
import rescuecore2.messages.control.Commands;
import rescuecore2.messages.control.Update;
import rescuecore2.view.ViewComponent;

import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.view.AnimatedWorldModelViewer;

import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import java.util.Collection;

/**
   A simple viewer.
 */
public class SampleViewer extends AbstractViewer<StandardEntity> {
    private static final int FONT_SIZE = 20;

    private ViewComponent viewer;
    private JLabel timeLabel;
    private StandardWorldModel world;
    private Collection<Command> commands;

    @Override
    protected WorldModel<StandardEntity> createWorldModel() {
        world = new StandardWorldModel();
        return world;
    }

    @Override
    protected void postConnect() {
        world.index();
        JFrame frame = new JFrame("Viewer " + getViewerID() + " (" + world.getAllEntities().size() + " entities)");
        viewer = new AnimatedWorldModelViewer();
        viewer.initialise(config);
        viewer.view(world);
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
    }

    @Override
    protected void handleCommands(Commands c) {
        commands = c.getCommands();
    }

    @Override
    protected void handleUpdate(final Update u) {
        super.handleUpdate(u);
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    timeLabel.setText("Time: " + u.getTime());
                    viewer.view(world, commands);
                    viewer.repaint();
                }
            });
    }

    @Override
    public String toString() {
        return "Sample viewer";
    }
}