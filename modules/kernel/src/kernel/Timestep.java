package kernel;

import java.util.Collection;

import rescuecore2.messages.Command;
import rescuecore2.worldmodel.ChangeSet;

/**
   A record of everything that happened in a timestep.
*/
public class Timestep {
    private int time;
    private Collection<Command> commands;
    private ChangeSet changes;

    /**
       Construct a timestep record.
       @param time The timestep number.
       @param commands The commands sent by agents.
       @param changes The world model changes.
    */
    public Timestep(int time, Collection<Command> commands, ChangeSet changes) {
        this.time = time;
        this.commands = commands;
        this.changes = changes;
    }

    /**
       Get the time.
       @return The time.
    */
    public int getTime() {
        return time;
    }

    /**
       Get the commands send by agents this timestep.
       @return The commands.
    */
    public Collection<Command> getCommands() {
        return commands;
    }

    /**
       Get the changes to entities during this timestep.
       @return The changes.
    */
    public ChangeSet getChangeSet() {
        return changes;
    }
}