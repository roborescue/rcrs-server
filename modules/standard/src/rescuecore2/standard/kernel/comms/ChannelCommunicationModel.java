package rescuecore2.standard.kernel.comms;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import kernel.AbstractCommunicationModel;

import rescuecore2.messages.Command;
import rescuecore2.config.Config;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.log.Logger;

import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.FireStation;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.PoliceOffice;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.AmbulanceCentre;
import rescuecore2.standard.messages.AKSpeak;
import rescuecore2.standard.messages.AKSubscribe;

/**
   The channel-based communication model.
 */
public class ChannelCommunicationModel extends AbstractCommunicationModel {
    /** The prefix for channels config options. */
    public static final String PREFIX = "comms.channels.";

    private static final String COUNT_KEY = "comms.channels.count";
    private static final String PLATOON_MAX_CHANNELS_KEY = "comms.channels.max.platoon";
    private static final String CENTRE_MAX_CHANNELS_KEY = "comms.channels.max.centre";

    private static final String TYPE_SUFFIX = ".type";
    private static final String NOISE_SUFFIX = ".noise";
    private static final String INPUT_SUFFIX = ".input";
    private static final String OUTPUT_SUFFIX = ".output";

    private static final String TYPE_VOICE = "voice";
    private static final String TYPE_RADIO = "radio";

    private static final String NOISE_TYPE_DROPOUT = "dropout";
    private static final String NOISE_TYPE_STATIC = "static";

    private Map<Integer, Channel> channels;
    private int platoonMax;
    private int centreMax;
    private StandardWorldModel world;

    /**
       Construct a ChannelCommunicationModel.
    */
    public ChannelCommunicationModel() {
        channels = new HashMap<Integer, Channel>();
    }

    @Override
    public String toString() {
        return "Channel communication model";
    }

    @Override
    public void initialise(Config config, WorldModel<? extends Entity> model) {
        super.initialise(config, model);
        channels.clear();
        world = StandardWorldModel.createStandardWorldModel(model);
        // Read the channel information
        int count = config.getIntValue(COUNT_KEY);
        for (int i = 0; i < count; ++i) {
            String type = config.getValue(PREFIX + i + TYPE_SUFFIX);
            Channel channel = null;
            if (TYPE_VOICE.equals(type)) {
                channel = new VoiceChannel(config, i, world);
            }
            else if (TYPE_RADIO.equals(type)) {
                channel = new RadioChannel(config, i);
            }
            else {
                Logger.error("Unrecognised channel type: " + PREFIX + i + TYPE_SUFFIX + " = '" + type + "'");
            }
            if (channel != null) {
                String key = PREFIX + i + NOISE_SUFFIX;
                Noise input = createNoiseObjects(config, key + INPUT_SUFFIX);
                Noise output = createNoiseObjects(config, key + OUTPUT_SUFFIX);
                channel.setInputNoise(input);
                channel.setOutputNoise(output);
                channels.put(i, channel);
                Logger.info("Created channel: " + channel);
            }
        }
        platoonMax = config.getIntValue(PLATOON_MAX_CHANNELS_KEY, 1);
        centreMax = config.getIntValue(CENTRE_MAX_CHANNELS_KEY, 2);
    }

    @Override
    public void process(int time, Collection<? extends Command> agentCommands) {
        Logger.debug("ChannelCommunicationModel processing commands at time " + time + ": " + agentCommands);
        super.process(time, agentCommands);
        // Update all channels
        for (Channel next : channels.values()) {
            next.timestep();
        }
        // Look for subscription commands
        for (Command next : agentCommands) {
            if (next instanceof AKSubscribe) {
                processSubscribe((AKSubscribe)next);
            }
        }
        // Now push all speak commands through the right channels
        for (Command next : agentCommands) {
            if (next instanceof AKSpeak) {
                try {
                    AKSpeak speak = (AKSpeak)next;
                    int channelNumber = speak.getChannel();
                    Channel channel = channels.get(channelNumber);
                    Logger.debug("Processing speak: " + speak);
                    if (channel == null) {
                        throw new InvalidMessageException("Unrecognised channel: " + channelNumber);
                    }
                    else {
                        channel.push(speak);
                    }
                }
                catch (InvalidMessageException e) {
                    Logger.warn("Invalid message: " + next + ": " + e.getMessage());
                }
            }
        }
        // And find out what each agent can hear
        for (Entity agent : world.getEntitiesOfType(StandardEntityURN.FIRE_BRIGADE,
                                                    StandardEntityURN.FIRE_STATION,
                                                    StandardEntityURN.POLICE_FORCE,
                                                    StandardEntityURN.POLICE_OFFICE,
                                                    StandardEntityURN.AMBULANCE_TEAM,
                                                    StandardEntityURN.AMBULANCE_CENTRE,
                                                    StandardEntityURN.CIVILIAN)) {
            for (Channel channel : channels.values()) {
                addHearing(agent, channel.getMessagesForAgent(agent));
            }
        }
    }

    /**
       Get a view of all registered channels.
       @return All channels.
    */
    public Collection<Channel> getAllChannels() {
        return Collections.unmodifiableCollection(channels.values());
    }

    private Noise createNoiseObjects(Config config, String key) {
        ChainedNoise result = new ChainedNoise();
        result.addChild(lookForFailure(config, key));
        result.addChild(lookForDropout(config, key));
        result.addChild(lookForStatic(config, key));
        return result;
    }

    private Noise lookForFailure(Config config, String key) {
        if (config.getBooleanValue(key + ".failure.use", false)) {
            return new FailureNoise(config.getFloatValue(key + ".failure.p"), config.getRandom());
        }
        return null;
    }

    private Noise lookForDropout(Config config, String key) {
        if (config.getBooleanValue(key + ".dropout.use", false)) {
            return new DropoutNoise(config.getFloatValue(key + ".dropout.p"), config.getRandom());
        }
        return null;
    }

    private Noise lookForStatic(Config config, String key) {
        if (config.getBooleanValue(key + ".static.use", false)) {
            return new StaticNoise(config.getFloatValue(key + ".static.p"), config.getRandom());
        }
        return null;
    }

    private void processSubscribe(AKSubscribe sub) {
        Logger.debug("Processing subscribe message : " + sub);
        List<Integer> requested = sub.getChannels();
        EntityID id = sub.getAgentID();
        Entity entity = world.getEntity(id);
        if (entity == null) {
            Logger.warn("Couldn't find entity " + id);
            return;
        }
        int max;
        if (entity instanceof FireBrigade || entity instanceof PoliceForce || entity instanceof AmbulanceTeam || entity instanceof Civilian) {
            max = platoonMax;
        }
        else if (entity instanceof FireStation || entity instanceof PoliceOffice || entity instanceof AmbulanceCentre) {
            max = centreMax;
        }
        else {
            Logger.warn("I don't know how to handle subscriptions for this entity: " + entity);
            return;
        }
        if (requested.size() > max) {
            Logger.warn("Agent " + id + " tried to subscribe to " + requested.size() + " channels but only " + max + " allowed");
            return;
        }
        // Unsubscribe from all old channels
        for (Channel next : channels.values()) {
            next.removeSubscriber(entity);
        }
        // Subscribe to new channels
        for (int next : requested) {
            Channel channel = channels.get(next);
            if (channel == null) {
                Logger.warn("Agent " + id + " tried to subscribe to non-existant channel " + next);
            }
            else {
                channel.addSubscriber(entity);
            }
        }
    }
}
