/*
 * Last change: $Date: 2005/02/17 02:25:38 $
 * $Revision: 1.10 $
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
   Encapsulation of a TYPE_ROAD object
   @see RescueConstants#TYPE_ROAD
 */

public class Road extends Edge {
	private IntProperty kind, carsToHead, carsToTail, humansToHead, humansToTail, width, block, cost, median, linesToHead, linesToTail, widthForWalkers;

	//	private boolean hasFreeLines;
	//	private int previousBlock;

    public Road() {
		kind = new IntProperty(RescueConstants.PROPERTY_ROAD_KIND);
		carsToHead = new IntProperty(RescueConstants.PROPERTY_CARS_PASS_TO_HEAD);
		carsToTail = new IntProperty(RescueConstants.PROPERTY_CARS_PASS_TO_TAIL);
		humansToHead = new IntProperty(RescueConstants.PROPERTY_HUMANS_PASS_TO_HEAD);
		humansToTail = new IntProperty(RescueConstants.PROPERTY_HUMANS_PASS_TO_TAIL);
		width = new IntProperty(RescueConstants.PROPERTY_WIDTH);
		block = new IntProperty(RescueConstants.PROPERTY_BLOCK);
		cost = new IntProperty(RescueConstants.PROPERTY_REPAIR_COST);
		median = new IntProperty(RescueConstants.PROPERTY_MEDIAN_STRIP);
		linesToHead = new IntProperty(RescueConstants.PROPERTY_LINES_TO_HEAD);
		linesToTail = new IntProperty(RescueConstants.PROPERTY_LINES_TO_TAIL);
		widthForWalkers = new IntProperty(RescueConstants.PROPERTY_WIDTH_FOR_WALKERS);
		//		previousBlock = -1;
    }

    public Road(int head, int tail, int length, int kind, int cth, int ctt, int hth, int htt, int width, int block, int cost, boolean median, int lth, int ltt, int wfw) {
		super(head,tail,length);
		this.kind = new IntProperty(RescueConstants.PROPERTY_ROAD_KIND,kind);
		this.carsToHead = new IntProperty(RescueConstants.PROPERTY_CARS_PASS_TO_HEAD,cth);
		this.carsToTail = new IntProperty(RescueConstants.PROPERTY_CARS_PASS_TO_TAIL,ctt);
		this.humansToHead = new IntProperty(RescueConstants.PROPERTY_HUMANS_PASS_TO_HEAD,hth);
		this.humansToTail = new IntProperty(RescueConstants.PROPERTY_HUMANS_PASS_TO_TAIL,htt);
		this.width = new IntProperty(RescueConstants.PROPERTY_WIDTH,width);
		this.block = new IntProperty(RescueConstants.PROPERTY_BLOCK,block);
		this.cost = new IntProperty(RescueConstants.PROPERTY_REPAIR_COST,cost);
		this.median = new IntProperty(RescueConstants.PROPERTY_MEDIAN_STRIP,median);
		this.linesToHead = new IntProperty(RescueConstants.PROPERTY_LINES_TO_HEAD,lth);
		this.linesToTail = new IntProperty(RescueConstants.PROPERTY_LINES_TO_TAIL,ltt);
		this.widthForWalkers = new IntProperty(RescueConstants.PROPERTY_WIDTH_FOR_WALKERS,wfw);
		//		previousBlock = -1;
    }

	public int getType() {
		return RescueConstants.TYPE_ROAD;
	}

	/*
    public boolean propertyExists(int property) {
		switch (property) {
		case RescueConstants.PROPERTY_ROAD_KIND:
		case RescueConstants.PROPERTY_CARS_PASS_TO_HEAD:
		case RescueConstants.PROPERTY_CARS_PASS_TO_TAIL:
		case RescueConstants.PROPERTY_HUMANS_PASS_TO_HEAD:
		case RescueConstants.PROPERTY_HUMANS_PASS_TO_TAIL:
		case RescueConstants.PROPERTY_WIDTH:
		case RescueConstants.PROPERTY_BLOCK:
		case RescueConstants.PROPERTY_REPAIR_COST:
		case RescueConstants.PROPERTY_MEDIAN_STRIP:
		case RescueConstants.PROPERTY_LINES_TO_HEAD:
		case RescueConstants.PROPERTY_LINES_TO_TAIL:
		case RescueConstants.PROPERTY_WIDTH_FOR_WALKERS:
			return true;
		}
		return super.propertyExists(property);
    }
	*/

    public Property getProperty(int property) /*throws UnknownPropertyException*/ {
		switch (property) {
		case RescueConstants.PROPERTY_ROAD_KIND:
			return kind;
		case RescueConstants.PROPERTY_CARS_PASS_TO_HEAD:
			return carsToHead;
		case RescueConstants.PROPERTY_CARS_PASS_TO_TAIL:
			return carsToTail;
		case RescueConstants.PROPERTY_HUMANS_PASS_TO_HEAD:
			return humansToHead;
		case RescueConstants.PROPERTY_HUMANS_PASS_TO_TAIL:
			return humansToTail;
		case RescueConstants.PROPERTY_WIDTH:
			return width;
		case RescueConstants.PROPERTY_BLOCK:
			return block;
		case RescueConstants.PROPERTY_REPAIR_COST:
			return cost;
		case RescueConstants.PROPERTY_MEDIAN_STRIP:
			return median;
		case RescueConstants.PROPERTY_LINES_TO_HEAD:
			return linesToHead;
		case RescueConstants.PROPERTY_LINES_TO_TAIL:
			return linesToTail;
		case RescueConstants.PROPERTY_WIDTH_FOR_WALKERS:
			return widthForWalkers;
		}
		return super.getProperty(property);
    }

    public int getRoadKind() {
		return kind.getValue();
    }

	public boolean setRoadKind(int k, int timestamp, Object source) {
		if (kind.updateValue(k,timestamp,source)) {
			firePropertyChanged(RescueConstants.PROPERTY_ROAD_KIND,timestamp,source);
			return true;
		}
		return false;
	}

    public int getCarsPassToHead() {
		return carsToHead.getValue();
    }

	public boolean setCarsPassToHead(int cars, int timestamp, Object source) {
		if (carsToHead.updateValue(cars,timestamp,source)) {
			firePropertyChanged(RescueConstants.PROPERTY_CARS_PASS_TO_HEAD,timestamp,source);
			return true;
		}
		return false;
	}

    public int getCarsPassToTail() {
		return carsToTail.getValue();
    }

	public boolean setCarsPassToTail(int cars, int timestamp, Object source) {
		if (carsToTail.updateValue(cars,timestamp,source)) {
			firePropertyChanged(RescueConstants.PROPERTY_CARS_PASS_TO_TAIL,timestamp,source);
			return true;
		}
		return false;
	}

    public int getHumansPassToHead() {
		return humansToHead.getValue();
    }

	public boolean setHumansPassToHead(int h, int timestamp, Object source) {
		if (humansToHead.updateValue(h,timestamp,source)) {
			firePropertyChanged(RescueConstants.PROPERTY_HUMANS_PASS_TO_HEAD,timestamp,source);
			return true;
		}
		return false;
	}

    public int getHumansPassToTail() {
		return humansToTail.getValue();
    }

	public boolean setHumansPassToTail(int h, int timestamp, Object source) {
		if (humansToTail.updateValue(h,timestamp,source)) {
			firePropertyChanged(RescueConstants.PROPERTY_HUMANS_PASS_TO_TAIL,timestamp,source);
			return true;
		}
		return false;
	}

    public int getWidth() {
		return width.getValue();
    }

	public boolean setWidth(int w, int timestamp, Object source) {
		if (width.updateValue(w,timestamp,source)) {
			firePropertyChanged(RescueConstants.PROPERTY_WIDTH,timestamp,source);
			return true;
		}
		return false;
	}

    public int getBlock() {
		return block.getValue();
    }

    public boolean setBlock(int b, int timestamp, Object source) {
		if (block.updateValue(b,timestamp,source)) {
			firePropertyChanged(RescueConstants.PROPERTY_BLOCK,timestamp,source);
			return true;
		}
		return false;
    }

    public int getRepairCost() {
		return cost.getValue();
    }

    public boolean setRepairCost(int c, int timestamp, Object source) {
		if (cost.updateValue(c,timestamp,source)) {
			firePropertyChanged(RescueConstants.PROPERTY_REPAIR_COST,timestamp,source);
			return true;
		}
		return false;
    }

    public boolean hasMedian() {
		return median.getValue()!=0;
    }

	public boolean setMedian(boolean b, int timestamp, Object source) {
		if (median.updateValue(b?1:0,timestamp,source)) {
			firePropertyChanged(RescueConstants.PROPERTY_MEDIAN_STRIP,timestamp,source);
			return true;
		}
		return false;
	}

    public int getLinesToHead() {
		return linesToHead.getValue();
    }

	public boolean setLinesToHead(int l, int timestamp, Object source) {
		if (linesToHead.updateValue(l,timestamp,source)) {
			firePropertyChanged(RescueConstants.PROPERTY_LINES_TO_HEAD,timestamp,source);
			return true;
		}
		return false;
	}

    public int getLinesToTail() {
		return linesToTail.getValue();
    }

	public boolean setLinesToTail(int l, int timestamp, Object source) {
		if (linesToTail.updateValue(l,timestamp,source)) {
			firePropertyChanged(RescueConstants.PROPERTY_LINES_TO_TAIL,timestamp,source);
			return true;
		}
		return false;
	}

    public int getWidthForWalkers() {
		return widthForWalkers.getValue();
    }

	public boolean setWidthForWalkers(int w, int timestamp, Object source) {
		if (widthForWalkers.updateValue(w,timestamp,source)) {
			firePropertyChanged(RescueConstants.PROPERTY_WIDTH_FOR_WALKERS,timestamp,source);
			return true;
		}
		return false;
	}

    public boolean isBlocked() {
		return getBlock() > 0;
    }

    public boolean hasBlockedLines() {
		return getBlockedLines()>0;
    }

    public boolean hasFreeLines() {
		return getFreeLinesToHead()>0 && getFreeLinesToTail()>0;
    }

    public int getFreeLinesToHead() {
		double lanes = getLinesToHead()+getLinesToTail();
		double laneWidth = getWidth()/lanes;
		int blockedLanes = (int)Math.floor(getBlock()/laneWidth/2.0 + 0.5);
		return getLinesToHead() - blockedLanes;
    }

    public int getFreeLinesToTail() {
		double lanes = getLinesToHead()+getLinesToTail();
		double laneWidth = getWidth()/lanes;
		int blockedLanes = (int)Math.floor(getBlock()/laneWidth/2.0 + 0.5);
		return getLinesToTail() - blockedLanes;
    }

    public int getBlockedLines() {
		double lanes = getLinesToHead()+getLinesToTail();
		double laneWidth = getWidth()/lanes;
		int blockedLanes = (int)Math.floor(getBlock()/laneWidth/2.0 + 0.5);
		return blockedLanes*2;
    }
}
