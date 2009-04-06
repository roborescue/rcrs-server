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

/**
   This class encapsulates information about an array property
 */
public class ArrayProperty extends Property {
	private int[] values;
	private int numValues;

	public ArrayProperty(int type) {
		super(type);
		values = new int[10];
		numValues = 0;
	}

	public ArrayProperty(int type, int[] values) {
		super(type);
		this.values = new int[values.length];
		numValues = values.length;
		System.arraycopy(values,0,this.values,0,values.length);
		lastUpdate = 0;
	}

	/*
    public Property copy(){
		ArrayProperty result = ArrayProperty(type,values,null);
		result.numValues = numValues;
		return result;
	}
	*/

	public int[] getValues() {
		int[] result = new int[numValues];
		System.arraycopy(values,0,result,0,numValues);
		return result;
	}

	public String getStringValue() {
		StringBuffer result = new StringBuffer();
		result.append("[");
		for (int i=0;i<numValues;++i) {
			result.append(values[i]);
			if (i<numValues-1) result.append(",");
		}
		result.append("]");
		return result.toString();
	}

	/**
	   Set the values of this property. The timestamp will also be updated. Note that this method does not check that the update is newer than the current value - it is up to the application to test for this.
	   @param newValues The new values
	   @param timestamp The timestamp of this update
	   @param source The source of this update
	   @return true if and only if the values were actually changed, i.e the new values are different from the old ones
	   @see #isOlderThan(int)
	*/
	public boolean setValues(int[] newValues, int timestamp, Object source) {
		lastUpdate = timestamp;
		lastUpdateSource = source;
		if (!different(newValues)) return false;
		values = new int[newValues.length];
		System.arraycopy(newValues,0,values,0,values.length);
		numValues = newValues.length;
		return true;
	}

	/**
	   Update the values of this property. The timestamp will also be updated.
	   @param newValues The new values
	   @param timestamp The timestamp of this update
	   @param source The source of this update
	   @return true if and only if the values were actually changed, i.e the new values are different from the old ones and the new timestamp is greater than the old timestamp
	   @see #isOlderThan(int)
	*/
	public boolean updateValues(int[] newValues, int timestamp, Object source) {
		if (timestamp <= lastUpdate) return false;
		if (lastUpdate>=0 && !different(newValues)) return false;
		lastUpdate = timestamp;
		lastUpdateSource = source;
		values = new int[newValues.length];
		System.arraycopy(newValues,0,values,0,values.length);
		numValues = newValues.length;
		return true;
	}

	/**
	   Append a value to the list
	   @param value The new value
	*/
	public void append(int value) {
		if (numValues == values.length) {
			int[] newValues = new int[values.length+10];
			System.arraycopy(values,0,newValues,0,values.length);
			values = newValues;
		}
		values[numValues++] = value;
	}

	public void clear() {
		numValues = 0;
	}

	/**
	   Merge another property into this one
	   @param p The Property to merge
	   @return true if and only if the value of this property was actually changed
	*/
	public boolean merge(Property p) {
		if (p instanceof ArrayProperty) {
			return updateValues(((ArrayProperty)p).values,p.lastUpdate,p.lastUpdateSource);
		}
		return false;

		/*
		if (p.lastUpdate <= this.lastUpdate) {
			return false;
		}
		if (p instanceof ArrayProperty) {
			lastUpdate = p.lastUpdate;
			lastUpdateSource = p.lastUpdateSource;
			ArrayProperty a = (ArrayProperty)p;
			if (!different(a.values,a.numValues)) {
				return false;
			}
			values = new int[a.values.length];
			numValues = a.numValues;
			System.arraycopy(a.values,0,values,0,values.length);
			return true;
		}
		return false;
		*/
	}

	/**
	   Write this property to an OutputBuffer
	   @param out The OutputBuffer to write this proprty to
	*/
	public void write(OutputBuffer out) {
		out.writeInt(numValues);
		for (int i=0;i<numValues;++i) {
			out.writeInt(values[i]);
		}
	}

	/**
	   Decode property data from an InputBuffer
	   @param in An InputBuffer to read data from
	   @param timestamp The timestamp of this update
	   @param source The source of this update
	   @return true if and only if a change was made
	*/
	public boolean read(InputBuffer in, int timestamp, Object source) {
		//		System.out.println("Timestep "+timestamp+": updating "+this+" from buffer");
		int number = in.readInt();
		int[] newValues = new int[number];
		for (int i=0;i<number;++i) newValues[i] = in.readInt();
		return updateValues(newValues,timestamp,source);
		/*
		if (lastUpdate < timestamp) {
			changed = different(newValues);
			if (changed) {
				values = newValues;
				numValues = number;
				lastUpdate = timestamp;
				lastUpdateSource = source;
			}
		}
		return changed;
		*/
	}

	private boolean different(int[] newValues) {
		return different(newValues,newValues.length);
	}

	private boolean different(int[] newValues, int count) {
		if (count != numValues) return true;
		for (int i=0;i<numValues && i<count;++i) {
			if (newValues[i]!=values[i]) return true;
		}
		return false;
	}
}
