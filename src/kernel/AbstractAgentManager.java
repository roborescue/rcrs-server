package kernel;

import java.util.Set;
import java.util.HashSet;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.WorldModel;

/**
   Abstract base class for AgentManager implementations.
   @param <S> The subclass of WorldModel that this AgentManager understands.
   @param <T> The subclass of Entity that this AgentManager understands.
*/
public abstract class AbstractAgentManager<T extends Entity, S extends WorldModel<T>> implements AgentManager<T, S> {
    private Set<AgentManagerListener> listeners;

    /**
       Construct an AbstractAgentManager.
     */
    protected AbstractAgentManager() {
        listeners = new HashSet<AgentManagerListener>();
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

    /**
       Fire an 'agent connected' event to all listeners.
       @param info The AgentInfo to send.
     */
    protected void fireAgentConnected(AgentInfo info) {
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