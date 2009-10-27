package kernel.ui;

import java.awt.Dimension;
import javax.swing.JComponent;

import rescuecore2.standard.view.StandardWorldModelViewer;

import kernel.Kernel;
import kernel.KernelListenerAdapter;
import kernel.Timestep;

/**
   A KernelGUIComponent that will view a standard world model.
*/
public class StandardWorldModelViewerComponent implements KernelGUIComponent {
    private static final int SIZE = 500;

    @Override
    public JComponent getGUIComponent(final Kernel kernel) {
        final StandardWorldModelViewer viewer = new StandardWorldModelViewer();
        viewer.setPreferredSize(new Dimension(SIZE, SIZE));
        viewer.view(kernel.getWorldModel());
        kernel.addKernelListener(new KernelListenerAdapter() {
                @Override
                public void timestepCompleted(Timestep time) {
                    viewer.view(kernel.getWorldModel(), time.getCommands(), time.getUpdates());
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
