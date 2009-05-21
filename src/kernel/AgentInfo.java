package kernel;

/**
   Information about an agent.
 */
public class AgentInfo {
    private String description;

    /**
       Construct an AgentInfo.
       @param description A description of the agent.
     */
    public AgentInfo(String description) {
        this.description = description;
    }

    /**
       Get the description of the agent.
       @return The description.
    */
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "Agent: " + description;
    }
}