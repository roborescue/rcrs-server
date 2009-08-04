/*
 * Last change: $Date: 2004/05/04 03:09:39 $
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

package rescuecore.view;

import java.util.*;
import rescuecore.*;

public class ConvexHull {
    private int[] xs;
    private int[] ys;
    private int numPoints;
    private HullPoint pivot;

    public ConvexHull(RescueObject[] os, Memory memory) throws CannotFindLocationException {
		if(os.length > 1){
			double[] tempXs = new double[os.length];
			double[] tempYs = new double[os.length];
			for (int i=0;i<os.length;++i) {
				int[] xy = memory.getXY(os[i]);
				tempXs[i] = xy[0];
				tempYs[i] = xy[1];
			}
			int[] hull = convexHull(tempXs,tempYs);
			xs = new int[hull.length];
			ys = new int[hull.length];
			for (int i=0;i<hull.length;++i) {
				xs[i] = (int)(tempXs[hull[i]]);
				ys[i] = (int)(tempYs[hull[i]]);
			}
			numPoints = hull.length;
		}
		else{
			numPoints = os.length;
			xs = new int[numPoints];
			ys = new int[numPoints];
			for(int i = 0; i < numPoints; i++){ //zero or one iterations
				int[] xy = memory.getXY(os[i]);
				xs[i] = xy[0];
				ys[i] = xy[1];
			}
		}
    }

    public int[] getXs() {
		return xs;
    }

    public int[] getYs() {
		return ys;
    }

    public int countPoints() {
		return numPoints;
    }

    /** Return the indices of the points that lie on a convex hull surrounding all points in the set given. The hull is returned starting with the point with highest y-coordinate and moving clockwise */
    private int[] convexHull(double[] xs, double[] ys) {
		// Find the highest y;
		int highest = 0;
		//	    System.out.println("Finding convex hull of "+xs.length+" points");
		HullPoint[] points = new HullPoint[xs.length];
		for (int i=0;i<ys.length;++i) {
			//		System.out.println("Point "+i+" ("+xs[i]+","+ys[i]+")");
			if (ys[i] > ys[highest]) highest = i;
			points[i] = new HullPoint(i,xs[i],ys[i]);
		}
		//	    System.out.println("Highest point: "+highest+" ("+xs[highest]+","+ys[highest]+")");
		// Swap point zero with highest
		HullPoint temp = points[0];
		points[0] = points[highest];
		points[highest] = temp;
		pivot = points[0];
		Arrays.sort(points,new Comparator() {
				public int compare(Object o1, Object o2) {
					if (o1==pivot) return -1;
					if (o2==pivot) return 1;
					double angle = ccw((HullPoint)o1,(HullPoint)o2);
					if (angle>0) return -1;
					return 1;
				}
			});
		HullPoint[] hull = new HullPoint[points.length];
		hull[0] = points[0];
		hull[1] = points[1];
		int hullSize = 2;
		for (int i=2;i<points.length;++i) {
			while (rightTurn(hull[hullSize-2],hull[hullSize-1],points[i])) {
				--hullSize;
			}
			hull[hullSize++] = points[i];
		}
		int[] result = new int[hullSize];
		for (int i=0;i<hullSize;++i) {
			result[i] = hull[i].index;
		}
		return result;
    }

    private boolean rightTurn(HullPoint p1, HullPoint p2) {
		return rightTurn(p1,pivot,p2);
    }

    private boolean rightTurn(HullPoint p1, HullPoint p2, HullPoint p3) {
		//	    if (p1==p2 || p1==p3 || p2==p3) System.err.println("WARNING: Checking for right turn with identical points: "+p1+", "+p2+", "+p3);
		return (p1.x - p2.x) * (p3.y - p2.y) - (p1.y - p2.y) * (p3.x - p2.x) > 0;
    }

    private double ccw(HullPoint p1, HullPoint p2) {
		return ccw(p1,pivot,p2);
    }

    private double ccw(HullPoint p1, HullPoint p2, HullPoint p3) {
		return (p1.x - p2.x) * (p3.y - p2.y) - (p1.y - p2.y) * (p3.x - p2.x);
    }

    private class HullPoint {
		int index;
		double x, y;

		HullPoint(int index, double x, double y) {
			this.index = index;
			this.x = x;
			this.y = y;
		}

		public String toString() {
			return "Point "+index+" ("+x+","+y+")";
		}
    }
}
