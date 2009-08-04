package rescuecore2.components;

import rescuecore2.config.Config;

/**
   Top-level interface for components of the Robocup Rescue simulation. Agents, simulators and viewers are all components.
 */
public interface Component {
    /**
       Initialise this component before connection.
       @param config The system configuration.
       @throws ComponentInitialisationException If there is a problem initialising the component.
     */
    void initialise(Config config) throws ComponentInitialisationException;
}