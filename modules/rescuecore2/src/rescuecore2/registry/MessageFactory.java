package rescuecore2.registry;

import java.io.IOException;
import java.io.InputStream;

import rescuecore2.messages.Message;
import rescuecore2.messages.protobuf.RCRSProto.MessageProto;

/**
 * Factory class for creating messages.
 */
public interface MessageFactory extends Factory {
  /**
   * Create a message based on its urn and populate it with data from a stream. If
   * the urn is not recognised then return null.
   *
   * @param urn  The urn of the message type to create.
   * @param data An InputStream to read message data from.
   * @return A new Message object, or null if the urn is not recognised.
   * @throws IOException If there is a problem reading the stream.
   */
  Message makeMessage(int urn, InputStream data) throws IOException;

  Message makeMessage(int urn, MessageProto proto);
}