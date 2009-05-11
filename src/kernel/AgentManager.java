package kernel;

import java.util.Set;
import java.util.Collection;

import rescuecore2.connection.ConnectionManagerListener;
import rescuecore2.messages.Message;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.WorldModel;

/**
   This class manages connections from agents, including assigning which Robocup Rescue object each agent controls, and passing messages between the kernel and the agents.
   @param <T> The subclass of Entity that this AgentManager understands.
 */
public interface AgentManager<T extends Entity> extends ConnectionManagerListener {
    /**
       Set the world model.
       @param world The new world model.
    */
    void setWorldModel(WorldModel<T> world);

    /**
       Wait until all agents have connected.
       @throws InterruptedException If this thread is interrupted while waiting for agents.
    */
    void waitForAllAgents() throws InterruptedException;

    /**
       Get all entities that are controlled by agents.
       @return A set of entities that are controlled by agents.
     */
    Set<T> getControlledEntities();

    /**
       Get all agent commands since the last call to this method.
       @param timestep The current timestep.
       @return A collection of messages representing all input from each agent since the last time this method was called.
     */
    Collection<Message> getAgentCommands(int timestep);

    /**
       Shut this manager down.
     */
    void shutdown();

    /**
       Notify an agent of a perception update.
       @param time The current timestep.
       @param agentEntity The entity that is controlled by an agent.
       @param visible The set of visible entitites.
     */
    void sendPerceptionUpdate(int time, T agentEntity, Collection<T> visible);
}