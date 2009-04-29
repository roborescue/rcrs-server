package kernel;

import rescuecore2.connection.ConnectionManagerListener;

/**
   This class manages connections from viewers.
 */
public interface ViewerManager extends ConnectionManagerListener {
    /**
       Wait until all viewers have acknowledged.
       @throws InterruptedException If this thread is interrupted while waiting for viewers.
    */
    void waitForAcknowledgements() throws InterruptedException;

    /**
       Shut this manager down.
     */
    void shutdown();
}