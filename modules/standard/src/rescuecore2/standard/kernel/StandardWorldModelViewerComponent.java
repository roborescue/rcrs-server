package rescuecore2.standard.kernel;

import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JPanel;

import java.util.List;

import rescuecore2.view.EntityInspector;
import rescuecore2.view.ViewListener;
import rescuecore2.view.ViewComponent;
import rescuecore2.view.RenderedObject;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
//import rescuecore2.config.Config;
import rescuecore2.Timestep;
import rescuecore2.GUIComponent;

import rescuecore2.standard.view.StandardWorldModelViewer;

import kernel.Kernel;
import kernel.KernelListenerAdapter;

/**
   A KernelGUIComponent that will view a standard world model.
*/
public class StandardWorldModelViewerComponent extends KernelListenerAdapter implements GUIComponent {
    private static final int SIZE = 500;

    private StandardWorldModelViewer viewer;
    private EntityInspector inspector;
    private JTextField field;
    private JComponent view;
    private WorldModel<? extends Entity> world;

    /**
       Construct a StandardWorldModelViewerComponent.
    */
    public StandardWorldModelViewerComponent() {
        viewer = new StandardWorldModelViewer();
        inspector = new EntityInspector();
        field = new JTextField();
        viewer.setPreferredSize(new Dimension(SIZE, SIZE));
        viewer.addViewListener(new ViewListener() {
                @Override
                public void objectsClicked(ViewComponent v, List<RenderedObject> objects) {
                    for (RenderedObject next : objects) {
                        if (next.getObject() instanceof Entity) {
                            inspector.inspect((Entity)next.getObject());
                            field.setText("");
                            return;
                        }
                    }
                }

                @Override
                public void objectsRollover(ViewComponent v, List<RenderedObject> objects) {
                }
            });
        field.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    String s = field.getText();
                    try {
                        int id = Integer.parseInt(s);
                        EntityID eid = new EntityID(id);
                        Entity e = world.getEntity(eid);
                        inspector.inspect(e);
                    }
                    catch (NumberFormatException e) {
                        field.setText("");
                    }
                }
            });
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, viewer, new JScrollPane(inspector));
        split.setResizeWeight(0.8);
        view = new JPanel(new BorderLayout());
        view.add(split, BorderLayout.CENTER);
        view.add(field, BorderLayout.NORTH);
    }

    @Override
    public void simulationStarted(Kernel kernel) {
        viewer.initialise(kernel.getConfig());
        world = kernel.getWorldModel();
        viewer.view(world);
        viewer.repaint();
    }

    @Override
    public void timestepCompleted(Kernel kernel, Timestep time) {
        viewer.view(kernel.getWorldModel(), time.getCommands(), time.getChangeSet());
        viewer.repaint();
    }

    @Override
    public JComponent getGUIComponent() {
        return view;
    }

    @Override
    public String getGUIComponentName() {
        return "World view";
    }
}
