// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.agent;

import java.util.*;
import yab.agent.object.RealObject;

public class Util {
    public static final int distance(RealObject from, RealObject to) {
        float dx = from.x() - to.x();
        float dy = from.y() - to.y();
        return (int) Math.sqrt(dx * dx + dy * dy);
    }

    public static final int direction(RealObject from, RealObject to) {
        int dx = to.x() - from.x();
        int dy = to.y() - from.y();
        float theta = (float) Math.atan2(-dx, dy);
        if (theta < 0)
            theta += 2 * Math.PI;
        return (int) (theta * 360 * 60 * 60 / (2 * Math.PI));
    }

    public static String classBaseName(Object obj) {
        StringTokenizer st = new StringTokenizer(obj.getClass().getName(),".");
        String token = null;
        while (st.hasMoreTokens())
            token = st.nextToken();
        return token;
    }

    public static int max(int x, int y) { return x >= y ? x : y; }
    public static int min(int x, int y) { return x <= y ? x : y; }

    public static Object randomChoice(Collection col, Random random) {
        if (col.isEmpty()) throw new Error("the col must not be empty.");
        Iterator it = col.iterator();
        for (int i = random.nextInt(col.size());  i > 0;  i --)
            it.next();
        return it.next();
    }
}
