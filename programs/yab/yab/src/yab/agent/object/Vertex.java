// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.agent.object;

import java.util.*;
import yab.agent.DisasterSpace;
import yab.io.object.*;

public abstract class Vertex extends PointObject {
    public Vertex(int id, BaseVertex object, DisasterSpace world)
    { super(id, object, world); }
    private BaseVertex obj() { return (BaseVertex)object; }

    private ArrayList m_edges = null;
    public ArrayList edges() {
        if (m_edges == null) {
            int[] es = obj().edges();
            m_edges = new ArrayList(es.length);
            for (int i = 0;  i < es.length;  i ++)
                m_edges.add(world.get(es[i]));
        }
        return m_edges;
    }

    public Collection neighborhood() { return edges(); }
}
