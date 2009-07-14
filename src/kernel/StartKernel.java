package kernel;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JComponent;
import javax.swing.WindowConstants;

import rescuecore2.connection.ConnectionManager;
import rescuecore2.config.Config;
import rescuecore2.config.ConfigException;
import rescuecore2.messages.MessageRegistry;
import rescuecore2.worldmodel.EntityRegistry;
import rescuecore2.view.WorldModelViewer;

import rescuecore2.standard.entities.StandardEntityFactory;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.messages.StandardMessageFactory;
import rescuecore2.standard.view.StandardViewLayer;

import kernel.standard.StandardComponentManager;
//import kernel.standard.StandardWorldModelCreator;
import kernel.standard.InlineWorldModelCreator;
import kernel.standard.StandardPerception;
import kernel.standard.TunableStandardPerception;
import kernel.standard.StandardCommunicationModel;
import kernel.standard.ChannelCommunicationModel;
import kernel.ui.KernelGUI;
import kernel.ui.KernelGUIComponent;

/**
   A class for launching the kernel.
 */
public final class StartKernel {
    private static final String CONFIG_FLAG = "-c";
    private static final String CONFIG_LONG_FLAG = "--config";
    private static final String NO_GUI = "--nogui";
    private static final String JUST_RUN = "--just-run";

    /** Utility class: private constructor. */
    private StartKernel() {}

    /**
       Start a kernel.
       @param args Command line arguments.
    */
    public static void main(String[] args) {
        Config config = new Config();
        boolean showGUI = true;
        boolean justRun = false;
        try {
            int i = 0;
            while (i < args.length) {
                if (args[i].equalsIgnoreCase(CONFIG_FLAG) || args[i].equalsIgnoreCase(CONFIG_LONG_FLAG)) {
                    config.read(new File(args[++i]));
                }
                else if (args[i].equalsIgnoreCase(NO_GUI)) {
                    showGUI = false;
                }
                else if (args[i].equalsIgnoreCase(JUST_RUN)) {
                    justRun = true;
                }
                else {
                    System.out.println("Unrecognised option: " + args[i]);
                }
                ++i;
            }
            KernelBuilder builder = new StandardKernelBuilder();
            final KernelInfo kernelInfo = builder.createKernel(config);
            if (showGUI) {
                KernelGUI gui = new KernelGUI(kernelInfo.kernel, kernelInfo.componentManager, config, !justRun);
                for (KernelGUIComponent next : kernelInfo.guiComponents) {
                    gui.addKernelGUIComponent(next);
                }
                JFrame frame = new JFrame("Kernel GUI");
                frame.getContentPane().add(gui);
                frame.pack();
                frame.addWindowListener(new WindowAdapter() {
                        public void windowClosing(WindowEvent e) {
                            kernelInfo.kernel.shutdown();
                            System.exit(0);
                        }
                    });
                frame.setVisible(true);
            }
            initialiseKernel(kernelInfo, config);
            if (!showGUI || justRun) {
                int maxTime = config.getIntValue("timesteps");
                while (kernelInfo.kernel.getTime() <= maxTime) {
                    kernelInfo.kernel.timestep();
                }
                kernelInfo.kernel.shutdown();
            }
        }
        catch (ConfigException e) {
            System.err.println("Couldn't start kernel");
            e.printStackTrace();
        }
        catch (KernelException e) {
            System.err.println("Couldn't start kernel");
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void initialiseKernel(KernelInfo kernel, Config config) throws KernelException {
        // Start the connection manager
        ConnectionManager connectionManager = new ConnectionManager();
        try {
            connectionManager.listen(config.getIntValue("kernel_port"), kernel.componentManager);
        }
        catch (IOException e) {
            throw new KernelException("Couldn't open kernel port", e);
        }
        // Wait for all connections
        try {
            kernel.componentManager.waitForAllAgents();
            kernel.componentManager.waitForAllSimulators();
            kernel.componentManager.waitForAllViewers();
        }
        catch (InterruptedException e) {
            throw new KernelException(e);
        }
    }

    private static interface KernelBuilder {
        /**
           Create a new KernelInfo object.
           @param config The kernel configuration.
           @return A new KernelInfo object.
           @throws KernelException If there is a problem constructing the kernel.
        */
        KernelInfo createKernel(Config config) throws KernelException;
    }

    private static class StandardKernelBuilder implements KernelBuilder {
        @Override
        public KernelInfo createKernel(Config config) throws KernelException {
            // Register standard messages and entities
            MessageRegistry.register(StandardMessageFactory.INSTANCE);
            EntityRegistry.register(StandardEntityFactory.INSTANCE);
            // Get the world model
            //            StandardWorldModel worldModel = new StandardWorldModelCreator().buildWorldModel(config);
            StandardWorldModel worldModel = new InlineWorldModelCreator().buildWorldModel(config);
            // Show the chooser GUI
            Perception[] perceptionChoices = new Perception[] {new StandardPerception(config, worldModel), new TunableStandardPerception(config, worldModel)};
            CommunicationModel[] commsChoices = new CommunicationModel[] {new StandardCommunicationModel(config, worldModel), new ChannelCommunicationModel(config, worldModel)};
            KernelChooserDialog dialog = new KernelChooserDialog(perceptionChoices, commsChoices);
            dialog.setVisible(true);
            Perception perception = dialog.getPerception();
            CommunicationModel comms = dialog.getCommunicationModel();
            Kernel kernel = new Kernel(config, perception, comms, worldModel);
            ComponentManager componentManager = new StandardComponentManager(kernel, worldModel, config);
            return new KernelInfo(kernel, perception, comms, componentManager, new StandardWorldModelViewerComponent(worldModel));
        }
    }

    private static class KernelChooserDialog extends JDialog {
        private JComboBox perceptionChooser;
        private JComboBox commsChooser;

        public KernelChooserDialog(Perception[] perceptionChoices, CommunicationModel[] commsChoices) {
            super((Frame)null, "Choose kernel options");
            perceptionChooser = new JComboBox(perceptionChoices);
            commsChooser = new JComboBox(commsChoices);
            JPanel main = new JPanel(new GridLayout(2, 2));
            main.add(new JLabel("Perception: "));
            main.add(perceptionChooser);
            main.add(new JLabel("Communication model: "));
            main.add(commsChooser);
            JButton ok = new JButton("OK");
            add(main, BorderLayout.CENTER);
            add(ok, BorderLayout.SOUTH);
            ok.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        setVisible(false);
                    }
                });
            setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
            pack();
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        }

        public Perception getPerception() {
            System.out.println("Selected index :" + perceptionChooser.getSelectedIndex() + " -> " + perceptionChooser.getSelectedItem());
            return (Perception)perceptionChooser.getSelectedItem();
        }

        public CommunicationModel getCommunicationModel() {
            return (CommunicationModel)commsChooser.getSelectedItem();
        }
    }

    private static class KernelInfo {
        Kernel kernel;
        ComponentManager componentManager;
        List<KernelGUIComponent> guiComponents;

        public KernelInfo(Kernel kernel, Perception perception, CommunicationModel comms, ComponentManager componentManager, KernelGUIComponent... otherComponents) {
            this.kernel = kernel;
            this.componentManager = componentManager;
            guiComponents = new ArrayList<KernelGUIComponent>();
            if (perception instanceof KernelGUIComponent) {
                guiComponents.add((KernelGUIComponent)perception);
            }
            if (comms instanceof KernelGUIComponent) {
                guiComponents.add((KernelGUIComponent)comms);
            }
            for (KernelGUIComponent next : otherComponents) {
                guiComponents.add(next);
            }
        }
    }

    private static class StandardWorldModelViewerComponent implements KernelGUIComponent {
        private StandardWorldModel world;

        public StandardWorldModelViewerComponent(StandardWorldModel world) {
            this.world = world;
        }

        @Override
        public JComponent getGUIComponent(Kernel kernel) {
            final WorldModelViewer viewer = new WorldModelViewer();
            // CHECKSTYLE:OFF:MagicNumber
            viewer.setPreferredSize(new Dimension(500, 500));
            // CHECKSTYLE:ON:MagicNumber
            viewer.addLayer(new StandardViewLayer());
            viewer.setWorldModel(world);
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
}