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

import java.util.*;
import java.io.*;

/**
   This class encapsulates information about an individual property within a RescueObject
 */
public class IntProperty extends Property {
	private int value;

	public IntProperty(int type) {
		super(type);
	}

	public IntProperty(int type, boolean value) {
		this(type,value?1:0);
	}

	public IntProperty(int type, int value) {
		super(type);
		this.value = value;
		lastUpdate = 0;
	}

	/*
    public Property copy() {
		return new IntProperty(type,value);
	}
	*/

	/**
	   Get the value of this property.
	   @return The integer value of this property
	*/
	public int getValue() {
		return value;
	}

	/**
	   Get the value of this property as a string
	   @return A nice string representation of the value of this property
	*/
	public String getStringValue() {
		StringBuffer result = new StringBuffer();
		result.append(value);
		return result.toString();
	}

	/**
	   Set the value of this property. The timestamp will also be updated. Note that this method does not check that the update is newer than the current value - it is up to the application to test for this.
	   @param newValue The new value
	   @param timestamp The timestamp of this update
	   @param source The source of this update
	   @return true if and only if the value was actually changed, i.e the new value is different from the old value
	   @see #isOlderThan(int)
	*/
	public boolean setValue(int newValue, int timestamp, Object source) {
		lastUpdate = timestamp;
		lastUpdateSource = source;
		if (value==newValue) return false;
		value = newValue;
		return true;
	}

	/**
	   Update the value of this property. The timestamp will also be updated.
	   @param newValue The new value
	   @param timestamp The timestamp of this update
	   @param source The source of this update
	   @return true if and only if the value was actually changed, i.e the new value is different from the old value, and the new timestamp is greater than the old timestamp
	   @see #isOlderThan(int)
	*/
	public boolean updateValue(int newValue, int timestamp, Object source) {
		if (timestamp <= lastUpdate) return false;
		if (lastUpdate>=0 && newValue==value) return false;
		lastUpdate = timestamp;
		lastUpdateSource = source;
		value = newValue;
		return true;
	}

	/**
	   Merge this Property with a different one
	   @param p The Property to merge from
	   @return true if and only if the value of this property was actually changed
	*/
	public boolean merge(Property p) {
		if (p instanceof IntProperty) {
			return updateValue(((IntProperty)p).value,p.lastUpdate,p.lastUpdateSource);
		}
		return false;


		/*
		if (p.lastUpdate <= this.lastUpdate) return false;
		if (p instanceof IntProperty) {
			IntProperty i = (IntProperty)p;
			lastUpdate = i.lastUpdate;
			lastUpdateSource = i.lastUpdateSource;
			if (value==i.value) return false;
			value = i.value;
			return true;
		}
		return false;
		*/
	}

	/**
	   Write this property to an OutputBuffer
	   @param out The OutputBuffer to write this property to
	*/
	public void write(OutputBuffer out) {
		out.writeInt(value);
	}

	/**
	   Decode property data from an InputBuffer.
	   @param in An InputBuffer to read data from
	   @param timestamp The timestamp of this update
	   @param source The source of this update
	   @return true if and only if a change was made
	*/
	public boolean read(InputBuffer in, int timestamp, Object source) {
		//		System.out.println("Timestep "+timestamp+": updating "+this+" from buffer");
		int newValue = in.readInt();
		return updateValue(newValue,timestamp,source);
		/*
		if (lastUpdate < timestamp) {
			changed = (newValue != value);
			if (changed) {
				value = newValue;
				lastUpdate = timestamp;
				lastUpdateSource = source;
			}
		}
		return changed;
		*/
	}
}
