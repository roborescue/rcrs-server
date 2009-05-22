package kernel;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import java.io.IOException;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionException;
import rescuecore2.messages.Message;

/**
   Abstract base class for AgentManager implementations.
   @param <S> The subclass of WorldModel that this AgentManager understands.
   @param <T> The subclass of Entity that this AgentManager understands.
*/
public abstract class AbstractAgentManager<T extends Entity, S extends WorldModel<T>> implements AgentManager<T, S> {
    private Set<AgentManagerListener> listeners;

    private Map<T, Agent<T>> allAgents;
    private Map<EntityID, Agent<T>> allAgentsByID;
    private Set<T> controlledEntities;

    private Map<EntityID, List<Message>> agentCommands;

    private S worldModel;

    private final Object lock = new Object();

    /**
       Construct an AbstractAgentManager.
     */
    protected AbstractAgentManager() {
        listeners = new HashSet<AgentManagerListener>();
        allAgents = new HashMap<T, Agent<T>>();
        allAgentsByID = new HashMap<EntityID, Agent<T>>();
        controlledEntities = new HashSet<T>();
        agentCommands = new HashMap<EntityID, List<Message>>();
    }

    @Override
    public void setWorldModel(S world) {
        worldModel = world;
    }

    /**
       Get the world model.
       @return The world model.
     */
    protected S getWorldModel() {
        return worldModel;
    }

    @Override
    public void addAgentManagerListener(AgentManagerListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    @Override
    public void removeAgentManagerListener(AgentManagerListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    @Override
    public final void sendPerceptionUpdate(int time, T entity, Collection<T> visible) {
        Agent<T> controller = getController(entity);
        if (controller == null) {
            throw new IllegalArgumentException("Unrecognised object: " + entity);
        }
        controller.send(getPerceptionMessages(time, controller, visible));
    }

    @Override
    public final void sendMessages(T entity, Collection<? extends Message> messages) {
        Agent<T> controller = getController(entity);
        if (controller == null) {
            throw new IllegalArgumentException("Unrecognised object: " + entity);
        }
        controller.send(messages);
    }

    @Override
    public void shutdown() {
    }

    @Override
    public Set<T> getControlledEntities() {
        return Collections.unmodifiableSet(controlledEntities);
    }

    @Override
    public void waitForAllAgents() throws InterruptedException {
        synchronized (lock) {
            while (allAgents.size() != controlledEntities.size()) {
                lock.wait();
                int waiting = controlledEntities.size() - allAgents.size();
                System.out.println("Waiting for " + waiting + " agents.");
            }
        }
    }

    /**
       Register an entity as being agent-controlled. This does not mean that an agent is currently in control of the entity; {@link #setController} does that job.
       @param entity The entity that will be agent-controlled.
     */
    protected void addControlledEntity(T entity) {
        controlledEntities.add(entity);
    }

    /**
       Create the perception message(s) to send to an agent.
       @param time The current time.
       @param agent The agent to create message(s) for.
       @param visible The set of entities visible to the agent.
       @return A collection of messages to send to the agent.
     */
    protected abstract Collection<? extends Message> getPerceptionMessages(int time, Agent<T> agent, Collection<T> visible);

    /**
       Set which agent is controlling a particular entity.
       @param entity The entity being controlled.
       @param agent The agent controlling the entity.
       @throws IllegalArgumentException If the entity has not been registered as agent-controlled.
       @see #addControlledEntity
     */
    protected void setController(T entity, Agent<T> agent) {
        if (!controlledEntities.contains(entity)) {
            throw new IllegalArgumentException(entity + " is not registered as being agent-controlled");
        }
        synchronized (lock) {
            allAgents.put(entity, agent);
            allAgentsByID.put(entity.getID(), agent);
            lock.notifyAll();
        }
    }

    /**
       Find out which agent is controlling an entity.
       @param entity The entity to look up.
       @return The Agent controlling the entity, or null if it is not controlled by an agent.
     */
    protected Agent<T> getController(T entity) {
        synchronized (lock) {
            return allAgents.get(entity);
        }
    }

    /**
       Find out which agent is controlling an entity.
       @param id The ID of the entity to look up.
       @return The Agent controlling the entity, or null if it is not controlled by an agent or the ID could not be found.
     */
    protected Agent<T> getController(EntityID id) {
        synchronized (lock) {
            return allAgentsByID.get(id);
        }
    }

    /**
       Fire an 'agent connected' event to all listeners.
       @param info The AgentInfo to send.
     */
    protected void fireAgentConnected(AgentInfo info) {
        for (AgentManagerListener next : getListeners()) {
            next.agentConnected(info);
        }
    }

    /**
       Register an agent command.
       @param agent The agent that sent the command.
       @param message The command message that arrived.
     */
    protected void agentCommandReceived(Agent<T> agent, Message message) {
        synchronized (agentCommands) {
            List<Message> messages = agentCommands.get(agent.getEntityID());
            if (messages == null) {
                messages = new ArrayList<Message>();
                agentCommands.put(agent.getEntityID(), messages);
            }
            messages.add(message);
        }
    }

    @Override
    public Collection<Message> getAgentCommands(int timestep) {
        Collection<Message> commands = new ArrayList<Message>();
        synchronized (agentCommands) {
            for (List<Message> list : agentCommands.values()) {
                commands.addAll(list);
            }
            agentCommands.clear();
        }
        filterAgentCommands(commands, timestep);
        return commands;
    }

    /**
       Filter the agent commands if necessary. The default implementation does nothing.
       @param commands The commands to filter.
       @param timestep The current time.
     */
    protected void filterAgentCommands(Collection<Message> commands, int timestep) {
    }

    private Set<AgentManagerListener> getListeners() {
        Set<AgentManagerListener> result;
        synchronized (listeners) {
            result = new HashSet<AgentManagerListener>(listeners);
        }
        return result;
    }

    /**
       An internal representation of an Agent that controls an entity.
     */
    protected static class Agent<T extends Entity> {
        private T entity;
        private Connection connection;

        /**
           Create an Agent.
           @param entity The entity controlled by this agent.
           @param connection The Connection for talking to the agent.
        */
        public Agent(T entity, Connection connection) {
            this.entity = entity;
            this.connection = connection;
        }

        /**
           Send some messages to the agent.
           @param m The messages to send.
         */
        public void send(Collection<? extends Message> m) {
            if (!connection.isAlive()) {
                return;
            }
            try {
                connection.sendMessages(m);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            catch (ConnectionException e) {
                e.printStackTrace();
            }
        }

        /**
           Get the entity controlled by this agent.
           @return The controlled entity.
         */
        public T getEntity() {
            return entity;
        }

        /**
           Get the ID of the entity controlled by this agent.
           @return The controlled entity ID.
         */
        public EntityID getEntityID() {
            return entity.getID();
        }

        /**
           Get the connection for talking to this agent.
           @return The connection.
         */
        public Connection getConnection() {
            return connection;
        }
    }
}