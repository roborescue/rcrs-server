/*
 * Last change: $Date$
 * $Revision$
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

import java.io.*;
import java.util.*;
import rescuecore.commands.*;
//import rescuecore.debug.*;

public abstract class Simulator extends RescueComponent {
	protected int timeStep;
    protected Memory memory;
	protected boolean running;

    protected Simulator() {
		running = false;
		timeStep = -1;
    }

	public final int getComponentType() {
		return RescueConstants.COMPONENT_TYPE_SIMULATOR;
	}

	public final Command generateConnectCommand() {
            return new SKConnect(getClass().getName());
	}

	public final boolean handleConnectOK(Command c) {
		return true;
	}

	public final String handleConnectError(Command c) {
		return null;
	}

	public boolean isRunning() {
		return running;
	}

	public void shutdown() {
		running = false;
	}

	public void handleMessage(Command c) {
		//		System.out.println("Handling "+c);
		switch (c.getType()) {
		case RescueConstants.COMMANDS:
			handleCommands(c);
			break;
		case RescueConstants.KS_CONNECT_OK:
			// Someone obviously didn't get our SK_ACKNOWLEDGE
			RescueMessage ack = new RescueMessage();
			//			ack.append(new SK_ACKNOWLEDGE());
			sendMessage(ack);
			break;
		}
	}

    /**
       Initialise this simulator. Subclasses that override this method should invoke super.initialise(knowledge) at some point.
       @param knowledge This simulator's knowledge of the world
	*/
    protected void initialise(RescueObject[] knowledge) {
		memory = generateMemory();
		for (int i=0;i<knowledge.length;++i) {
			memory.add(knowledge[i],0,RescueConstants.SOURCE_INITIAL);
		}
    }

    /**
       Construct a new Memory object for use by this Agent. This method allows Agents to customise their choice of Memory object. The default implementation returns an {@link HashMemory}.
       @return A new Memory object
	*/
    protected Memory generateMemory() {
		return new HashMemory();
    }

	private void handleCommands(Command c) {
	}
}
