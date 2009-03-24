/*
 * Last change: $Date: 2005/02/18 03:34:33 $
 * $Revision: 1.3 $
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

/**
   This is a piece of a LongUDPMessage
 */
public class LongUDPFragment implements Comparable {
    private final static int MAGIC = 0x0008;
    private int id;
    private int number;
    private int total;
    private byte[] data;

    /**
       Generate a new LongUDPFragment ready for sending
       @param id The id of the LongUDPMessage we are sending
       @param number The number of this fragment
       @param total The total number of fragments we are sending
       @param data The body of this fragment
	*/
    public LongUDPFragment(int id, int number, int total, byte[] data) {
		this.id = id;
		this.number = number;
		this.total = total;
		this.data = new byte[data.length];
		System.arraycopy(data,0,this.data,0,data.length);
    }

    /**
       Generate a new LongUDPFragment from some data sent by the kernal
       @param input The raw data from the kernal. It should consist of four 16-bit numbers (magic number, id, fragment number, total number of fragments) followed by some data.
	*/
    public LongUDPFragment(byte[] input) {
		int magic = input[0]<<8 | input[1];
		if (magic!=MAGIC) System.err.println("Oh oh - we got a LongUDPFragment with a bad magic number ("+magic+" instead of "+MAGIC+")");
		id = input[2]<<8 | input[3];
		number = input[4]<<8 | input[5];
		total = input[6]<<8 | input[7];
		data = new byte[input.length-8];
		System.arraycopy(input,8,data,0,data.length);
		//	System.out.println((number+1)+" of "+total);
    }

    public String toString() {
		return "LongUDPFragment: "+id+" ("+(number+1)+" of "+total+")";
    }

    public int compareTo(Object o) {
		LongUDPFragment l = (LongUDPFragment)o;
		return this.number-l.number;
    }

    /**
       Turn this fragment into a byte array suitable for transmission to the kernal
       @return A byte array containing the raw data of this fragment
	*/
    public byte[] toByteArray() {
		byte[] result = new byte[data.length+8];
		write(result,0,MAGIC);
		write(result,2,id);
		write(result,4,number);
		write(result,6,total);
		System.arraycopy(data,0,result,8,data.length);
		return result;
    }

    /**
       Get the body of this fragment
       @return A byte array containing the body of this fragment
	*/
    public byte[] getData() {
		return data;
    }

    /**
       Get the message id of this fragment
       @return This fragment's message id
	*/
    public int getID() {
		return id;
    }

    /**
       Get the sequence number of this fragment
       @return This fragment's sequence number
	*/
    public int getNumber() {
		return number;
    }

    /**
       Get the total number of fragments in this fragment's message
       @return The total number of fragments in this fragment's message
	*/
    public int getTotal() {
		return total;
    }

    private void write(byte[] buffer, int off, int value) {
		buffer[off] = (byte)((value>>8)&0xFF);
		buffer[off+1] = (byte)(value&0xFF);
    }
}
