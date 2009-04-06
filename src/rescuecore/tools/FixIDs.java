/*
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

package rescuecore.tools;

import rescuecore.*;
import rescuecore.objects.*;
import java.util.*;

public class FixIDs {
	private final static String source = "FixIDs";
	public static void main(String[] args) {
		try {
			Road[] roads = MapFiles.loadRoads("road.bin");
			Node[] nodes = MapFiles.loadNodes("node.bin");
			Building[] buildings = MapFiles.loadBuildings("building.bin");
			fixIDs(roads,nodes,buildings);
			// Write out the new files
			MapFiles.writeBuildings("building.bin",buildings);
			MapFiles.writeRoads("road.bin",roads);
			MapFiles.writeNodes("node.bin",nodes);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void fixIDs(Road[] roads, Node[] nodes, Building[] buildings) {
		/*
		int maxID = 0;
		for (int i=0;i<roads.length;++i) maxID = Math.max(maxID,roads[i].getID());
		for (int i=0;i<nodes.length;++i) maxID = Math.max(maxID,nodes[i].getID());
		for (int i=0;i<buildings.length;++i) maxID = Math.max(maxID,buildings[i].getID());
		int nextID = ++maxID;
		*/
		int nextID = 1;
		HashMap<Integer,Integer> idMap = new HashMap<Integer,Integer>();
		HashMap<Integer,Node> nodeMap = new HashMap<Integer,Node>();
		for (int i=0;i<roads.length;++i) {
			//			idMap.put(roads[i].getID(),nextID);
			//			System.out.println("Road: "+roads[i].getID()+" -> "+nextID);
			roads[i].setID(nextID++);
		}
		for (int i=0;i<buildings.length;++i) {
			//			idMap.put(buildings[i].getID(),nextID);
			//			System.out.println("Building: "+buildings[i].getID()+" -> "+nextID);
			buildings[i].setID(nextID++);
		}
		for (int i=0;i<nodes.length;++i) {
			idMap.put(nodes[i].getID(),nextID);
			//			System.out.println("Node: "+nodes[i].getID()+" -> "+nextID);
			nodes[i].setID(nextID++);
			//			System.out.println("Node "+nodes[i].getID()+" used to have "+nodes[i].getEdges().length+" edges");
			nodes[i].clearEdges(0,null);
			nodeMap.put(nodes[i].getID(),nodes[i]);
		}
		// Fix the road head/tail entries
		for (int i=0;i<roads.length;++i) {
			int headID = idMap.get(roads[i].getHead());
			int tailID = idMap.get(roads[i].getTail());
			//			if (headID==0) System.out.println("Couldn't find the new head node for road "+roads[i].getID());
			//			if (tailID==0) System.out.println("Couldn't find the new tail node for road "+roads[i].getID());
			roads[i].setHead(idMap.get(roads[i].getHead()),0,null);
			roads[i].setTail(idMap.get(roads[i].getTail()),0,null);
			Node head = nodeMap.get(roads[i].getHead());
			Node tail = nodeMap.get(roads[i].getTail());
			head.appendEdge(roads[i].getID(),0,null);
			tail.appendEdge(roads[i].getID(),0,null);
		}
		// Fix the building entrances
		for (int i=0;i<buildings.length;++i) {
			int[] entrances = buildings[i].getEntrances();
			for (int j=0;j<entrances.length;++j) {
				//				System.out.println("Entrance "+entrances[j]+" -> "+idMap.get(entrances[j]));
				entrances[j] = idMap.get(entrances[j]);
				Node node = nodeMap.get(entrances[j]);
				node.appendEdge(buildings[i].getID(),0,null);
			}
			buildings[i].setEntrances(entrances,0,null);
		}
		// Check that the nodes shortcut/pocket/signal timing are the right size
		for (int i=0;i<nodes.length;++i) {
			Node next = nodes[i];
			int numEdges = next.getEdges().length;
			int shortcutSize = next.getShortcutToTurn().length;
			int pocketSize = next.getPocketToTurnAcross().length;
			int timingSize = next.getSignalTiming().length;
			while (shortcutSize < numEdges) {
				next.appendShortcutToTurn(0,0,null);
				++shortcutSize;
			}
			while (pocketSize < numEdges*2) {
				next.appendPocketToTurnAcross(0,0,null);
				++pocketSize;
			}
			while (timingSize < numEdges*3) {
				next.appendSignalTiming(0,0,null);
				++timingSize;
			}
			if (shortcutSize > numEdges) {
				//				System.out.println("Node "+next.getID()+" has too many shortcuts");
				next.clearShortcutToTurn(0,null);
				for (int j=0;j<numEdges;++j) next.appendShortcutToTurn(0,0,null);
			}
			if (pocketSize > numEdges*2) {
				//				System.out.println("Node "+next.getID()+" has too many pockets");
				next.clearPocketToTurnAcross(0,null);
				for (int j=0;j<numEdges*2;++j) next.appendPocketToTurnAcross(0,0,null);
			}
			if (timingSize > numEdges*3) {
				//				System.out.println("Node "+next.getID()+" has too many timings");
				next.clearSignalTiming(0,null);
				for (int j=0;j<numEdges*3;++j) next.appendSignalTiming(0,0,null);
			}
		}
	}
}
