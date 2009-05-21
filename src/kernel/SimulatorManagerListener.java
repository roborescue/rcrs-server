package kernel;

/**
   Interface for objects interested in SimulatorManager events.
 */
public interface SimulatorManagerListener {
    /**
       Notification that a simulation has connected.
       @param info Information about the simulator.
    */
    void simulatorConnected(SimulatorInfo info);
}