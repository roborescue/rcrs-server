package rescuecore2.messages.control;

import rescuecore2.messages.Control;
import rescuecore2.messages.Command;
import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.components.IntComponent;
import rescuecore2.messages.components.CommandListComponent;

import java.util.Collection;
import java.util.List;

import java.io.InputStream;
import java.io.IOException;

/**
   A message containing a list of agent commands. This is sent from the kernel to all simulators.
 */
public class KSCommands extends AbstractMessage implements Control {
    private IntComponent id;
    private IntComponent time;
    private CommandListComponent commands;

    /**
       A KSCommands message that populates its data from a stream.
       @param in The InputStream to read.
       @throws IOException If there is a problem reading the stream.
     */
    public KSCommands(InputStream in) throws IOException {
        this();
        read(in);
    }

    /**
       A populated KSCommands message.
       @param id The id of the simulator receiving the update.
       @param time The timestep of the simulation.
       @param commands All agent Commands.
     */
    public KSCommands(int id, int time, Collection<? extends Command> commands) {
        this();
        this.id.setValue(id);
        this.time.setValue(time);
        this.commands.setCommands(commands);
    }

    private KSCommands() {
        super(ControlMessageURN.KS_COMMANDS);
        id = new IntComponent("ID");
        time = new IntComponent("Time");
        commands = new CommandListComponent("Commands");
        addMessageComponent(id);
        addMessageComponent(time);
        addMessageComponent(commands);
    }

    /**
       Get the id of the component that this message is addressed to.
       @return The ID of the target component.
     */
    public int getTargetID() {
        return id.getValue();
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
    public List<Command> getCommands() {
        return commands.getCommands();
    }
}
