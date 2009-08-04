/*
 * Last change: $Date: 2004/05/04 03:40:13 $
 * $Revision: 1.8 $
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
   Encapsulation of a TYPE_BUILDING object
   @see RescueConstants#TYPE_BUILDING
 */
public class Building extends MotionlessObject {
	private IntProperty x,y,floors, attributes, ignition, fieryness, brokenness, code, ground, total, temperature, importance;
	private ArrayProperty entrances,apexes;

    public Building() {
		x = new IntProperty(RescueConstants.PROPERTY_X);
		y = new IntProperty(RescueConstants.PROPERTY_Y);
		floors = new IntProperty(RescueConstants.PROPERTY_FLOORS);
		attributes = new IntProperty(RescueConstants.PROPERTY_BUILDING_ATTRIBUTES);
		ignition = new IntProperty(RescueConstants.PROPERTY_IGNITION);
		fieryness = new IntProperty(RescueConstants.PROPERTY_FIERYNESS);
		brokenness = new IntProperty(RescueConstants.PROPERTY_BROKENNESS);
		entrances = new ArrayProperty(RescueConstants.PROPERTY_ENTRANCES);
		//		shape = new IntProperty(RescueConstants.PROPERTY_BUILDING_SHAPE_ID);
		code = new IntProperty(RescueConstants.PROPERTY_BUILDING_CODE);
		ground = new IntProperty(RescueConstants.PROPERTY_BUILDING_AREA_GROUND);
		total = new IntProperty(RescueConstants.PROPERTY_BUILDING_AREA_TOTAL);
		apexes = new ArrayProperty(RescueConstants.PROPERTY_BUILDING_APEXES);
		temperature = new IntProperty(RescueConstants.PROPERTY_BUILDING_TEMPERATURE);
		importance = new IntProperty(RescueConstants.PROPERTY_BUILDING_IMPORTANCE);
    }

	public Building(Building other) {
		this(other.getX(),other.getY(),other.getFloors(),other.getBuildingAttributes(),other.isIgnited(),other.getFieryness(),other.getBrokenness(),other.getEntrances(),other.getBuildingCode(),other.getGroundArea(),other.getTotalArea(),other.getApexes(),other.getTemperature(),other.getImportance());
	}

	public Building(int x, int y, int floors, int attributes, boolean ignition, int fieryness, int brokenness, int[] entrances, int code, int ground, int total, int[] apexes, int temperature,int importance) {
		this.x = new IntProperty(RescueConstants.PROPERTY_X,x);
		this.y = new IntProperty(RescueConstants.PROPERTY_Y,y);
		this.floors = new IntProperty(RescueConstants.PROPERTY_FLOORS,floors);
		this.attributes = new IntProperty(RescueConstants.PROPERTY_BUILDING_ATTRIBUTES,attributes);
		this.ignition = new IntProperty(RescueConstants.PROPERTY_IGNITION,ignition);
		this.fieryness = new IntProperty(RescueConstants.PROPERTY_FIERYNESS,fieryness);
		this.brokenness = new IntProperty(RescueConstants.PROPERTY_BROKENNESS,brokenness);
		this.entrances = new ArrayProperty(RescueConstants.PROPERTY_ENTRANCES,entrances);
		//		this.shape = new IntProperty(RescueConstants.PROPERTY_BUILDING_SHAPE_ID,shape);
		this.code = new IntProperty(RescueConstants.PROPERTY_BUILDING_CODE,code);
		this.ground = new IntProperty(RescueConstants.PROPERTY_BUILDING_AREA_GROUND,ground);
		this.total = new IntProperty(RescueConstants.PROPERTY_BUILDING_AREA_TOTAL,total);
		this.apexes = new ArrayProperty(RescueConstants.PROPERTY_BUILDING_APEXES,apexes);
		this.temperature = new IntProperty(RescueConstants.PROPERTY_BUILDING_TEMPERATURE,temperature);
		this.importance = new IntProperty(RescueConstants.PROPERTY_BUILDING_IMPORTANCE,importance);
	}

	public int getType() {
		return RescueConstants.TYPE_BUILDING;
	}

	/*
    public boolean propertyExists(int property) {
		switch (property) {
		case RescueConstants.PROPERTY_X:
		case RescueConstants.PROPERTY_Y:
		case RescueConstants.PROPERTY_FLOORS:
		case RescueConstants.PROPERTY_BUILDING_ATTRIBUTES:
		case RescueConstants.PROPERTY_IGNITION:
		case RescueConstants.PROPERTY_FIERYNESS:
		case RescueConstants.PROPERTY_BROKENNESS:
		case RescueConstants.PROPERTY_ENTRANCES:
			//		case RescueConstants.PROPERTY_BUILDING_SHAPE_ID:
		case RescueConstants.PROPERTY_BUILDING_CODE:
		case RescueConstants.PROPERTY_BUILDING_AREA_GROUND:
		case RescueConstants.PROPERTY_BUILDING_AREA_TOTAL:
		case RescueConstants.PROPERTY_BUILDING_APEXES:
		case RescueConstants.PROPERTY_BUILDING_IMPORTANCE:
			return true;
		}
		return super.propertyExists(property);
    }
	*/

    public Property getProperty(int property) /*throws UnknownPropertyException*/ {
		switch (property) {
		case RescueConstants.PROPERTY_X:
			return x;
		case RescueConstants.PROPERTY_Y:
			return y;
		case RescueConstants.PROPERTY_FLOORS:
			return floors;
		case RescueConstants.PROPERTY_BUILDING_ATTRIBUTES:
			return attributes;
		case RescueConstants.PROPERTY_IGNITION:
			return ignition;
		case RescueConstants.PROPERTY_FIERYNESS:
			return fieryness;
		case RescueConstants.PROPERTY_BROKENNESS:
			return brokenness;
		case RescueConstants.PROPERTY_ENTRANCES:
			return entrances;
			//		case RescueConstants.PROPERTY_BUILDING_SHAPE_ID:
			//			return shape;
		case RescueConstants.PROPERTY_BUILDING_CODE:
			return code;
		case RescueConstants.PROPERTY_BUILDING_AREA_GROUND:
			return ground;
		case RescueConstants.PROPERTY_BUILDING_AREA_TOTAL:
			return total;
		case RescueConstants.PROPERTY_BUILDING_APEXES:
			return apexes;
		case RescueConstants.PROPERTY_BUILDING_TEMPERATURE:
			return temperature;
		case RescueConstants.PROPERTY_BUILDING_IMPORTANCE:
			return importance;
		}
		return super.getProperty(property);
    }


    public int getX() {
		return x.getValue();
    }

	public boolean setX(int newX, int timestamp, Object source) {
		if (x.updateValue(newX,timestamp,source)) {
			firePropertyChanged(RescueConstants.PROPERTY_X,timestamp,source);
			return true;
		}
		return false;
	}

    public int getY() {
		return y.getValue();
    }

	public boolean setY(int newY, int timestamp, Object source) {
		if (y.updateValue(newY,timestamp,source)) {
			firePropertyChanged(RescueConstants.PROPERTY_Y,timestamp,source);
			return true;
		}
		return false;
	}

    public int getFloors() {
		return floors.getValue();
    }

	public boolean setFloors(int f, int timestamp, Object source) {
		if (floors.updateValue(f,timestamp,source)) {
			firePropertyChanged(RescueConstants.PROPERTY_FLOORS,timestamp,source);
			return true;
		}
		return false;
	}

    public int getBuildingAttributes() {
		return attributes.getValue();
    }

	public boolean setBuildingAttributes(int b, int timestamp, Object source) {
		if (attributes.updateValue(b,timestamp,source)) {
			firePropertyChanged(RescueConstants.PROPERTY_BUILDING_ATTRIBUTES,timestamp,source);
			return true;
		}
		return false;
	}

    public boolean isIgnited() {
		return ignition.getValue() != 0;
    }

	public boolean setIgnition(boolean b, int timestamp, Object source) {
		if (ignition.updateValue(b?1:0,timestamp,source)) {
			firePropertyChanged(RescueConstants.PROPERTY_IGNITION,timestamp,source);
			return true;
		}
		return false;
	}

    public int getFieryness() {
		return fieryness.getValue();
    }

    public boolean setFieryness(int f, int timestamp, Object source) {
		if (fieryness.updateValue(f,timestamp,source)) {
			firePropertyChanged(RescueConstants.PROPERTY_FIERYNESS,timestamp,source);
			return true;
		}
		return false;
    }

    public int getBrokenness() {
		return brokenness.getValue();
    }

    public boolean setBrokenness(int b, int timestamp, Object source) {
		if (brokenness.updateValue(b,timestamp,source)) {
			firePropertyChanged(RescueConstants.PROPERTY_BROKENNESS,timestamp,source);
			return true;
		}
		return false;
    }

    public int[] getEntrances() {
		return entrances.getValues();
    }

	public boolean setEntrances(int[] e, int timestamp, Object source) {
		if (entrances.updateValues(e,timestamp,source)) {
			firePropertyChanged(RescueConstants.PROPERTY_ENTRANCES,timestamp,source);
			return true;
		}
		return false;
	}

	public void clearEntrances(int timestamp, Object source) {
		entrances.clear();
		firePropertyChanged(RescueConstants.PROPERTY_ENTRANCES,timestamp,source);
	}

	public void appendEntrances(int next, int timestamp, Object source) {
		entrances.append(next);
		firePropertyChanged(RescueConstants.PROPERTY_ENTRANCES,timestamp,source);
	}

	/*
    public int getBuildingShapeID() {
		return shape.getValue();
    }

	public boolean setBuildingShapeID(int s, int timestamp, Object source) {
		if (shape.updateValue(s,timestamp,source)) {
			firePropertyChanged(RescueConstants.PROPERTY_BUILDING_SHAPE_ID,timestamp,source);
			return true;
		}
		return false;
	}
	*/

    public int getBuildingCode() {
		return code.getValue();
    }

	public boolean setBuildingCode(int c, int timestamp, Object source) {
		if (code.updateValue(c,timestamp,source)) {
			firePropertyChanged(RescueConstants.PROPERTY_BUILDING_CODE,timestamp,source);
			return true;
		}
		return false;
	}

    public int getGroundArea() {
		return ground.getValue();
    }

	public boolean setGroundArea(int a, int timestamp, Object source) {
		if (ground.updateValue(a,timestamp,source)) {
			firePropertyChanged(RescueConstants.PROPERTY_BUILDING_AREA_GROUND,timestamp,source);
			return true;
		}
		return false;
	}

    public int getTotalArea() {
		return total.getValue();
    }

	public boolean setTotalArea(int a, int timestamp, Object source) {
		if (total.updateValue(a,timestamp,source)) {
			firePropertyChanged(RescueConstants.PROPERTY_BUILDING_AREA_TOTAL,timestamp,source);
			return true;
		}
		return false;
	}

    public int[] getApexes() {
		return apexes.getValues();
    }

	public boolean setApexes(int[] a, int timestamp, Object source) {
		if (apexes.updateValues(a,timestamp,source)) {
			firePropertyChanged(RescueConstants.PROPERTY_BUILDING_APEXES,timestamp,source);
			return true;
		}
		return false;
	}

	public void clearApexes(int timestamp, Object source) {
		apexes.clear();
		firePropertyChanged(RescueConstants.PROPERTY_BUILDING_APEXES,timestamp,source);
	}

	public void appendApex(int next, int timestamp, Object source) {
		apexes.append(next);
		firePropertyChanged(RescueConstants.PROPERTY_BUILDING_APEXES,timestamp,source);
	}

	public int getTemperature() {
		return temperature.getValue();
	}

	public boolean setTemperature(int value, int timestamp, Object source) {
		if (temperature.updateValue(value,timestamp,source)) {
			firePropertyChanged(RescueConstants.PROPERTY_BUILDING_TEMPERATURE,timestamp,source);
			return true;
		}
		return false;
	}

    public int getImportance() {
		return importance.getValue();
    }

    public boolean setImportance(int i, int timestamp, Object source) {
		if (importance.updateValue(i,timestamp,source)) {
			firePropertyChanged(RescueConstants.PROPERTY_BUILDING_IMPORTANCE,timestamp,source);
			return true;
		}
		return false;
    }

    public boolean isOnFire() {
		return getFieryness() > 0 && getFieryness() < 4;
    }

    public boolean isExtinguished() {
		return getFieryness() > 3 && getFieryness() < 7;
    }

    public boolean isBurntOut() {
		return getFieryness() == 7;
    }

    public boolean isUnburnt() {
		return getFieryness() == 0;
    }
}
