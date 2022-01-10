/*
 * Last change: $Date: 2004/05/20 23:42:00 $
 * $Revision: 1.1 $
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

package rescuecore.commands;

import rescuecore.Handy;
import rescuecore.InputBuffer;
import rescuecore.OutputBuffer;

/**
   This class encapsulates a command.
   @see RescueConstants#COMMAND_MOVE
   @see RescueConstants#COMMAND_EXTINGUISH
   @see RescueConstants#COMMAND_CLEAR
   @see RescueConstants#COMMAND_LOAD
   @see RescueConstants#COMMAND_UNLOAD
   @see RescueConstants#COMMAND_RESCUE
 */
public abstract class AgentCommand extends Command {
	protected int senderID;
    protected int time;

    protected AgentCommand(int type, int senderID, int time) {
		super(type);
		this.senderID = senderID;
                this.time = time;
    }

	public final int getSender() {
		return senderID;
	}

    public final int getTime() {
        return time;
    }

    public void write(OutputBuffer out) {
		out.writeInt(senderID);
                out.writeInt(time);
	}

	public void read(InputBuffer in) {
		senderID = in.readInt();
                time = in.readInt();
	}

    public String toString() {
		return Handy.getCommandTypeName(type)+" from agent "+senderID+" at time " + time;
    }
}
