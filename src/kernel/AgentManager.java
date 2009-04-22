package kernel;

import rescuecore2.worldmodel.WorldModel;
import rescuecore2.connection.ConnectionManagerListener;

/**
   This class manages connections from agents, including assigning which Robocup Rescue object each agent controls, and passing messages between the kernel and the agents.
 */
public interface AgentManager extends ConnectionManagerListener {
    /**
       Tell the AgentManager which world model to use.
       @param model The new WorldModel.
     */
    public void setWorldModel(WorldModel model);
}