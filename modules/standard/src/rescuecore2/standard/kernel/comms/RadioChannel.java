package rescuecore2.standard.kernel.comms;

import rescuecore2.config.Config;
import rescuecore2.log.Logger;

import rescuecore2.standard.messages.AKSpeak;

/**
   A radio channel.
*/
public class RadioChannel extends AbstractChannel {
    private static final String BANDWIDTH_SUFFIX = ".bandwidth";

    private int bandwidth;
    private int usedBandwidth;

    /**
       Create a RadioChannel.
       @param config The configuration to read.
       @param channelID The id of this channel.
     */
    public RadioChannel(Config config, int channelID) {
        super(channelID);
        bandwidth = config.getIntValue(ChannelCommunicationModel.PREFIX + channelID + BANDWIDTH_SUFFIX);
    }

    @Override
    public void timestep() {
        super.timestep();
        usedBandwidth = 0;
    }

    @Override
    public void pushImpl(AKSpeak speak, int originalSize) throws InvalidMessageException {
    	usedBandwidth += originalSize;
    	if(speak==null)
    		return;
    	
        byte[] data = speak.getContent();
        if (usedBandwidth > bandwidth) {
            throw new InvalidMessageException("Discarding message on channel " + channelID + ": already used " + usedBandwidth + " of " + bandwidth + " bytes, new message is " + data.length + " bytes.");
        }
        Logger.debug(this + " accepted message from " + speak.getAgentID());
        addMessageForSubscribers(speak);
//        usedBandwidth += data.length;
    }

    @Override
    public String toString() {
        return "Radio channel " + channelID + " (bandwidth = " + bandwidth + ")";
    }
}

