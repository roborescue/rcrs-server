package kernel;

import java.io.File;
import java.io.IOException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;

import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionManager;
import rescuecore2.connection.ConnectionManagerListener;
import rescuecore2.config.Config;
import rescuecore2.config.ConfigException;
import rescuecore2.messages.MessageRegistry;
import rescuecore2.worldmodel.EntityRegistry;

import rescuecore2.standard.entities.StandardEntityFactory;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.messages.StandardMessageFactory;

import kernel.standard.LegacyComponentManager;
import kernel.standard.StandardWorldModelCreator;
import kernel.standard.StandardPerception;
import kernel.standard.StandardCommunicationModel;
import kernel.ui.KernelGUI;

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
            KernelBuilder builder = new LegacyKernelBuilder();
            final Kernel kernel = builder.createKernel(config);
            if (showGUI) {
                KernelGUI gui = new KernelGUI(kernel, config, !justRun);
                JFrame frame = new JFrame("Kernel GUI");
                frame.getContentPane().add(gui);
                frame.pack();
                frame.addWindowListener(new WindowAdapter() {
                        public void windowClosing(WindowEvent e) {
                            kernel.shutdown();
                            System.exit(0);
                        }
                    });
                frame.setVisible(true);
            }
            builder.initialiseKernel(kernel, config);
            if (!showGUI || justRun) {
                int maxTime = config.getIntValue("timesteps");
                while (kernel.getTime() <= maxTime) {
                    kernel.timestep();
                }
                kernel.shutdown();
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

    private static interface KernelBuilder {
        /**
           Create a new Kernel object.
           @param config The kernel configuration.
           @return A new, uninitialised Kernel object.
           @throws KernelException If there is a problem constructing the kernel.
        */
        Kernel createKernel(Config config) throws KernelException;

        /**
           Initialise a kernel: add agents/viewers/simulators if required and do any other necessary startup.
           @param kernel The kernel to initialise.
           @param config The kernel configuration.
           @throws KernelException If there is a problem initialising the kernel.
        */
        void initialiseKernel(Kernel kernel, Config config) throws KernelException;
    }

    private static class LegacyKernelBuilder implements KernelBuilder {
        @Override
        public Kernel createKernel(Config config) throws KernelException {
            // Register legacy messages and entities
            MessageRegistry.register(StandardMessageFactory.INSTANCE);
            EntityRegistry.register(StandardEntityFactory.INSTANCE);
            // Get the world model
            StandardWorldModel worldModel = new StandardWorldModelCreator().buildWorldModel(config);
            StandardPerception perception = new StandardPerception(config, worldModel);
            StandardCommunicationModel comms = new StandardCommunicationModel(config, worldModel);
            return new Kernel(config, perception, comms, worldModel);
        }

        @Override
        public void initialiseKernel(Kernel kernel, Config config) throws KernelException {
            StandardWorldModel worldModel = (StandardWorldModel)kernel.getWorldModel();
            final LegacyComponentManager manager = new LegacyComponentManager(kernel, worldModel, config);

            // Start the connection manager
            ConnectionManager connectionManager = new ConnectionManager();
            ConnectionManagerListener listener = new ConnectionManagerListener() {
                    public void newConnection(Connection c) {
                        System.out.println("New connection: " + c);
                        manager.newConnection(c);
                        c.startup();
                    }
                };
            try {
                connectionManager.listen(config.getIntValue("kernel_port"), listener);
            }
            catch (IOException e) {
                throw new KernelException("Couldn't open kernel port", e);
            }
            // Wait for all connections
            try {
                manager.waitForAllAgents();
                manager.waitForAllSimulators();
                manager.waitForAllViewers();
            }
            catch (InterruptedException e) {
                throw new KernelException(e);
            }
        }
    }
}