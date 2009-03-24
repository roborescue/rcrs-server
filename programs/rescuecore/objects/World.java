/*
 * Last change: $Date: 2004/05/20 23:42:00 $
 * $Revision: 1.7 $
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

package rescuecore.objects;

import rescuecore.*;

/**
   Encapsulation of a TYPE_WORLD object
   @see RescueConstants#TYPE_WORLD
 */

public class World extends VirtualObject {
	private IntProperty startTime, longitude, latitude, windForce, windDirection;

    public World() {
		startTime = new IntProperty(RescueConstants.PROPERTY_START_TIME);
		longitude = new IntProperty(RescueConstants.PROPERTY_LONGITUDE);
		latitude = new IntProperty(RescueConstants.PROPERTY_LATITUDE);
		windForce = new IntProperty(RescueConstants.PROPERTY_WIND_FORCE);
		windDirection = new IntProperty(RescueConstants.PROPERTY_WIND_DIRECTION);
    }

	public World(int start, int lon, int lat, int force, int direction) {
		startTime = new IntProperty(RescueConstants.PROPERTY_START_TIME,start);
		longitude = new IntProperty(RescueConstants.PROPERTY_LONGITUDE,lon);
		latitude = new IntProperty(RescueConstants.PROPERTY_LATITUDE,lat);
		windForce = new IntProperty(RescueConstants.PROPERTY_WIND_FORCE,force);
		windDirection = new IntProperty(RescueConstants.PROPERTY_WIND_DIRECTION,direction);
	}

	public int getType() {
		return RescueConstants.TYPE_WORLD;
	}

	/*
    public boolean propertyExists(int property) {
		switch (property) {
		case RescueConstants.PROPERTY_START_TIME:
		case RescueConstants.PROPERTY_LONGITUDE:
		case RescueConstants.PROPERTY_LATITUDE:
		case RescueConstants.PROPERTY_WIND_FORCE:
		case RescueConstants.PROPERTY_WIND_DIRECTION:
			return true;
		}
		return super.propertyExists(property);
    }
	*/

	public Property getProperty(int property) /*throws UnknownPropertyException*/ {
		switch (property) {
		case RescueConstants.PROPERTY_START_TIME:
			return startTime;
		case RescueConstants.PROPERTY_LONGITUDE:
			return longitude;
		case RescueConstants.PROPERTY_LATITUDE:
			return latitude;
		case RescueConstants.PROPERTY_WIND_FORCE:
			return windForce;
		case RescueConstants.PROPERTY_WIND_DIRECTION:
			return windDirection;
		}
		return super.getProperty(property);
	}

    public int getStartTime() {
		return startTime.getValue();
    }

	public boolean setStartTime(int s, int timestamp, Object source) {
		if (startTime.updateValue(s,timestamp,source)) {
			firePropertyChanged(RescueConstants.PROPERTY_START_TIME,timestamp,source);
			return true;
		}
		return false;
	}

    public int getLongitude() {
		return longitude.getValue();
    }

	public boolean setLongitude(int l, int timestamp, Object source) {
		if (longitude.updateValue(l,timestamp,source)) {
			firePropertyChanged(RescueConstants.PROPERTY_LONGITUDE,timestamp,source);
			return true;
		}
		return false;
	}

    public int getLatitude() {
		return latitude.getValue();
    }

	public boolean setLatitude(int l, int timestamp, Object source) {
		if (latitude.updateValue(l,timestamp,source)) {
			firePropertyChanged(RescueConstants.PROPERTY_LATITUDE,timestamp,source);
			return true;
		}
		return false;
	}

    public int getWindForce() {
		return windForce.getValue();
    }

	public boolean setWindForce(int w, int timestamp, Object source) {
		if (windForce.updateValue(w,timestamp,source)) {
			firePropertyChanged(RescueConstants.PROPERTY_WIND_FORCE,timestamp,source);
			return true;
		}
		return false;
	}

    public int getWindDirection() {
		return windDirection.getValue();
	}

	public boolean setWindDirection(int w, int timestamp, Object source) {
		if (windDirection.updateValue(w,timestamp,source)) {
			firePropertyChanged(RescueConstants.PROPERTY_WIND_DIRECTION,timestamp,source);
			return true;
		}
		return false;
	}
}
