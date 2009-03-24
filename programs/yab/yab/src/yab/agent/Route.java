// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.agent;

import java.util.*;
import yab.agent.Util.*;
import yab.agent.object.*;

public class Route {
    public final MotionlessObject end;
    public final float cost;
    private Route m_previous;

    /*public*/ Route previous() { return m_previous; }
    /*public*/ void setPrevious(Route previous) { m_previous = previous; }

    public Route(MotionlessObject end) { this(end, 0, null); }
    private Route(MotionlessObject end, float cost, Route previous) {
        this.end = end;
        this.cost = cost;
        m_previous = previous;
    }

    public Route expandedRoute(MotionlessObject next,
                               Router.CostFunction costFunc) {
        return new Route(next, cost + costFunc.cost(end,next), this);
    }

    public int size() {
        int result = 0;
        for (Route rt = this;  rt != null;  rt = rt.previous())
            result ++;
        return result;
    }

    public int[] toIDs() {
        int i = size();
        int[] ids = new int[i];
        for (Route rt = this;  rt != null;  rt = rt.previous())
            ids[-- i] = rt.end.id;
        return ids;
    }

    public String toString() {
        //return Util.classBaseName(this) + "(cost:" + cost + ",end:" +end+")";
        String str = "";
        for (Route rt = this;  rt != null;  rt = rt.previous())
            str = rt.end + "," + str;
        return str;
    }
}
