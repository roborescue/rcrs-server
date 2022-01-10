/*
 * Last change: $Date: 2004/05/04 03:09:39 $
 * $Revision: 1.2 $
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * An abstract class that provides functionality for adding buildings to a
 * RescueMap one city block at a time.
 *
 * @author Jonathan Teutenberg
 * @version 1.0 Aug 2003
 **/
public abstract class BlocksBuildingGenerator implements BuildingGenerator {
  /** The outer ring of roads. **/
  private int[] outerBlock;

  /**
   * Adds the buildings.
   *
   * @param rm           The RescueMap to add to.
   * @param uniformity   The regularity amongst buildings.
   * @param buildDensity The density of the buildings.
   * @param rand         The random number generator.
   **/
  public void addBuildings(RescueMap rm, int uniformity, int buildDensity, Random rand) {
    int[][] blocks = getBlocks(rm);
    System.out.println("Filling blocks");
    for (int i = 0; i < blocks.length; i++) {
      int[][][] builds = fillBlock(rm, blocks[i], buildDensity, rand);
      for (int j = 0; j < builds.length; j++) {
        int[] centre = RescueMapToolkit.centre(builds[j]);
        rm.addBuilding(builds[j], RescueMapToolkit.makeEntrance(rm, centre), (int) (rand.nextDouble() * 4) + 1, 0);
      }
      System.out.println(i + " of " + blocks.length);
    }
    int[][][] builds = fillOuterBlock(rm, outerBlock, buildDensity, rand);
    for (int j = 0; j < builds.length; j++) {
      int[] centre = RescueMapToolkit.centre(builds[j]);
      rm.addBuilding(builds[j], RescueMapToolkit.makeEntrance(rm, centre), (int) (rand.nextDouble() * 4) + 1, 0);
    }
  }

  /**
   * Give a list of buildings for a block. This should not do the adding of
   * buildings.
   *
   * @param rm           The RescueMap containing the block.
   * @param block        A list of nodes in anti-clockwise order around the block.
   * @param buildDensity The density of buildings to add.
   * @param rand         The random number generator.
   * @return Buildings to add as an array of form [building][apex][coordinate]
   **/
  protected abstract int[][][] fillBlock(RescueMap rm, int[] block, int buildDensity, Random rand);

  /**
   * Give a list of buildings for the outside of the city.
   *
   * @param rm           The RescueMap containing the block.
   * @param block        A list of nodes in anti-clockwise order around the block.
   * @param buildDensity The density of buildings to add.
   * @param rand         The random number generator.
   * @return Buildings to add as an array of form [building][apex][coordinate]
   **/
  protected abstract int[][][] fillOuterBlock(RescueMap rm, int[] block, int buildDensity, Random rand);

  /**
   * Gets a list of city blocks.
   *
   * @param m The RescueMap to find the blocks of.
   * @return Blocks as an array of arrays of nodes: [blocks][nodes]
   **/
  private int[][] getBlocks(RescueMap m) {
    int[][] rs = m.getRoads();
    HashMap visited = new HashMap();
    ArrayList blocks = new ArrayList(100);
    for (int i = 0; i < rs.length; i++) {
      Integer vis = (Integer) visited.get(hash(rs[i]));
      int v = 0;
      if (vis != null)
        v = vis.intValue();
      if ((v & 1) == 0) {
        v = v | 1;
        visited.put(hash(rs[i]), Integer.valueOf(v));
        // walk head-tail circuit
        int[] c = walkCircuit(m, rs[i], visited, true);
        if (c.length > 0)
          blocks.add(c);
      }
      if ((v & 2) == 0) {
        v = v | 2;
        visited.put(hash(rs[i]), Integer.valueOf(v));
        // walk tail-head circuit
        int[] c = walkCircuit(m, rs[i], visited, false);
        if (c.length > 0)
          blocks.add(c);
      }
    }
    int[][] blks = new int[blocks.size()][];
    blocks.toArray(blks);
    return blks;
  }

  /**
   * Creates a hash key for a road.
   **/
  private static Integer hash(int[] road) {
    int a = Math.min(road[0], road[1]);
    int b = Math.max(road[0], road[1]);
    return Integer.valueOf(b * 10000 + a);
  }

  /**
   * Walks around a block in an anti-clockwise direction This also marks each road
   * visited in the 'visited' map.
   *
   * @param curr    The road to begin with.
   * @param visited A mapping from nodes to their visited status (either not
   *                visited, visited in one direction, or visited in both
   *                directions.
   * @param isTail  Whether the next road to visit starts at curr's tail or not.
   * @return An inorder list of nodes in the circuit (block).
   **/
  private int[] walkCircuit(RescueMap m, int[] curr, HashMap visited, boolean isTail) {
    ArrayList circ = new ArrayList(20);
    double totalAngle = 0;
    int t = 0;
    int s = 1;
    if (isTail) {
      t = 1;
      s = 0;
    }
    int[] first = { curr[t], curr[s] };
    circ.add(Integer.valueOf(curr[t]));
    do {
      circ.add(Integer.valueOf(curr[s]));
      int[] nbs = m.getUnderlyingNeighbours(curr[s]);
      // get the neighbour with largest angle
      int ind = 0;
      double maxAngle = -Math.PI;
      double angle;
      for (int k = 0; k < nbs.length; k++) {
        if (nbs[k] == curr[t]) // no going back!
          continue;
        angle = RescueMapToolkit.angle(m, curr[t], curr[s], nbs[k]);
        // we want the smallest angle greater than 180, otherwise the smallest less than
        // 180.
        if (angle > maxAngle) {
          maxAngle = angle;
          ind = k;
        }
      }
      totalAngle += maxAngle;
      // make it current, and set its visited status
      curr[t] = curr[s];
      curr[s] = nbs[ind];
      Integer val = (Integer) visited.get(hash(curr));
      int v = 0;
      if (val != null)
        v = val.intValue();

      if (curr[s] > curr[t])
        visited.put(hash(curr), Integer.valueOf(v | 1));
      else
        visited.put(hash(curr), Integer.valueOf(v | 2));
    } while (curr[s] != first[0]);

    int[] rs = new int[circ.size()];
    for (int i = 0; i < rs.length; i++)
      rs[i] = ((Integer) circ.get(i)).intValue();
    if (totalAngle <= 0) { // wrong direction - happens with boundary roads
      outerBlock = rs;
      return new int[0];
    }
    return rs;
  }
}