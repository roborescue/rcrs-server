package rescuecore2.standard.kernel;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

import kernel.AgentProxy;
import kernel.CommunicationModel;

import rescuecore2.messages.Message;
import rescuecore2.messages.Command;
import rescuecore2.config.Config;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.Constants;

import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.FireStation;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.PoliceOffice;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.AmbulanceCentre;
import rescuecore2.standard.messages.AKSpeak;
import rescuecore2.standard.messages.AKSubscribe;
import rescuecore2.standard.messages.KAHearChannel;

/**
   The channel-based communication model.
 */
public class ChannelCommunicationModel implements CommunicationModel {
    private static final String COUNT_KEY = "comms.channels.count";
    private static final String PLATOON_MAX_CHANNELS_KEY = "comms.channels.max.platoon";
    private static final String CENTRE_MAX_CHANNELS_KEY = "comms.channels.max.centre";

    private static final String PREFIX = "comms.channels.";
    private static final String TYPE_SUFFIX = ".type";
    private static final String NOISE_SUFFIX = ".noise";

    private static final String TYPE_VOICE = "voice";
    private static final String TYPE_RADIO = "radio";

    private static final String NOISE_TYPE_DROPOUT = "dropout";
    private static final String NOISE_TYPE_STATIC = "static";

    // Voice constants
    private static final String RANGE_SUFFIX = ".range";
    private static final String MESSAGE_SIZE_SUFFIX = ".messages.size";
    private static final String MESSAGE_MAX_SUFFIX = ".messages.max";

    // Radio constants
    private static final String BANDWIDTH_SUFFIX = ".bandwidth";

    private Map<Integer, Channel> channels;
    private Map<Integer, Noise> noise;
    private int platoonMax;
    private int centreMax;
    private StandardWorldModel world;

    private Random random;

    /**
       Construct a ChannelCommunicationModel.
    */
    public ChannelCommunicationModel() {
    }

    @Override
    public void initialise(Config config, WorldModel<? extends Entity> model) {
        world = StandardWorldModel.createStandardWorldModel(model);
        // Read the channel information
        channels = new HashMap<Integer, Channel>();
        noise = new HashMap<Integer, Noise>();
        int count = config.getIntValue(COUNT_KEY);
        for (int i = 0; i < count; ++i) {
            String type = config.getValue(PREFIX + i + TYPE_SUFFIX);
            if (TYPE_VOICE.equals(type)) {
                channels.put(i, new VoiceChannel(config, i, world));
            }
            else if (TYPE_RADIO.equals(type)) {
                channels.put(i, new RadioChannel(config, i));
            }
            else {
                System.err.println("Unrecognised channel type: " + PREFIX + i + TYPE_SUFFIX + " = '" + type + "'");
            }
        }
        createNoiseObjects(config);
        platoonMax = config.getIntValue(PLATOON_MAX_CHANNELS_KEY, 1);
        centreMax = config.getIntValue(CENTRE_MAX_CHANNELS_KEY, 2);
        random = new Random();
        long seed = config.getIntValue(Constants.RANDOM_SEED_KEY, 0);
        if (seed != 0) {
            random.setSeed(seed);
        }
    }

    @Override
    public String toString() {
        return "Channel communication model";
    }

    @Override
    public Map<AgentProxy, Collection<Message>> process(int time, Collection<AgentProxy> agents, Collection<Command> agentCommands) {
        // Reset all channels
        for (Channel next : channels.values()) {
            next.timestep();
            next.setAgents(agents);
        }
        // Look for subscription commands
        for (Command next : agentCommands) {
            if (next instanceof AKSubscribe) {
                processSubscribe(agents, (AKSubscribe)next);
            }
        }
        // Now push all speak commands through the right channels
        for (Command next : agentCommands) {
            if (next instanceof AKSpeak) {
                AKSpeak speak = (AKSpeak)next;
                int channelNumber = speak.getChannel();
                Channel channel = channels.get(channelNumber);
                Noise n = noise.get(channelNumber);
                if (channel == null) {
                    System.out.println("Unrecognised channel: " + channelNumber);
                }
                else {
                    if (n != null) {
                        //                        System.out.println("Adding noise to message:");
                        //                        rescuecore2.connection.ByteLogger.log(speak.getContent());
                        speak = n.addNoise(speak);
                        //                        System.out.println("Added noise to message:");
                        //                        rescuecore2.connection.ByteLogger.log(speak.getContent());
                    }
                    channel.push(speak);
                }
            }
        }
        // And find out what each agent can hear
        Map<AgentProxy, Collection<Message>> result = new HashMap<AgentProxy, Collection<Message>>();
        for (AgentProxy agent : agents) {
            Collection<Message> msgs = new ArrayList<Message>();
            for (Channel next : channels.values()) {
                msgs.addAll(next.getMessagesForAgent(agent));
            }
            result.put(agent, msgs);
        }
        return result;
    }

    private void createNoiseObjects(Config config) {
        int count = config.getIntValue(COUNT_KEY);
        for (int i = 0; i < count; ++i) {
            String key = PREFIX + i + NOISE_SUFFIX;
            if (config.isDefined(key)) {
                List<String> types = config.getArrayValue(key);
                List<Noise> noises = new ArrayList<Noise>(types.size());
                for (String next : types) {
                    if (next.startsWith(NOISE_TYPE_DROPOUT)) {
                        double p = Double.parseDouble(next.substring(next.indexOf("(") + 1, next.indexOf(")")));
                        noises.add(new DropoutNoise(p));
                    }
                    else if (next.startsWith(NOISE_TYPE_STATIC)) {
                        double p = Double.parseDouble(next.substring(next.indexOf("(") + 1, next.indexOf(")")));
                        noises.add(new StaticNoise(p));
                    }
                    else {
                        System.err.println("Unrecognised noise type: " + key + " = '" + next + "'");
                    }
                }
                Noise result;
                if (noises.size() == 1) {
                    result = noises.get(0);
                }
                else {
                    result = new ChainedNoise(noises);
                }
                noise.put(i, result);
            }
        }
    }

    private AgentProxy findAgent(Collection<AgentProxy> agents, Entity e) {
        for (AgentProxy next : agents) {
            if (next.getControlledEntity().equals(e)) {
                return next;
            }
        }
        return null;
    }

    private void processSubscribe(Collection<AgentProxy> agents, AKSubscribe sub) {
        List<Integer> requested = sub.getChannels();
        EntityID id = sub.getAgentID();
        Entity entity = world.getEntity(id);
        if (entity == null) {
            System.out.println("Couldn't find entity " + id);
            return;
        }
        AgentProxy agent = findAgent(agents, entity);
        if (agent == null) {
            System.out.println("Couldn't find agent controlling entity " + entity);
            return;
        }
        int max;
        if (entity instanceof FireBrigade || entity instanceof PoliceForce || entity instanceof AmbulanceTeam) {
            max = platoonMax;
        }
        else if (entity instanceof FireStation || entity instanceof PoliceOffice || entity instanceof AmbulanceCentre) {
            max = centreMax;
        }
        else {
            System.out.println("I don't know how to handle subscriptions for this entity: " + entity);
            return;
        }
        if (requested.size() > max) {
            System.out.println("Agent tried to subscribe to " + requested.size() + " channels but only " + max + " allowed");
            return;
        }
        // Unsubscribe from all old channels
        for (Channel next : channels.values()) {
            next.removeSubscriber(agent);
        }
        // Subscribe to new channels
        for (int next : requested) {
            Channel channel = channels.get(next);
            if (channel == null) {
                System.out.println("Agent tried to subscribe to non-existant channel " + next);
            }
            else {
                channel.addSubscriber(agent);
            }
        }
    }

    private static interface Channel {
        void timestep();
        void addSubscriber(AgentProxy a);
        void removeSubscriber(AgentProxy a);
        Collection<AgentProxy> getSubscribers();
        void push(AKSpeak message);
        Collection<Message> getMessagesForAgent(AgentProxy agent);
        void setAgents(Collection<AgentProxy> agents);
    }

    private abstract static class AbstractChannel implements Channel {
        protected Collection<AgentProxy> subscribers;
        protected int channelID;
        protected Collection<AgentProxy> allAgents;
        private Map<AgentProxy, Collection<KAHearChannel>> messagesForAgents;

        public AbstractChannel(int channelID) {
            this.channelID = channelID;
            subscribers = new HashSet<AgentProxy>();
            messagesForAgents = new HashMap<AgentProxy, Collection<KAHearChannel>>();
        }

        @Override
        public void timestep() {
            messagesForAgents.clear();
        }

        @Override
        public void addSubscriber(AgentProxy a) {
            subscribers.add(a);
        }

        @Override
        public void removeSubscriber(AgentProxy a) {
            subscribers.remove(a);
        }

        @Override
        public Collection<AgentProxy> getSubscribers() {
            return subscribers;
        }

        @Override
        public Collection<Message> getMessagesForAgent(AgentProxy agent) {
            Collection<KAHearChannel> c = messagesForAgents.get(agent);
            if (c == null) {
                c = new ArrayList<KAHearChannel>();
            }
            return new ArrayList<Message>(c);
        }

        @Override
        public void setAgents(Collection<AgentProxy> agents) {
            allAgents = agents;
        }

        protected void addMessageForAgent(AgentProxy a, KAHearChannel msg) {
            Collection<KAHearChannel> c = messagesForAgents.get(a);
            if (c == null) {
                c = new ArrayList<KAHearChannel>();
                messagesForAgents.put(a, c);
            }
            c.add(msg);
        }
    }

    private static class VoiceChannel extends AbstractChannel {
        private int range;
        private int maxSize;
        private int maxMessages;
        private Map<EntityID, Integer> uttered;
        private StandardWorldModel world;

        public VoiceChannel(Config config, int index, StandardWorldModel world) {
            super(index);
            this.world = world;
            range = config.getIntValue(PREFIX + index + RANGE_SUFFIX);
            maxSize = config.getIntValue(PREFIX + index + MESSAGE_SIZE_SUFFIX);
            maxMessages = config.getIntValue(PREFIX + index + MESSAGE_MAX_SUFFIX);
            uttered = new HashMap<EntityID, Integer>();
        }

        @Override
        public void timestep() {
            super.timestep();
            uttered.clear();
        }

        @Override
        public void push(AKSpeak speak) {
            EntityID id = speak.getAgentID();
            byte[] data = speak.getContent();
            Integer i = uttered.get(id);
            int count = i == null ? 0 : i.intValue();
            if (count >= maxMessages) {
                System.out.println("Agent " + id + " has uttered too many voice messages on channel " + channelID + ": limit is " + maxMessages);
                return;
            }
            if (data.length > maxSize) {
                System.out.println("Agent " + id + " tried to send an oversize voice message: " + data.length + " bytes but the limit is " + maxSize);
                return;
            }
            uttered.put(id, count + 1);
            // Find out who can hear it
            StandardEntity sender = world.getEntity(id);
            for (AgentProxy agent : allAgents) {
                StandardEntity target = (StandardEntity)agent.getControlledEntity();
                if (world.getDistance(sender, target) <= range) {
                    addMessageForAgent(agent, new KAHearChannel(id, channelID, data));
                }
            }
        }
    }

    private static class RadioChannel extends AbstractChannel {
        private int bandwidth;
        private int usedBandwidth;

        public RadioChannel(Config config, int index) {
            super(index);
            bandwidth = config.getIntValue(PREFIX + index + BANDWIDTH_SUFFIX);
        }

        @Override
        public void timestep() {
            usedBandwidth = 0;
        }

        @Override
        public void push(AKSpeak speak) {
            byte[] data = speak.getContent();
            if (usedBandwidth + data.length > bandwidth) {
                System.out.println("Discarding message on channel " + channelID + ": already used " + usedBandwidth + " of " + bandwidth + " bytes, new message is " + data.length + " bytes.");
                return;
            }
            for (AgentProxy next : subscribers) {
                addMessageForAgent(next, new KAHearChannel(speak.getAgentID(), channelID, data));
            }
            usedBandwidth += data.length;
        }
    }

    /**
       Noise implementations mess with messages in some way.
     */
    private static interface Noise {
        /**
           Optionally add some noise to a message and return either the original message or a replacement.
           @param message The message to tinker with.
           @return The original message or a replacement.
        */
        AKSpeak addNoise(AKSpeak message);
    }

    /**
       A Noise implementation that chains Noise objects together.
    */
    private static class ChainedNoise implements Noise {
        private List<Noise> chain;

        public ChainedNoise(Collection<Noise> chain) {
            this.chain = new ArrayList<Noise>(chain);
        }

        @Override
        public AKSpeak addNoise(AKSpeak message) {
            AKSpeak current = message;
            for (Noise next : chain) {
                current = next.addNoise(current);
            }
            return current;
        }
    }

    /**
       Dropout noise completely zeroes a message with some probability.
    */
    private class DropoutNoise implements Noise {
        private double p;

        /**
           Construct a DropoutNoise object that will wipe out messages with some probability.
           @param p The probability of destroying a message.
        */
        public DropoutNoise(double p) {
            this.p = p;
        }

        @Override
        public AKSpeak addNoise(AKSpeak message) {
            if (random.nextDouble() >= p) {
                return message;
            }
            return new AKSpeak(message.getAgentID(), message.getTime(), message.getChannel(), new byte[0]);
        }
    }

    /**
       Static noise flips bits in the message with some probability.
    */
    private class StaticNoise implements Noise {
        private static final int BITS = 8;

        private double p;

        /**
           Construct a StaticNoise object that will flip bits with some probability.
           @param p The probability of flipping a bit.
        */
        public StaticNoise(double p) {
            this.p = p;
        }

        @Override
        public AKSpeak addNoise(AKSpeak message) {
            byte[] data = message.getContent();
            for (int i = 0; i < data.length; ++i) {
                for (int j = 0; j < BITS; ++j) {
                    if (random.nextDouble() < p) {
                        // Flip this bit
                        data[i] = (byte)(data[i] ^ (1 << j));
                    }
                }
            }
            return new AKSpeak(message.getAgentID(), message.getTime(), message.getChannel(), data);
        }
    }
}