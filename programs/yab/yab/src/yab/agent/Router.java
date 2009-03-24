// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.agent;

import java.util.*;
import yab.agent.object.*;
import yab.agent.Condition;

public class Router {
    /** This method returns the minimum cost route reaching one of
     *  <code>destinations</code> from <code>origin</code> with
     *  estimating cost by <code>costFunc</code>.
     */
    public static Route get(MotionlessObject origin, Collection destinations,
                            CostFunction costFunc) {
        if (destinations.isEmpty())
            throw new Error("the destinations must not be empty.");
        final HashSet dests = destinations instanceof HashSet
            ? (HashSet) destinations : new HashSet(destinations);
        Condition containsCnd = new Condition() {
                public boolean eval(Object obj) {
                    return dests.contains(obj); }};
        return get(origin, containsCnd, costFunc);
    }

    /** @return null when there are no routes reaching an object
     *  satisfying <code>destinationCondition</code>.
     */
    public static Route get(MotionlessObject origin,
                            Condition destinationCondition,
                            CostFunction costFunc) {
        LinkedList open = new LinkedList();
        HashSet visited = new HashSet();
        Route route = new Route(origin);
        open.add(route);
        while (true) {
            if (open.isEmpty())
                return null;
            route = (Route) open.removeFirst();
            if (destinationCondition.eval(route.end))
                return route;
            visited.add(route.end);
            for(Iterator it=route.end.neighborhood().iterator();it.hasNext();){
                MotionlessObject e = (MotionlessObject) it.next();
                if ((!(e instanceof Building) || destinationCondition.eval(e))
                    && !visited.contains(e))
                    update(open, route.expandedRoute(e, costFunc));
            }
        }
    }

    private static void update(LinkedList open, Route route) {
        int size = open.size(), insertIndex = size;
        for (int i = 0;  i < size;  i ++) {
            Route rt = (Route) open.get(i);
            if (rt.end == route.end) {
                if (rt.cost > route.cost) {
                    open.remove(i);
                    open.add(insertIndex, route);
                }
                return;
            }
            if (rt.cost > route.cost  &&  i < insertIndex)
            insertIndex = i;
        }
        open.add(insertIndex, route);
    }

    public interface CostFunction {
        /** This method returns cost for reaching the <code>to</code>
         *  from the <code>from</code>.  When the <code>to</code> is a
         *  road, this method should return cost for reaching short of
         *  the road's another end-point from the <code>from</code>.
         */
        public float cost(MotionlessObject from, MotionlessObject to);
    }
}
