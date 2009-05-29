package kernel;

import java.io.File;
import java.io.IOException;

import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionManager;
import rescuecore2.connection.ConnectionManagerListener;
import rescuecore2.config.Config;
import rescuecore2.config.ConfigException;

import rescuecore2.version0.entities.RescueEntity;

import kernel.ui.KernelStatus;
import javax.swing.JFrame;

import kernel.legacy.GISWorldModelCreator;
import kernel.legacy.LegacyComponentManager;
import kernel.legacy.LegacyPerception;
import kernel.legacy.LegacyCommunicationModel;
import kernel.legacy.IndexedWorldModel;
import rescuecore2.version0.messages.Version0MessageFactory;

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
            Kernel<RescueEntity> kernel = createLegacyKernel(config);
            if (gui) {
                KernelStatus<RescueEntity> status = new KernelStatus<RescueEntity>(kernel, config, !justRun);
                kernel.addKernelListener(status);
                JFrame frame = new JFrame("Kernel status");
                frame.getContentPane().add(status);
                frame.pack();
                frame.setVisible(true);
            }
            setupLegacyKernel(config, kernel);
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

    private static Kernel<RescueEntity> createLegacyKernel(Config config) throws KernelException, ConfigException, InterruptedException {
        // Get the world model
        IndexedWorldModel worldModel = new GISWorldModelCreator().buildWorldModel(config);
        LegacyPerception perception = new LegacyPerception(config, worldModel);
        LegacyCommunicationModel comms = new LegacyCommunicationModel(config, worldModel);
        return new Kernel<RescueEntity>(config, perception, comms, worldModel);
    }

    private static void setupLegacyKernel(Config config, Kernel<RescueEntity> kernel) throws KernelException, InterruptedException {
        final LegacyComponentManager manager = new LegacyComponentManager(kernel, config);

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
            connectionManager.listen(config.getIntValue("kernel_port"), Version0MessageFactory.INSTANCE, listener);
        }
        catch (IOException e) {
            throw new KernelException("Couldn't open kernel port", e);
        }
        // Wait for all connections
        manager.waitForAllAgents();
        manager.waitForAllSimulators();
        manager.waitForAllViewers();
    }
}