package kernel;

import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.WorldModel;

/**
   Abstract base class for AgentManager implementations.
   @param <S> The subclass of WorldModel that this AgentManager understands.
   @param <T> The subclass of Entity that this AgentManager understands.
*/
public abstract class AbstractAgentManager<T extends Entity, S extends WorldModel<? super T>> implements AgentManager<T, S> {
    private Set<AgentManagerListener> listeners;

    private Map<T, Agent<T>> allAgents;
    private Map<EntityID, Agent<T>> allAgentsByID;
    private Set<T> controlledEntities;

    private S worldModel;

    /**
       Construct an AbstractAgentManager.
     */
    protected AbstractAgentManager() {
        listeners = new HashSet<AgentManagerListener>();
        allAgents = new HashMap<T, Agent<T>>();
        allAgentsByID = new HashMap<EntityID, Agent<T>>();
        controlledEntities = new HashSet<T>();
    }

    @Override
    public final void setWorldModel(S world) {
        worldModel = world;
        processNewWorldModel(world);
    }

    /**
       Perform any processing required after the world model has been set.
       @param world The new world model.
     */
    protected abstract void processNewWorldModel(S world);

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
    public void shutdown() {
    }

    @Override
    public Collection<Agent<T>> getAllAgents() throws InterruptedException {
        synchronized (allAgents) {
            while (allAgents.size() != controlledEntities.size()) {
                allAgents.wait();
                int waiting = controlledEntities.size() - allAgents.size();
                System.out.println("Waiting for " + waiting + " agents.");
            }
        }
        return new HashSet<Agent<T>>(allAgents.values());
    }

    /**
       Register an entity as being agent-controlled. This does not mean that an agent is currently in control of the entity; {@link #addAgent} does that job.
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
    //    protected abstract Collection<? extends Message> getPerceptionMessages(int time, Agent<T> agent, Collection<T> visible);

    /**
       Add an agent.
       @param agent The new agent.
       @throws IllegalArgumentException If the entity controlled by this agent has not been registered as agent-controlled.
       @see #addControlledEntity
     */
    protected void addAgent(Agent<T> agent) {
        T entity = agent.getControlledEntity();
        if (!controlledEntities.contains(entity)) {
            throw new IllegalArgumentException(entity + " is not registered as being agent-controlled");
        }
        synchronized (allAgents) {
            allAgents.put(entity, agent);
            allAgentsByID.put(entity.getID(), agent);
            fireAgentConnected(agent);
            allAgents.notifyAll();
        }
    }

    /**
       Find out which agent is controlling an entity.
       @param entity The entity to look up.
       @return The Agent controlling the entity, or null if it is not controlled by an agent.
     */
    protected Agent<T> getController(T entity) {
        synchronized (allAgents) {
            return allAgents.get(entity);
        }
    }

    /**
       Find out which agent is controlling an entity.
       @param id The ID of the entity to look up.
       @return The Agent controlling the entity, or null if it is not controlled by an agent or the ID could not be found.
     */
    protected Agent<T> getController(EntityID id) {
        synchronized (allAgents) {
            return allAgentsByID.get(id);
        }
    }

    /**
       Fire an 'agent connected' event to all listeners.
       @param agent The AgentInfo to send.
     */
    protected void fireAgentConnected(Agent<T> agent) {
        AgentInfo info = new AgentInfo(agent.toString());
        for (AgentManagerListener next : getListeners()) {
            next.agentConnected(info);
        }
    }

    private Set<AgentManagerListener> getListeners() {
        Set<AgentManagerListener> result;
        synchronized (listeners) {
            result = new HashSet<AgentManagerListener>(listeners);
        }
        return result;
    }
}