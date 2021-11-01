/*
 * Last change: $Date: 2004/08/03 03:25:05 $
 * $Revision: 1.4 $
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

package rescuecore.sample;

import rescuecore.*;
import java.util.*;

public class SampleSearch {
    /**
       Do a breadth first search from one location to another
       @param start The location we start at
       @param goal The location we want to get to
       @param memory The memory of the agent doing the searching
       @return The path from start to goal, or null if no path can be found
    */
    public static int[] breadthFirstSearch(RescueObject start, RescueObject goal, Memory memory) {
		List open = new LinkedList();
		Map ancestors = new HashMap();
		open.add(start);
		RescueObject next = null;
		do {
			next = (RescueObject)open.remove(0);
			RescueObject[] neighbours = memory.findNeighbours(next);
			if (neighbours==null) continue;
			for (int i=0;i<neighbours.length;++i) {
				if (neighbours[i]==null) continue;
				if (neighbours[i]==goal) {
					ancestors.put(neighbours[i],next);
					next = neighbours[i];
					break;
				}
				else {
					if (!ancestors.containsKey(neighbours[i]) && !neighbours[i].isBuilding()) {
						open.add(neighbours[i]);
						ancestors.put(neighbours[i],next);
					}
				}
			}
		} while (next != goal && next != null);
		if (next==null) {
			// No path
			return null;
		}
		// Walk back from goal to start
		RescueObject current = goal;
		Stack path = new Stack();
		do {
			path.push(current);
			current = (RescueObject)ancestors.get(current);
		} while (current!=start && current!=null);
		int[] result = new int[path.size()];
		for (int i=0;i<result.length;++i) {
			result[i] = ((RescueObject)path.pop()).getID();
		}
		return result;	
    }

    /**
       Sort a list of RescueObjects by distance. This list will be sorted in place.
       @param objects The objects to be sorted. When the method returns this list will be sorted.
       @param reference The RescueObject to measure distances from
       @param memory The memory of the agent doing the sorting
    */
    public static void sortByDistance(List objects, RescueObject reference, Memory memory) {
		synchronized(DISTANCE_SORTER) {
			DISTANCE_SORTER.memory = memory;
			DISTANCE_SORTER.reference = reference;
			Collections.sort(objects,DISTANCE_SORTER);
		}
    }

    /**
       Sort an array of RescueObjects by distance. This array will be sorted in place.
       @param objects The objects to be sorted. When the method returns this array will be sorted.
       @param reference The RescueObject to measure distances from
       @param memory The memory of the agent doing the sorting
    */
    public static void sortByDistance(RescueObject[] objects, RescueObject reference, Memory memory) {
		synchronized(DISTANCE_SORTER) {
			DISTANCE_SORTER.memory = memory;
			DISTANCE_SORTER.reference = reference;
			Arrays.sort(objects,DISTANCE_SORTER);
		}
    }

    /**
       A Comparator for use when sorting RescueObjects by distance
    */
    private static class DistanceSorter implements Comparator {
		Memory memory;
		RescueObject reference;

		public int compare(Object o1, Object o2) {
			try {
				double d1 = memory.getDistance(reference,(RescueObject)o1);
				double d2 = memory.getDistance(reference,(RescueObject)o2);
				if (d1 < d2) // Object o1 is closer
					return -1;
				if (d1 > d2) // Object o2 is closer
					return 1;
			}
			catch (CannotFindLocationException e) {
				System.err.println(e);
			}
			// They are the same distance (or we couldn't find one of them). Return the lower id first
			return ((RescueObject)o1).getID()-((RescueObject)o2).getID();

		}
    }

    private final static DistanceSorter DISTANCE_SORTER = new DistanceSorter();
}
