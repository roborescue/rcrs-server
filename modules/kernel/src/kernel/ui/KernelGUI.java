package kernel.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import kernel.Kernel;
import kernel.ComponentManager;

import rescuecore2.config.Config;
import rescuecore2.registry.Registry;
import rescuecore2.GUIComponent;

/**
   A GUI for the kernel.
 */
public class KernelGUI extends JPanel {
    private static final int STATUS_SIZE = 300;

    private Kernel kernel;
    private KernelStatus status;
    private KernelControlPanel control;
    private JTabbedPane tabs;
    private Config config;

    /**
       Construct a KernelGUI component.
       @param kernel The kernel to watch.
       @param componentManager The kernel component manager.
       @param config The kernel configuration.
       @param registry The registry to use for new connections.
       @param controls Whether to show the control panel or not.
    */
    public KernelGUI(Kernel kernel, ComponentManager componentManager, Config config, Registry registry, boolean controls) {
        super(new BorderLayout());
        this.kernel = kernel;
        this.config = config;
        status = new KernelStatus(kernel);
        status.setPreferredSize(new Dimension(STATUS_SIZE, STATUS_SIZE));
        add(status, BorderLayout.EAST);
        tabs = new JTabbedPane();
        add(tabs, BorderLayout.CENTER);
        if (controls) {
            control = new KernelControlPanel(kernel, config, componentManager, registry);
            add(control, BorderLayout.WEST);
            control.activate();
        }
        addGUIComponent(componentManager);
        
    }

    /**
       Add a kernel GUI component.
       @param c The GUI component to add.
     */
    public void addGUIComponent(final GUIComponent c) {
        SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (c.getGUIComponent() != null) {
                        tabs.addTab(c.getGUIComponentName(), c.getGUIComponent());
                    }
                }
            });
    }
}
