package rescuecore2.log;

import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.config.Config;

import java.util.Set;

/**
   An interface for objects that know how to read log files.
 */
public interface LogReader {
    /**
       Get the configuration entry in the log file.
       @return The configuration stored in the log.
       @throws LogException If there is a problem reading the log.
     */
    Config getConfig() throws LogException;

    /**
       Get the last timestep recorded in this log.
       @return The last timestep number.
       @throws LogException If there is a problem reading the log.
     */
    int getMaxTimestep() throws LogException;

    /**
       Get the state of the world at a particular timestep.
       @param time The timestep to look up. If this is zero then the world initial conditions will be returned.
       @return The world model at the given timestep.
       @throws LogException If there is a problem reading the log.
    */
    WorldModel<? extends Entity> getWorldModel(int time) throws LogException;

    /**
       Get the set of EntityIDs that have perception updates in a given timestep.
       @param time The timestep to look up.
       @return The set of EntityIDs that have perception records for that time.
       @throws LogException If there is a problem reading the log.
     */
    Set<EntityID> getEntitiesWithUpdates(int time) throws LogException;

    /**
       Get the perception record for a particular entity at a particular time.
       @param time The timestep to look up.
       @param entity The entity to look up.
       @return The perception record for the timestep/agent, or null if there is no such entry.
       @throws LogException If there is a problem reading the log.
     */
    PerceptionRecord getPerception(int time, EntityID entity) throws LogException;

    /**
       Get the agent commands at a particular timestep.
       @param time The timestep to look up.
       @return The commands record for the timestep, or null if there is no commands record at that timestep.
       @throws LogException If there is a problem reading the log.
     */
    CommandsRecord getCommands(int time) throws LogException;

    /**
       Get the simulator updates at a particular timestep.
       @param time The timestep to look up.
       @return The updates record for the timestep, or null if there is no updates record at that timestep.
       @throws LogException If there is a problem reading the log.
     */
    UpdatesRecord getUpdates(int time) throws LogException;
}
