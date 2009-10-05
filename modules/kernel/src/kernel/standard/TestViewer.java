package kernel.standard;

import rescuecore2.components.AbstractViewer;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.messages.control.Commands;
import rescuecore2.messages.control.Update;

import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.view.StandardWorldModelViewer;

import java.awt.Dimension;
import javax.swing.JFrame;

/**
   A simple viewer.
*/
public class TestViewer extends AbstractViewer<StandardEntity> {
    private StandardWorldModelViewer viewer;
    private StandardWorldModel world;

    @Override
    protected WorldModel<StandardEntity> createWorldModel() {
        world = new StandardWorldModel();
        return world;
    }

    @Override
    protected void postConnect() {
        world.index();
        JFrame frame = new JFrame("Viewer " + getViewerID() + " (" + world.getAllEntities().size() + " entities)");
        viewer = new StandardWorldModelViewer();
        viewer.setWorldModel(world);
        // CHECKSTYLE:OFF:MagicNumber
        viewer.setPreferredSize(new Dimension(500, 500));
        // CHECKSTYLE:ON:MagicNumber
        frame.add(viewer);
        frame.pack();
        frame.setVisible(true);
    }

    @Override
    protected void handleCommands(Commands c) {
    }

    @Override
    protected void handleUpdate(Update u) {
        world.merge(u.getUpdatedEntities());
        viewer.repaint();
    }

    @Override
    public String toString() {
        return "Test viewer";
    }
}