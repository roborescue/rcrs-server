package rescuecore2.registry;

import java.io.IOException;
import java.io.InputStream;

import rescuecore2.URN;
import rescuecore2.messages.Message;
import rescuecore2.messages.protobuf.RCRSProto.MessageProto;

/**
 * An abstract message factory with helper methods for defining URNs with enums.
 *
 * @param <T> An enum type that defines the URNs this factory knows about.
 */
public abstract class AbstractMessageFactory<T extends Enum<T> & URN> extends AbstractFactory<T>
    implements MessageFactory {

  /**
   * Constructor for AbstractMessageFactory.
   *
   * @param clazz The class of enum this factory uses.
   */
  protected AbstractMessageFactory(Class<T> clazz) {
    super(clazz);
  }

  @Override
  public Message makeMessage(int urn, InputStream data) throws IOException {
    return makeMessage(getURNEnum(urn), data);
  }

  @Override
  public Message makeMessage(int urn, MessageProto data) {
    return makeMessage(getURNEnum(urn), data);
  }

  /**
   * Create a message based on its urn and populate it with data from a stream. If
   * the urn is not recognised then return null.
   *
   * @param urn  The urn of the message type to create.
   * @param data An InputStream to read message data from.
   * @return A new Message object, or null if the urn is not recognised.
   * @throws IOException If there is a problem reading the stream.
   */
  protected abstract Message makeMessage(T urn, InputStream data) throws IOException;

  protected abstract Message makeMessage(T urn, MessageProto data);
}