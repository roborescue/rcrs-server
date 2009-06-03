package rescuecore2.messages;

import static rescuecore2.misc.EncodingTools.readInt32;
import static rescuecore2.misc.EncodingTools.writeInt32;
import static rescuecore2.misc.EncodingTools.readBytes;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
   A message component made up of a list of agent commands.
 */
public class CommandListComponent extends AbstractMessageComponent {
    private List<Command> commands;

    /**
       Construct a CommandListComponent with no content.
       @param name The name of the component.
    */
    public CommandListComponent(String name) {
        super(name);
        commands = new ArrayList<Command>();
    }

    /**
       Construct a CommandListComponent with a specific list of agent commands.
       @param name The name of the component.
       @param commands The agent commands in this message component.
    */
    public CommandListComponent(String name, Collection<? extends Command> commands) {
        super(name);
        this.commands = new ArrayList<Command>(commands);
    }

    /**
       Get the agent commands that make up this message component.
       @return The agent commands in this component.
    */
    public List<Command> getCommands() {
        return commands;
    }

    /**
       Set the commands that make up this message component.
       @param commands The commands in this component.
    */
    public void setCommands(Collection<? extends Command> commands) {
        this.commands = new ArrayList<Command>(commands);
    }

    @Override
    public void write(OutputStream out) throws IOException {
        for (Command next : commands) {
            ByteArrayOutputStream gather = new ByteArrayOutputStream();
            next.write(gather);
            // Type
            writeInt32(next.getMessageTypeID(), out);
            // Size
            byte[] bytes = gather.toByteArray();
            writeInt32(bytes.length, out);
            out.write(bytes);
        }
        // End-of-list marker
        writeInt32(0, out);
    }

    @Override
    public void read(InputStream in) throws IOException {
        commands.clear();
        int typeID;
        do {
            typeID = readInt32(in);
            if (typeID != 0) {
                int size = readInt32(in);
                byte[] data = readBytes(size, in);
                ByteArrayInputStream dataIn = new ByteArrayInputStream(data);
                Message next = MessageRegistry.createMessage(typeID, dataIn);
                if (next instanceof Command) {
                    commands.add((Command)next);
                }
                else {
                    System.err.println("Command list stream contained a non-command message: " + next + " (" + next.getClass().getName() + ")");
                }
            }
        } while (typeID != 0);
    }

    @Override
    public String toString() {
        return commands.size() + " commands";
    }
}