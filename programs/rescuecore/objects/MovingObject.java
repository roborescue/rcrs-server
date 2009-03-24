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

public abstract class MovingObject extends RealObject {
	protected IntProperty position, positionExtra, direction;
	protected ArrayProperty positionHistory;

    protected MovingObject() {
		position = new IntProperty(RescueConstants.PROPERTY_POSITION);
		positionExtra = new IntProperty(RescueConstants.PROPERTY_POSITION_EXTRA);
		direction = new IntProperty(RescueConstants.PROPERTY_DIRECTION);
		positionHistory = new ArrayProperty(RescueConstants.PROPERTY_POSITION_HISTORY);
    }

    protected MovingObject(int pos, int extra, int dir, int[] history) {
		position = new IntProperty(RescueConstants.PROPERTY_POSITION,pos);
		positionExtra = new IntProperty(RescueConstants.PROPERTY_POSITION_EXTRA,extra);
		direction = new IntProperty(RescueConstants.PROPERTY_DIRECTION,dir);
		positionHistory = new ArrayProperty(RescueConstants.PROPERTY_POSITION_HISTORY,history);
    }

	/*
    public boolean propertyExists(int property) {
		switch (property) {
		case RescueConstants.PROPERTY_POSITION:
		case RescueConstants.PROPERTY_POSITION_EXTRA:
		case RescueConstants.PROPERTY_DIRECTION:
		case RescueConstants.PROPERTY_POSITION_HISTORY:
			return true;
		}
		return super.propertyExists(property);
	}
	*/

	public Property getProperty(int property) /*throws UnknownPropertyException*/ {
		switch (property) {
		case RescueConstants.PROPERTY_POSITION:
			return position;
		case RescueConstants.PROPERTY_POSITION_EXTRA:
			return positionExtra;
		case RescueConstants.PROPERTY_DIRECTION:
			return direction;
		case RescueConstants.PROPERTY_POSITION_HISTORY:
			return positionHistory;
		}
		return super.getProperty(property);
    }

	public int getPosition() {
		return position.getValue();
	}

	public boolean setPosition(int p, int timestamp, Object source) {
		if (position.updateValue(p,timestamp,source)) {
			firePropertyChanged(RescueConstants.PROPERTY_POSITION,timestamp,source);
			return true;
		}
		return false;
	}

	public int getPositionExtra() {
		return positionExtra.getValue();
	}

	public boolean setPositionExtra(int e, int timestamp, Object source) {
		if (positionExtra.updateValue(e,timestamp,source)) {
			firePropertyChanged(RescueConstants.PROPERTY_POSITION_EXTRA,timestamp,source);
			return true;
		}
		return false;
	}

	public int getDirection() {
		return direction.getValue();
	}

	public boolean setDirection(int d, int timestamp, Object source) {
		if (direction.updateValue(d,timestamp,source)) {
			firePropertyChanged(RescueConstants.PROPERTY_DIRECTION,timestamp,source);
			return true;
		}
		return false;
	}

	public int[] getPositionHistory() {
		return positionHistory.getValues();
	}

	public boolean setPositionHistory(int[] h, int timestamp, Object source) {
		if (positionHistory.updateValues(h,timestamp,source)) {
			firePropertyChanged(RescueConstants.PROPERTY_POSITION_HISTORY,timestamp,source);
			return true;
		}
		return false;
	}

	public void clearPositionHistory(int timestamp, Object source) {
		positionHistory.clear();
		firePropertyChanged(RescueConstants.PROPERTY_POSITION_HISTORY,timestamp,source);
	}

	public void appendPositionHistory(int next, int timestamp, Object source) {
		positionHistory.append(next);
		firePropertyChanged(RescueConstants.PROPERTY_POSITION_HISTORY,timestamp,source);
	}
}
