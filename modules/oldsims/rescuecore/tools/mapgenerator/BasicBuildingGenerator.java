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

import java.util.*;
/**
 * A BuildingGenerator that recursively chops a block into rectangular buildings.
 * @author Jonathan Teutenberg
 * @version 1.0 Aug 2003
 **/
public class BasicBuildingGenerator extends BlocksBuildingGenerator{
	/**
	 * Divides a block into a number of rectangular buildings.
	 * @param rm The RescueMap containing the block.
	 * @param block A list of nodes in anti-clockwise order around the block.
	 * @param density The density of buildings to add.
	 * @param rand The random number generator.
	 * @return Buildings to add as an array of form [building][apex][coordinate]
	 **/
	protected int[][][] fillBlock(RescueMap rm, int[] block,int density, Random rand){
		//make a building filling the whole block
		int[][] build = new int[block.length+1][2];
		for(int i = 0; i < block.length; i++){
			build[i][0] = rm.getX(block[i]);
			build[i][1] = rm.getY(block[i]);
			if(i > 0 && i < block.length-1)
				shift(rm,block[i-1],block[i],block[i+1],build[i]);
		}
		shift(rm,block[block.length-1],block[0],block[1],build[0]);
		shift(rm,block[block.length-2],block[block.length-1],block[0],build[build.length-2]);
		build[build.length-1][0] = build[0][0];
		build[build.length-1][1] = build[0][1];
		//now break it up
		int minArea = 500000/(density+1);
		ArrayList builds = processBuilding(build,minArea,rand);
		int[][][] bs = new int[builds.size()][][];
		for(int i = 0; i < bs.length; i++) {
			bs[i] = (int[][])builds.get(i);
			shrink(bs[i],rand.nextDouble()*0.4);
		}
		return bs;
	}

	private void shrink(int[][] building, double amount) {
		int xMin = building[0][0];
		int xMax = building[0][0];
		int yMin = building[0][1];
		int yMax = building[0][1];
		for (int i=1;i<building.length;++i) {
			xMin = Math.min(xMin,building[i][0]);
			xMax = Math.max(xMax,building[i][0]);
			yMin = Math.min(yMin,building[i][1]);
			yMax = Math.max(yMax,building[i][1]);
		}
		int xCenter = (xMin+xMax)/2;
		int yCenter = (yMin+yMax)/2;
		for (int i=0;i<building.length;++i) {
			double dx = building[i][0]-xCenter;
			double dy = building[i][1]-yCenter;
			building[i][0] -= (int)(dx*amount);
			building[i][1] -= (int)(dy*amount);
		}
	}

	/**
	 * No buildings returned - just have an empty outer rim.
	 * @param rm The RescueMap containing the block.
	 * @param block A list of nodes in anti-clockwise order around the block.
	 * @param density The density of buildings to add.
	 * @param rand The random number generator.
	 * @return Buildings to add as an array of form [building][apex][coordinate]
	 **/
	protected int[][][] fillOuterBlock(RescueMap rm, int[] block, int density,Random rand){
		return new int[0][][];
	}


	/**
	 * Shifts a set of coordinates in the direction normal to the two adjacent road.
	 * Assumes n1 --> n2 --> n3 is an anticlockwise motion.
	 * @param n1 First node of the roads.
	 * @param n2 Middle node of the two roads
	 * @param n3 The last node of the two roads
	 * @param c1 The set of coordinates to shift
	 **/
	private static void shift(RescueMap rm, int n1, int n2, int n3, int[] c){
		//get the normal to the first road
		long n1y = rm.getX(n2) - rm.getX(n1);
		long n1x = rm.getY(n1) - rm.getY(n2);
		//get normal to the second road
		long n2y = rm.getX(n3) - rm.getX(n2);
		long n2x = rm.getY(n2) - rm.getY(n3);
		//get length of each normal
		double len1 = Math.sqrt(n1y*n1y+n1x*n1x);
		double len2 = Math.sqrt(n2y*n2y+n2x*n2x);

		int d = 3000;//Math.max(rm.getRoad(n1,n2),rm.getRoad(n2,n3))*2000 +500;

		int x1 = rm.getX(n1) - (int)(n1x*d*1.0/len1);
		int x2 = rm.getX(n2) - (int)(n1x*d*1.0/len1);
		int y1 = rm.getY(n1) - (int)(n1y*d*1.0/len1);
		int y2 = rm.getY(n2) - (int)(n1y*d*1.0/len1);
		int x3 = rm.getX(n2) - (int)(n2x*d*1.0/len2);
		int x4 = rm.getX(n3) - (int)(n2x*d*1.0/len2);
		int y3 = rm.getY(n2) - (int)(n2y*d*1.0/len2);
		int y4 = rm.getY(n3) - (int)(n2y*d*1.0/len2);

		int[] intersect = intersection(x1,y1,x2,y2,x3,y3,x4,y4);
		if(intersect == null){
			c[0] -= (n1x/len1)*d;
			c[1] -= (n1y/len1)*d;
		}
		else{
			c[0] = intersect[0];
			c[1] = intersect[1];
		}
	}

	/**
	 * Where two line segments intersect.
	 **/
	private static int[] intersection(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4){

		double b1 = (y2 - y1)/(x2 - x1);
		double a1 = y1 - (b1*x1);
		double b2 = (y4 - y3)/(x4 - x3);
		double a2 = y3 - (b2*x3);
		if(x2-x1 == 0) //vertical
			return new int[]{(int)x1,(int)(a2+(b2*x1))};
		if(x3-x4 == 0) //vertical
			return new int[]{(int)x3,(int)(a1+(b1*x3))};
	/*	if(Math.abs(b1-b2) < 0.001)
			return null;
		double intX = -(a1-a2)/(b1-b2);
		double intY = a1+(b1*intX);*/
		double d = ((y4-y3)*(x2-x1) - (x4-x3)*(y2-y1));
		if(d == 0)
			return null; //parallel roads
		double a = ((x4-x3)*(y1-y3) - (y4-y3)*(x1-x3))*1.0/d;
		int intX = (int)x1+(int)(a*(x2-x1));
		int intY = (int)y1+(int)(a*(y2-y1));
		return new int[]{(int)intX,(int)intY};
	}

	/**
	 * Recursively splits a building in half, until it is lower than the given threshold
	 * or gets lucky when within a reasonable size.
	 * @param building The initial building to split.
	 * @param minArea The lower threshold that forms the base case.
	 * @param rand The random number generator.
	 * @return A list containing all buildings split through recursive calls.
	 **/
	private ArrayList processBuilding(int[][] building, int minArea, Random rand){
		int a = RescueMapToolkit.area(building);
		//System.out.println(minArea+", "+a);
		if(a < 1000) //kill these ones...
			return new ArrayList(0);
		if(a < minArea){ //primary base case
			ArrayList l = new ArrayList(1);
			l.add(building);
			return l;
		}
		int lower = (int)(rand.nextDouble()*minArea);
		lower = lower*4;
		if(a < lower){ //probabilistic base case
			ArrayList l = new ArrayList(1);
			l.add(building);
			return l;
		}
		//find the max and min points
		int minX = building[0][0];
		int minY = building[0][1];
		int maxX = building[0][0];
		int maxY = building[0][1];
		for(int i = 1; i < building.length; i++){
			if(minX > building[i][0]) minX = building[i][0];
			if(maxX < building[i][0]) maxX = building[i][0];
			if(minY > building[i][1]) minY = building[i][1];
			if(maxY < building[i][1]) maxY = building[i][1];
		}
		int midX = (minX+maxX)/2;
		int midY = (minY+maxY)/2;
		//split the building in half
		int[][][] split;
		if(maxX-minX > maxY-minY)
			split = RescueMapToolkit.split(building,midX,minY,midX,maxY);
		else
			split = RescueMapToolkit.split(building,minX,midY,maxX,midY);

		if(split == null || RescueMapToolkit.area(split[0]) == 0 || RescueMapToolkit.area(split[1]) == 0)
			return new ArrayList(0);

		//and recurse
		ArrayList a1 = processBuilding(split[0],minArea,rand);
		ArrayList a2 = processBuilding(split[1],minArea,rand);
		ArrayList toRet = new ArrayList(a1.size()+a2.size());
		for(int i = 0; i < a1.size(); i++)
			toRet.add(a1.get(i));
		for(int i = 0; i < a2.size(); i++)
			toRet.add(a2.get(i));
		return toRet;
	}

}
