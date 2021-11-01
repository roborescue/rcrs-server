/*
 * Last change: $Date: 2004/05/20 23:41:59 $ $Revision: 1.5 $ Copyright (c)
 * 2004, The Black Sheep, Department of Computer Science, The University of
 * Auckland All rights reserved. Redistribution and use in source and binary
 * forms, with or without modification, are permitted provided that the
 * following conditions are met: Redistributions of source code must retain the
 * above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution. Neither the name of
 * The Black Sheep, The Department of Computer Science or The University of
 * Auckland nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package rescuecore;

import rescuecore.commands.*;
import java.util.*;

/**
 * A RescueMessage is a package of Commands to be sent to the kernal
 */
public class RescueMessage {

  private List parts;


  public RescueMessage() {
    parts = new ArrayList();
  }


  /**
   * Append a new Command to the end of this message
   *
   * @param command
   *          The Command to append
   */
  public void append( Command command ) {
    parts.add( command );
  }


  /**
   * Go through all parts of this message and make sure we don't have too many
   * of any kind of Command
   *
   * @param type
   *          The bitwise or of the types of Command that are being culled
   * @param max
   *          The maximum number of Commands of the given type that we are
   *          allowed
   * @return The number of matching Commands left in this message once we're
   *         finished
   */
  @Deprecated
  public int cull( int type, int max ) {
    int count = 0;
    for ( Iterator it = parts.iterator(); it.hasNext(); ) {
      Command next = (Command) it.next();
      if ( ( next.getType() & type ) != 0 ) {
        ++count;
        if ( count > max ) it.remove();
      }
    }
    return count;
  }


  /**
   * Go through all parts of this message and count how many of each type of
   * Commannd we have
   *
   * @param type
   *          The bitwise or of the types of Command that are being counted
   * @return The number of matching Commands
   */
  @Deprecated
  public int count( int type ) {
    int count = 0;
    for ( Iterator it = parts.iterator(); it.hasNext(); ) {
      Command next = (Command) it.next();
      if ( ( next.getType() & type ) != 0 ) {
        ++count;
      }
    }
    return count;
  }


  /**
   * Turn this RescueMessage into a raw byte array ready for wrapping in a
   * LongUDPMessage
   *
   * @return A byte array ready for wrapping
   * @see LongUDPMessage
   */
  public byte[] toByteArray() {
    OutputBuffer out = new OutputBuffer();
    Command[] all = new Command[parts.size()];
    parts.toArray( all );
    out.writeCommands( all );
    return out.getBytes();
  }


  public String toString() {
    StringBuffer result = new StringBuffer();
    result.append( "RescueMessage with " );
    result.append( parts.size() );
    result.append( parts.size() == 1 ? " part" : " parts" );
    result.append( ": " );
    for ( Iterator it = parts.iterator(); it.hasNext(); ) {
      result.append(
          Handy.getCommandTypeName( ( (Command) it.next() ).getType() ) );
      if ( it.hasNext() ) result.append( ", " );
    }
    return result.toString();
  }
}
