// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.

package yab.agent;

import java.net.*;
import java.util.*;
import yab.agent.*;
import yab.agent.object.*;

public abstract class HumanoidAgent extends Agent {
    protected HumanoidAgent(int agentType, InetAddress address, int port) {
        super(agentType, address, port);
    }

    private Humanoid self() { return (Humanoid) world.self; }

//    protected int hearingLimit() { return HEARING_LIMIT_OF_HUMANOID; }

    protected void move(MotionlessObject destination)
        throws ActionCommandException {
        move(Collections.singleton(destination));
    }
    protected void move(Collection destinations) throws ActionCommandException{
        move(getRoute(destinations));
    }
    protected void move(Route routePlan) throws ActionCommandException {
        move(routePlan.toIDs());
    }
    protected void move(int[] routePlan) throws ActionCommandException {
        socket().akMove(self().id, routePlan);
        throw new ActionCommandException();
    }

    /** This method returns the minimum cost route reaching one of
     *  <code>destinations</code> in consideration for a blockade
     *  located at the midpoint of a road where the controlled object
     *  is.
     *  This method routes, estimating cost of a route based upon its
     *  passability:
     *  <ul>
     *      <li>if the agent has seen a road and the road is passable,
     *      cost for reaching the road is low</li>
     *      <li>if the agent has seen a road and the road is not
     *      passable, the cost is high</li>
     *      <li>if the agent has not seen a road but the road may be
     *      passable, the cost is the middle</li>
     *  </ul>
     * @see Router.CostFunction#cost(MotionlessObject, MotionlessObject) cost
     */
    protected Route getRoute(Collection destinations) {
        MotionlessObject pos = self().motionlessPosition();
        if (!(pos instanceof Road)  ||  ((Road) pos).passableLines() >= 1)
            return Router.get(pos, destinations, RELIABILITY_COST_FUNCTION);
        Route route = Router.get(outsidePosition(), destinations,
                                 RELIABILITY_COST_FUNCTION);
        modifyOriginToCurrentPosition(route);
        return route;
    }
    private MotionlessObject outsidePosition() {
        Road rd = (Road) self().motionlessPosition();
        return (self().positionExtra() < rd.length() / 2)
            ? rd.head() : rd.tail();
    }
    private void modifyOriginToCurrentPosition(Route route) {
        MotionlessObject pos = self().motionlessPosition();
        for (Route rt = route;  true;  rt = rt.previous()) {
            if (rt.end == pos) {
                rt.setPrevious(null);
                return;
            }
            if (rt.previous() == null) {
                rt.setPrevious(new Route(pos));
                return;
            }
        }
    }
    private static final Router.CostFunction RELIABILITY_COST_FUNCTION
        = new Router.CostFunction() {
                public float cost(MotionlessObject from, MotionlessObject to) {
                    final int WEIGHT = Integer.MAX_VALUE;
                    float c = 1;
                    if (!(to instanceof Road))
                        return c;
                    Road rd = (Road) to;
                    PointObject po = (PointObject) from;
                    if (rd.passableLinesFrom(po) == 0)
                        return c * WEIGHT * WEIGHT;
                    return rd.hasBeenSeen() ? c : c * WEIGHT; }};
}
