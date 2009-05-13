package kernel;

import java.io.File;
import java.io.IOException;

import rescuecore2.config.Config;
import rescuecore2.config.ConfigException;
import rescuecore2.worldmodel.Entity;

import rescuecore2.version0.entities.RescueObject;

import kernel.legacy.GISWorldModelCreator;
import kernel.legacy.LegacySimulatorManager;
import kernel.legacy.LegacyViewerManager;
import kernel.legacy.LegacyAgentManager;
import kernel.legacy.LegacyPerception;

/**
   A class for launching the kernel.
 */
public final class StartKernel {
    private static final String CONFIG_FLAG = "-c";
    private static final String CONFIG_LONG_FLAG = "--config";

    /** Utility class: private constructor. */
    private StartKernel() {}

    /**
       Start a kernel.
       @param args Command line arguments.
     */
    public static void main(String[] args) {
        Kernel<? extends Entity> kernel = null;
        Config config = new Config();
        try {
            int i = 0;
            while (i < args.length) {
                if (args[i].equalsIgnoreCase(CONFIG_FLAG) || args[i].equalsIgnoreCase(CONFIG_LONG_FLAG)) {
                    config.read(new File(args[++i]));
                }
                else {
                    System.out.println("Unrecognised option: " + args[i]);
                }
                ++i;
            }

            WorldModelCreator<RescueObject> worldModelCreator = new GISWorldModelCreator();
            SimulatorManager<RescueObject> simulatorManager = new LegacySimulatorManager();
            ViewerManager<RescueObject> viewerManager = new LegacyViewerManager();
            AgentManager<RescueObject> agentManager = new LegacyAgentManager(config);
            Perception<RescueObject> perception = new LegacyPerception(config);
            kernel = new Kernel<RescueObject>(config, worldModelCreator, simulatorManager, viewerManager, agentManager, perception);
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
        try {
            if (kernel != null) {
                kernel.runSimulation();
            }
        }
        catch (KernelException e) {
            System.err.println("Error running the kernel");
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}