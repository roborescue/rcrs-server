package kernel;

import rescuecore2.worldmodel.Entity;
import rescuecore2.messages.Command;

import java.util.Collection;
import java.util.HashSet;

/**
   Abstract base class for Agent implementations.
 */
public abstract class AbstractAgent<T extends Entity> implements Agent<T> {
    private T entity;

    protected AbstractAgent(T entity) {
        this.entity = entity;
    }

    @Override
    public T getControlledEntity() {
        return entity;
    }
}