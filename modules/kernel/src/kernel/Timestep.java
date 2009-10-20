package kernel;

import java.util.Collection;

import rescuecore2.messages.Command;
import rescuecore2.worldmodel.Entity;

/**
   A record of everything that happened in a timestep.
*/
public class Timestep {
    private int time;
    private Collection<Command> commands;
    private Collection<Entity> updates;

    /**
       Construct a timestep record.
       @param time The timestep number.
       @param commands The commands.
       @param updates The updates.
    */
    public Timestep(int time, Collection<Command> commands, Collection<Entity> updates) {
        this.time = time;
        this.commands = commands;
        this.updates = updates;
    }

    /**
       Get the time.
       @return The time.
    */
    public int getTime() {
        return time;
    }

    /**
       Get the commands.
       @return The commands.
    */
    public Collection<Command> getCommands() {
        return commands;
    }

    /**
       Get the updates.
       @return The updates.
    */
    public Collection<Entity> getUpdates() {
        return updates;
    }
}