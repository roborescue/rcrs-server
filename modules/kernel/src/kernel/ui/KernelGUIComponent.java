package kernel.ui;

import javax.swing.JComponent;
import kernel.Kernel;

/**
   Implementers of this interface indicate to the kernel that they support a GUI component.
 */
public interface KernelGUIComponent {
    /**
       Get a JComponent that should be added to the GUI.
       @param kernel The kernel.
       @return A JComponent.
     */
    JComponent getGUIComponent(Kernel kernel);

    /**
       Get the name of this part of the kernel GUI. This will be used in things like tabbed panes and borders around GUI components.
       @return The name of this GUI component.
     */
    String getGUIComponentName();
}