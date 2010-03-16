package rescuecore2;

import javax.swing.JComponent;

/**
   Tagging interface for objects that have a GUI component.
 */
public interface GUIComponent {
    /**
       Get a JComponent that should be added to the GUI.
       @return A JComponent.
     */
    JComponent getGUIComponent();

    /**
       Get the name of this part of the GUI. This will be used in things like tabbed panes and borders around GUI components.
       @return The name of this GUI component.
     */
    String getGUIComponentName();
}
