package kernel.ui;

import javax.swing.JComponent;
import kernel.Kernel;

import rescuecore2.config.Config;

/**
   Implementers of this interface indicate to the kernel that they support a GUI component.
 */
public interface KernelGUIComponent {
    /**
       Get a JComponent that should be added to the GUI.
       @param kernel The kernel.
       @param config The system configuration.
       @return A JComponent.
     */
    JComponent getGUIComponent(Kernel kernel, Config config);

    /**
       Get the name of this part of the kernel GUI. This will be used in things like tabbed panes and borders around GUI components.
       @return The name of this GUI component.
     */
    String getGUIComponentName();
}