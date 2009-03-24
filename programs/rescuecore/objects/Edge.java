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

public abstract class Edge extends MotionlessObject {
	protected IntProperty head,tail,length;

    protected Edge() {
		head = new IntProperty(RescueConstants.PROPERTY_HEAD);
		tail = new IntProperty(RescueConstants.PROPERTY_TAIL);
		length = new IntProperty(RescueConstants.PROPERTY_LENGTH);
    }

	protected Edge(int head, int tail, int length) {
		this.head = new IntProperty(RescueConstants.PROPERTY_HEAD,head);
		this.tail = new IntProperty(RescueConstants.PROPERTY_TAIL,tail);
		this.length = new IntProperty(RescueConstants.PROPERTY_LENGTH,length);
	}

	/*
    public boolean propertyExists(int property) {
		switch (property) {
		case RescueConstants.PROPERTY_HEAD:
		case RescueConstants.PROPERTY_TAIL:
		case RescueConstants.PROPERTY_LENGTH:
			return true;
		}
		return super.propertyExists(property);
    }
	*/

    public Property getProperty(int property) /*throws UnknownPropertyException*/ {
		switch (property) {
		case RescueConstants.PROPERTY_HEAD:
			return head;
		case RescueConstants.PROPERTY_TAIL:
			return tail;
		case RescueConstants.PROPERTY_LENGTH:
			return length;
		}
		return super.getProperty(property);
    }

    public int getHead() {
		return head.getValue();
    }

	public boolean setHead(int h, int timestamp, Object source) {
		if (head.updateValue(h,timestamp,source)) {
			firePropertyChanged(RescueConstants.PROPERTY_HEAD,timestamp,source);
			return true;
		}
		return false;
	}

    public int getTail() {
		return tail.getValue();
    }

	public boolean setTail(int t, int timestamp, Object source) {
		if (tail.updateValue(t,timestamp,source)) {
			firePropertyChanged(RescueConstants.PROPERTY_TAIL,timestamp,source);
			return true;
		}
		return false;
	}

    public int getLength() {
		return length.getValue();
    }

	public boolean setLength(int l, int timestamp, Object source) {
		if (length.updateValue(l,timestamp,source)) {
			firePropertyChanged(RescueConstants.PROPERTY_LENGTH,timestamp,source);
			return true;
		}
		return false;
	}
}
