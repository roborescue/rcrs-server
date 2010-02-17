/*
 * Last change: $Date: 2004/07/11 22:51:54 $
 * $Revision: 1.3 $
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

package rescuecore.tools.mapgenerator;

import java.util.Random;

/**
 * A class that provides a factory method for creating and setting up a RescueMap.
 * @author Jonathan Teutenberg
 * @version 1.0 Aug 2003
 **/
public class RoadSetFactory{
	/** Minimum road length for grid initial condition. **/
	private static final int MIN_START_DISTANCE = 30000; //30m road
	/** Maximum road length for grid initial condition. **/
	private static final int MAX_START_DISTANCE = 80000; //80m road

	/**
	 * Creates a RescueMap, then initialises its nodes and roads.
	 * @param scheme The name of the initialisation scheme to use.
	 * @param width Approximate width to make the map in millimeters.
	 * @param height Approximate height to make the map in millimeters.
	 * @param density How dense to make the roads of map - from 0..100.
	 * @param rand The random number generator.
	 * @return A new road map. Null if an invalid scheme is used.
	 **/
	public static RescueMap createRoadSet(String scheme, int width, int height, int density, Random rand){
		//try to find a scheme we know
		if(scheme.equals("  ")){
			return null;
		}
		else
			return grid(width,height,density);
	}

	/**
	 * Constructs a new RescueMap uniformly distributed over the given rectangle.
	 * The nodes are connected in a grid.
	 * @param width The width of the rectangle.
	 * @param height The height of the rectangle.
	 * @param density How dense to make the roads of map - from 0..100.
	 * @return A new RescueMap laid out in a grid.
	 **/
	private static RescueMap grid(int width, int height, int density){
		int spacing = (100-density)*(MAX_START_DISTANCE-MIN_START_DISTANCE)/100 + MIN_START_DISTANCE;
		int xCount = width/spacing;
		int yCount = height/spacing;
		System.out.println("Creating a "+xCount+"x"+yCount+" grid = "+xCount*yCount+" intersections.");
		RescueMap rm = new RescueMap(xCount*yCount,width,height);
		for(int i = 0; i < yCount; i++)
			for(int j = 0; j < xCount; j++){
				int index = i*xCount+j;
				rm.setX(index,j*spacing);
				rm.setY(index, i*spacing);
				if(i > 0){
					rm.setRoad(index,index-xCount,1);
					rm.setRoad(index-xCount,index,1);
				}
				if(j > 0){
					rm.setRoad(index,index-1,1);
					rm.setRoad(index-1,index,1);
				}
			}
		return rm;
	}


 }
