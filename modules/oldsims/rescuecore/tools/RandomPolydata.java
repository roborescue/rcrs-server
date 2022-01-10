/*
 * Last change: $Date: 2004/08/10 20:47:10 $
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

package rescuecore.tools;

import java.util.*;
import java.io.*;
import rescuecore.objects.Node;
import rescuecore.objects.Building;

public class RandomPolydata {
	private final static int DEFAULT_CENTERS = 4;
	private final static int DEFAULT_MAX_LEVEL = 4;
	private final static int DEFAULT_MIN_LEVEL = 1;
	private final static int DEFAULT_REPEAT = 1;
	private final static int DEFAULT_MAX_SIZE = 50;
	private final static int DEFAULT_MIN_SIZE = 10;

	private static void printUsage() {
		System.out.println("Usage: RandomPolydata [options]");
		System.out.println("Options");
		System.out.println("=======");
		System.out.println("-n\t--num-centers\tThe number of epicenters");
		System.out.println("-l\t--max-level\tThe maximum magnitude level");
		System.out.println("-e\t--min-level\tThe minimum magnitude level");
		System.out.println("-r\t--repeat-levels\tThe number of times to repeat levels");
		System.out.println("-x\t--max-size\tThe maximum size of the polygons produced by each center as a proportion of the map size (100 means 100% of the map can be covered)");
		System.out.println("-m\t--min-size\tThe minimum size of the polygons produced by each center as a proportion of the map size");
		System.out.println("--box=left,right,top,bottom,magnitude");
	}

	public static void main(String[] args) {
		int numCenters = DEFAULT_CENTERS;
		int maxLevel = DEFAULT_MAX_LEVEL;
		int minLevel = DEFAULT_MIN_LEVEL;
		int repeat = DEFAULT_REPEAT;
		int maxSize = DEFAULT_MAX_SIZE;
		int minSize = DEFAULT_MIN_SIZE;
		Collection boxes = new ArrayList();
		for (int i=0;i<args.length;++i) {
			if (args[i].equalsIgnoreCase("-h") || args[i].equalsIgnoreCase("--help")) {
				printUsage();
				return;
			}
			else if (args[i].equalsIgnoreCase("-n") || args[i].equalsIgnoreCase("--num-centers")) {
				numCenters = Integer.parseInt(args[++i]);
			}
			else if (args[i].equalsIgnoreCase("-l") || args[i].equalsIgnoreCase("--max-level")) {
				maxLevel = Integer.parseInt(args[++i]);
			}
			else if (args[i].equalsIgnoreCase("-e") || args[i].equalsIgnoreCase("--min-level")) {
				minLevel = Integer.parseInt(args[++i]);
			}
			else if (args[i].equalsIgnoreCase("-r") || args[i].equalsIgnoreCase("--repeat-levels")) {
				repeat = Integer.parseInt(args[++i]);
			}
			else if (args[i].equalsIgnoreCase("-x") || args[i].equalsIgnoreCase("--max-size")) {
			    maxSize = Integer.parseInt(args[++i]);
			}
			else if (args[i].equalsIgnoreCase("-m") || args[i].equalsIgnoreCase("--min-size")) {
				minSize = Integer.parseInt(args[++i]);
			}
			else if (args[i].startsWith("--box")) {
				StringTokenizer tokens = new StringTokenizer(args[i].substring(6),",");
				double left = Double.parseDouble(tokens.nextToken());
				double right = Double.parseDouble(tokens.nextToken());
				double top = Double.parseDouble(tokens.nextToken());
				double bottom = Double.parseDouble(tokens.nextToken());
				int magnitude = Integer.parseInt(tokens.nextToken());
				boxes.add(new Box(left,right,top,bottom,magnitude));
			}
		}
		// Find the size of the city
		Node[] allNodes;
		Building[] allBuildings;
		try {
			allNodes = MapFiles.loadNodes();
			allBuildings = MapFiles.loadBuildings();
		}
		catch (IOException e) {
			e.printStackTrace();
			return;
		}
		int minX = allNodes[0].getX();
		int minY = allNodes[0].getY();
		int maxX = minX;
		int maxY = minY;
		for (int i=0;i<allNodes.length;++i) {
			minX = Math.min(minX,allNodes[i].getX());
			maxX = Math.max(maxX,allNodes[i].getX());
			minY = Math.min(minY,allNodes[i].getY());
			maxY = Math.max(maxY,allNodes[i].getY());
		}
		for (int i=0;i<allBuildings.length;++i) {
			minX = Math.min(minX,allBuildings[i].getX());
			maxX = Math.max(maxX,allBuildings[i].getX());
			minY = Math.min(minY,allBuildings[i].getY());
			maxY = Math.max(maxY,allBuildings[i].getY());
		}
		int xRange = maxX-minX;
		int yRange = maxY-minY;

		Polygon[] shindo;
		Polygon[] gal;

		if (boxes.size()==0) {
			List shindoPolygons = new ArrayList();
			List galPolygons = new ArrayList();
			int levelRange = maxLevel-minLevel;
			// Place the epicenters
			for (int i=0;i<numCenters;++i) {
				int centerX = ((int)(Math.random()*xRange)) + minX;
				int centerY = ((int)(Math.random()*yRange)) + minY;
				int level = ((int)(Math.random()*levelRange)) + minLevel;
				int repeats = ((int)(Math.random()*repeat)) + repeat;
				// Generate the polygon
				double xSize = Math.random()*(maxSize-minSize) + minSize;
				int xExtent = (int)(xRange*xSize/100.0);
				double ySize = Math.random()*(maxSize-minSize) + minSize;
				int yExtent = (int)(yRange*ySize/100.0);
				System.out.println("Placing level "+level+" epicenter at "+centerX+","+centerY+" with "+repeats+" repeats. Size is "+xExtent+" x "+yExtent);
				// Let's make it an octagon numbered clockwise from zero at the top
				int[] xs = new int[8];
				int[] ys = new int[8];
				xs[0] = xs[4] = centerX;
				xs[1] = xs[3] = centerX + xExtent/2;
				xs[2] = centerX + xExtent;
				xs[5] = xs[7] = centerX - xExtent/2;
				xs[6] = centerX - xExtent;
				ys[0] = centerY + yExtent;
				ys[1] = ys[7] = centerY + yExtent/2;
				ys[2] = ys[6] = centerY;
				ys[3] = ys[5] = centerY - yExtent/2;
				ys[4] = centerY - yExtent;
				for (int j=0;j<repeat;++j) {
					shindoPolygons.add(new Polygon(level,xs,ys,8));
					galPolygons.add(new Polygon(level,xs,ys,8));
				}
			}
			shindo = new Polygon[shindoPolygons.size()];
			gal = new Polygon[galPolygons.size()];
			shindoPolygons.toArray(shindo);
			galPolygons.toArray(gal);
		}
		else {
			shindo = new Polygon[boxes.size()];
			gal = new Polygon[boxes.size()];
			int i=0;
			System.out.println("World extends from "+minX+","+minY+" to "+maxX+","+maxY);
			for (Iterator it = boxes.iterator();it.hasNext();i++) {
				Box next = (Box)it.next();
				int[] xs = new int[4];
				int[] ys = new int[4];
				int left = (int)((xRange * next.left)+minX);
				int right = (int)((xRange * next.right)+minX);
				int top = (int)((yRange * next.top)+minY);
				int bottom = (int)((yRange * next.bottom)+minY);
				xs[0] = xs[3] = left;
				xs[1] = xs[2] = right;
				ys[0] = ys[1] = top;
				ys[2] = ys[3] = bottom;
				shindo[i] = new Polygon(next.size,xs,ys,4);
				gal[i] = new Polygon(next.size,xs,ys,4);
				System.out.println("Putting box from "+left+","+top+" to "+right+","+bottom);
			}
		}
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("shindopolydata.dat")));
			writePolydata(shindo,out);
			out.flush();
			out.close();
			out = new PrintWriter(new BufferedWriter(new FileWriter("galpolydata.dat")));
			writePolydata(gal,out);
			out.flush();
			out.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void writePolydata(Polygon[] polygons, PrintWriter out) throws IOException {
		// olydata format is:
		// Line 1: <number of values to read, including the <level>,<number of vertices> pairs>,<number of polygons>
		// Each polygon then has:
		// <level>,<number of vertices>
		// Followed by <number of vertices> pairs of coordinates
		int sum = 0;
		for (int i=0;i<polygons.length;++i) {
			sum += polygons[i].numPoints*2;
			sum += 2;
		}
		out.print(sum);
		out.print(",");
		out.println(polygons.length);
		for (int i=0;i<polygons.length;++i) {
			out.print(polygons[i].level);
			out.print(",");
			out.println(polygons[i].numPoints);
			for (int j=0;j<polygons[i].numPoints;++j) {
				out.print(polygons[i].xs[j]);
				out.print(",");
				out.println(polygons[i].ys[j]);
			}
		}
	}

	private static class Polygon {
		int level;
		int[] xs;
		int[] ys;
		int numPoints;

		Polygon(int l, int[] x, int[] y, int num) {
			level = l;
			xs = x;
			ys = y;
			numPoints = num;
		}
	}

	private static class Box {
		double left,right,top,bottom;
		int size;

		public Box(double l, double r, double t, double b, int s) {
			left = l;
			right = r;
			top = t;
			bottom = b;
			size = s;
		}
	}
}
