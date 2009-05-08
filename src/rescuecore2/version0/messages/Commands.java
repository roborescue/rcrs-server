package rescuecore2.version0.messages;

import rescuecore2.messages.AbstractMessage;

import java.util.Collection;
import java.util.List;

public class Commands extends AbstractMessage {
    private IntComponent time;
    private CommandListComponent commands;

    /**
       An empty Commands message.
     */
    public Commands() {
        super("Commands", MessageConstants.COMMANDS);
        time = new IntComponent("Time");
        commands = new CommandListComponent("Commands");
        addMessageComponent(time);
        addMessageComponent(commands);
    }

    /**
       A populated Commands message.
       @param time The timestep of the simulation.
       @param commands All AgentCommands.
     */
    public Commands(int time, Collection<AgentCommand> commands) {
        this();
        this.time.setValue(time);
        this.commands.setCommands(commands);
    }

    /**
       Get the time of the simulation.
       @return The simulation time.
     */
    public int getTime() {
        return time.getValue();
    }

    /**
       Get the list of agent commands.
       @return The agent commands.
     */
    public List<AgentCommand> getCommands() {
        return commands.getCommands();
    }
}