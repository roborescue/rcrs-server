package rescuecore2.version0.messages;

import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.IntComponent;

import java.util.Collection;
import java.util.List;

/**
   A message containing a list of agent commands. This is sent from the kernel to all simulators and viewers.
 */
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
    public Commands(int time, Collection<? extends AgentCommand> commands) {
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