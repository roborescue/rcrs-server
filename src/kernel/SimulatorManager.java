package kernel;

import rescuecore2.connection.ConnectionManagerListener;

/**
   This class manages connections from simulators.
 */
public interface SimulatorManager extends ConnectionManagerListener {
    /**
       Wait until all simulators have acknowledged.
       @throws InterruptedException If this thread is interrupted while waiting for simulators.
    */
    void waitForAcknowledgements() throws InterruptedException;

    /**
       Shut this manager down.
     */
    void shutdown();
}