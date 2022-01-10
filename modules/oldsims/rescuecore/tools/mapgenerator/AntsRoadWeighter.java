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
import java.util.Collections;
import java.util.Random;

/**
 * A road weighter that decides on road importance by making 10,000 trips
 * through the RescueMap between random pairs of nodes.
 *
 * @author Jonathan Teutenberg
 * @version 1.0 Aug 2003
 **/
public class AntsRoadWeighter implements RoadWeighter {
  /** Number of runs to use to determine main roads. **/
  private static final int RUNS = 15000;
  /** Base percentage of 3 lane roads. **/
  private static final int THREELANE = 10;
  /** Base percentage of 2 lane roads. **/
  private static final int TWOLANE = 20;

  /** A count of how many times each road (adjacent node pair) has been used. **/
  private int[][] usedCount;

  /** The Euclidean distances between all pairs of nodes. **/
  private int[][] distances;

  public void connect(RescueMap rm, int uniformity, boolean nooneway, Random rand) {
    int nodes = rm.getNodeCount();
    // store all the distances
    distances = new int[nodes][nodes];
    for (int i = 0; i < nodes; i++)
      for (int j = 0; j < i; j++) {
        int x = rm.getX(i) - rm.getX(j);
        int y = rm.getY(i) - rm.getY(j);
        distances[i][j] = (int) Math.sqrt(x * x + y * y);
        distances[j][i] = distances[i][j];
      }
    // record how often each road is used
    usedCount = new int[nodes][nodes];
    System.out.print("Simulating road use.");
    System.out.flush();
    int steps = RUNS / 20;
    // two arrays to be used in the searches
    int[] prevs = new int[nodes];
    int[] dists = new int[nodes];
    for (int i = 0; i < RUNS; i++) {
      int[] picked = pickNodes(rm, rand);
      runPath(prevs, dists, rm, picked[0], picked[1]);
      if (i % steps == 0) {
        System.out.print(".");
        System.out.flush();
      }
    }
    System.out.println("done.");

    // find the two cutoffs
    ArrayList l = new ArrayList(nodes * 5);
    for (int i = 0; i < nodes; i++)
      for (int j = 0; j < nodes; j++)
        if (rm.getRoad(i, j) > 0) {
          l.add(Integer.valueOf(usedCount[i][j]));
        }
    Collections.sort(l);
    int index1 = (int) (l.size() * (1 - THREELANE / 100.0));
    int v1 = ((Integer) (l.get(index1))).intValue();
    int v2 = ((Integer) (l.get(index1 - (int) (l.size() * TWOLANE / 100.0)))).intValue();
    // now upgrade the roads
    for (int i = 0; i < nodes; i++)
      for (int j = 0; j < nodes; j++) {
        if (usedCount[i][j] >= v1 || (nooneway && usedCount[j][i] >= v1)) {
          rm.setRoad(i, j, 3);
          if (nooneway)
            rm.setRoad(j, i, 3);
        } else if (usedCount[i][j] >= v2 || (nooneway && usedCount[j][i] >= v2)) {
          rm.setRoad(i, j, 2);
          if (nooneway)
            rm.setRoad(j, i, 2);
        }
      }
  }

  public int[] pickNodes(RescueMap rm, Random rand) {
    return new int[] { (int) (rand.nextDouble() * rm.getNodeCount()), (int) (rand.nextDouble() * rm.getNodeCount()) };
  }

  private void runPath(int[] prevs, int[] dists, RescueMap rm, int start, int end) {
    int nodes = rm.getNodeCount();
    // find shortest path with A*
    for (int i = 0; i < dists.length; i++)
      dists[i] = Integer.MAX_VALUE;
    PairHeap q = new PairHeap();
    prevs[start] = -1;
    dists[start] = distances[start][end];
    int next = start;
    while (next != end) {
      // update every neighbour of next - add if necessary
      for (int j = 0; j < nodes; j++) {
        if (j != next && rm.getRoad(next, j) > 0) { // connected
          int guess = dists[next] + distances[next][j] + distances[j][end];
          if (dists[j] > guess) {
            // update!
            dists[j] = guess;
            prevs[j] = next;
            q.insert(j, guess);
          }
        }
      }
      next = q.deleteMin();
    }
    // now extract the path
    int prev = end;
    while (prevs[prev] != -1) {
      usedCount[prevs[prev]][prev]++;
      prev = prevs[prev];
    }
  }
}