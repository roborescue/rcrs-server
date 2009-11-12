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
import javax.swing.JFrame;
import java.util.Collection;

/**
   A simple viewer.
 */
public class SampleViewer extends AbstractViewer<StandardEntity> {
    private ViewComponent viewer;
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
        frame.add(viewer);
        frame.pack();
        frame.setVisible(true);
    }

    @Override
    protected void handleCommands(Commands c) {
        commands = c.getCommands();
    }

    @Override
    protected void handleUpdate(Update u) {
        super.handleUpdate(u);
        viewer.view(world, commands);
        viewer.repaint();
    }

    @Override
    public String toString() {
        return "Sample viewer";
    }
}