package kernel.ui;

import java.awt.Dimension;
import javax.swing.JComponent;

import rescuecore2.view.WorldModelViewer;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.view.StandardWorldModelViewer;

import kernel.Kernel;
import kernel.KernelListenerAdapter;

/**
   A KernelGUIComponent that will view a standard world model.
*/
public class StandardWorldModelViewerComponent implements KernelGUIComponent {
    private static final int SIZE = 500;

    @Override
    public JComponent getGUIComponent(Kernel kernel) {
        final WorldModelViewer<StandardWorldModel> viewer = new StandardWorldModelViewer();
        viewer.setPreferredSize(new Dimension(SIZE, SIZE));
        viewer.setWorldModel(StandardWorldModel.createStandardWorldModel(kernel.getWorldModel()));
        kernel.addKernelListener(new KernelListenerAdapter() {
                @Override
                public void timestepCompleted(int time) {
                    viewer.repaint();
                }
            });
        return viewer;
    }

    @Override
    public String getGUIComponentName() {
        return "World view";
    }
}
