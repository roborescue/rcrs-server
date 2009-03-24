/*
 * Last change: $Date: 2004/05/04 03:09:37 $
 * $Revision: 1.6 $
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

import java.util.*;

/**
   This is an implementation of Memory that stores the data in an array
 */
public class ArrayMemory extends Memory {
	private final static int DEFAULT_MEMORY_SIZE = 3000;
	private final static int DEFAULT_RESIZE_FACTOR = 1000;

	protected RescueObject[] data;
	private int dataSize;
	private int resizeFactor;

	/**
	   Construct a new empty memory
	*/
	public ArrayMemory() {
		this(DEFAULT_MEMORY_SIZE,DEFAULT_RESIZE_FACTOR);
	}

	/**
	   Construct a new empty memory
	   @param size The initial size of the array
	   @param factor The amount to increase the array by if we run out of room
	*/
	public ArrayMemory(int size, int factor) {
		data = new RescueObject[size];
		resizeFactor = factor;
		dataSize = 0;
	}

	public RescueObject lookup(int id) {
		return id>=data.length?null:data[id];
	}

    public Collection<RescueObject> getAllObjects() {
		Collection<RescueObject> result = new HashSet<RescueObject>(dataSize);
		for (int i=0;i<data.length;++i) {
			if (data[i]!=null) result.add(data[i]);
		}
		return result;
	}

    public void getObjectsOfType(Collection<RescueObject> result, int... types) {
		for (int i=0;i<data.length;++i) {
			RescueObject next = data[i];
			int type = next.getType();
			for (int nextType : types) {
				if (type==nextType) {
					result.add(next);
					break;
				}
			}
		}
	}

	/*
	  public RescueObject[] getObjectsOfInternalType(int type) {
	  List result = new ArrayList();
	  for (int i=0;i<data.length;++i) {
	  RescueObject next = data[i];
	  if (next!=null && (next.getInternalType() & type)!=0) result.add(next);
	  }
	  return (RescueObject[])result.toArray(new RescueObject[0]);
	  }
	*/

	public void add(RescueObject o, int timestamp, Object source) {
		int id = o.getID();
		if (id >= data.length) resizeMemory(id+1);
		if (data[id]==null) ++dataSize;
		data[id] = o;
		super.add(o,timestamp,source);
	}

	public void remove(RescueObject o) {
		int id = o.getID();
		if (id >= data.length) resizeMemory(id+1);
		data[id] = null;
		super.remove(o);
	}

	private void resizeMemory(int sizeNeeded) {
		RescueObject[] newData = new RescueObject[Math.max(sizeNeeded,data.length+resizeFactor)];
		System.arraycopy(data,0,newData,0,data.length);
		data = newData;
	}

    /**
     * A deep clone of this memory.
     * Any listeners on the original memory will not be registered on the new memory.
     **/
    public Memory copy() {
		ArrayMemory m = new ArrayMemory(data.length,resizeFactor);
		for(int i = 0; i< data.length; i++)
			if(data[i] != null)
				m.add(data[i].copy(),0,RescueConstants.SOURCE_UNKNOWN);
		return m;
	}
}
