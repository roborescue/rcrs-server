/*
 * Last change: $Date: 2005/02/20 01:29:55 $
 * $Revision: 1.15 $
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
import rescuecore.objects.*;
import java.util.*;

public class InputBuffer {
	private byte[] data;
	private int index;

	public InputBuffer(byte[] b) {
		data = new byte[b.length];
		System.arraycopy(b,0,data,0,b.length);
	}

	public int getSize() {
		return data.length;
	}

	public int available() {
		return data.length-index;
	}

	public byte readByte() {
		return data[index++];
	}

	public void readBytes(byte[] result) {
		System.arraycopy(data,index,result,0,result.length);
		index += result.length;
	}

	public short readShort() {
		int result = ((data[index++] << 8) & 0xFF00) | (data[index++] & 0x00FF);
		return (short)result;
	}

	public int readInt() {
		int result = ((data[index++] << 24) & 0xFF000000) | ((data[index++] << 16) & 0x00FF0000) | ((data[index++] << 8) & 0x0000FF00) | (data[index++] & 0x000000FF);
		return result;
	}

	public String readString() {
		byte[] result = new byte[readInt()];
		readBytes(result);
		return new String(result);
	}

	public void reset() {
		index = 0;
	}

	public void skip(int size) {
		index += size;
	}

	public int getPosition() {
		return index;
	}

	public void setPosition(int i) {
		index = i;
	}

    /**
       Decode a new RescueObject from the buffer
       @param timestamp The current simulation timestamp
	   @param source The source of the new object
       @return The next RescueObject in the buffer. Returns null if the next object is TYPE_NULL.
       @see RescueConstants#TYPE_NULL
    */
    public RescueObject readObject(int timestamp, Object source) {
		int type = readInt();
		if (type==RescueConstants.TYPE_NULL) return null;
                int id = readInt();
		int size = readInt();
		RescueObject result = RescueObject.newObject(type);
		if (result==null) skip(size);
		else if (size>0) {
			result.read(this,timestamp,source);
		}
                result.setID(id);
		return result;
    }

    /**
       Decode a set of objects from the buffer.
       @param timestamp The current simulation timestamp
	   @param source The source of the new objects
       @return The next RescueObject array in the buffer
    */
    public RescueObject[] readObjects(int timestamp, Object source) {
		List<RescueObject> result = new ArrayList<RescueObject>();
                int count = readInt();
                for (int i = 0; i < count; ++i) {
                    RescueObject next = readObject(timestamp, source);
                    result.add(next);
		};
		return (RescueObject[])result.toArray(new RescueObject[0]);
    }
    
	public Command readCommand() {
		int type = readInt();
		if (type==RescueConstants.HEADER_NULL) return null;
		int size = readInt();
		return readCommand(type,size);
	}

	public Command readCommand(int type, int size) {
		Command c = null;
		switch (type) {
		case RescueConstants.AK_EXTINGUISH:
			c = new AKExtinguish(this);
			break;
		case RescueConstants.AK_MOVE:
			c = new AKMove(this);
			break;
		case RescueConstants.AK_CLEAR:
			c = new AKClear(this);
			break;
		case RescueConstants.AK_LOAD:
			c = new AKLoad(this);
			break;
		case RescueConstants.AK_UNLOAD:
			c = new AKUnload(this);
			break;
		case RescueConstants.AK_RESCUE:
			c = new AKRescue(this);
			break;
		case RescueConstants.AK_SAY:
			c = new AKSay(this);
			break;
		case RescueConstants.AK_TELL:
			c = new AKTell(this);
			break;
		case RescueConstants.AK_REST:
			c = new AKRest(this);
			break;
		case RescueConstants.AK_CONNECT:
			c = new AKConnect(this);
			break;
		case RescueConstants.AK_ACKNOWLEDGE:
			c = new AKAcknowledge(this);
			break;
		case RescueConstants.KA_CONNECT_OK:
			c = new KAConnectOK(this);
			break;
		case RescueConstants.KA_CONNECT_ERROR:
			c = new KAConnectError(this);
			break;
		case RescueConstants.KA_SENSE:
			c = new KASense(this);
			break;
		case RescueConstants.KA_HEAR:
		case RescueConstants.KA_HEAR_SAY:
		case RescueConstants.KA_HEAR_TELL:
			c = new KAHear(this);
			break;
			/*
		case RescueConstants.KA_HEAR_SAY:
			c = KAHear.KA_HEAR_SAY(this);
			break;
		case RescueConstants.KA_HEAR_TELL:
			c = KAHear.KA_HEAR_TELL(this);
			break;
			*/
		case RescueConstants.SK_CONNECT:
			c = new SKConnect(this);
			break;
		case RescueConstants.SK_ACKNOWLEDGE:
			c = new SKAcknowledge(this);
			break;
		case RescueConstants.SK_UPDATE:
			c = new SKUpdate(this);
			break;
		case RescueConstants.KS_CONNECT_OK:
			c = new KSConnectOK(this);
			break;
		case RescueConstants.KS_CONNECT_ERROR:
			c = new KSConnectError(this);
			break;
		case RescueConstants.COMMANDS:
			c = new Commands(this);
			break;
		case RescueConstants.UPDATE:
			c = new Update(this);
			break;
		case RescueConstants.VK_CONNECT:
			c = new VKConnect(this);
			break;
		case RescueConstants.VK_ACKNOWLEDGE:
			c = new VKAcknowledge(this);
			break;
		case RescueConstants.KV_CONNECT_OK:
			c = new KVConnectOK(this);
			break;
		case RescueConstants.KV_CONNECT_ERROR:
			c = new KVConnectError(this);
			break;
			//		case RescueConstants.KV_UPDATE:
			//			c = new KVUpdate(this);
			//			break;
		default:
			System.err.println("Don't know how to decode commands of type "+Handy.getCommandTypeName(type));
			skip(size);
			break;
		}
		return c;
	}

    /**
       Decode a set of commands from the buffer.
       @return The next Command array in the buffer
    */
    public Command[] readCommands() {
		List<Command> result = new ArrayList<Command>();
		Command next = null;
		do {
			next = readCommand();
			if (next!=null) {
				result.add(next);
			}
		} while (next!=null);
		return (Command[])result.toArray(new Command[0]);
    }
}
