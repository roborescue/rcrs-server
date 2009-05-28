package kernel;

import java.util.Set;
import java.util.Collection;

import rescuecore2.connection.ConnectionManagerListener;
import rescuecore2.messages.Message;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.WorldModel;

/**
   This class manages connections from agents. When a new connection is established by the ConnectionManager this class will be informed. If the connection is from an agent then it should be assigned an entity to control and a new Agent object should be created. This should eventually be returned from a call to {@link #getAllAgents}.
   @param <S> The subclass of WorldModel that this AgentManager understands.
   @param <T> The subclass of Entity that this AgentManager understands.
 */
public interface AgentManager<T extends Entity, S extends WorldModel<? super T>> extends ConnectionManagerListener, WorldModelAware<S>  {
    /**
       Add an AgentManagerListener.
       @param l The listener to add.
    */
    void addAgentManagerListener(AgentManagerListener l);

    /**
       Remove an AgentManagerListener.
       @param l The listener to add.
    */
    void removeAgentManagerListener(AgentManagerListener l);

    /**
       Get all Agents. This method may block if it needs to wait for agents to connect.
       @throws InterruptedException If this thread is interrupted while waiting for agents.
    */
    Set<Agent<T>> getAllAgents() throws InterruptedException;

    /**
       Get all entities that are controlled by agents.
       @return A set of entities that are controlled by agents.
     */
    //    Set<T> getControlledEntities();

    /**
       Shut this manager down.
     */
    void shutdown();
}