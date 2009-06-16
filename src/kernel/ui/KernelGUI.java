package kernel.ui;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import kernel.Kernel;

import rescuecore2.config.Config;

/**
   A GUI for the kernel.
 */
public class KernelGUI extends JPanel {
    private KernelStatus status;
    private KernelControlPanel control;
    private JTabbedPane tabs;

    /**
       Construct a KernelGUI component.
       @param kernel The kernel to watch.
       @param config The kernel configuration.
       @param allowControl Whether to allow the control buttons or not.
    */
    public KernelGUI(Kernel kernel, Config config, boolean allowControl) {
        super(new BorderLayout());
        status = new KernelStatus(kernel);
        add(status, BorderLayout.CENTER);
        tabs = new JTabbedPane();
        add(tabs, BorderLayout.EAST);
        if (allowControl) {
            control = new KernelControlPanel(kernel, config);
            add(control, BorderLayout.WEST);
            control.activate();
        }
    }

    /**
       Add a kernel GUI component.
       @param c The GUI component to add.
     */
    public void addKernelGUIComponent(KernelGUIComponent c) {
        tabs.addTab(c.getGUIComponentName(), c.getGUIComponent());
    }
}