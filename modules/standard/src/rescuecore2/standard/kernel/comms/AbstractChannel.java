package rescuecore2.standard.kernel.comms;

import java.util.Collection;
import java.util.Map;
import java.util.HashSet;
import java.util.ArrayList;

import rescuecore2.worldmodel.Entity;
import rescuecore2.misc.collections.LazyMap;
import rescuecore2.log.Logger;

import rescuecore2.standard.messages.AKSpeak;

/**
   Abstract base class for channels.
*/
public abstract class AbstractChannel implements Channel {
    /** The set of subscribers. */
    protected Collection<Entity> subscribers;

    /** This channels's ID. */
    protected int channelID;

    private Map<Entity, Collection<AKSpeak>> messagesForAgents;
    private Noise inputNoise;
    private Noise outputNoise;

    /**
       Construct an AbstractChannel.
       @param channelID The ID of this channel.
    */
    protected AbstractChannel(int channelID) {
        this.channelID = channelID;
        subscribers = new HashSet<Entity>();
        messagesForAgents = new LazyMap<Entity, Collection<AKSpeak>>() {
            @Override
            public Collection<AKSpeak> createValue() {
                return new ArrayList<AKSpeak>();
            }
        };
        inputNoise = null;
        outputNoise = null;
    }

    @Override
    public void setInputNoise(Noise noise) {
        inputNoise = noise;
    }

    @Override
    public void setOutputNoise(Noise noise) {
        outputNoise = noise;
    }

    @Override
    public void timestep() {
        messagesForAgents.clear();
    }

    @Override
    public void addSubscriber(Entity a) {
        subscribers.add(a);
    }

    @Override
    public void removeSubscriber(Entity a) {
        subscribers.remove(a);
    }

    @Override
    public Collection<Entity> getSubscribers() {
        return subscribers;
    }

    @Override
    public Collection<AKSpeak> getMessagesForAgent(Entity agent) {
        Collection<AKSpeak> c = messagesForAgents.get(agent);
        return new ArrayList<AKSpeak>(c);
    }

    @Override
    public final void push(AKSpeak speak) throws InvalidMessageException {
        int channel = speak.getChannel();
        if (channel != channelID) {
            throw new InvalidMessageException("Tried to push '" + speak + "' to channel " + channelID);
        }
        int originalSize = speak.getContent().length;
        if(originalSize == 0){
        	throw new InvalidMessageException("Tried to push empty message to channel " + channelID);
        }
        Logger.debug("Pushing " + speak + " through channel " + channelID);
        
        speak = applyInputNoise(speak);
        Logger.debug("Input noise result: " + speak);
//        if (speak != null) {
            pushImpl(speak,originalSize);
//        }
    }

    /**
       Push a message after input noise has been applied.
       @param msg The message.
       @throws InvalidMessageException If the message is invalid.
    */
    protected abstract void pushImpl(AKSpeak msg, int originalSize) throws InvalidMessageException;

    /**
       Register a message that should be send to an agent on the next call to @{link #getMessagesForAgent(AgentProxy)}. This method will ignore the subscribers list so subclasses should use @{link #isSubscribed(Entity)} if they wish to restrict messages to subscribers only.
       @param a The agent.
       @param msg The message.
     */
    protected void addMessageForAgent(Entity a, AKSpeak msg) {
        Logger.debug("Adding message " + msg + " for agent " + a);
        msg = applyOutputNoise(msg);
        Logger.debug("Output noise result: " + msg);
        if (msg != null) {
            Collection<AKSpeak> c = messagesForAgents.get(a);
            c.add(msg);
        }
    }

    /**
       Register a message that should be send to all subscribers.
       @param msg The message.
     */
    protected void addMessageForSubscribers(AKSpeak msg) {
        for (Entity e : subscribers) {
            addMessageForAgent(e, msg);
        }
    }

    /**
       Find out if an entity is subscribed to this channel.
       @param e The entity to check.
       @return True iff the entity is subscribed to this channel.
    */
    protected boolean isSubscribed(Entity e) {
        return subscribers.contains(e);
    }

    /**
       Apply the input noise to a message.
       @param msg The message to apply input noise to.
       @return A message with noise added, or the original message, or null.
    */
    private AKSpeak applyInputNoise(AKSpeak msg) {
        if (inputNoise != null) {
            return inputNoise.applyNoise(msg);
        }
        return msg;
    }

    /**
       Apply the output noise to a message.
       @param msg The message to apply output noise to.
       @return A message with noise added, or the original message, or null.
    */
    private AKSpeak applyOutputNoise(AKSpeak msg) {
        if (outputNoise != null) {
            return outputNoise.applyNoise(msg);
        }
        return msg;
    }
}
