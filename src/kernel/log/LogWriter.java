package kernel.log;

import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.messages.Message;
import rescuecore2.messages.Command;

import java.util.Collection;

/**
   An interface for objects that know how to write kernel logs.
 */
public interface LogWriter {
    /**
       Log the initial conditions after the kernel has been started but before the first timestep.
       @param world The world model at startup.
       @throws KernelLogException If there is a problem writing the log.
    */
    void logInitialConditions(WorldModel<? extends Entity> world) throws KernelLogException;

    /**
       Log an agent's perception update.
       @param time The timestep.
       @param agentID The ID of the agent being updated.
       @param visible The set of entities being updated.
       @param comms The set of communication messages being sent.
       @throws KernelLogException If there is a problem writing the log.
    */
    void logPerception(int time, EntityID agentID, Collection<? extends Entity> visible, Collection<? extends Message> comms) throws KernelLogException;

    /**
       Log a set of agent commands.
       @param time The timstep.
       @param commands The set of agent commands.
       @throws KernelLogException If there is a problem writing the log.
    */
    void logCommands(int time, Collection<? extends Command> commands) throws KernelLogException;

    /**
       Log a set of simulator updates.
       @param time The timstep.
       @param updates The set of updates.
       @throws KernelLogException If there is a problem writing the log.
    */
    void logUpdates(int time, Collection<? extends Entity> updates) throws KernelLogException;

    /**
       Close the log.
    */
    void close();
}