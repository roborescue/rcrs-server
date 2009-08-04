package kernel.log;

import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.messages.Message;
import rescuecore2.messages.Command;
import rescuecore2.misc.Pair;

import java.util.Collection;
import java.util.Set;

/**
   An interface for objects that know how to read kernel logs.
 */
public interface LogReader {
    /**
       Get the last timestep recorded in this log.
       @return The last timestep number.
       @throws KernelLogException If there is a problem reading the log.
     */
    int getMaxTimestep() throws KernelLogException;

    /**
       Get the state of the world at a particular timestep.
       @param time The timestep to look up. If this is zero then the world initial conditions will be returned.
       @return The world model at the given timestep.
       @throws KernelLogException If there is a problem reading the log.
    */
    WorldModel<? extends Entity> getWorldModel(int time) throws KernelLogException;

    /**
       Get the set of EntityIDs that have perception updates in a given timestep.
       @param time The timestep to look up.
       @return The set of EntityIDs that have perception updates for that time.
       @throws KernelLogException If there is a problem reading the log.
     */
    Set<EntityID> getEntitiesWithUpdates(int time) throws KernelLogException;

    /**
       Get the perception update for a particular entity at a particular time.
       @param time The timestep to look up.
       @param entity The entity to look up.
       @return The set of entity updates and communication messages for the given entity at the given timestep.
       @throws KernelLogException If there is a problem reading the log.
     */
    Pair<Collection<Entity>, Collection<Message>> getEntityUpdates(int time, EntityID entity) throws KernelLogException;

    /**
       Get the agent commands at a particular timestep.
       @param time The timestep to look up.
       @return The set of agent commands for the given timestep.
       @throws KernelLogException If there is a problem reading the log.
     */
    Collection<Command> getCommands(int time) throws KernelLogException;

    /**
       Get the simulator updates at a particular timestep.
       @param time The timestep to look up.
       @return The set of simulator updates for the given timestep.
       @throws KernelLogException If there is a problem reading the log.
     */
    Collection<Entity> getUpdates(int time) throws KernelLogException;
}