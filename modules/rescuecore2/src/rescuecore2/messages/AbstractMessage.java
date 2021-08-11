package rescuecore2.messages;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * An abstract base class for Message objects. This class is implemented in
 * terms of MessageComponent objects so subclasses need only provide a urn and a
 * list of MessageComponent objects.
 */
public abstract class AbstractMessage implements Message {

  private String urn;


  /**
   * Construct a message with a given urn.
   *
   * @param urn
   *          The urn of the message.
   */
  protected AbstractMessage( String urn ) {
    this.urn = urn;
  }


  /**
   * Construct a message with a urn defined as an enum.
   *
   * @param urn
   *          The urn of the message.
   */
  protected AbstractMessage( Enum<?> urn ) {
    this( urn.toString() );
  }


  @Override
  public final String getURN() {
    return this.urn;
  }


  @Override
  abstract public void write( OutputStream out ) throws IOException;

  @Override
  abstract public void read( InputStream in ) throws IOException;
}