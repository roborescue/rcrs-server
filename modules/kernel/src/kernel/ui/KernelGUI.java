package kernel.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import kernel.Kernel;
import kernel.ComponentManager;

import rescuecore2.config.Config;

/**
   A GUI for the kernel.
 */
public class KernelGUI extends JPanel {
    private static final int STATUS_SIZE = 300;

    private Kernel kernel;
    private KernelStatus status;
    private KernelControlPanel control;
    private JTabbedPane tabs;

    /**
       Construct a KernelGUI component.
       @param kernel The kernel to watch.
       @param componentManager The kernel component manager.
       @param config The kernel configuration.
       @param allowControl Whether to allow the control buttons or not.
    */
    public KernelGUI(Kernel kernel, ComponentManager componentManager, Config config, boolean allowControl) {
        super(new BorderLayout());
        this.kernel = kernel;
        status = new KernelStatus(kernel);
        status.setPreferredSize(new Dimension(STATUS_SIZE, STATUS_SIZE));
        add(status, BorderLayout.EAST);
        tabs = new JTabbedPane();
        add(tabs, BorderLayout.CENTER);
        if (allowControl) {
            control = new KernelControlPanel(kernel, config, componentManager);
            add(control, BorderLayout.WEST);
            control.activate();
        }
        addKernelGUIComponent(componentManager);
    }

    /**
       Add a kernel GUI component.
       @param c The GUI component to add.
     */
    public void addKernelGUIComponent(KernelGUIComponent c) {
        tabs.addTab(c.getGUIComponentName(), c.getGUIComponent(kernel));
    }
}