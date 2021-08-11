package rescuecore2.standard.messages;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.messages.components.RawDataComponent;
import rescuecore2.messages.AbstractCommand;

import java.io.InputStream;
import java.io.IOException;

/**
   An agent say command.
 */
public class AKSay extends AbstractCommand {
    private RawDataComponent data;

    /**
       An AKSay message that populates its data from a stream.
       @param in The InputStream to read.
       @throws IOException If there is a problem reading the stream.
     */
    public AKSay(InputStream in) throws IOException {
        this();
        read(in);
    }

    /**
       Construct a say command.
       @param agent The ID of the agent issuing the command.
       @param data The content of the command.
       @param time The time the command was issued.
     */
    public AKSay(EntityID agent, int time, byte[] data) {
        this();
        setAgentID(agent);
        setTime(time);
        this.data.setData(data);
    }

    /**
       Create an empty AKSay command.
     */
    private AKSay() {
        super(StandardMessageURN.AK_SAY);
        data = new RawDataComponent("Message");
        addMessageComponent(data);
    }

    /**
       Get the content of the message.
       @return The message content.
     */
    public byte[] getContent() {
        return data.getData();
    }
}
