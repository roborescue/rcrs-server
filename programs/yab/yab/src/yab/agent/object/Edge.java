// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.agent.object;

import java.util.*;
import yab.agent.DisasterSpace;
import yab.io.object.*;

public abstract class Edge extends MotionlessObject {
    public Edge(int id, BaseEdge object, DisasterSpace world)
    { super(id, object, world); }
    private BaseEdge obj() { return (BaseEdge) object; }

    private PointObject m_head = null;
    private PointObject m_tail = null;
    public PointObject head() {
        if (m_head == null) m_head = (PointObject) world.get(obj().head());
        return m_head;
    }
    public PointObject tail() {
        if (m_tail == null)
            m_tail = (PointObject) world.get(obj().tail());
        return m_tail;
    }
    public int length() { return obj().length(); }

    public int x() { return (head().x() + tail().x()) / 2; }
    public int y() { return (head().y() + tail().y()) / 2; }

    private ArrayList m_endpoints = null;
    public Collection neighborhood() {
        if (m_endpoints == null) {
            m_endpoints = new ArrayList(2);
            m_endpoints.add(head());
            m_endpoints.add(tail());
        }
        return m_endpoints;
    }
}
