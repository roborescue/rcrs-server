package traffic3.simulator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Run gets a Graph Object and Runs The Dijkstra From single and Multiple sources .. with methods getPathArray() & getCost You can Get the Results of The Dijkstra !!!!!!!!!IMPORTANT!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! you Can use the Dijkstra object multiple Times without initializing it ... But the First time or when the Graph is changed(number of nodes are changed) you shoould call setGraphSize()
 */
public class Dijkstra {

	private int parent[];
	private long minCost[];
	private int mark[];
	private int numberOfVertex, marker = 1;

	public Dijkstra(int n) { // number of Graph Nodes
		setGraphSize(n);
	}

	public Dijkstra() {
		
	}

	/**
	 * Use This Method only if the Graph Size has Changed ... or its the First Time you need to Use this ...
	 */
	public void setGraphSize(int n) {
		parent = new int[n + 1];
		mark = new int[n + 1];
		minCost = new long[n + 1];
		this.numberOfVertex = n;
	}

	/**
	 * Run multi Src Dijkstra
	 */
	public void Run(int[][]graph1, int src) throws Exception {
		marker++;
		for (int i = 0; i < numberOfVertex; ++i) {
			minCost[i] = Long.MAX_VALUE/2;
		}
		for(int i=0;i<parent.length;i++){
			parent[i] = -1;
		}
		PriorityQueue<Integer> PQ = new PriorityQueue<Integer>(100, new Cmp());
			parent[src] = -1;
			minCost[src] = 0;
			PQ.add(src);
		while (PQ.size() != 0) {
			int node = (PQ.poll());
			if (mark[node] == marker)
				continue;
			else
				mark[node] = marker;
			for (int i = 0; i < graph1.length; i++) {
				if(graph1[node][i]>100000)
					continue;
				if(node==i)
					continue;

				int childIndex = i;
				if (mark[childIndex] == marker)
					continue;
				int w = graph1[node][i];
				if (w <= 0 || minCost[node] + w<0)
					throw new Exception("Negetive Cost");

				if (minCost[childIndex] > minCost[node] + w) {
					minCost[childIndex] = minCost[node] + w;
					parent[childIndex] = node;
					PQ.add(childIndex);
				}
			}
		}
	}

	/**
	 * Get Path From Des to Src ... both Src and Des are included in path
	 */
	public ArrayList<Integer> getpathArray(int desVertex) {
		ArrayList<Integer> ar = new ArrayList<Integer>();
		if (parent[desVertex] ==desVertex) {
			System.err.println("How it executed!===>loop in getpath array");
			return ar;
		}
		if (parent[desVertex] != -1) {
			ar = getpathArray(parent[desVertex]);
		}
		ar.add(desVertex);
		return ar;
	}

	/**
	 * Get Cost From Des to Src ...
	 */
	public long getWeight(int desVertex) {
		if(minCost[desVertex]<0)
			new Error("Cost is negetive....").printStackTrace();
		return minCost[desVertex];
	}

	private class Cmp implements Comparator<Integer> {
		@Override
		public int compare(Integer a, Integer b) {
			if (minCost[a] > minCost[b])
				return 1;
			else if (minCost[a] == minCost[b])
				return 0;
			else
				return -1;
		}
	}
}
