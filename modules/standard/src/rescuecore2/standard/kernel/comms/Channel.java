package rescuecore2.standard.kernel.comms;

import java.util.Collection;

import rescuecore2.worldmodel.Entity;

import rescuecore2.standard.messages.AKSpeak;

/**
   Interface for Channels used by the ChannelCommunicationModel.
*/
public interface Channel {
    /**
       Notify the channel that a timestep has elapsed.
    */
    void timestep();

    /**
       Add a subscriber to this channel.
       @param e The subscriber.
    */
    void addSubscriber(Entity e);

    /**
       Remove a subscriber from this channel.
       @param e The subscriber.
    */
    void removeSubscriber(Entity e);

    /**
       Get all subscribers.
       @return All subscribers.
    */
    Collection<Entity> getSubscribers();

    /**
       Push a message through this channel.
       @param message The message to push.
       @throws InvalidMessageException If the message is invalid.
    */
    void push(AKSpeak message) throws InvalidMessageException;

    /**
       Get the messages that should be send to an agent.
       @param agent The agent to look up.
       @return All messages for that agent.
    */
    Collection<AKSpeak> getMessagesForAgent(Entity agent);

    /**
       Set the input noise object for this channel. Input noise is applied to the message once on arrival.
       @param noise The input noise.
     */
    void setInputNoise(Noise noise);

    /**
       Set the output noise object for this channel. Output noise is applied to the message once for each listener as it is sent.
       @param noise The output noise.
    */
    void setOutputNoise(Noise noise);
}

