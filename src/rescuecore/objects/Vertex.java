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

public abstract class Vertex extends MotionlessObject {
	protected IntProperty x,y;
	protected ArrayProperty edges;

    protected Vertex() {
		x = new IntProperty(RescueConstants.PROPERTY_X);
		y = new IntProperty(RescueConstants.PROPERTY_Y);
		edges = new ArrayProperty(RescueConstants.PROPERTY_EDGES);
    }

	protected Vertex(int x, int y, int[] edges) {
		this.x = new IntProperty(RescueConstants.PROPERTY_X,x);
		this.y = new IntProperty(RescueConstants.PROPERTY_Y,y);
		this.edges = new ArrayProperty(RescueConstants.PROPERTY_EDGES,edges);
	}

    public Property getProperty(int property) /*throws UnknownPropertyException*/ {
		switch (property) {
		case RescueConstants.PROPERTY_X:
			return x;
		case RescueConstants.PROPERTY_Y:
			return y;
		case RescueConstants.PROPERTY_EDGES:
			return edges;
		}
		return super.getProperty(property);
    }

	/*
    public boolean propertyExists(int property) {
		switch (property) {
		case RescueConstants.PROPERTY_X:
		case RescueConstants.PROPERTY_Y:
		case RescueConstants.PROPERTY_EDGES:
			return true;
		}
		return super.propertyExists(property);
    }
	*/


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

    public int[] getEdges() {
		return edges.getValues();
    }

	public boolean setEdges(int[] newEdges, int timestamp, Object source) {
		if (edges.updateValues(newEdges,timestamp,source)) {
			firePropertyChanged(RescueConstants.PROPERTY_EDGES,timestamp,source);
			return true;
		}
		return false;
	}

	public void appendEdge(int next, int timestamp, Object source) {
		edges.append(next);
		firePropertyChanged(RescueConstants.PROPERTY_EDGES,timestamp,source);
	}

	public void clearEdges(int timestamp, Object source) {
		edges.clear();
		firePropertyChanged(RescueConstants.PROPERTY_EDGES,timestamp,source);
	}
}
