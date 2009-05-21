package kernel;

import java.io.File;
import java.io.IOException;

import rescuecore2.config.Config;
import rescuecore2.config.ConfigException;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.WorldModel;

import rescuecore2.version0.entities.RescueObject;

import kernel.ui.KernelStatus;
import javax.swing.JFrame;

import kernel.legacy.GISWorldModelCreator;
import kernel.legacy.LegacySimulatorManager;
import kernel.legacy.LegacyViewerManager;
import kernel.legacy.LegacyAgentManager;
import kernel.legacy.LegacyPerception;
import kernel.legacy.LegacyCommunicationModel;
import kernel.legacy.IndexedWorldModel;

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
        Kernel<? extends Entity, ? extends WorldModel<? extends Entity>> kernel = null;
        Config config = new Config();
        KernelStatus status = new KernelStatus();
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

            WorldModelCreator<RescueObject, IndexedWorldModel> worldModelCreator = new GISWorldModelCreator();
            SimulatorManager<RescueObject, IndexedWorldModel> simulatorManager = new LegacySimulatorManager();
            ViewerManager<RescueObject, IndexedWorldModel> viewerManager = new LegacyViewerManager();
            AgentManager<RescueObject, IndexedWorldModel> agentManager = new LegacyAgentManager(config);
            Perception<RescueObject, IndexedWorldModel> perception = new LegacyPerception(config);
            CommunicationModel<RescueObject, IndexedWorldModel> comms = new LegacyCommunicationModel(config);
            kernel = new Kernel<RescueObject, IndexedWorldModel>(config, worldModelCreator, simulatorManager, viewerManager, agentManager, perception, comms);
            agentManager.addAgentManagerListener(status);
            simulatorManager.addSimulatorManagerListener(status);
            viewerManager.addViewerManagerListener(status);
            kernel.addKernelListener(status);
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
                JFrame frame = new JFrame("Kernel status");
                frame.getContentPane().add(status);
                frame.pack();
                frame.setVisible(true);
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