package rescuecore2.messages.control;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.control.ControlMessageProto.VKAcknowledgeProto;

/**
 * A message for acknowledging a connection to the kernel.
 */
public class VKAcknowledge extends AbstractMessage {

  private int requestID;
  private int viewerID;


  /**
   * A VKAcknowledge message that populates its data from a stream.
   *
   * @param in
   *          The InputStream to read.
   * @throws IOException
   *           If there is a problem reading the stream.
   */
  public VKAcknowledge( InputStream in ) throws IOException {
    super( ControlMessageURN.VK_ACKNOWLEDGE.toString() );
    this.read( in );
  }


  /**
   * VKAcknowledge message with specific request ID and viewer ID components.
   *
   * @param requestID
   *          The value of the request ID component.
   * @param viewerID
   *          The value of the viewer ID component.
   */
  public VKAcknowledge( int requestID, int viewerID ) {
    super( ControlMessageURN.VK_ACKNOWLEDGE.toString() );
    this.requestID = requestID;
    this.viewerID = viewerID;
  }


  /**
   * Get the request ID.
   *
   * @return The request ID component.
   */
  public int getRequestID() {
    return this.requestID;
  }


  /**
   * Get the viewer ID.
   *
   * @return The viewer ID component.
   */
  public int getViewerID() {
    return this.viewerID;
  }


  public void write( OutputStream out ) throws IOException {
    VKAcknowledgeProto vkAcknowledge = VKAcknowledgeProto.newBuilder()
        .setRequestID( this.requestID ).setViewerID( this.viewerID ).build();

    vkAcknowledge.writeTo( out );
  }


  public void read( InputStream in ) throws IOException {
    VKAcknowledgeProto vkAcknowledge = VKAcknowledgeProto.parseFrom( in );

    this.requestID = vkAcknowledge.getRequestID();
    this.viewerID = vkAcknowledge.getViewerID();
  }
}