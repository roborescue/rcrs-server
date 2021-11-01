/*
 * Last change: $Date: 2004/08/10 21:20:24 $
 * $Revision: 1.5 $
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

import java.io.*;
import java.util.Random;

/**
 * The main class of the mapgenerator package.
 * This creates a new random city map for use in the Robocup Rescue simulation.
 * @author Jonathan Teutenberg
 * @version 1.0 Aug 2003
 **/
public class MapGenerator{

	/**
	 * Creates a RescueMap object to specification.
	 * @param rotate Whether to randomly rotate the nodes.
	 * @param nooneway Whether to allow different number of lanes in each direction on a road.
	 * @param roadDensity How dense the roads are - 0..100.
	 * @param buildDensity How dense the buildings are - 0..100.
	 * @param width Approximate width to make the map in millimeters.
	 * @param height Approximate height to make the map in millimeters.
	 * @param uniformity How uniform (non-random) to make the map - 0..100.
	 * @param movement The maximum amount we can adjust nodes by in meters.
	 * @param smooth Total number of lanes a road must be before it has corners smoothed.
	 * @param initScheme The scheme to use to initialise the map.
	 * @param connScheme The scheme to use to decide on number of lanes per road.
	 * @param buildScheme The scheme to use to add buildings to the map.
	 * @param seed The random seed.
	 * @return A new RescueMap.
	 **/
	public static RescueMap generateMap(boolean rotate, boolean nooneway, int roadDensity, int roadRemoval, int buildDensity, int width, int height, int uniformity, int movement, int smooth, String initScheme, String connScheme, String buildScheme, long seed){
		Random rand = new Random(seed);
		//add and connect nodes of a RoadSet
		RescueMap rm = RoadSetFactory.createRoadSet(initScheme,width,height,roadDensity,rand);
		//randomise the RoadSet
		RescueMapToolkit.randomise(rm,uniformity, roadDensity, roadRemoval,movement, nooneway,rand);
		//rotate the nodes
		if(rotate)
			RescueMapToolkit.rotate(rm,rand.nextDouble()*Math.PI*2);
		//add intersections for overlapping roads
		RescueMapToolkit.findIntersections(rm);
		//weight the roads
		weightRoads(rm,uniformity,connScheme,nooneway,rand);
		//smooth main roads
		RescueMapToolkit.smoothRoads(rm,smooth);
		//add buildings and convert to a memory
		addBuildings(rm,uniformity,buildDensity,buildScheme,rand);
		//make sure we have no negative coordinates
		rm.align();
		System.out.println("Construction complete.");
		return rm;
	}

	/**
	 * Applies the road weighting scheme.
	 * @param rm The RescueMap to alter.
	 * @param uniformity The uniformity of the map - 0..100.
	 * @param connScheme The scheme to use.
	 * @param nooneway Whether roads can be different widths in each direction.
	 * @param rand The random number generator.
	 **/
	private static void weightRoads(RescueMap rm, int uniformity, String connScheme, boolean nooneway, Random rand){
		RoadWeighter rw;
		if(connScheme.equals("ants"))
			rw = new AntsRoadWeighter();
		else
			return;

		rw.connect(rm,uniformity,nooneway,rand);

	}
	/**
	 * Applies the building scheme.
	 * @param rm The RescueMap to alter.
	 * @param uniformity The uniformity of the map - 0..100.
	 * @param buildDensity The density of buildings - 0..100.
	 * @param buildScheme The scheme to use.
	 * @param rand The random number generator.
	 **/
	private static void addBuildings(RescueMap rm, int uniformity, int buildDensity, String buildScheme, Random rand){
		BuildingGenerator bg;
		if(buildScheme.equals("blocks"))
			bg = new BasicBuildingGenerator();
		else
			return;

		bg.addBuildings(rm,uniformity,buildDensity,rand);

	}

	/**
	 * Runs the program from the command line.
	 **/
	public static void main(String[] args){
		int roadDensity = 60;
		int roadRemoval = 15;
		int buildDensity = 50;
		int width = 1000000;
		int height = 1000000;
		int uniformity = 75;
		int movement = 20;
		int smooth = 5;
		boolean rotate = false;
		boolean nooneway = false;
		String initScheme = "grid";
		String connScheme = "none";
		String buildScheme = "blocks";
		long seed = System.currentTimeMillis();
		//get the switches
		for(int i = 0; i < args.length; i+=2){
			if(args[i].equals("-rd"))
				roadDensity = Integer.parseInt(args[i+1]);
			else if(args[i].equals("-rr"))
				roadRemoval = Integer.parseInt(args[i+1]);
			else if(args[i].equals("-bd"))
				buildDensity = Integer.parseInt(args[i+1]);
			else if(args[i].equals("-w"))
				width = Integer.parseInt(args[i+1]);
			else if(args[i].equals("-h"))
				height = Integer.parseInt(args[i+1]);
			else if(args[i].equals("-u"))
				uniformity = Integer.parseInt(args[i+1]);
			else if(args[i].equals("-m"))
				movement = Integer.parseInt(args[i+1]);
			else if(args[i].equals("-sm"))
				smooth = Integer.parseInt(args[i+1]);
			else if(args[i].equals("-init"))
				initScheme = args[i+1];
			else if(args[i].equals("-weight"))
				connScheme = args[i+1];
			else if(args[i].equals("-build"))
				buildScheme = args[i+1];
			else if(args[i].equals("-r"))
				seed = Long.parseLong(args[i+1]);
			else if(args[i].equals("-rotate")){
				rotate = true;
				i--;
			}
			else if(args[i].equals("-nooneway")){
				nooneway = true;
				i--;
			}
			else{
				System.out.println("Switches: [-switch] [valid range] [default] [description]");
				System.out.println("-rd [0..100] 60 \t\tRoad density");
				System.out.println("-rr [5..90] 15 \t\tRoad removal percentage");
				System.out.println("-bd [0..100] 50 \t\tBuilding density");
				System.out.println("-u [0..100] 75 \t\t\tRoad uniformity");
				System.out.println("-w [0..*] 1000000 \t\tMap width (millimeters)");
				System.out.println("-h [0..*] 1000000 \t\tMap height (millimeters)");
				System.out.println("-m [0..*] 20 \t\t\tMax node movement (meters)");
				System.out.println("-sm [1..8] 5 \t\t\tSmooth wide roads (lanes)");
				System.out.println("-init [grid] grid \t\tMap initialisation scheme");
				System.out.println("-weight [none,ants] none \tRoad weighting scheme");
				System.out.println("-build [blocks] \t\tblocks Building scheme");
				System.out.println("-rotate \t\t\tSet random map rotation flag");
				System.out.println("-nooneway \t\t\tSwitch off one way streets");
				return;
			}
		}
		RescueMap m = generateMap(rotate,nooneway,roadDensity,roadRemoval,buildDensity,width,height,uniformity,movement,smooth,initScheme,connScheme,buildScheme,seed);
		try {
			m.toFile();
		}
		catch (IOException e) {
		   e.printStackTrace();
		}
	}
}
