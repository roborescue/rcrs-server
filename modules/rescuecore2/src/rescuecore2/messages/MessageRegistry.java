package rescuecore2.messages;

import rescuecore2.messages.control.ControlMessageFactory;

import java.util.Map;
import java.util.HashMap;
import java.io.InputStream;
import java.io.IOException;

/**
   A class for managing the different types of messages that can be passed around.
 */
public final class MessageRegistry {
    private static Map<String, MessageFactory> factories;

    // Static class; private constructor.
    private MessageRegistry() {}

    static {
        factories = new HashMap<String, MessageFactory>();
        // Automatically register control messages
        register(ControlMessageFactory.INSTANCE);
    }

    /**
       Register a message factory.
       @param factory The message factory to register.
     */
    public static void register(MessageFactory factory) {
        for (String urn : factory.getKnownMessageURNs()) {
            register(urn, factory);
        }
    }

    /**
       Register a message urn and assign a MessageFactory for decoding instances of this message type.
       @param urn The message urn to register.
       @param factory The factory that is responsible for decoding messages of this type.
     */
    public static void register(String urn, MessageFactory factory) {
        synchronized (factories) {
            MessageFactory old = factories.get(urn);
            if (old != null && old != factory) {
                System.out.println("WARNING: Message " + urn + " is being clobbered by " + factory + ". Old factory: " + old);
            }
            factories.put(urn, factory);
        }
    }

    /**
       Deregister a message urn.
       @param urn The message urn to deregister.
     */
    public static void deregister(String urn) {
        synchronized (factories) {
            factories.remove(urn);
        }
    }

    /**
       Create a message based on its urn and populate it with data from a stream. If the urn is not recognised then return null. This method will delegate to the {@link #register(MessageFactory) previously registered} MessageFactory.
       @param urn The urn of the message type to create.
       @param data An InputStream to read message data from.
       @return A new Message object, or null if the urn is not recognised.
       @throws IOException If there is a problem reading the stream.
     */
    public static Message createMessage(String urn, InputStream data) throws IOException {
        MessageFactory factory;
        synchronized (factories) {
            factory = factories.get(urn);
        }
        if (factory == null) {
            System.out.println("Message " + urn + " not recognised.");
            return null;
        }
        return factory.createMessage(urn, data);
    }
}