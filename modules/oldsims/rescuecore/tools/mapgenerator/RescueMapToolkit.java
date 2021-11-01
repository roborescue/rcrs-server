/*
 * Last change: $Date: 2004/07/11 22:51:54 $
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

package rescuecore.tools.mapgenerator;

import java.util.*;

/**
 * A toolkit class with useful methods for manipulating RescueMaps.
 * @author Jonathan Teutenberg
 * @version 1.0 Aug 2003
 **/
public class RescueMapToolkit{

	/**
	 * Shifts some nodes in the map by random distances and removes
	 * a number of randomly selected roads.
	 * @param m The RescueMap to alter.
	 * @param uniformity Indicates how likely any given node is to be moved, from 0..100.
	 * @param density Indicates how likely it is that a road be removed, from 0..100.
	 * @param movement The maximum distance a node will be moved in meters.
	 * @param nooneway Whether we are allowed to remove roads in only one direction at a time.
	 * @param rand The random number generator.
	 **/
	public static void randomise(RescueMap m, int uniformity, int density, int removal, int movement, boolean nooneway, Random rand){
		int nodeCount = m.getNodeCount();
		//between 100% and 0% of roads moved
		System.out.println("Adjusting nodes by up to "+movement+"m.");
		for(int i = 0; i < nodeCount; i++){
			if(rand.nextDouble()*100 > uniformity){
				m.setX(i,m.getX(i)+(int)((rand.nextDouble()-0.5)*movement*2000));
				m.setY(i,m.getY(i)+(int)((rand.nextDouble()-0.5)*movement*2000));
			}
		}
		//remove 5% - 90% of the roads
		double thresh = 0.01*removal;
		System.out.println("Removing about "+((int)(thresh*100))+"% of roads.");
		int[][] roads = m.getRoads();
		for(int i = 0; i < roads.length; i++){
			if(rand.nextDouble() < thresh){
				//try both directions
				m.setRoad(roads[i][0],roads[i][1],0);
				m.setRoad(roads[i][1],roads[i][0],0);
				//now test connectedness
				if(!m.pathExists(roads[i][1],roads[i][0])){
					//try one direction
					m.setRoad(roads[i][1],roads[i][0],1);
					if(!m.pathExists(roads[i][0],roads[i][1]))
						m.setRoad(roads[i][0],roads[i][1],1); //replace both directions..
				}
				else if(!m.pathExists(roads[i][0],roads[i][1]))
					m.setRoad(roads[i][0],roads[i][1],1); //reset the other road
			}
			else if(!nooneway && rand.nextDouble() < thresh*0.2){
				//let's make it 1-way!
				m.setRoad(roads[i][0],roads[i][1],0);
				if(!m.pathExists(roads[i][0],roads[i][1]))
					m.setRoad(roads[i][0],roads[i][1],1);
			}
		}
	}

	/**
	 * Rotate all nodes of a RescueMap. This should be applied BEFORE any
	 * buildings are added.
	 * @param m The RescueMap to rotate.
	 * @param radians The number of radians to rotate by.
	 **/
	public static void rotate(RescueMap m,double radians){
		int width = m.getWidth();
		int height = m.getHeight();
		int nodeCount = m.getNodeCount();
		for(int i = 0; i < nodeCount; i++){
			double x = m.getX(i) - width/2;
			double y = m.getY(i) - height/2;
			double radius = Math.sqrt(x*x+y*y);
			double theta = Math.acos(x/radius);
			if(y > 0)
				theta = Math.PI*2-theta;
			x = radius*Math.cos(theta+radians);
			y = radius*Math.sin(theta+radians);
			m.setX(i,(int)x+width/2);
			m.setY(i,(int)y+height/2);
		}
	}

	/**
	 * Finds any overlapping roads and creates a new node
	 * at their intersection.
	 * @param m The RescueMap to work on.
	 **/
	public static void findIntersections(RescueMap m){
		int[][] rs = m.getRoads();
		//now check all pairs of roads
		for(int i = 0; i < rs.length; i++){
			int n1 = rs[i][0];
			int n2 = rs[i][1];
			for(int j = 0; j < rs.length; j++){
				int n3 = rs[j][0];
				int n4 = rs[j][1];
				if(n1 == n3 || n1 == n4 || n2 == n3 || n2 == n4)
					continue; //shared node
				int x1 = m.getX(n1);
				int x2 = m.getX(n2);
				int x3 = m.getX(n3);
				int x4 = m.getX(n4);
				int y1 = m.getY(n1);
				int y2 = m.getY(n2);
				int y3 = m.getY(n3);
				int y4 = m.getY(n4);
				//get intersection
				int d = ((y4-y3)*(x2-x1) - (x4-x3)*(y2-y1));
				if(d == 0)
					continue; //parallel roads
				double a = ((x4-x3)*(y1-y3) - (y4-y3)*(x1-x3))*1.0/d;
				int x = x1+(int)(a*(x2-x1));
				int y = y1+(int)(a*(y2-y1));
				//check it is within the roads
				if(x > Math.min(x1,x2) && x < Math.max(x1,x2)
				&& x < Math.max(x3,x4) && x > Math.min(x3,x4)
				&& y > Math.min(y1,y2) && y < Math.max(y1,y2)
				&& y < Math.max(y3,y4) && y > Math.min(y3,y4)){ //they cross!!
					intersect(m,n1,n2,n3,n4,x,y);
					rs = m.getRoads();
					//start again
					i = Math.max(0,i-4);
					j = 0;
					break;
				}
			}
		}
	}
	/**
	 * Used by findIntersections() to deal with a single pair of
	 * overlapping roads.
	 **/
	private static void intersect(RescueMap m, int n1, int n2, int n3, int n4, int x, int y){
		m.addNode();
		int nodeCount = m.getNodeCount();
		//make the node
		m.setX(nodeCount-1,x);
		m.setY(nodeCount-1,y);
		//split the two roads
		m.setRoad(n1,nodeCount-1,m.getRoad(n1,n2));
		m.setRoad(n2,nodeCount-1,m.getRoad(n2,n1));
		m.setRoad(n3,nodeCount-1,m.getRoad(n3,n4));
		m.setRoad(n4,nodeCount-1,m.getRoad(n4,n3));
		m.setRoad(nodeCount-1,n1,m.getRoad(n2,n1));
		m.setRoad(nodeCount-1,n2,m.getRoad(n1,n2));
		m.setRoad(nodeCount-1,n3,m.getRoad(n4,n3));
		m.setRoad(nodeCount-1,n4,m.getRoad(n3,n4));
		//delete the old roads
		m.setRoad(n1,n2,0);
		m.setRoad(n2,n1,0);
		m.setRoad(n3,n4,0);
		m.setRoad(n4,n3,0);
	}

	/**
	 * Rounds the corners of wide roads.
	 * @param m The RescueMap to smooth the roads of.
	 * @param size The minimum size of roads to smooth - given in total lanes for both directions.
	 **/
	public static void smoothRoads(RescueMap m, int size){
		int max = m.getNodeCount();
		for(int i = 0; i < max; i++){
			if(m.getOutDegree(i) >= 2 && m.getInDegree(i) >= 2){ //candidate
				//find two roads
				int n1 = -1;
				int n3 = -1;
				for(int j = 0; j < m.getNodeCount(); j++){
					if(m.getRoad(i,j) + m.getRoad(j,i) >= size){
						if(n1 == -1)
							n1 = j;
						else if(n3 == -1)
							n3 = j;
						else{
							n1 = -1;
							n3 = -1;
							break;
						}
					}
				}
				if(n1 != -1 && n3 != -1)
					smooth(m,n1,i,n3);
			}
		}
	}

	/**
	 * Used by smoothRoads() to deal with a single corner.
	 **/
	private static void smooth(RescueMap m, int n1, int n2, int n3){
		//find the mid point
		int x = (m.getX(n1)+m.getX(n3))/2;
		int y = (m.getY(n1)+m.getY(n3))/2;
		//make two nodes halfway up each road
		m.addNode();
		m.addNode();
		int nodeCount = m.getNodeCount();
		m.setX(nodeCount-2,(m.getX(n1)+m.getX(n2))/2);
		m.setY(nodeCount-2,(m.getY(n1)+m.getY(n2))/2);
		m.setX(nodeCount-1,(m.getX(n3)+m.getX(n2))/2);
		m.setY(nodeCount-1,(m.getY(n3)+m.getY(n2))/2);
		//.. and drag toward mid point
		m.setX(nodeCount-2,(int)(m.getX(nodeCount-2)*0.75+x*0.25));
		m.setY(nodeCount-2,(int)(m.getY(nodeCount-2)*0.75+y*0.25));
		m.setX(nodeCount-1,(int)(m.getX(nodeCount-1)*0.75+x*0.25));
		m.setY(nodeCount-1,(int)(m.getY(nodeCount-1)*0.75+y*0.25));
		//shift the old roads
		m.setRoad(n1,nodeCount-2,m.getRoad(n1,n2));
		m.setRoad(nodeCount-2,n1,m.getRoad(n2,n1));
		m.setRoad(n2,nodeCount-2,m.getRoad(n2,n1));
		m.setRoad(nodeCount-2,n2,m.getRoad(n1,n2));
		m.setRoad(n3,nodeCount-1,m.getRoad(n3,n2));
		m.setRoad(nodeCount-1,n3,m.getRoad(n2,n3));
		m.setRoad(n2,nodeCount-1,m.getRoad(n2,n3));
		m.setRoad(nodeCount-1,n2,m.getRoad(n3,n2));
		m.setRoad(n1,n2,0);
		m.setRoad(n2,n1,0);
		m.setRoad(n3,n2,0);
		m.setRoad(n2,n3,0);
		//get the mid point of the new nodes
		x = (m.getX(nodeCount-2)+m.getX(nodeCount-1))/2;
		y = (m.getY(nodeCount-2)+m.getY(nodeCount-1))/2;
		//shift the middle node toward them
		m.setX(n2,(int)(m.getX(n2)*0.25+x*0.75));
		m.setY(n2,(int)(m.getY(n2)*0.25+y*0.75));
	}

	/**
	 * Gets the angle between two connected sets of nodes.
	 * @param m The RescueMap holding the nodes.
	 * @param n1 The first node.
	 * @param n2 The connecting (middle) node.
	 * @param n3 The third node.
	 * @return A number between 0 and Pi*2. Eg: 0.01 indicates a road turning slightly left, 6.28 indicates one turning slightly right.
	 **/
	public static double angle(RescueMap m, int n1, int n2, int n3){
		//get the vector for the two roads
		double x1 = m.getX(n2) - m.getX(n1);
		double y1 = m.getY(n2) - m.getY(n1);
		double x2 = m.getX(n3) - m.getX(n2);
		double y2 = m.getY(n3) - m.getY(n2);
		//get the length of the vectors
		double mag1 = Math.sqrt(x1*x1+y1*y1);
		double mag2 = Math.sqrt(x2*x2+y2*y2);
		//and the angles
		double theta1 = Math.acos(x1/mag1);
		double theta2 = Math.acos(x2/mag2);
		if(y1 < 0)
			theta1 = Math.PI*2 - theta1;
		if(y2 < 0)
			theta2 = Math.PI*2 - theta2;
		double theta = theta2-theta1;
		if(theta < 0)
			theta += Math.PI*2;
		//just a hack here - should be merged above this
		if(theta > Math.PI)
			theta = Math.PI*2 - theta;
		else
			theta = -theta;
		return theta;
	}

	/**
	 * Gets the centre of a building from its apexes.
	 * Not a smart algorithm.
	 * @param api The apexes of the building.
	 * @return An integer array of form {x,y} giving the centre coordinates.
	 **/
	public static int[] centre(int[][] api){
		int[] centre = new int[2];
		for(int i = 0; i < api.length-1; i++){
			centre[0] += api[i][0];
			centre[1] += api[i][1];
		}
		centre[0] /= (api.length-1);
		centre[1] /= (api.length-1);
		return centre;
	}

	/**
	 * Find the area of a building (in 100ths of sqr meters) from the apexes.
	 * The apexes are assumed to double up on first and last vertices.
	 * @param api The apexes of the building.
	 * @return The area.
	 **/
	public static int area(int[][] api){
		long area = 0;
		long[] distances = new long[api.length];
		for(int i = 0; i < api.length; i++){
			//use point 0,0 as reference for triangles - compute 'em!
			long a = (long)api[i][0];
			long b = (long)api[i][1];
			distances[i] = (long)Math.sqrt(a*a+b*b);
		}
		//now do the areas

		for(int i = 0; i < api.length-1; i++){
			long ar = tArea(api[i][0],api[i][1],distances[i],api[i+1][0],api[i+1][1],distances[i+1]);
			if(isLeft(0,0,api[i][0],api[i][1],api[i+1][0],api[i+1][1]))
				area -= ar;
			else
				area += ar;
		}
		area /= 10000;
		return (int)area;
	}

	/**
	 * Area of the triangle formed by two vectors.
	 * @param x1 x coordinate of the first vector.
	 * @param y1 y coordinate of the first vector.
	 * @param m1 The magnitude of the first vector.
	 * @param x2 x coordinate of the second vector.
	 * @param y2 y coordinate of the second vector.
	 * @param m2 The magnitude of the second vector.
	 * @return The area of the triangle.
	 **/
	public static long tArea(int x1, int y1, long m1, int x2, int y2, long m2){
		double theta1 = 0;
		double theta2 = 0;
		if(m1 != 0)
			theta1 = Math.asin(y1*1.0/m1);
		if(m2 != 0)
			theta2 = Math.asin(y2*1.0/m2);
		if(theta1-theta2 == 0)
			return 0;
		double a = m1*m2*Math.sin(theta1-theta2)/2;
		return (long)Math.abs(a);
	}

	/**
	 * Whether a point is left of a directed line.
	 * @param x The x coordinate of the point.
	 * @param y The y coordinate of the point.
	 * @param ax The x coordinate of the first point of the line.
	 * @param ay The y coordinate of the first point of the line.
	 * @param bx The x coordinate of the second point of the line.
	 * @param by The y coordinate of the second point of the line.
	 * @return True if x,y is to the left of the line, otherwise false.
	 **/
	public static boolean isLeft(int x, int y, int ax, int ay, int bx, int by){
		//make a the origin
		x -= ax;
		bx -= ax;
		y -= by;
		by -= ay;
		//get the normal to a-b
		long nx = (long)-by;
		long ny = (long)bx;
		//find magnitude of projection of x,y onto the normal
		long m = nx*x + ny*y;
		if(m > 0)
			return true;
		return false;
	}

	/**
	 * Gets the closest point on a road to a given point.
	 * @param m The RescueMap holding the road.
	 * @param x The x coordinate of the point.
	 * @param y The y coordinate of the point.
	 * @param n1 The first node of the road.
	 * @param n2 The second node of the road.
	 * @return The coordinates of the nearest point as an array of form {x,y}.
	 **/
	public static int[] nearestPoint(RescueMap m, int x, int y, int n1, int n2){
		//use n1 as origin
		long x2 = m.getX(n2) - m.getX(n1);
		long y2 = m.getY(n2) - m.getY(n1);
		x -= m.getX(n1);
		y -= m.getY(n1);
		//project x,y onto n2
		int d = (int)Math.sqrt(x2*x2+y2*y2);
		int len = (int)((x*x2 + y*y2)/d);
		//check for points out of the road's bounds
		if(len >= d)
			return new int[]{m.getX(n2),m.getY(n2)};
		if(len <= 0)
			return new int[]{m.getX(n1),m.getY(n1)};

		double alt = len*1.0/d;

		return new int[]{(int)(x2*alt)+m.getX(n1),(int)(y2*alt)+m.getY(n1)};
	}

	/**
	 * Splits a road to make a new entrance node. This will be the nearest
	 * point on the nearest road in the map.
	 * @param rm The RescueMap to add the entrance node to.
	 * @param centre The centre of the building that needs an entrance.
	 * @return The new entrance node.
	 **/
	public static int makeEntrance(RescueMap rm, int[] centre){
		int[][] roads = rm.getRoads();
		int[] min = null;
		int minD = Integer.MAX_VALUE;
		int ind = -1;
		for(int i = 0; i < roads.length; i++){
			int[] p = RescueMapToolkit.nearestPoint(rm,centre[0],centre[1],roads[i][0],roads[i][1]);
			long dx = centre[0]-p[0];
			long dy = centre[1]-p[1];
			int d = (int)Math.sqrt(dx*dx + dy*dy);
			if(minD > d){
				min = p;
				minD = d;
				ind = i;
			}
		}
		//check to see if we are using an existing node
		if(min[0] == rm.getX(roads[ind][0]) && min[1] == rm.getY(roads[ind][0]))
			return roads[ind][0];
		if(min[0] == rm.getX(roads[ind][1]) && min[1] == rm.getY(roads[ind][1]))
			return roads[ind][1];
		//make a new node at the closest point
		rm.addNode();
		int nodeCount = rm.getNodeCount();
		rm.setX(nodeCount-1,min[0]);
		rm.setY(nodeCount-1,min[1]);
		rm.setRoad(roads[ind][0],nodeCount-1,rm.getRoad(roads[ind][0],roads[ind][1]));
		rm.setRoad(nodeCount-1,roads[ind][0],rm.getRoad(roads[ind][1],roads[ind][0]));
		rm.setRoad(roads[ind][1],nodeCount-1,rm.getRoad(roads[ind][1],roads[ind][0]));
		rm.setRoad(nodeCount-1,roads[ind][1],rm.getRoad(roads[ind][0],roads[ind][1]));
		rm.setRoad(roads[ind][1],roads[ind][0],0);
		rm.setRoad(roads[ind][0],roads[ind][1],0);
		return nodeCount-1;
	}

	/**
	 * Splits a building across a line. The building should not yet be added
	 * to a RescueMap.
	 * @param build The apexes of the building.
	 * @param x1 The x coordinates of the first point on the line.
	 * @param y1 The y coordinates of the first point on the line.
	 * @param x2 The x coordinates of the second point on the line.
	 * @param y2 The y coordinates of the second point on the line.
	 * @return The two new polygons. An array of size [2][polygonsize][coordinates].
	 **/
	public static int[][][] split(int[][] build, int x1, int y1, int x2, int y2){
		//find two points to split by
		int splitA = -1; //indices of sides to split
		int splitB = -1;
		int[] spA = null;
		int[] spB = null;

		for(int i = 0; i < build.length-1; i++){
			int[] coords = intersectionI(x1,y1,x2,y2,build[i][0],build[i][1],build[i+1][0],build[i+1][1]);
			if(coords[0] != -1){ //found a splitting side!
				if(splitA == -1){
					splitA = i;
					spA = coords;
				}
				else if(splitB == -1){
					splitB = i;
					spB = coords;
					break; //found two splits - this will do
				}
			}
		}
		if(spB == null)
			return null;

		//now generate the two polygons
		int[][][] split = new int[2][][];
		split[0] = new int[splitB-splitA+3][2];
		split[1] = new int[build.length - (splitB-splitA) + 2][2];
		//make the first poly (anti clockwise)
		int ind = 0;
		for(int i = splitA+1; i <= splitB; i++){
			split[0][ind][0] = build[i][0];
			split[0][ind++][1] = build[i][1];
		}
		split[0][split[0].length-3] = spB;
		split[0][split[0].length-2] = spA;
		split[0][split[0].length-1][0] = split[0][0][0];
		split[0][split[0].length-1][1] = split[0][0][1];
		//now make 2nd poly (anti clockwise)
		for(ind = 0; ind <= splitA; ind++){
			split[1][ind][0] = build[ind][0];
			split[1][ind][1] = build[ind][1];
		}
		split[1][ind][0] = spA[0];
		split[1][ind++][1] = spA[1];
		split[1][ind][0] = spB[0];
		split[1][ind++][1] = spB[1];
		for(int i = splitB+1; i < build.length; i++){
			split[1][ind][0] = build[i][0];
			split[1][ind++][1] = build[i][1];
		}
		return split;
	}

	/**
	 * Finds if an intersection of a line with a polygon exists.
	 * @param x1 x coordinate of the first point on the line.
	 * @param y1 y coordinate of the first point on the line.
	 * @param x2 x coordinate of the second point on the line.
	 * @param y2 y coordinate of the second point on the line.
	 * @param api The apexes of the polygon to check.
	 * @return True if an intersection exists, otherwise false.
	 **/
	public static boolean intersects(int x1, int y1, int x2, int y2, int[][] api){
		for(int i = 0; i < api.length-1; i++){
			int x3 = api[i][0];
			int x4 = api[i+1][0];
			int y3 = api[i][1];
			int y4 = api[i+1][1];
			//get intersection
			if(intersection(x1,y1,x2,y2,x3,y3,x4,y4)[0] != -1)
				return true;
		}
		return false;
	}

	/**
	 * Whether two line segments intersect.
	 * @param x1 x coordinate of the first point on the first line.
	 * @param y1 y coordinate of the first point on the first line.
	 * @param x2 x coordinate of the second point on the first line.
	 * @param y2 y coordinate of the second point on the first line.
	 * @param x3 x coordinate of the first point on the second line.
	 * @param y3 y coordinate of the first point on the second line.
	 * @param x4 x coordinate of the second point on the second line.
	 * @param y4 y coordinate of the second point on the second line.
	 **/
	private static int[] intersection(int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4){
		long d = ((long)(y4-y3))*(x2-x1) - ((long)(x4-x3))*(y2-y1);
		if(d == 0)
			return new int[]{-1,-1}; //parallel lines
		double a = (((long)(x4-x3))*(y1-y3) - ((long)(y4-y3))*(x1-x3))*1.0/d;
		int x = x1+(int)(a*(x2-x1));
		int y = y1+(int)(a*(y2-y1));
		//check they are within the bounds of the lines
		if(x >= Math.min(x1,x2) && x <= Math.max(x1,x2)
		&& x <= Math.max(x3,x4) && x >= Math.min(x3,x4)
		&& y >= Math.min(y1,y2) && y <= Math.max(y1,y2)
		&& y <= Math.max(y3,y4) && y >= Math.min(y3,y4)) //they cross!!
			return new int[]{x,y};
		return new int[]{-1,-1};
	}

	/**
	 * Whether an infinite line and a line segment intersect.
	 * @param x1 x coordinate of the first point on the infinite line.
	 * @param y1 y coordinate of the first point on the infinite line.
	 * @param x2 x coordinate of the second point on the infinite line.
	 * @param y2 y coordinate of the second point on the infinite line.
	 * @param x3 x coordinate of the first point on the line segment.
	 * @param y3 y coordinate of the first point on the line segment.
	 * @param x4 x coordinate of the second point on the line segment.
	 * @param y4 y coordinate of the second point on the line segment.
	 **/
	private static int[] intersectionI(int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4){
		long d = ((long)(y4-y3))*(x2-x1) - ((long)(x4-x3))*(y2-y1);
		if(d == 0)
			return new int[]{-1,-1}; //parallel lines
		double a = (((long)(x4-x3))*(y1-y3) - ((long)(y4-y3))*(x1-x3))*1.0/d;
		int x = x1+(int)(a*(x2-x1));
		int y = y1+(int)(a*(y2-y1));
		//check they are within the bounds of the finite line
		if(x <= Math.max(x3,x4) && x >= Math.min(x3,x4)
		&& y <= Math.max(y3,y4) && y >= Math.min(y3,y4)) //they cross!!
			return new int[]{x,y};
		return new int[]{-1,-1};
	}
}
