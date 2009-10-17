package kernel.standard;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import kernel.AgentProxy;
import kernel.CommunicationModel;

import rescuecore2.messages.Message;
import rescuecore2.messages.Command;
import rescuecore2.config.Config;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.WorldModel;

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
    private static final String COUNT_KEY = "channels.count";
    private static final String PLATOON_MAX_CHANNELS_KEY = "channels.platoon.max";
    private static final String CENTRE_MAX_CHANNELS_KEY = "channels.centre.max";

    private static final String PREFIX = "channels.";
    private static final String TYPE_SUFFIX = ".type";

    private static final String TYPE_VOICE = "voice";
    private static final String TYPE_RADIO = "radio";

    // Voice constants
    private static final String RANGE_SUFFIX = ".range";
    private static final String MESSAGE_SIZE_SUFFIX = ".messages.size";
    private static final String MESSAGE_MAX_SUFFIX = ".messages.max";

    // Radio constants
    private static final String BANDWIDTH_SUFFIX = ".bandwidth";

    private Map<Integer, Channel> channels;
    private int platoonMax;
    private int centreMax;
    private StandardWorldModel world;

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
        int count = config.getIntValue(COUNT_KEY);
        for (int i = 1; i <= count; ++i) {
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
        platoonMax = config.getIntValue(PLATOON_MAX_CHANNELS_KEY, 1);
        centreMax = config.getIntValue(CENTRE_MAX_CHANNELS_KEY, 2);
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
                if (channel == null) {
                    System.out.println("Unrecognised channel: " + channelNumber);
                }
                else {
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
            return new ArrayList<Message>(messagesForAgents.get(agent));
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
            int count = uttered.get(id);
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
}