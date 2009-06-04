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
    private static Map<Integer, MessageFactory> factories;

    // Static class; private constructor.
    private MessageRegistry() {}

    static {
        factories = new HashMap<Integer, MessageFactory>();
        // Automatically register control messages
        register(new ControlMessageFactory());
    }

    /**
       Register a message factory.
       @param factory The message factory to register.
     */
    public static void register(MessageFactory factory) {
        for (int i : factory.getKnownMessageTypeIDs()) {
            register(i, factory);
        }
    }

    /**
       Register a message ID number and assign a MessageFactory for decoding instances of this message type.
       @param id The message type ID to register.
       @param factory The factory that is responsible for decoding messages of this type.
     */
    public static void register(int id, MessageFactory factory) {
        synchronized (factories) {
            MessageFactory old = factories.get(id);
            if (old != null && old != factory) {
                System.out.println("WARNING: Message ID " + id + " is being clobbered by " + factory + ". Old factory: " + old);
            }
            factories.put(id, factory);
        }
    }

    /**
       Deregister a message ID number.
       @param id The message type ID to deregister.
     */
    public static void deregister(int id) {
        synchronized (factories) {
            factories.remove(id);
        }
    }

    /**
       Create a message based on its type ID and populate it with data from a stream. If the ID is not recognised then return null. This method will delegate to the {@link #register(int) previously registered} MessageFactory.
       @param id The id of the message type to create.
       @param data An InputStream to read message data from.
       @return A new Message object, or null if the ID is not recognised.
       @throws IOException If there is a problem reading the stream.
     */
    public static Message createMessage(int id, InputStream data) throws IOException {
        MessageFactory factory;
        synchronized (factories) {
            factory = factories.get(id);
        }
        if (factory == null) {
            System.out.println("Message id " + id + " not recognised.");
            return null;
        }
        return factory.createMessage(id, data);
    }
}