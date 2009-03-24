/*
 * Last change: $Date: 2004/05/04 03:09:38 $
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
   This class encapsulates a long UDP message, as defined by the robocup rescue manual version 0.4
 */
public class LongUDPMessage {
    public final static int CHUNK_SIZE = 1024;
    private byte[] data;

    /**
       Construct a new LongUDPMessage containing the given data
       @param data The body of the message
	*/
    public LongUDPMessage(byte[] data) {
		this.data = data;
    }

    /**
       Break up this message into fragments ready for sending
       @param id The id number assigned to each fragment
       @return An array of LongUDPFragments that can be sent to the kernal
	*/
    public LongUDPFragment[] fragment(short id) {
		short count = 0;
		int done = 0;
		short fragments = (short)(data.length/CHUNK_SIZE);
		if (data.length%CHUNK_SIZE!=0) ++fragments;
		LongUDPFragment[] result = new LongUDPFragment[fragments];
		while (done < data.length) {
			byte[] someData = new byte[Math.min(CHUNK_SIZE,data.length-done)];
			System.arraycopy(data,done,someData,0,someData.length);
			result[count] = new LongUDPFragment(id,count,fragments,someData);
			++count;
			done += someData.length;
		}
		return result;
    }

    /**
       Get the body of this message
       @return The message body
	*/
    public byte[] getData() {
		return data;
    }

    /**
       Defragment a set of LongUDPFragments into one LongUDPMessage
       @param fragments The fragmented message to reassemble
       @return A new LongUDPMessage that has been assembled from the given fragments. If there is a problem, then null is returned.
	*/
    public static LongUDPMessage defragment(LongUDPFragment[] fragments) {
		//	System.out.println("Defragmenting message");
		java.util.Arrays.sort(fragments);
		// Check that all the fragments are correct
		if (!checkFragments(fragments)) return null;
		int totalSize = 0;
		for (int i=0;i<fragments.length;++i) {
			totalSize += fragments[i].getData().length;
		}
		//	System.out.println("Total size: "+totalSize);
		byte[] data = new byte[totalSize];
		int offset = 0;
		for (int i=0;i<fragments.length;++i) {
			byte[] next = fragments[i].getData();
			//	    System.out.println("Fragment "+(i+1)+" gets appended at position "+offset);
			System.arraycopy(next,0,data,offset,next.length);
			offset += next.length;
		}
		//	Handy.printBytes("Fragment 1",fragments[0].getData());
		//	Handy.printBytes("Fragment 2",fragments[1].getData());
		//	Handy.printBytes("Message received",data);
		return new LongUDPMessage(data);
    }

    private static boolean checkFragments(LongUDPFragment[] fragments) {
		//	System.out.println("Checking fragments");
		int id = fragments[0].getID();
		int total = fragments[0].getTotal();
		for (int i=0;i<fragments.length;++i) {
			if (fragments[i].getID()!=id) {
				System.out.println("Fragment "+(i+1)+" has id "+fragments[i].getID()+" - it should be "+id);
				return false;
			}
			if (fragments[i].getTotal()!=total) {
				System.out.println("Fragment "+(i+1)+" thinks there should be "+fragments[i].getTotal()+" fragments in total - it should be "+total);
				return false;
			}
		}
		// Make sure they're in order
		for (int i=0;i<fragments.length;++i) {
			if (fragments[i].getNumber()!=i) {
				System.out.println("Fragment "+(i+1)+" thinks it should be number "+(fragments[i].getNumber()+1));
				return false;
			}
		}
		return true;
    }
}
