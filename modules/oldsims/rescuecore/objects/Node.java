/*
 * Last change: $Date: 2004/05/04 03:09:38 $
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
   Encapsulation of a TYPE_NODE object
   @see RescueConstants#TYPE_NODE
 */

public class Node extends Vertex {
	private IntProperty signal;
	private ArrayProperty shortcut, pocket, timing;

    public Node() {
		signal = new IntProperty(RescueConstants.PROPERTY_SIGNAL);
		shortcut = new ArrayProperty(RescueConstants.PROPERTY_SHORTCUT_TO_TURN);
		pocket = new ArrayProperty(RescueConstants.PROPERTY_POCKET_TO_TURN_ACROSS);
		timing = new ArrayProperty(RescueConstants.PROPERTY_SIGNAL_TIMING);
    }

    public Node(int x, int y) {
		this(x,y,new int[0],false,new int[0], new int[0], new int[0]);
	}

    public Node(int x, int y, int[] edges, boolean signal, int[] shortcut, int[] pocket, int[] timing){
		super(x,y,edges);
		this.signal = new IntProperty(RescueConstants.PROPERTY_SIGNAL,signal);
		this.shortcut = new ArrayProperty(RescueConstants.PROPERTY_SHORTCUT_TO_TURN,shortcut);
		this.pocket = new ArrayProperty(RescueConstants.PROPERTY_POCKET_TO_TURN_ACROSS,pocket);
		this.timing = new ArrayProperty(RescueConstants.PROPERTY_SIGNAL_TIMING,timing);
    }

	public int getType() {
		return RescueConstants.TYPE_NODE;
	}

    public Property getProperty(int property) /*throws UnknownPropertyException*/ {
		switch (property) {
		case RescueConstants.PROPERTY_SIGNAL:
			return signal;
		case RescueConstants.PROPERTY_SHORTCUT_TO_TURN:
			return shortcut;
		case RescueConstants.PROPERTY_POCKET_TO_TURN_ACROSS:
			return pocket;
		case RescueConstants.PROPERTY_SIGNAL_TIMING:
			return timing;
		}
		return super.getProperty(property);
    }

	/*
    public boolean propertyExists(int property) {
		switch (property) {
		case RescueConstants.PROPERTY_SIGNAL:
		case RescueConstants.PROPERTY_SHORTCUT_TO_TURN:
		case RescueConstants.PROPERTY_POCKET_TO_TURN_ACROSS:
		case RescueConstants.PROPERTY_SIGNAL_TIMING:
			return true;
		}
		return super.propertyExists(property);
    }
	*/

    public boolean hasSignal() {
		return signal.getValue()!=0;
    }

	public boolean setSignal(boolean b, int timestamp, Object source) {
		if (signal.updateValue(b?1:0,timestamp,source)) {
			firePropertyChanged(RescueConstants.PROPERTY_SIGNAL,timestamp,source);
			return true;
		}
		return false;
	}

    public int[] getShortcutToTurn() {
		return shortcut.getValues();
    }

	public boolean setShortcutToTurn(int[] s, int timestamp, Object source) {
		if (shortcut.updateValues(s,timestamp,source)) {
			firePropertyChanged(RescueConstants.PROPERTY_SHORTCUT_TO_TURN,timestamp,source);
			return true;
		}
		return false;
	}

	public void clearShortcutToTurn(int timestamp, Object source) {
		shortcut.clear();
		firePropertyChanged(RescueConstants.PROPERTY_SHORTCUT_TO_TURN,timestamp,source);
	}

	public void appendShortcutToTurn(int next, int timestamp, Object source) {
		shortcut.append(next);
		firePropertyChanged(RescueConstants.PROPERTY_SHORTCUT_TO_TURN,timestamp,source);
	}

    public int[] getPocketToTurnAcross() {
		return pocket.getValues();
    }

	public boolean setPocketToTurnAcross(int[] p, int timestamp, Object source) {
		if (pocket.updateValues(p,timestamp,source)) {
			firePropertyChanged(RescueConstants.PROPERTY_POCKET_TO_TURN_ACROSS,timestamp,source);
			return true;
		}
		return false;
	}

	public void clearPocketToTurnAcross(int timestamp, Object source) {
		pocket.clear();
		firePropertyChanged(RescueConstants.PROPERTY_POCKET_TO_TURN_ACROSS,timestamp,source);
	}

	public void appendPocketToTurnAcross(int next, int timestamp, Object source) {
		pocket.append(next);
		firePropertyChanged(RescueConstants.PROPERTY_POCKET_TO_TURN_ACROSS,timestamp,source);
	}

    public int[] getSignalTiming() {
		return timing.getValues();
    }

	public boolean setSignalTiming(int[] t, int timestamp, Object source) {
		if (timing.updateValues(t,timestamp,source)) {
			firePropertyChanged(RescueConstants.PROPERTY_SIGNAL_TIMING,timestamp,source);
			return true;
		}
		return false;
	}

	public void clearSignalTiming(int timestamp, Object source) {
		timing.clear();
		firePropertyChanged(RescueConstants.PROPERTY_SIGNAL_TIMING,timestamp,source);
	}

	public void appendSignalTiming(int next, int timestamp, Object source) {
		timing.append(next);
		firePropertyChanged(RescueConstants.PROPERTY_SIGNAL_TIMING,timestamp,source);
	}
}
