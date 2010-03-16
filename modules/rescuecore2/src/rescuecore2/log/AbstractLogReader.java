package rescuecore2.log;

import rescuecore2.registry.Registry;

/**
   Abstract base class for LogReader implementations.
*/
public abstract class AbstractLogReader implements LogReader {
    /**
       The registry to use for reading log entries.
    */
    protected Registry registry;

    /**
       Create a new AbstractLogReader.
       @param registry The registry to use for reading log entries.
    */
    protected AbstractLogReader(Registry registry) {
        this.registry = registry;
    }
}
