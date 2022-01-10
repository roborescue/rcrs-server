/*
 * Last change: $Date: 2004/07/11 22:51:53 $
 * $Revision: 1.6 $
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

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * An internal representation of a city map used in Robocup Rescue simulation.
 * This includes information on nodes, roads and buildings.
 *
 * @author Jonathan Teutenberg
 * @version 1.0 Aug 2003
 **/
public class RescueMap {
  /** The number of potential intersections. **/
  private int nodeCount;
  /** The number of roads / connections in the graph. **/
  private int roadCount;
  /** The x coordinates of each node. **/
  private int[] xs;
  /** The y coordinates of each node. **/
  private int[] ys;
  /** The adjacency matrix of the graph. **/
  private int[][] roads;
  /** Approximate width of the map. **/
  private int width;
  /** Approximate height of the map. **/
  private int height;

  /** In-degree of the nodes. **/
  private int[] inDegree;
  /** Out-degree of the nodes. **/
  private int[] outDegree;
  /** All Euclidean distances between nodes. **/
  private int[][] distances;
  /** Whether the distance to or from a node must be recomputed. **/
  private boolean[] invalid;

  /** x coordinates of each building centre. **/
  private int[] buildingXs;
  /** y coordinates of each building centre. **/
  private int[] buildingYs;
  /** Coordinates of each building's apexes. **/
  private int[][][] buildingApi;
  /** A single entrance for each building. **/
  private int[] buildingEntrances;
  /** Number of floors for each building. **/
  private int[] buildingFloors;
  /** Type of each building. **/
  private int[] buildingTypes;
  /** Number of buildings. **/
  private int buildingCount;

  /**
   * Constructs new RescueMap.
   *
   * @param nodeCount The number of potential intersections.
   * @param width     The approximate width of the map.
   * @param height    The approximate height of the map.
   **/
  public RescueMap(int nodeCount, int width, int height) {
    this.width = width;
    this.height = height;
    this.nodeCount = nodeCount;
    xs = new int[nodeCount * 2];
    ys = new int[nodeCount * 2];
    inDegree = new int[nodeCount * 2];
    outDegree = new int[nodeCount * 2];
    roads = new int[nodeCount * 2][nodeCount * 2];
    distances = new int[nodeCount * 2][nodeCount * 2];
    invalid = new boolean[nodeCount * 2];
    roadCount = 0;

    buildingXs = new int[nodeCount * 2];
    buildingYs = new int[nodeCount * 2];
    buildingEntrances = new int[nodeCount * 2];
    buildingApi = new int[nodeCount * 2][][];
    buildingFloors = new int[nodeCount * 2];
    buildingTypes = new int[nodeCount * 2];
  }

  /**
   * Set the x coordinate of the node.
   *
   * @param node The node to change.
   * @param x    The new x value.
   **/
  public void setX(int node, int x) {
    if (x != xs[node])
      invalid[node] = true;
    xs[node] = x;
  }

  /**
   * Set the y coordinate of the node.
   *
   * @param node The node to change.
   * @param y    The new y value.
   **/
  public void setY(int node, int y) {
    if (y != ys[node])
      invalid[node] = true;
    ys[node] = y;
  }

  /**
   * Add, remove or resize a road between two nodes.
   *
   * @param a    The 'from' node.
   * @param b    The 'to' node.
   * @param size The number of lanes.
   **/
  public void setRoad(int a, int b, int size) {
    if (size != 0 && roads[a][b] == 0) {
      roadCount++;
      inDegree[b]++;
      outDegree[a]++;
    } else if (size == 0 && roads[a][b] != 0) {
      roadCount--;
      inDegree[b]--;
      outDegree[a]--;
    }
    roads[a][b] = size;
  }

  /**
   * Get the number of nodes in this map.
   *
   * @return The number of nodes.
   **/
  public int getNodeCount() {
    return nodeCount;
  }

  /**
   * Get the x coordinate of a node.
   *
   * @param node The node to get info from.
   * @return The x coordinate.
   **/
  public int getX(int node) {
    return xs[node];
  }

  /**
   * Get the y coordinate of a node.
   *
   * @param node The node to get info from.
   * @return The y coordinate.
   **/
  public int getY(int node) {
    return ys[node];
  }

  /**
   * Get the width of the map.
   *
   * @return The approcimate width.
   **/
  public int getWidth() {
    return width;
  }

  /**
   * Get the height of the map.
   *
   * @return The approximate height.
   **/
  public int getHeight() {
    return height;
  }

  /**
   * Gets the indegree of a node.
   *
   * @param node The node to get info from.
   * @return The indegree.
   **/
  public int getInDegree(int node) {
    return inDegree[node];
  }

  /**
   * Gets the outdegree of a node.
   *
   * @param node The node to get info from.
   * @return The outdegree.
   **/
  public int getOutDegree(int node) {
    return outDegree[node];
  }

  /**
   * Gets the size of the road between two nodes.
   *
   * @param n1 The 'from' node.
   * @param n2 The 'to' node.
   * @return The number of lanes on the road.
   **/
  public int getRoad(int n1, int n2) {
    return roads[n1][n2];
  }

  /**
   * Adds a node to the map.
   **/
  public void addNode() {
    if (nodeCount + 1 > xs.length)
      resize();
    nodeCount++;
  }

  /**
   * Adds a new building. Uses the RescueMapToolit function centre() to find the
   * centre.
   *
   * @param api      The apexes of the building.
   * @param entrance The entrance node.
   **/
  public void addBuilding(int[][] api, int entrance, int floors, int type) {
    if (buildingCount + 1 > buildingXs.length)
      resizeBs();
    buildingCount++;
    buildingApi[buildingCount - 1] = new int[api.length][2];
    for (int i = 0; i < api.length; i++) {
      buildingApi[buildingCount - 1][i][0] = api[i][0];
      buildingApi[buildingCount - 1][i][1] = api[i][1];

    }
    int[] cent = RescueMapToolkit.centre(api);
    buildingXs[buildingCount - 1] = cent[0];
    buildingYs[buildingCount - 1] = cent[1];

    buildingEntrances[buildingCount - 1] = entrance;
    buildingFloors[buildingCount - 1] = floors;
    buildingTypes[buildingCount - 1] = type;
  }

  /**
   * Get a list of roads in the underlying graph.
   *
   * @return Roads in an array of size [roads][2], with the second dimension
   *         holing x,y coodinates.
   **/
  public int[][] getRoads() {
    ArrayList rs = new ArrayList(nodeCount * 4);
    for (int i = 0; i < nodeCount; i++)
      for (int j = i + 1; j < nodeCount; j++)
        if (roads[i][j] > 0 || roads[j][i] > 0) {
          rs.add(Integer.valueOf(j));
          rs.add(Integer.valueOf(i));
        }
    int[][] rds = new int[rs.size() / 2][2];
    for (int i = 0; i < rs.size(); i += 2) {
      rds[i / 2][0] = ((Integer) rs.get(i)).intValue();
      rds[i / 2][1] = ((Integer) rs.get(i + 1)).intValue();
    }
    return rds;
  }

  /**
   * Get a list of neighbours of a node.
   *
   * @param node The node to get the neighbours of.
   * @return A list of neighbouring nodes.
   **/
  public int[] getNeighbours(int node) {
    ArrayList ns = new ArrayList(10);// should be plenty
    for (int i = 0; i < nodeCount; i++)
      if (roads[node][i] > 0)
        ns.add(Integer.valueOf(i));
    int[] nbs = new int[ns.size()];
    for (int i = 0; i < ns.size(); i++)
      nbs[i] = ((Integer) ns.get(i)).intValue();
    return nbs;
  }

  /**
   * Get a list of neighbours of a node in the underlying graph.
   *
   * @param node The node to get the neighbours of.
   * @return A list of neighbouring nodes in the underlying graph.
   **/
  public int[] getUnderlyingNeighbours(int node) {
    ArrayList ns = new ArrayList(10);// should be plenty
    for (int i = 0; i < nodeCount; i++)
      if (roads[node][i] > 0 || roads[i][node] > 0)
        ns.add(Integer.valueOf(i));
    int[] nbs = new int[ns.size()];
    for (int i = 0; i < ns.size(); i++)
      nbs[i] = ((Integer) ns.get(i)).intValue();
    return nbs;
  }

  /**
   * Get a list of edge IDs for a node.
   *
   * @param bIds The IDs assigned to each building.
   * @param rIds The IDs assigned to each road.
   * @param n    The node to find the edges of.
   * @return A list of the IDs of all adjacent objects.
   **/
  public int[] edges(int[] bIds, int[] rIds, int[][] rs, int n) {
    ArrayList es = new ArrayList(10);
    for (int i = 0; i < buildingCount; i++)
      if (buildingEntrances[i] == n)
        es.add(Integer.valueOf(bIds[i]));
    for (int i = 0; i < rs.length; i++)
      if (rs[i][0] == n || rs[i][1] == n)
        es.add(Integer.valueOf(rIds[i]));
    int[] edges = new int[es.size()];
    for (int i = 0; i < edges.length; i++)
      edges[i] = ((Integer) es.get(i)).intValue();
    return edges;
  }

  /**
   * Writes the RescueMap to files in the current directory.
   */
  public void toFile() throws IOException {
    toFile(null);
  }

  /**
   * Writes this RescueMap to three files - road.bin, building.bin and node.bin in
   * the given parent directory
   **/
  public void toFile(File parentDir) throws IOException {
    // do road.bin
    File road = new File(parentDir, "road.bin");
    DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(road)));

    writeInt(out, 0);
    writeInt(out, 0);
    writeInt(out, 0);
    int[][] rds = getRoads();
    writeInt(out, rds.length);
    int[] rIds = new int[rds.length];
    int id = nodeCount + 2;
    for (int i = 0; i < rds.length; i++) {
      writeInt(out, 17); // size
      rIds[i] = id;
      writeInt(out, id++); // id
      writeInt(out, rds[i][0] + 1); // head
      writeInt(out, rds[i][1] + 1); // tail
      writeInt(out, distance(rds[i][0], rds[i][1])); // length
      writeInt(out, 0); // kind
      writeInt(out, 0); // cars to head
      writeInt(out, 0); // cars to tail
      writeInt(out, 0); // humans to head
      writeInt(out, 0); // humans to tail
      writeInt(out, (roads[rds[i][1]][rds[i][0]] + roads[rds[i][0]][rds[i][1]]) * 2000);// width
      writeInt(out, 0); // block
      writeInt(out, 0); // repair cost
      writeInt(out, 0); // median strip
      writeInt(out, roads[rds[i][1]][rds[i][0]]); // lines to head
      writeInt(out, roads[rds[i][0]][rds[i][1]]); // lines to tail
      writeInt(out, 0); // width for walkers... sorry peds
    }
    out.flush();
    out.close();

    // do building.bin
    File building = new File(parentDir, "building.bin");
    out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(building)));

    int[] bIds = new int[buildingCount];
    writeInt(out, 0);
    writeInt(out, 0);
    writeInt(out, 0);
    writeInt(out, buildingCount);
    for (int i = 0; i < buildingCount; i++) {
      writeInt(out, 16 + buildingApi[i].length * 2); // size
      bIds[i] = id;
      writeInt(out, id++); // id
      writeInt(out, buildingXs[i]); // x
      writeInt(out, buildingYs[i]); // y
      writeInt(out, buildingFloors[i]); // floors
      writeInt(out, buildingTypes[i]); // type
      writeInt(out, 0); // ignition
      writeInt(out, 0); // fieryness
      writeInt(out, 0); // brokenness
      writeInt(out, 1); // entrances - only 1
      writeInt(out, buildingEntrances[i] + 1); // entrance
      writeInt(out, 0); // shape
      int area = RescueMapToolkit.area(buildingApi[i]);
      writeInt(out, area); // floor area
      writeInt(out, area * buildingFloors[i]); // total area
      writeInt(out, 0); // building code
      writeInt(out, buildingApi[i].length); // num apexes
      for (int j = 0; j < buildingApi[i].length; j++) {
        writeInt(out, buildingApi[i][j][0]); // x
        writeInt(out, buildingApi[i][j][1]); // y
      }
    }
    out.flush();
    out.close();

    // do node.bin
    File node = new File(parentDir, "node.bin");
    out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(node)));

    writeInt(out, 0);
    writeInt(out, 0);
    writeInt(out, 0);
    writeInt(out, nodeCount); // number of nodes
    for (int i = 0; i < nodeCount; i++) {
      // build the edges list
      int[] edges = edges(bIds, rIds, rds, i);
      writeInt(out, 6 + 7 * edges.length);
      writeInt(out, i + 1); // id
      writeInt(out, xs[i]); // x
      writeInt(out, ys[i]); // y
      writeInt(out, edges.length); // edges
      for (int j = 0; j < edges.length; j++)
        writeInt(out, edges[j]); // object id
      writeInt(out, 0); // signal
      for (int j = 0; j < edges.length; j++)
        writeInt(out, 0); // shortcut
      for (int j = 0; j < edges.length; j++) {
        writeInt(out, 0); // pocket
        writeInt(out, 0);
      }
      for (int j = 0; j < edges.length; j++) {
        writeInt(out, 0); // signal timing
        writeInt(out, 0);
        writeInt(out, 0);
      }
    }
    out.flush();
    out.close();

  }

  /**
   * Writes an int to the stream.
   *
   * @param out The output stream.
   * @param n   The integer to write.
   **/
  private void writeInt(DataOutputStream out, int n) throws IOException {
    int result = ((n & 0xFF) << 24) | ((n & 0xFF00) << 8) | ((n & 0xFF0000) >> 8) | ((n & 0xFF000000) >> 24);
    out.writeInt(result);
  }

  /**
   * Gets the Euclidean distance between two nodes.
   *
   * @param n1 The first node.
   * @param n2 The second node.
   * @return The distance between them.
   **/
  public int distance(int n1, int n2) {
    if (invalid[n1]) {
      for (int i = 0; i < nodeCount; i++) {
        distances[n1][i] = 0;
        distances[i][n1] = 0;
      }
      invalid[n1] = false;
    }
    if (invalid[n2]) {
      for (int i = 0; i < nodeCount; i++) {
        distances[n2][i] = 0;
        distances[i][n2] = 0;
      }
      invalid[n2] = false;
    }
    if (distances[n1][n2] == 0) {
      long x = xs[n1] - xs[n2];
      long y = ys[n1] - ys[n2];
      distances[n1][n2] = (int) Math.sqrt(x * x + y * y);
      distances[n2][n1] = distances[n1][n2];
    }
    return distances[n1][n2];
  }

  /**
   * Checks whether a path exists between two nodes.
   *
   * @param start The node to start the path from.
   * @param end   The node we wish to reach.
   * @return True if a path exists, otherwise false.
   **/
  public boolean pathExists(int start, int end) {
    boolean[] visited = new boolean[nodeCount];
    // find a path with a greedy search
    PairHeap q = new PairHeap();
    visited[start] = true;
    int next = start;
    try {
      while (next != end) {
        // check every neighbour of next - add if necessary
        for (int j = 0; j < nodeCount; j++) {
          if (roads[next][j] > 0 && !visited[j]) { // connected
            visited[j] = true;
            int x = xs[j] - xs[end];
            int y = ys[j] - ys[end];
            int d = (int) Math.sqrt(x * x + y * y);
            q.insert(j, d);
          }
        }
        next = q.deleteMin();
      }
      return true;
    } catch (NullPointerException e) {
      return false;
    }
  }

  /**
   * Finds the lowest x and y coordinates and aligns the map so these have value
   * 1.
   **/
  public void align() {
    // get min values
    int x = Integer.MAX_VALUE;
    int y = x;
    for (int i = 0; i < nodeCount; i++) {
      if (xs[i] < x)
        x = xs[i];
      if (ys[i] < y)
        y = ys[i];
    }
    for (int i = 0; i < buildingCount; i++) {
      if (buildingXs[i] < x)
        x = buildingXs[i];
      if (buildingYs[i] < y)
        y = buildingYs[i];
      for (int j = 0; j < buildingApi[i].length; j++) {
        if (buildingApi[i][j][0] < x)
          x = buildingApi[i][j][0];
        if (buildingApi[i][j][1] < y)
          y = buildingApi[i][j][1];
      }
    }
    // now alter them all
    int dx = x - 1;
    int dy = y - 1;
    System.out.println("Shifting by (" + (-dx) + "," + (-dy) + ").");
    for (int i = 0; i < nodeCount; i++) {
      xs[i] -= dx;
      ys[i] -= dy;
    }
    for (int i = 0; i < buildingCount; i++) {
      buildingXs[i] -= dx;
      buildingYs[i] -= dy;
      for (int j = 0; j < buildingApi[i].length; j++) {
        buildingApi[i][j][0] -= dx;
        buildingApi[i][j][1] -= dy;
      }
    }
  }

  /**
   * Adjusts the data structures when number of nodes gets too large.
   **/
  private void resize() {
    int newSize = (xs.length * 3) / 2;
    int[] newXs = new int[newSize];
    int[] newYs = new int[newSize];
    int[] newIn = new int[newSize];
    int[] newOut = new int[newSize];
    int[][] newRoads = new int[newSize][newSize];
    int[][] newDist = new int[newSize][newSize];
    boolean[] newVal = new boolean[newSize];
    System.arraycopy(xs, 0, newXs, 0, nodeCount);
    System.arraycopy(ys, 0, newYs, 0, nodeCount);
    System.arraycopy(inDegree, 0, newIn, 0, nodeCount);
    System.arraycopy(outDegree, 0, newOut, 0, nodeCount);
    System.arraycopy(invalid, 0, newVal, 0, nodeCount);
    for (int i = 0; i < nodeCount; i++) {
      System.arraycopy(roads[i], 0, newRoads[i], 0, nodeCount);
      System.arraycopy(distances[i], 0, newDist[i], 0, nodeCount);
    }
    xs = newXs;
    ys = newYs;
    inDegree = newIn;
    outDegree = newOut;
    roads = newRoads;
    distances = newDist;
    invalid = newVal;
  }

  /**
   * Adjusts the data structures when number of buildings gets too large.
   **/
  private void resizeBs() {
    int newSize = (buildingXs.length * 2);
    int[] newXs = new int[newSize];
    int[] newYs = new int[newSize];
    int[] newEnt = new int[newSize];
    int[] newFloors = new int[newSize];
    int[] newTypes = new int[newSize];
    int[][][] newApi = new int[newSize][][];
    System.arraycopy(buildingXs, 0, newXs, 0, buildingCount);
    System.arraycopy(buildingYs, 0, newYs, 0, buildingCount);
    System.arraycopy(buildingEntrances, 0, newEnt, 0, buildingCount);
    System.arraycopy(buildingFloors, 0, newFloors, 0, buildingCount);
    System.arraycopy(buildingTypes, 0, newTypes, 0, buildingCount);
    for (int i = 0; i < buildingCount; i++)
      newApi[i] = buildingApi[i];

    buildingXs = newXs;
    buildingYs = newYs;
    buildingEntrances = newEnt;
    buildingFloors = newFloors;
    buildingTypes = newTypes;
    buildingApi = newApi;
  }
}