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

import rescuecore2.version0.entities.RescueEntity;
import rescuecore2.version0.entities.RescueEntityFactory;
import rescuecore2.version0.messages.Version0MessageFactory;

import kernel.legacy.LegacyWorldModelCreator;
import kernel.legacy.LegacyComponentManager;
import kernel.legacy.LegacyPerception;
import kernel.legacy.LegacyCommunicationModel;
import kernel.legacy.IndexedWorldModel;
import kernel.ui.KernelStatus;

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
        boolean gui = true;
        boolean justRun = false;
        try {
            int i = 0;
            while (i < args.length) {
                if (args[i].equalsIgnoreCase(CONFIG_FLAG) || args[i].equalsIgnoreCase(CONFIG_LONG_FLAG)) {
                    config.read(new File(args[++i]));
                }
                else if (args[i].equalsIgnoreCase(NO_GUI)) {
                    gui = false;
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
            if (gui) {
                KernelStatus status = new KernelStatus(config, kernel, !justRun);
                kernel.addKernelListener(status);
                status.activate();
                JFrame frame = new JFrame("Kernel status");
                frame.getContentPane().add(status);
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
            if (!gui || justRun) {
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
        public Kernel createKernel(Config config) throws KernelException;

        /**
           Initialise a kernel: add agents/viewers/simulators if required and do any other necessary startup.
           @param kernel The kernel to initialise.
           @param config The kernel configuration.
           @throws KernelException If there is a problem initialising the kernel.
        */
        public void initialiseKernel(Kernel kernel, Config config) throws KernelException;
    }

    private static class LegacyKernelBuilder implements KernelBuilder {
        @Override
        public Kernel createKernel(Config config) throws KernelException {
            // Register legacy messages and entities
            MessageRegistry.register(Version0MessageFactory.INSTANCE);
            EntityRegistry.register(RescueEntityFactory.INSTANCE);
            // Get the world model
            IndexedWorldModel worldModel = new LegacyWorldModelCreator().buildWorldModel(config);
            LegacyPerception perception = new LegacyPerception(config, worldModel);
            LegacyCommunicationModel comms = new LegacyCommunicationModel(config, worldModel);
            return new Kernel(config, perception, comms, worldModel);
        }

        @Override
        public void initialiseKernel(Kernel kernel, Config config) throws KernelException {
            IndexedWorldModel worldModel = (IndexedWorldModel)kernel.getWorldModel();
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