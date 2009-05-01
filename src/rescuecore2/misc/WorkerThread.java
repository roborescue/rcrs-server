package rescuecore2.misc;

/**
   Abstract class for threads that need to repeat a unit of work, for example listening on a socket and dispatching data to listeners.
 */
public abstract class WorkerThread extends Thread {
    private volatile boolean running;
    private volatile boolean killed;

    /**
       Construct a worker thread.
    */
    public WorkerThread() {
        running = true;
        killed = false;
    }

    /**
       Interrupt any current work, tell the thread to stop and wait for the thread to die.
       @throws InterruptedException If the thread that called kill is itself interrupted.
    */
    public void kill() throws InterruptedException {
        running = false;
        killed = true;
        this.interrupt();
        this.join();
    }

    @Override
	public void run() {
	setup();
	try {
	    while (running && !killed) {
		running = work();
	    }
	}
	finally {
	    cleanup();
	}
    }

    /**
       Do a unit of work and return whether there is more work to be done. Implementations should check periodically for interruptions and return when signalled.
       @return True if more work remains, false otherwise.
    */
    protected abstract boolean work();

    /**
       Perform any setup necessary before work begins. Default implementation does nothing.
    */
    protected void setup() {}

    /**
       Perform any cleanup necessary after work finishes. This will be called even if the {@link #work} method throws an exception. It is highly recommended that this method does not throw any exceptions. Default implementation does nothing.
    */
    protected void cleanup() {}
}