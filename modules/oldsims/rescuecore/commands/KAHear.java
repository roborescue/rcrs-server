/*
 * Last change: $Date: 2004/08/03 03:25:04 $
 * $Revision: 1.2 $
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

import rescuecore.InputBuffer;
import rescuecore.OutputBuffer;
import rescuecore.RescueConstants;

public class KAHear extends Command {
	private int toID;
	private int fromID;
	private int length;
	private byte[] msg;
	private byte channel;

	public KAHear(int to, int from, int length, byte[] data, byte channel) {
		super(RescueConstants.KA_HEAR);
		toID = to;
		fromID = from;
		this.length = length;
		msg = new byte[length];
		System.arraycopy(data,0,msg,0,length);
		this.channel = channel;
	}

	public KAHear(InputBuffer in) {
		super(RescueConstants.KA_HEAR);
		read(in);
	}

	public void read(InputBuffer in) {
		toID = in.readInt();
		fromID = in.readInt();
		channel = (byte)in.readInt();
		length = in.readInt();
		msg = new byte[length];
		in.readBytes(msg);
	}

	public void write(OutputBuffer out) {	
		out.writeInt(toID);
		out.writeInt(fromID);
		out.writeInt(channel);
		out.writeInt(length);
		out.writeBytes(msg);
	}

	public int getToID() {
		return toID;
	}

	public int getFromID() {
		return fromID;
	}

	public int getLength() {
		return length;
	}

	public byte[] getData() {
		return msg;
	}

	public byte getChannel() {
		return channel;
	}

	public String toString() {
		return super.toString()+" from "+fromID+" to "+toID+": "+length+" bytes on channel "+channel;
	}
}
