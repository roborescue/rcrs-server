package rescuecore2.standard.messages;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.messages.IntComponent;
import rescuecore2.messages.RawDataComponent;
import rescuecore2.messages.AbstractCommand;

/**
   An agent speak (channel) command.
 */
public class AKSpeak extends AbstractCommand {
    private IntComponent channel;
    private RawDataComponent data;

    /**
       Create an empty AKSpeak command.
     */
    AKSpeak() {
        super("AK_SPEAK", MessageConstants.AK_SPEAK);
        init();
    }

    /**
       Construct a speak command.
       @param agent The ID of the agent issuing the command.
       @param channel The ID of the channel to speak on.
       @param data The content of the message.
       @param time The time the command was issued.
     */
    public AKSpeak(EntityID agent, int channel, byte[] data, int time) {
        super("AK_SPEAK", MessageConstants.AK_SPEAK, agent, time);
        init();
        this.channel.setValue(channel);
        this.data.setData(data);
    }

    /**
       Get the channel that was used.
       @return The channel.
    */
    public int getChannel() {
        return channel.getValue();
    }

    /**
       Get the content of the message.
       @return The message content.
     */
    public byte[] getContent() {
        return data.getData();
    }

    private void init() {
        channel = new IntComponent("Channel");
        data = new RawDataComponent("Message");
        addMessageComponent(channel);
        addMessageComponent(data);
    }
}