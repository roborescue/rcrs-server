package kernel;

/**
   Information about a simulator.
 */
public class SimulatorInfo {
    private String description;

    /**
       Construct a SimulatorInfo.
       @param description A description of the simulator.
     */
    public SimulatorInfo(String description) {
        this.description = description;
    }

    /**
       Get the description of the simulator.
       @return The description.
    */
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "Simulator: " + description;
    }
}