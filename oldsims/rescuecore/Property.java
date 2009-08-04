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
   This class encapsulates information about an individual property within a RescueObject
 */
public abstract class Property implements java.io.Serializable {
	private int type;
	protected int lastUpdate;
	protected Object lastUpdateSource;

	protected Property(int type) {
		this.type = type;
		lastUpdate = RescueConstants.VALUE_UNKNOWN;
		lastUpdateSource = null;
	}

	/**
	   Get the type of this property
	   @return The type of this property
	*/
	public int getType() {
		return type;
	}

	public abstract boolean read(InputBuffer in, int timestamp, Object source);
	public abstract void write(OutputBuffer out);

	/**
	   Get the last time this property was updated
	   @return The last time this property was updated
	*/
	public int getLastUpdate() {
		return lastUpdate;
	}

	/**
	   Get the source of the last update
	   @return The source of the last update
	*/
	public Object getLastUpdateSource() {
		return lastUpdateSource;
	}

	/**
	   Find out whether this property was last updated before a certain time
	   @param time The time to check against
	   @return true if and only if this property was last updated before the given time
	*/
	public boolean isOlderThan(int time) {
		return lastUpdate < time;
	}

	/**
	   Find out whether the value of this property is known or not
	   @return true if and only if the value of this property is known
	*/
	public boolean isValueKnown() {
		return lastUpdate >= 0;
	}

	/**
	   Find out whether the value of this property is assumed or not
	   @return true if and only if the value of this property is assumed
	*/
	public boolean isValueAssumed() {
		return lastUpdate == RescueConstants.VALUE_ASSUMED;
	}

	public String toString() {
		return Handy.getPropertyName(type)+": "+getStringValue()+" (last update: "+lastUpdate+")";
	}

	/**
	   Get the value of this property as a string
	   @return A nice string representation of the value of this property
	*/
	public abstract String getStringValue();

	/**
	   Update this Property from a different one
	   @param p The Property to update from
	   @return true if and only if the value of this property was actually changed
	*/
	public abstract boolean merge(Property p);
}
