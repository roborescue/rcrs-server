package kernel;

import java.io.File;
import java.io.IOException;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.GridLayout;
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
import javax.swing.WindowConstants;

import rescuecore2.connection.ConnectionManager;
import rescuecore2.config.Config;
import rescuecore2.config.ConfigException;
import rescuecore2.messages.MessageRegistry;
import rescuecore2.worldmodel.EntityRegistry;

import rescuecore2.standard.entities.StandardEntityFactory;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.messages.StandardMessageFactory;

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
                KernelGUI gui = new KernelGUI(kernelInfo.kernel, config, !justRun);
                if (kernelInfo.perception instanceof KernelGUIComponent) {
                    gui.addKernelGUIComponent((KernelGUIComponent)kernelInfo.perception);
                }
                if (kernelInfo.comms instanceof KernelGUIComponent) {
                    gui.addKernelGUIComponent((KernelGUIComponent)kernelInfo.comms);
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
        catch (IOException e) {
            System.err.println("Couldn't start kernel");
            e.printStackTrace();
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
            return new KernelInfo(kernel, perception, comms, componentManager);
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
        Perception perception;
        CommunicationModel comms;
        ComponentManager componentManager;

        public KernelInfo(Kernel kernel, Perception perception, CommunicationModel comms, ComponentManager componentManager) {
            this.kernel = kernel;
            this.perception = perception;
            this.comms = comms;
            this.componentManager = componentManager;
        }
    }
}