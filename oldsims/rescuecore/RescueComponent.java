/*
 * Last change: $Date: 2005/06/14 21:55:50 $
 * $Revision: 1.7 $
 *
 * Copyright (c) 2004, The Black Sheep, Department of Computer Science, The University of Auckland
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of The Black Sheep, The Department of Computer Science or The University of Auckland nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package rescuecore;

import rescuecore.commands.*;
import java.io.*;

public abstract class RescueComponent {
	private RescueMessage currentMessage;
	private Connection connection;

	/** Handle an incoming message
		@param msg The incoming message
	*/
	public abstract void handleMessage(Command msg);

	/**
	   Get the type of this component, either COMPONENT_TYPE_AGENT, COMPONENT_TYPE_SIMULATOR or COMPONENT_TYPE_VIEWER.
	   @return The type of this component
	   @see RescueConstants#COMPONENT_TYPE_AGENT
	   @see RescueConstants#COMPONENT_TYPE_SIMULATOR
	   @see RescueConstants#COMPONENT_TYPE_VIEWER
	*/
	public abstract int getComponentType();

	/**
	   Generate a Command that can be used to connect this component to the kernel
	   @return A Command that will connect this component to the kernel
	*/
	public abstract Command generateConnectCommand();

	/**
	   Handle a message that looks like a successful connection to the kernel
	   @param c The message from the kernel
	   @return If this really was a successful connection
	*/
	public abstract boolean handleConnectOK(Command c);

	/**
	   Handle a message that looks like an unsuccessful connection to the kernel
	   @param c The message from the kernel
	   @return A string describing the error, or null if this message does not indicate a failed connection
	 */
	public abstract String handleConnectError(Command c);

	/**
	   Find out whether this component is still running
	   @return true iff this component is still running
	*/
	public abstract boolean isRunning();

	/**
	   Shut the component down
	*/
	public abstract void shutdown();

	/**
	   Set the Connection that should be used for talking to the kernel. Subclasses should NOT attempt to read input from this connection.
	   @param connection The Connection to use when talking to the kernel.
	*/
	public void setConnection(Connection connection) {
		this.connection = connection;
	}

    /**
       Send a message to the kernel
       @param message The message to send
    */
    public void sendMessage(RescueMessage message) {
		if (connection==null) {
			System.out.println(this+" tried to send to a null socket");
			return;
		}
		try {
			connection.send(message.toByteArray());
		}
		catch (IOException e) {
			System.err.println("Error while sending message: "+e);
			e.printStackTrace();
		}
    }

    /**
       Add a Command to our current set of Commands to be sent to the kernel. Commands will be buffered until @{link #flushCommands()} is called.
       @param c The next Command to send.
	*/
    protected void appendCommand(Command c) {
		if (currentMessage==null) currentMessage = new RescueMessage();
		currentMessage.append(c);
    }

    /**
       Send all our buffered commands to the kernel
	*/
    protected void flushCommands() {
		if (currentMessage!=null) {
			sendMessage(currentMessage);
		}
		currentMessage = null;
    }
}
