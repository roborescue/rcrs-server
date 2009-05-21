package kernel;

/**
   Interface for objects interested in AgentManager events.
 */
public interface AgentManagerListener {
    /**
       Notification that an agent has connected.
       @param info Information about the agent.
    */
    void agentConnected(AgentInfo info);
}