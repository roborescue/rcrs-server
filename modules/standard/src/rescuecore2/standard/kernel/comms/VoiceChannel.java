package rescuecore2.standard.kernel.comms;

import java.util.Map;

import rescuecore2.config.Config;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.misc.collections.LazyMap;
import rescuecore2.log.Logger;

import rescuecore2.standard.messages.AKSpeak;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.Human;

/**
   A voice channel.
*/
public class VoiceChannel extends AbstractChannel {
    // Config option suffixes
    private static final String RANGE_SUFFIX = ".range";
    private static final String MESSAGE_SIZE_SUFFIX = ".messages.size";
    private static final String MESSAGE_MAX_SUFFIX = ".messages.max";

    private int range;
    private int maxSize;
    private int maxMessages;
    private Map<EntityID, Integer> uttered;
    private StandardWorldModel world;

    /**
       Create a VoiceChannel.
       @param config The configuration to read.
       @param channelID The id of this channel.
       @param world The world model.
    */
    public VoiceChannel(Config config, int channelID, StandardWorldModel world) {
        super(channelID);
        this.world = world;
        range = config.getIntValue(ChannelCommunicationModel.PREFIX + channelID + RANGE_SUFFIX);
        maxSize = config.getIntValue(ChannelCommunicationModel.PREFIX + channelID + MESSAGE_SIZE_SUFFIX);
        maxMessages = config.getIntValue(ChannelCommunicationModel.PREFIX + channelID + MESSAGE_MAX_SUFFIX);
        uttered = new LazyMap<EntityID, Integer>() {
            @Override
            public Integer createValue() {
                return 0;
            }
        };
    }

    @Override
    public void timestep() {
        super.timestep();
        uttered.clear();
    }

    @Override
    protected void pushImpl(AKSpeak speak, int originalSize) throws InvalidMessageException {
        if(speak==null)
        	return;
        
    	EntityID agentID = speak.getAgentID();
        Entity e = world.getEntity(agentID);
        if (!(e instanceof Human)) {
            throw new InvalidMessageException("Agent " + agentID + " is not a human: " + (e == null ? "null" : e.getClass().getName()));
        }
        byte[] data = speak.getContent();
        int count = uttered.get(agentID);
        if (count >= maxMessages) {
            throw new InvalidMessageException("Agent " + agentID + " has uttered too many voice messages on " + this);
        }
        if (originalSize > maxSize) {
            throw new InvalidMessageException("Agent " + agentID + " tried to send an oversize voice message: " + data.length + " bytes but the limit is " + maxSize);
        }
        uttered.put(agentID, count + 1);
        // Find out who can hear it
        StandardEntity sender = world.getEntity(agentID);
        for (StandardEntity target : world.getEntitiesOfType(StandardEntityURN.FIRE_BRIGADE, StandardEntityURN.AMBULANCE_TEAM, StandardEntityURN.POLICE_FORCE, StandardEntityURN.CIVILIAN)) {
            if (world.getDistance(sender, target) <= range) {
                Logger.debug(target + " can hear voice message from " + sender);
                addMessageForAgent(target, speak);
            }
        }
    }

    @Override
    public String toString() {
        return "Voice channel " + channelID + " (range = " + range + ", max " + maxMessages + " messages of size " + maxSize + ")";
    }
}
